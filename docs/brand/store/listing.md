# Play Store listing

Copy and assets for the Google Play listing. Lengths are checked by
`docs/brand/store/check-listing.py`.

## Title (max 30)

```
ByGrams: Your Recipes by Gram
```

## Short description (max 80)

```
Cook from your own GitHub recipe repo. Weigh in grams, scale to any batch.
```

## Full description (max 4000)

```
ByGrams is a recipe viewer for cooks who measure by weight.

Your recipes live in a public GitHub repo that you control — plain YAML files you can read, edit, diff and back up like any other text. ByGrams only ever reads them. There is no account to create, nothing to upload, and no way to get locked in. Delete the app and your recipes are exactly where you left them.

WHY WEIGH?

Cups measure volume, and volume lies. A cup of flour can vary by a third depending on how you scoop it. Grams don't. Weighing is faster, dirties fewer dishes, and turns scaling a recipe into arithmetic the app can do for you.

SCALING

• Tap 0.5x, 1.5x, 2x, or type your own multiplier
• Anchor scaling: pick an ingredient, say how much you actually have, and every other quantity follows. "I have 35 g of olive oil" becomes a 2.5x batch
• Quantities that can't be scaled exactly are marked, not silently rounded

GRAMS

• Volume and count measures show gram equivalents alongside the original
• A conversions file in your repo teaches the app your own ingredients and weights
• Ingredients repeated across steps are totalled into one list

IN THE KITCHEN

• Recipes are cached on the device, so a patchy signal at the stove doesn't matter
• Search by recipe name or by ingredient
• Filter by tag — subdirectories in your repo become tags automatically, so an "indian" folder tags everything inside it
• Every step keeps its own ingredients, timing and notes

GETTING STARTED

1. Create a public GitHub repo
2. Add recipes as .yaml files
3. Point ByGrams at it as owner/repo

Two Claude Code skills are published alongside the app: one scaffolds a new recipe repo, the other converts a recipe from a URL or pasted text into the right format. Neither is required — the format is simple enough to write by hand.

PRIVACY

ByGrams collects nothing. There are no accounts, no analytics, no advertising and no tracking of any kind. The only network requests it makes are to GitHub, to fetch the public recipe files you asked for.

OPEN SOURCE

ByGrams is MIT licensed. The source, the recipe format and the conversion tools are all at github.com/walnutgeek/bygrams.
```

## Categorisation

| Field | Value |
| --- | --- |
| Application type | App |
| Category | Food & Drink |
| Tags | Recipes, Cooking |

Food & Drink is the right home: Play's own examples for it are recipe and
cooking apps. Tools would technically fit the "reads files from a repo"
framing, but nobody browsing Tools is looking for a recipe app.

## Content rating

The questionnaire should come back **Everyone / PEGI 3**. Truthful answers:

| Question | Answer |
| --- | --- |
| Violence, sexuality, profanity, controlled substances | No |
| User-generated content shared between users | **No** — recipes come from a repo the user configures; nothing is shared through the app and users cannot see each other |
| User interaction / communication features | No |
| Shares user location | No |
| Digital purchases | No |
| Advertising | No |

The one question worth reading twice is user-generated content. ByGrams
displays files from a repo the user chose, but there is no submission, no
moderation surface, and no communication between users — so it is not UGC in
the sense the questionnaire means.

## Data safety

Declare **no data collected and no data shared**. Accurate as of this version:
the app has only the INTERNET permission, contains no analytics, advertising or
crash-reporting SDKs, and everything it stores (the repo name and the cached
recipe files) stays on the device.

Note for the form: fetching public files from GitHub means GitHub sees the
request, as it would for any web request. That is not collection *by this app*
and does not need declaring, but the privacy policy says so plainly anyway.

## Assets

| Asset | File |
| --- | --- |
| App icon (512x512) | `play-icon-512.png` |
| Feature graphic (1024x500) | `feature-graphic-1024x500.jpg` |
| Phone screenshots (1080x1920) | `screenshots/01-list.png` … `05-about.png` |

Screenshots are captured from a real device at native 1080x1920 — nothing
cropped, scaled or mocked up.

## Privacy policy URL

```
https://walnutgeek.github.io/bygrams/privacy.html
```
