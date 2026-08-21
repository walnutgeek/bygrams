package com.walnutgeek.bygrams.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Repo root — also the base every documentation anchor hangs off. */
const val SOURCE_URL = "https://github.com/walnutgeek/bygrams"

/**
 * Anchors into the README. The fragments must stay in sync with the README's headings;
 * `AboutLinksTest` derives the expected headings from these constants so there is one
 * source of truth rather than two.
 */
const val SETUP_REPO_URL = "$SOURCE_URL#setting-up-a-recipe-repo"
const val CONVERT_RECIPE_URL = "$SOURCE_URL#converting-a-recipe-from-a-url"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val version = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ByGrams reads recipes from a public GitHub repo you control. " +
                    "Recipes are plain YAML files — the app only ever reads them, never writes.",
                style = MaterialTheme.typography.bodyLarge
            )

            SectionTitle("Setting up a recipe repo")
            Text(
                text = "Create a public GitHub repo and add recipes as .yaml files. " +
                    "Subdirectories become tags automatically, so a recipe in indian/ is " +
                    "tagged \"indian\". A conversions.yaml at the root maps ingredients and " +
                    "units to gram weights.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "The /setup-recipe-repo skill for Claude Code scaffolds these " +
                    "conventions and can seed starter recipes.",
                style = MaterialTheme.typography.bodyMedium
            )
            LinkRow("Setup guide on GitHub", SETUP_REPO_URL)

            SectionTitle("Converting a recipe from a URL")
            Text(
                text = "Install the ByGrams skills into your recipe repo, then hand Claude " +
                    "Code a recipe URL — or just paste the text. The convert-recipe skill " +
                    "fetches it, restructures it into named actions, and normalises " +
                    "ingredients to the gram-first format.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Commit and push, then tap the sync button here to pick it up.",
                style = MaterialTheme.typography.bodyMedium
            )
            LinkRow("Conversion guide on GitHub", CONVERT_RECIPE_URL)

            SectionTitle("Source")
            Text(
                text = "ByGrams is open source under the MIT License.",
                style = MaterialTheme.typography.bodyMedium
            )
            LinkRow("github.com/walnutgeek/bygrams", SOURCE_URL)

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Version ${version ?: "—"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun LinkRow(label: String, url: String) {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // A device with no browser throws ActivityNotFoundException; a dead tap beats a crash.
            .clickable { runCatching { uriHandler.openUri(url) } }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            // OpenInNew lives only in material-icons-extended; not worth the dependency.
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
    }
}
