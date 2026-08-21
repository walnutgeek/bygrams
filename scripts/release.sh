#!/usr/bin/env bash
#
# Build a signed Android App Bundle for the Play Store.
#
# This script never creates or modifies signing keys. If the upload key is
# missing it stops and tells you how to make one -- generating a key is a
# deliberate act, not a side effect of a build.
#
#   ./scripts/release.sh                 build, test, sign
#   ./scripts/release.sh --skip-tests    skip the unit tests
#   ./scripts/release.sh --apk           also build a signed APK for sideloading
#
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

SKIP_TESTS=0
WANT_APK=0
for arg in "$@"; do
  case "$arg" in
    --skip-tests) SKIP_TESTS=1 ;;
    --apk)        WANT_APK=1 ;;
    -h|--help)    sed -n '3,12p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'; exit 0 ;;
    *) echo "unknown argument: $arg (try --help)" >&2; exit 2 ;;
  esac
done

say()  { printf '\n\033[1m==> %s\033[0m\n' "$*"; }
fail() { printf '\n\033[1;31mERROR:\033[0m %s\n' "$*" >&2; exit 1; }

# ---------------------------------------------------------------- java -----
if [ -z "${JAVA_HOME:-}" ] && [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
fi
command -v keytool >/dev/null 2>&1 || [ -x "${JAVA_HOME:-}/bin/keytool" ] \
  || fail "keytool not found. Set JAVA_HOME to a JDK 17+ install."
KEYTOOL="${JAVA_HOME:+$JAVA_HOME/bin/}keytool"

# ------------------------------------------------------------ credentials --
# Environment wins over the properties file, matching build.gradle.kts.
PROPS="$REPO_ROOT/keystore.properties"
prop() {
  [ -f "$PROPS" ] || return 0
  # Trailing \r tolerated so a file edited on Windows still works.
  sed -n "s/^$1=//p" "$PROPS" | tail -1 | tr -d '\r'
}

STORE_FILE="${BYGRAMS_STORE_FILE:-$(prop storeFile)}"
KEY_ALIAS="${BYGRAMS_KEY_ALIAS:-$(prop keyAlias)}"
STORE_PASSWORD="${BYGRAMS_STORE_PASSWORD:-$(prop storePassword)}"

if [ -z "$STORE_FILE" ]; then
  cat >&2 <<EOF

ERROR: no upload key configured.

This script will not create one for you. To make an upload key:

  $KEYTOOL -genkeypair -v \\
    -keystore upload-key.jks \\
    -alias upload \\
    -keyalg RSA -keysize 4096 -validity 10000

Then create keystore.properties in the repo root (it is gitignored):

  storeFile=upload-key.jks
  storePassword=<the store password>
  keyAlias=upload
  keyPassword=<the key password>

Back the .jks up somewhere durable and off this machine. With Play App
Signing a lost upload key can be reset by Google, but a lost key still
blocks releases until that is done. See docs/release.md.
EOF
  exit 1
fi

# storeFile may be relative to the repo root, as build.gradle.kts resolves it.
case "$STORE_FILE" in
  /*) STORE_ABS="$STORE_FILE" ;;
  *)  STORE_ABS="$REPO_ROOT/$STORE_FILE" ;;
esac
[ -f "$STORE_ABS" ] || fail "keystore not found at $STORE_ABS (storeFile=$STORE_FILE)"
[ -n "$KEY_ALIAS" ] || fail "keyAlias is not set in keystore.properties or BYGRAMS_KEY_ALIAS"

# ---------------------------------------------------------------- version --
# No `| head -1`: head exits early, SIGPIPEs sed, and pipefail turns that into a
# script-killing failure. Take the first line with parameter expansion instead.
first_line() { printf '%s' "${1%%$'\n'*}"; }

version_name_matches="$(sed -n 's/.*versionName *= *"\(.*\)".*/\1/p' app/build.gradle.kts)"
version_code_matches="$(sed -n 's/.*versionCode *= *\([0-9]*\).*/\1/p' app/build.gradle.kts)"
VERSION_NAME="$(first_line "$version_name_matches")"
VERSION_CODE="$(first_line "$version_code_matches")"
[ -n "$VERSION_NAME" ] && [ -n "$VERSION_CODE" ] || fail "could not read version from app/build.gradle.kts"

say "ByGrams $VERSION_NAME (versionCode $VERSION_CODE)"

if [ -n "$(git status --porcelain 2>/dev/null)" ]; then
  printf '\033[1;33mwarning:\033[0m working tree is dirty; this build will not match any commit\n'
fi

# ------------------------------------------------------------------ build --
if [ "$SKIP_TESTS" -eq 0 ]; then
  say "Running unit tests"
  ./gradlew --quiet testDebugUnitTest
fi

say "Building signed bundle"
./gradlew --quiet bundleRelease

AAB="app/build/outputs/bundle/release/app-release.aab"
[ -f "$AAB" ] || fail "expected bundle at $AAB but it was not produced"

if [ "$WANT_APK" -eq 1 ]; then
  say "Building signed APK"
  ./gradlew --quiet assembleRelease
fi

# ----------------------------------------------------------------- verify --
# A bundle that silently came out unsigned is the whole failure mode this script
# exists to prevent, so check rather than assume.
#
# Do NOT use jarsigner's exit code: on an unsigned bundle it prints "no manifest."
# and still exits 0, even with -strict. That check passes everything and is worse
# than no check at all. Verified both ways against a real signed and unsigned AAB.
# The signal is the literal "jar verified" line, corroborated by the presence of a
# signature block in META-INF.
#
# Both checks capture their output first and match with a here-string rather than
# piping into `grep -q`. `grep -q` exits the moment it matches, which SIGPIPEs the
# producer; under `set -o pipefail` that turns a SUCCESSFUL match into a non-zero
# pipeline and `set -e` then kills the script. It is timing-dependent -- small
# bundles finish writing before grep bails and appear to work, while a real
# multi-megabyte bundle loses the race and reports a perfectly good build as
# unsigned. No pipelines here, so no race.
say "Verifying signature"
JARSIGNER="${JAVA_HOME:+$JAVA_HOME/bin/}jarsigner"

sig_output="$("$JARSIGNER" -verify "$AAB" 2>&1 || true)"
if ! grep -q "jar verified" <<<"$sig_output"; then
  fail "$AAB is NOT signed (jarsigner did not report 'jar verified').
       Check the credentials in keystore.properties or the BYGRAMS_* variables."
fi

zip_entries="$(unzip -l "$AAB" 2>/dev/null || true)"
if ! grep -qE "META-INF/[^/]*\.(RSA|DSA|EC)$" <<<"$zip_entries"; then
  fail "$AAB has no signature block in META-INF. Refusing to call this signed."
fi
echo "signed OK"

say "Upload certificate fingerprint"
# Play Console shows this for your upload key; they must match.
"$KEYTOOL" -list -v -keystore "$STORE_ABS" -alias "$KEY_ALIAS" \
  ${STORE_PASSWORD:+-storepass "$STORE_PASSWORD"} 2>/dev/null \
  | grep -E "SHA1:|SHA256:" | sed 's/^[[:space:]]*/  /' \
  || echo "  (could not read fingerprint; wrong store password?)"

say "Done"
echo "  bundle:  $AAB"
[ "$WANT_APK" -eq 1 ] && echo "  apk:     app/build/outputs/apk/release/app-release.apk"
cat <<EOF

Next:
  1. Play Console -> your app -> Production -> Create new release
  2. Upload $AAB
  3. Confirm the upload certificate fingerprint above matches the one
     Play Console lists under Setup -> App integrity.

Remember to tag the release:  git tag v$VERSION_NAME && git push --tags
EOF
