# ByGrams

An Android recipe viewer that fetches YAML recipes from a public GitHub repo, with a focus on gram-based measurements and ingredient scaling.

## Language

**Recipe**:
A YAML file describing a dish — its name, sources, tags, and an ordered list of actions.
_Avoid_: meal, dish (when referring to the data object)

**Action**:
A named step within a recipe that groups the ingredients needed for that step and optionally carries time and notes.
_Avoid_: step, instruction, direction

**Ingredient Line**:
A single free-form text string inside an action's ingredient list. May be parseable into amount, unit, and item — or may be purely descriptive.
_Avoid_: ingredient (when referring to the full line including quantity)

**Source**:
An attribution for a recipe — a book, website, person, or other origin. Has a name, a URL, or both.
_Avoid_: citation, reference, link

**Tag**:
A free-form label on a recipe used for filtering. Typically describes equipment, cuisine, or meal type.
_Avoid_: category, label

**Conversion Table**:
A repo-level YAML file mapping ingredient aliases and volume/count units to gram weights, used to offer gram equivalents in the app.
_Avoid_: density table, unit map

**Scaling**:
Adjusting all parseable ingredient quantities in a recipe by a ratio. Supports two modes: a direct multiplier, or anchoring on a specific ingredient amount and back-calculating the ratio.
_Avoid_: adjusting, resizing

**Anchor Ingredient**:
The ingredient line a user selects as the basis for "I have X of this" scaling. Must have a parseable numeric amount.
_Avoid_: base ingredient, reference ingredient
