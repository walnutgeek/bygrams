package com.walnutgeek.bygrams.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.walnutgeek.bygrams.data.RecipeEntry
import com.walnutgeek.bygrams.domain.ConversionTable
import com.walnutgeek.bygrams.domain.ParseResult
import com.walnutgeek.bygrams.domain.Recipe
import com.walnutgeek.bygrams.domain.ScalingEngine
import com.walnutgeek.bygrams.domain.Source
import com.walnutgeek.bygrams.domain.formatIngredientWithGrams
import com.walnutgeek.bygrams.domain.getScalableIngredients
import com.walnutgeek.bygrams.domain.mergeIngredients

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    entry: RecipeEntry,
    onBack: () -> Unit,
    showGrams: Boolean = false,
    conversionTable: ConversionTable? = null
) {
    // Keep screen awake
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    when (val result = entry.result) {
        is ParseResult.ParsedRecipe -> ParsedRecipeContent(
            result.recipe, onBack, showGrams, conversionTable
        )
        is ParseResult.RawText -> RawTextContent(result.filename, result.content, onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ParsedRecipeContent(
    recipe: Recipe,
    onBack: () -> Unit,
    showGrams: Boolean = false,
    conversionTable: ConversionTable? = null
) {
    var scalingRatio by remember { mutableStateOf(1.0) }
    var anchorMode by remember { mutableStateOf(false) }
    var selectedAnchorLine by remember { mutableStateOf<String?>(null) }
    var anchorInput by remember { mutableStateOf("") }
    var customMultiplierText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Description
            if (!recipe.description.isNullOrBlank()) {
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Scaling Controls
            ScalingControls(
                recipe = recipe,
                scalingRatio = scalingRatio,
                anchorMode = anchorMode,
                selectedAnchorLine = selectedAnchorLine,
                anchorInput = anchorInput,
                customMultiplierText = customMultiplierText,
                onRatioChange = { scalingRatio = it },
                onAnchorModeChange = { anchorMode = it },
                onSelectedAnchorLineChange = { selectedAnchorLine = it },
                onAnchorInputChange = { anchorInput = it },
                onCustomMultiplierTextChange = { customMultiplierText = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Total Ingredients (scaled)
            val scaledRecipe = if (scalingRatio != 1.0) {
                ScalingEngine.scaleRecipe(recipe, scalingRatio)
            } else {
                recipe
            }
            val merged = mergeIngredients(scaledRecipe.actions)
            if (merged.isNotEmpty()) {
                Text(
                    text = if (scalingRatio != 1.0) "Total Ingredients (scaled)" else "Total Ingredients",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                for (line in merged) {
                    val displayLine = applyGramConversion(line, showGrams, conversionTable)
                    val hasWarning = line.startsWith("~")
                    Text(
                        text = if (hasWarning) "\u2022 $displayLine \u26a0" else "\u2022 $displayLine",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Actions
            for (action in recipe.actions) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = action.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (action.time != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        SuggestionChip(
                            onClick = {},
                            label = { Text(action.time) }
                        )
                    }
                }

                if (action.note != null) {
                    Text(
                        text = action.note,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (action.ingredients.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    for (ingredient in action.ingredients) {
                        var displayLine = if (scalingRatio != 1.0) {
                            ScalingEngine.scaleIngredientLine(ingredient, scalingRatio)
                        } else {
                            ingredient
                        }
                        displayLine = applyGramConversion(displayLine, showGrams, conversionTable)
                        val hasWarning = displayLine.startsWith("~")
                        Text(
                            text = if (hasWarning) "\u2022 $displayLine \u26a0" else "\u2022 $displayLine",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Sources
            if (recipe.src.isNotEmpty()) {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sources",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                for (source in recipe.src) {
                    SourceItem(source)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ScalingControls(
    recipe: Recipe,
    scalingRatio: Double,
    anchorMode: Boolean,
    selectedAnchorLine: String?,
    anchorInput: String,
    customMultiplierText: String,
    onRatioChange: (Double) -> Unit,
    onAnchorModeChange: (Boolean) -> Unit,
    onSelectedAnchorLineChange: (String?) -> Unit,
    onAnchorInputChange: (String) -> Unit,
    onCustomMultiplierTextChange: (String) -> Unit
) {
    Column {
        Text(
            text = "Scale",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (!anchorMode) {
            // Multiplier mode
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val presets = listOf(0.5, 1.0, 1.5, 2.0)
                for (preset in presets) {
                    val label = if (preset == preset.toLong().toDouble()) {
                        "${preset.toLong()}x"
                    } else {
                        "${preset}x"
                    }
                    FilterChip(
                        selected = scalingRatio == preset && customMultiplierText.isEmpty(),
                        onClick = {
                            onRatioChange(preset)
                            onCustomMultiplierTextChange("")
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
                // Custom multiplier field
                OutlinedTextField(
                    value = customMultiplierText,
                    onValueChange = { text ->
                        onCustomMultiplierTextChange(text)
                        val value = text.toDoubleOrNull()
                        if (value != null && value > 0) {
                            onRatioChange(value)
                        }
                    },
                    label = { Text("Custom") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.width(90.dp)
                )
            }
        } else {
            // Anchor mode
            AnchorModeControls(
                recipe = recipe,
                selectedAnchorLine = selectedAnchorLine,
                anchorInput = anchorInput,
                onSelectedAnchorLineChange = onSelectedAnchorLineChange,
                onAnchorInputChange = { text ->
                    onAnchorInputChange(text)
                    if (selectedAnchorLine != null) {
                        val newAmount = text.toDoubleOrNull()
                        if (newAmount != null && newAmount > 0) {
                            val ratio = ScalingEngine.calculateAnchorRatio(selectedAnchorLine, newAmount)
                            if (ratio != null) onRatioChange(ratio)
                        }
                    }
                },
                onRatioChange = onRatioChange
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Toggle anchor mode
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilterChip(
                selected = anchorMode,
                onClick = {
                    onAnchorModeChange(!anchorMode)
                    if (anchorMode) {
                        // Switching back to multiplier mode, reset
                        onRatioChange(1.0)
                        onSelectedAnchorLineChange(null)
                        onAnchorInputChange("")
                    } else {
                        onRatioChange(1.0)
                        onCustomMultiplierTextChange("")
                    }
                },
                label = { Text("Anchor mode", style = MaterialTheme.typography.labelSmall) }
            )
            if (scalingRatio != 1.0) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Ratio: ${formatRatioDisplay(scalingRatio)}x",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnchorModeControls(
    recipe: Recipe,
    selectedAnchorLine: String?,
    anchorInput: String,
    onSelectedAnchorLineChange: (String?) -> Unit,
    onAnchorInputChange: (String) -> Unit,
    onRatioChange: (Double) -> Unit
) {
    val scalableIngredients = remember(recipe) { getScalableIngredients(recipe) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column {
        // Ingredient picker dropdown
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedAnchorLine ?: "Pick ingredient...",
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodySmall
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                for ((line, _) in scalableIngredients) {
                    DropdownMenuItem(
                        text = { Text(line, style = MaterialTheme.typography.bodySmall) },
                        onClick = {
                            onSelectedAnchorLineChange(line)
                            dropdownExpanded = false
                            // Recalculate ratio if there's already input
                            val newAmount = anchorInput.toDoubleOrNull()
                            if (newAmount != null && newAmount > 0) {
                                val ratio = ScalingEngine.calculateAnchorRatio(line, newAmount)
                                if (ratio != null) onRatioChange(ratio)
                            }
                        }
                    )
                }
            }
        }

        if (selectedAnchorLine != null) {
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = anchorInput,
                onValueChange = onAnchorInputChange,
                label = { Text("I have...") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.width(140.dp),
                textStyle = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun applyGramConversion(line: String, showGrams: Boolean, table: ConversionTable?): String {
    if (!showGrams || table == null) return line
    return formatIngredientWithGrams(line, table)
}

private fun formatRatioDisplay(ratio: Double): String {
    return if (ratio == ratio.toLong().toDouble()) {
        ratio.toLong().toString()
    } else {
        String.format("%.2f", ratio)
    }
}

@Composable
private fun SourceItem(source: Source) {
    val uriHandler = LocalUriHandler.current

    if (source.url != null) {
        val label = source.name ?: "View original"
        val annotatedString = buildAnnotatedString {
            pushStringAnnotation(tag = "URL", annotation = source.url)
            withStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(label)
            }
            pop()
        }
        ClickableText(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp),
            onClick = { offset ->
                annotatedString.getStringAnnotations("URL", offset, offset)
                    .firstOrNull()?.let { uriHandler.openUri(it.item) }
            }
        )
    } else if (source.name != null) {
        Text(
            text = source.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RawTextContent(filename: String, content: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(filename) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
