# Recipe YAML Schema

## Recipe file

Each recipe is a single YAML file (`.yaml` or `.yml`) in the repo root.

```yaml
# Required
name: Hummus

# Optional — short subtitle when the name alone isn't descriptive
description: Classic Middle Eastern chickpea dip

# Optional — list of source attributions
src:
  - name: The Food Lab
    url: https://example.com/hummus
  - name: Grandma's notebook

# Optional — flat list of free-form strings for filtering
tags:
  - Appetizer
  - Food Processor

# Required — ordered list of actions
actions:
  - name: Blend
    ingredients:
      - 400g (1 can) chickpeas, drained
      - 45g (3 tbsp) tahini
      - 28g (2 tbsp) olive oil
      - 15g (1 tbsp) fresh lemon juice
      - 3 cloves garlic, crushed
      - salt to taste
    time: 2m
    note: Blend until smooth, scraping sides as needed
```

### Top-level fields

| Field         | Type             | Required | Description                          |
|---------------|------------------|----------|--------------------------------------|
| `name`        | string           | yes      | Recipe title                         |
| `description` | string           | no       | Short subtitle or explanation        |
| `src`         | list of Source    | no       | Attribution(s) for the recipe        |
| `tags`        | list of string   | no       | Free-form labels for filtering       |
| `actions`     | list of Action   | yes      | Ordered steps to make the dish       |

### Source object

| Field  | Type   | Required | Description                              |
|--------|--------|----------|------------------------------------------|
| `name` | string | no*      | Name of book, person, or site            |
| `url`  | string | no*      | URL to the original recipe               |

*At least one of `name` or `url` must be present.

### Action object

| Field         | Type           | Required | Description                          |
|---------------|----------------|----------|--------------------------------------|
| `name`        | string         | yes      | Step title                           |
| `ingredients` | list of string | no       | Ingredient lines for this step       |
| `time`        | string         | no       | Free-form duration/temperature       |
| `note`        | string         | no       | Free-form commentary                 |

### Ingredient line format

Ingredient lines are free-form strings. The preferred format for parseability is:

```
{amount} {unit} {item}[, {prep}]
```

Examples:
- `250g all purpose flour` — grams, no volume equivalent
- `250g (2 cups) all purpose flour` — grams with volume equivalent in parens
- `3 cloves garlic, crushed` — count-based
- `salt and pepper to taste` — unparseable, displayed as-is

When gram amounts are known, prefer grams as the primary unit with volume equivalents in parentheses.

### Deprecated fields

These fields appeared in older recipes and should be converted:

| Old field    | Action                                       |
|--------------|----------------------------------------------|
| `src_url`    | Convert to `src: [{url: ...}]`               |
| `citations`  | Convert to `src: [{url: ...}, ...]`           |
| `depends`    | Drop — use action list order instead          |
| `level`      | Drop — fold into `time` or `note`            |

### Fallback

Files that do not conform to this schema are displayed as raw text in the app.

---

## Conversions file

An optional `conversions.yaml` in the repo root provides unit-to-gram mappings for the gram conversion toggle.

```yaml
- names: [flour, all purpose flour, all-purpose flour, ap flour]
  conversions:
    cup: 125
    tbsp: 8
    tsp: 3

- names: [butter, unsalted butter, salted butter]
  conversions:
    cup: 227
    tbsp: 14
    stick: 113

- names: [milk, whole milk]
  conversions:
    cup: 245
    tbsp: 15

- names: [water]
  conversions:
    cup: 237
    tbsp: 15
    tsp: 5

- names: [olive oil, extra-virgin olive oil, peanut oil, oil]
  conversions:
    cup: 216
    tbsp: 14
    tsp: 5

- names: [sugar, granulated sugar, white sugar]
  conversions:
    cup: 200
    tbsp: 12
    tsp: 4

- names: [heavy cream, heavy whipping cream, whipping cream, cream]
  conversions:
    cup: 238
    tbsp: 15

- names: [greek yogurt, yogurt]
  conversions:
    cup: 245
    tbsp: 15

- names: [honey]
  conversions:
    cup: 340
    tbsp: 21

- names: [tahini]
  conversions:
    cup: 240
    tbsp: 15
```

### Conversions entry

| Field         | Type             | Required | Description                              |
|---------------|------------------|----------|------------------------------------------|
| `names`       | list of string   | yes      | Case-insensitive aliases for the ingredient |
| `conversions` | map string→number| yes      | Unit name to grams mapping               |

The app bundles a default conversion table. If `conversions.yaml` exists in the user's repo, it overrides/extends the bundled defaults.
