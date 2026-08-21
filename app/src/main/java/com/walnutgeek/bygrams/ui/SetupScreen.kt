package com.walnutgeek.bygrams.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.walnutgeek.bygrams.data.RepoConfigStore
import com.walnutgeek.bygrams.domain.RepoConfig

@Composable
fun SetupScreen(
    configStore: RepoConfigStore,
    onSetupComplete: () -> Unit,
    onAboutClick: () -> Unit
) {
    var input by remember { mutableStateOf("walnutgeek/recipes") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ByGrams",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = input,
            onValueChange = {
                input = it
                error = null
            },
            label = { Text("GitHub repo (owner/repo:branch)") },
            isError = error != null,
            supportingText = error?.let { msg -> { Text(msg) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (!input.contains('/')) {
                    error = "Input must contain a '/' separating owner and repo"
                } else {
                    val config = RepoConfig.parse(input)
                    configStore.saveConfig(config)
                    onSetupComplete()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Started")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onAboutClick) {
            Text("Don't have a recipe repo yet?")
        }
    }
}
