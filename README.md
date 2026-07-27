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

## Setup

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
