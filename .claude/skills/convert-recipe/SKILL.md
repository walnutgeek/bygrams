---
name: convert-recipe
description: Convert free-form text or non-conforming YAML into the canonical ByGrams recipe schema. Use when the user pastes a recipe, provides a URL, or asks to normalize an existing recipe file.
---

Convert the input into a valid ByGrams recipe YAML file following the schema in `recipe-schema.md`, next to this file.

## Steps

1. **Read the schema** — read `recipe-schema.md` in this skill's own directory to get the canonical format.

2. **Parse the input** — the input may be:
   - Free-form text (pasted from a website, typed from memory)
   - An existing YAML file that uses old/non-conforming fields
   - A URL to a recipe page (fetch and extract)

3. **Normalize fields:**
   - Set `name` from the recipe title
   - Convert `src_url` or `citations` to `src: [{url: ...}]` or `src: [{name: ...}]`
   - Drop `depends` and `level` fields
   - Move `level` info into `time` or `note` as appropriate
   - Add `description` if the name alone is not descriptive (e.g., non-English names)
   - Preserve `tags` as-is

4. **Structure actions:**
   - Group ingredients under named actions/steps
   - If the input has no steps, create a single action named after the primary technique (e.g., "Mix", "Cook", "Blend")
   - Preserve `time` and `note` fields on actions

5. **Normalize ingredient lines** to the preferred format `{amount} {unit} {item}[, {prep}]`:
   - When gram amounts are known, put grams first: `250g (2 cups) all purpose flour`
   - When only volume/count is given and you know the gram equivalent, add it: `125g (1 cup) all purpose flour`
   - When only volume/count is given and you do NOT know the gram equivalent, leave as-is: `3 cloves garlic, crushed`
   - Unparseable ingredients stay as-is: `salt and pepper to taste`
   - Do NOT invent gram amounts you are not confident about

6. **Output** the result as a YAML code block. Ask the user where to save it if the destination is not obvious.

## Example

Input:
```
Tzatziki - mix 1 cup greek yogurt, 2 cucumbers peeled and grated,
juice of half a lemon, 2 tbsp olive oil, salt and pepper, 3 cloves garlic minced
```

Output:
```yaml
name: Tzatziki
description: Greek yogurt and cucumber dip
actions:
  - name: Mix
    ingredients:
      - 245g (1 cup) greek yogurt
      - 2 cucumbers, peeled and grated
      - 15g (1 tbsp) lemon juice
      - 28g (2 tbsp) olive oil
      - salt and pepper to taste
      - 3 cloves garlic, minced
```
