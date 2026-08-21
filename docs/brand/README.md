# Launcher icon

`icon.svg` is the master artwork: a shallow bowl on a kitchen scale reading `42g`,
white on terracotta `#B4472B`. The numerals are Inter Bold, already converted to
outlines, so editing the SVG needs no font tooling.

## Where it ends up

| File | Role |
| --- | --- |
| `res/mipmap-anydpi-v26/ic_launcher{,_round}.xml` | adaptive icon, three layers |
| `res/values/ic_launcher_background.xml` | the terracotta |
| `res/drawable/ic_launcher_foreground.xml` | the mark |
| `res/drawable/ic_launcher_monochrome.xml` | themed icons (Android 13+) |
| `res/mipmap-*dpi/ic_launcher{,_round}.webp` | legacy bitmaps, all five densities |

## Two constraints that break silently

**The art must stay within radius 34 of centre.** The adaptive canvas is 108dp, but
only the middle 72dp is ever drawn and Pixel's launcher masks that to a circle.
Artwork sized to the full canvas loses its corners — the first draft of this icon had
the scale body's corners sliced off, and nothing in the build complained.
`LauncherIconTest` measures this and fails if it regresses.

**The readout must be a hole, not paint.** Themed icons tint every opaque pixel of the
monochrome layer, so a `42g` painted in the background colour would be tinted along
with the body and disappear. Both layers are a single compound path with the glyphs
and plate groove subtracted from the body.

Getting those holes right is a boolean problem, not a fill-rule one. Two approaches
that look correct and are not:

- **evenOdd over raw font outlines** — Inter's `4` is built from overlapping contours,
  and evenOdd turns every overlap into a spurious hole straight through the numeral.
- **nonZero with reversed glyph contours** — fixes `2` and `g`, but where two reversed
  contours overlap inside `4` the winding count reaches −1, which is still nonzero, so
  the overlap fills solid.

The shipped path is a real boolean difference (union the solids, union the holes,
subtract), which emits correctly wound non-overlapping contours and renders the same
under either fill rule.

## Regenerating

Editing `icon.svg` by hand is fine for tweaks. To change the *text*, you need
`fonttools` and `skia-pathops` to outline and subtract new glyphs, plus
[Inter](https://github.com/rsms/inter) (SIL OFL — its licence explicitly permits
logos, unlike the macOS system fonts). Re-export the bitmaps with `rsvg-convert`
into the centre `18 18 72 72` viewport, then `cwebp -lossless`.

Run `./gradlew testDebugUnitTest --tests "*LauncherIconTest"` afterwards.
