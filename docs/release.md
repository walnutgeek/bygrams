# Releasing to the Play Store

`scripts/release.sh` builds a signed Android App Bundle. It never creates or
modifies signing keys — that is a deliberate act, not a side effect of a build.

```bash
./scripts/release.sh                 # test, build, sign, verify
./scripts/release.sh --skip-tests    # skip the unit tests
./scripts/release.sh --apk           # also produce a signed APK for sideloading
```

Output: `app/build/outputs/bundle/release/app-release.aab`

## One-time: create the upload key

Play App Signing means Google holds the *app signing key*; you only hold an
**upload key**, which identifies you when you upload. If the upload key is lost,
Google can reset it — unlike the app signing key, which is unrecoverable. That
makes this far less frightening than it used to be, but a lost upload key still
blocks releases until the reset goes through.

```bash
keytool -genkeypair -v \
  -keystore upload-key.jks \
  -alias upload \
  -keyalg RSA -keysize 4096 -validity 10000
```

Then create `keystore.properties` in the repo root:

```properties
storeFile=upload-key.jks
storePassword=<store password>
keyAlias=upload
keyPassword=<key password>
```

`keystore.properties`, `*.jks` and `*.keystore` are gitignored. **Back the
keystore up somewhere durable and off this machine** — a password manager or an
encrypted archive, not just this laptop.

## CI

Environment variables take precedence over `keystore.properties`, so the same
script works unchanged on a build server with nothing secret on disk:

```
BYGRAMS_STORE_FILE, BYGRAMS_STORE_PASSWORD, BYGRAMS_KEY_ALIAS, BYGRAMS_KEY_PASSWORD
```

## Version numbers

Both live in `app/build.gradle.kts`:

- `versionName` — what users see, e.g. `1.0.1`
- `versionCode` — an integer Play uses for ordering. **Must increase on every
  upload.** Play permanently rejects a code it has already seen, even from a
  deleted draft release.

## Why the script re-verifies the signature

`jarsigner -verify` exits **0 on an unsigned bundle**, printing `no manifest.`
— and `-strict` does not change that. Trusting its exit code would wave through
exactly the failure the check exists to catch. The script instead requires the
literal `jar verified` line *and* a signature block in `META-INF`. Both
behaviours were confirmed against real signed and unsigned bundles.

## Not configured yet

`isMinifyEnabled = false` for release builds, so no R8 shrinking or obfuscation.
Turning it on shrinks the download but needs testing against the reflective
paths in SnakeYAML first — worth doing before the app grows, not as part of a
release.
