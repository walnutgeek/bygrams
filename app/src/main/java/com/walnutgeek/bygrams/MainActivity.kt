package com.walnutgeek.bygrams

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.walnutgeek.bygrams.data.ConversionRepository
import com.walnutgeek.bygrams.data.GitHubApi
import com.walnutgeek.bygrams.data.RecipeCache
import com.walnutgeek.bygrams.data.RecipeRepository
import com.walnutgeek.bygrams.data.RepoConfigStore
import com.walnutgeek.bygrams.ui.AboutScreen
import com.walnutgeek.bygrams.ui.RecipeDetailScreen
import com.walnutgeek.bygrams.ui.RecipeListScreen
import com.walnutgeek.bygrams.ui.RecipeListViewModel
import com.walnutgeek.bygrams.ui.SetupScreen
import com.walnutgeek.bygrams.ui.SettingsScreen
import com.walnutgeek.bygrams.ui.theme.ByGramsTheme
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val configStore = RepoConfigStore(this)
        val cacheDir = File(filesDir, "recipe_cache")
        val repository = RecipeRepository(GitHubApi(), RecipeCache(cacheDir))
        setContent {
            ByGramsTheme {
                AppNavigation(configStore, repository)
            }
        }
    }
}

@Composable
private fun AppNavigation(configStore: RepoConfigStore, repository: RecipeRepository) {
    val navController = rememberNavController()
    val startDestination = if (configStore.getConfig() != null) "recipeList" else "setup"

    val scope = rememberCoroutineScope()
    val viewModel = remember {
        RecipeListViewModel(repository, configStore::getConfig, scope).also {
            it.loadRecipes()
            if (configStore.getConfig() != null) {
                it.sync()
            }
        }
    }

    // Re-sync when the app returns to the foreground. Without this, a warm resume reuses the
    // remembered ViewModel and never syncs, so the list can sit on stale content indefinitely.
    // syncIfStale() throttles, so the ON_RESUME that fires on first composition is a no-op here.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.syncIfStale()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("setup") {
            SetupScreen(
                configStore = configStore,
                onSetupComplete = {
                    navController.navigate("recipeList") {
                        popUpTo("setup") { inclusive = true }
                    }
                    viewModel.sync()
                },
                onAboutClick = { navController.navigate("about") }
            )
        }
        composable("recipeList") {
            RecipeListScreen(
                viewModel = viewModel,
                onRecipeClick = { entry ->
                    val index = viewModel.recipes.value.indexOf(entry)
                    navController.navigate("recipeDetail/$index")
                },
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                configStore = configStore,
                onBack = {
                    navController.popBackStack()
                    viewModel.sync()
                },
                onAboutClick = { navController.navigate("about") }
            )
        }
        composable("about") {
            AboutScreen(onBack = { navController.popBackStack() })
        }
        composable("recipeDetail/{index}") { backStackEntry ->
            val index = backStackEntry.arguments?.getString("index")?.toIntOrNull() ?: 0
            val recipes = viewModel.recipes.value
            if (index in recipes.indices) {
                val showGrams = configStore.isShowGramsEnabled()
                val conversionTable = if (showGrams) {
                    remember(repository) { ConversionRepository.getConversionTable(repository) }
                } else null
                RecipeDetailScreen(
                    entry = recipes[index],
                    onBack = { navController.popBackStack() },
                    showGrams = showGrams,
                    conversionTable = conversionTable
                )
            }
        }
    }
}
