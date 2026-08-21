# ByGrams

An Android app for viewing and scaling recipes stored as YAML files in a public GitHub repo. Designed for cooks who prefer gram-based measurements — easier to scale, no measuring cups needed.

## Features

- Fetches recipes from any public GitHub repo
- Offline caching — works in the kitchen without WiFi
- Two scaling modes: multiplier (2x, 0.5x) or "I have X grams of Y" anchor-based scaling
- Gram conversion toggle for volume-based ingredients
- Search by recipe name or ingredient
- Filter by tags

## Recipe format

Recipes are YAML files following a [simple schema](docs/recipe-schema.md). See [walnutgeek/recipes](https://github.com/walnutgeek/recipes) for an example repo.

## Installing the Claude Code skills

This repo publishes two Claude Code skills for working with recipe repos. Install them
into your recipe repo with the [`skills`](https://github.com/vercel-labs/skills) CLI:

```bash
cd your-recipes
npx skills@latest add walnutgeek/bygrams
```

Installing into the recipe repo (rather than globally, with `-g`) keeps the skills
alongside the recipes they operate on, so anyone cloning your recipes gets the tooling too.

## Setting up a recipe repo

Create a public GitHub repo and add your recipes as `.yaml` files. Subdirectories become
tags automatically — a recipe at `indian/tikka.yaml` is tagged `indian`. A
`conversions.yaml` at the repo root maps ingredient aliases and volume/count units to
gram weights, which is what powers the app's gram-equivalent toggle.

Run **`/setup-recipe-repo`** once, in the recipe repo, after installing the skills. It
writes down the conventions agents need (notably that subdirectories become tags) and
can seed starter recipes from [walnutgeek/recipes](https://github.com/walnutgeek/recipes).

## Converting a recipe from a URL

With the skills installed, hand Claude Code a recipe URL — or just paste the recipe text.
The **`convert-recipe`** skill fetches it, restructures it into named actions, normalises
ingredient lines to the gram-first format, and produces a YAML file in the
[schema](docs/recipe-schema.md). It is invoked automatically when you paste a recipe.

The skill writes the file locally; it does not push. Commit and push, then hit sync in
the app to pick the recipe up.

## Pointing the app at your repo

Point the app at your GitHub repo as `owner/repo` (e.g., `walnutgeek/recipes`). Recipes are fetched from the `main` branch by default — specify a different branch with `owner/repo:branch`.

## Building and testing

Requires JDK 17+ and the Android SDK. The project uses the Gradle wrapper, so no separate Gradle installation is needed.

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Run a specific test class
./gradlew testDebugUnitTest --tests "com.walnutgeek.bygrams.parser.IngredientParserTest"

# Build debug APK
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Build release APK (unsigned)
./gradlew assembleRelease

# Run both tests and build
./gradlew testDebugUnitTest assembleDebug
```

If using the JDK bundled with Android Studio:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew testDebugUnitTest assembleDebug
```
