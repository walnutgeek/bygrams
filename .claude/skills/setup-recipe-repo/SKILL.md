---
name: setup-recipe-repo
description: Configure a GitHub repo to hold ByGrams recipes — write the repo conventions agents need, and optionally seed starter recipes. Run once, in the recipe repo, after installing these skills.
disable-model-invocation: true
---

# Set up a recipe repo

Scaffold the conventions a recipe repo needs so that agents working in it — and the
ByGrams app reading it — agree on how recipes are organised.

A **recipe repo** is a plain public GitHub repo of YAML recipe files. It is not owned
by ByGrams; ByGrams just reads it. This skill writes down the conventions that are
otherwise invisible.

This is a prompt-driven skill, not a script. Explore, present what you found, confirm
with the user, then write.

## Process

### 1. Explore

- Is this a git repo? `git remote -v` — is it public GitHub, and on which branch?
- Are there `.yaml`/`.yml` files already? At the root, or in subdirectories?
- Does `conversions.yaml` exist at the root?
- Does `AGENTS.md` or `CLAUDE.md` exist? Does either already have a
  `## Recipe repo conventions` section?
- Is the `convert-recipe` skill installed alongside this one?

If this looks like it is **not** a recipe repo — no recipe YAML and a populated
source tree in some other language — say so and stop. Don't scaffold over someone's
application code.

### 2. Present findings and ask

Summarise what's present and missing, then take the sections in order — one section,
one answer, then the next. Lead with the recommended answer so the user can accept it
in a word.

**Section A — Agent conventions file.**

Write a `## Recipe repo conventions` section into `AGENTS.md` (or `CLAUDE.md` if that
is what the repo already uses; create `AGENTS.md` if neither exists). Ask only which
file, and only if it's genuinely ambiguous.

The section must cover:

- **Recipe files** — one recipe per `.yaml`/`.yml` file. The schema lives in
  `recipe-schema.md` inside the installed `convert-recipe` skill.
- **Folder tags** — every subdirectory in a recipe's path automatically becomes a tag
  in the app. `desserts/cakes/tiramisu.yaml` gets tags `desserts` and `cakes`, with
  nothing written in the YAML. This is the convention most worth recording: it is
  invisible from the app and from the files themselves. Directory names are therefore
  user-facing — name them as you'd want them to read as tags.
- **Conversions** — `conversions.yaml` at the repo root is optional. The app bundles a
  default conversion table; a repo file *overrides* it. Any ingredient named in the
  repo file replaces the bundled entry for that ingredient entirely. Add entries only
  for ingredients whose defaults are wrong or missing — a wholesale copy of the
  defaults freezes them and you stop getting upstream corrections.
- **Adding recipes** — use the `convert-recipe` skill.

**Section B — Do not create `conversions.yaml`.**

Do not scaffold this file, and do not offer to. Explain it in Section A's prose and
leave the repo without one. An empty or seeded conversions file only creates a way to
accidentally shadow the bundled defaults.

**Section C — Seed starter recipes.** Offer this only if the repo has no recipes yet.

> Would you like to seed a few starter recipes from
> [walnutgeek/recipes](https://github.com/walnutgeek/recipes)? (recommended: **yes**,
> if you're starting from scratch)

If yes:

1. List what's available in that repo (its GitHub tree) and show the user the options,
   grouped by folder.
2. Let the user **select which recipes** — do not copy the whole repo. The point is a
   worked example of the schema and the folder-tag convention, not someone else's
   recipe collection.
3. Copy the selected files, preserving their subdirectory paths so the folder-tag
   convention is demonstrated rather than just described.
4. Tell the user the recipes came from `walnutgeek/recipes` and are theirs to edit or
   delete.

This step needs network access. If it fails, say so and continue — seeding is
optional and the repo is fully usable without it.

### 3. Report

State what was written and what the user should do next: commit the changes, push, and
point the ByGrams app at `owner/repo`.
