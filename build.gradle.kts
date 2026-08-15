plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// --- convert-recipe skill packaging ---
//
// The convert-recipe skill is published from `.claude/skills/` via the `skills` CLI
// and must carry the recipe schema as a sibling file so it works standalone after
// install. `docs/recipe-schema.md` stays the source of truth; the copy is generated.
//
// syncRecipeSkill deliberately writes into the tracked source tree — see
// docs/adr/0001-package-recipe-schema-into-skill.md before "fixing" it.

val recipeSchemaSource = layout.projectDirectory.file("docs/recipe-schema.md")
val skillDir = layout.projectDirectory.dir(".claude/skills/convert-recipe")
val packagedSchema = skillDir.file("recipe-schema.md")

val syncRecipeSkill by tasks.registering(Copy::class) {
    group = "documentation"
    description = "Copies docs/recipe-schema.md into the convert-recipe skill directory."
    from(recipeSchemaSource)
    into(skillDir)
}

val verifyRecipeSkill by tasks.registering {
    group = "verification"
    description = "Fails if the packaged recipe schema has drifted from docs/recipe-schema.md."

    val source = recipeSchemaSource.asFile
    val packaged = packagedSchema.asFile
    inputs.file(source)

    doLast {
        if (!packaged.exists()) {
            throw GradleException(
                "${packaged.relativeTo(rootDir)} is missing. Run ./gradlew syncRecipeSkill"
            )
        }
        if (packaged.readText() != source.readText()) {
            throw GradleException(
                "${packaged.relativeTo(rootDir)} has drifted from ${source.relativeTo(rootDir)}. " +
                    "Run ./gradlew syncRecipeSkill"
            )
        }
    }
}

subprojects {
    tasks.matching { it.name == "preBuild" }.configureEach {
        dependsOn(syncRecipeSkill)
    }
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(verifyRecipeSkill)
    }
}
