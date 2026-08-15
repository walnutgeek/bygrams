# Package the recipe schema into the convert-recipe skill at build time

The `convert-recipe` skill is published from `.claude/skills/` for installation into
recipe repos via the `skills` CLI, so it cannot read `docs/recipe-schema.md` — that
file does not exist in a recipe repo. `docs/recipe-schema.md` stays the source of
truth and a Gradle `Copy` task, `syncRecipeSkill`, writes a copy into the skill
directory as a sibling file, which the skill reads instead.

## Consequences

`syncRecipeSkill` writes into the **tracked source tree**, and is wired into
`preBuild` so it runs on an ordinary `./gradlew assembleDebug`. This is a deliberate
departure from the Gradle convention that task outputs belong under `build/`, and it
means a build can leave the working tree dirty.

The alternative — generate under `build/` and let a `check`-time verify task catch
drift — is cleaner but unenforced: this repo has no CI, and the documented workflow is
`./gradlew testDebugUnitTest assembleDebug`, so a `check`-only gate would effectively
never run. Writing on build makes the one moment we can rely on — a human or agent
actually building — the moment the copy is refreshed. The write is idempotent, so the
tree only goes dirty on a commit that edited the schema and needed the skill updated
anyway.

`verifyRecipeSkill` is wired into `check` as a second net. If CI is ever added, invert
this: generate to `build/`, and let CI enforce `verifyRecipeSkill`.

The task must be `Copy`, not `Sync` — `Sync` would delete `SKILL.md` from the
destination directory.
