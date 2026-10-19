package com.jokinojewels.game.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jokinojewels.game.logic.GameLogic
import com.jokinojewels.game.ui.screens.LevelsScreen
import com.jokinojewels.game.ui.screens.MainScreen
import com.jokinojewels.game.ui.screens.MenuScreen
import com.jokinojewels.game.ui.screens.PairsResultsScreen
import com.jokinojewels.game.ui.screens.PairsScreen
import com.jokinojewels.game.ui.screens.QuizCategoryScreen
import com.jokinojewels.game.ui.screens.QuizResultsScreen
import com.jokinojewels.game.ui.screens.QuizScreen
import com.jokinojewels.game.ui.screens.ResultsSummaryScreen
import com.jokinojewels.game.ui.screens.RulesScreen
import com.jokinojewels.game.ui.screens.SettingsScreen
import com.jokinojewels.game.ui.theme.JewelPalette

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = MainRoute,
        modifier = Modifier.fillMaxSize().background(JewelPalette.Midnight),
        enterTransition = { fadeIn(tween(220)) },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { fadeOut(tween(180)) }
    ) {
        composable<MainRoute> {
            MainScreen(onStart = { navController.safeNavigate(MenuRoute) })
        }

        composable<MenuRoute> {
            MenuScreen(
                onQuiz = { navController.safeNavigate(QuizCategoriesRoute) },
                onPairs = { navController.safeNavigate(LevelsRoute) },
                onResults = { navController.safeNavigate(SummaryRoute) },
                onRules = { navController.safeNavigate(RulesRoute) },
                onSettings = { navController.safeNavigate(SettingsRoute) },
                onBack = { navController.safeBack() }
            )
        }

        composable<QuizCategoriesRoute> {
            QuizCategoryScreen(
                onCategory = { id -> navController.safeNavigate(QuizRoute(categoryId = id)) },
                onBack = { navController.safeBack() }
            )
        }

        composable<QuizRoute> { entry ->
            val route = remember(entry) { entry.safeRoute<QuizRoute>() }
            QuizScreen(
                categoryId = route?.categoryId,
                onFinished = { id, score, total ->
                    navController.safeNavigate(QuizResultsRoute(categoryId = id, score = score, total = total)) {
                        popUpTo<QuizRoute> { inclusive = true }
                    }
                },
                onMenu = { navController.toMenu() },
                onBack = { navController.safeBack() }
            )
        }

        composable<QuizResultsRoute> { entry ->
            val route = remember(entry) { entry.safeRoute<QuizResultsRoute>() }
            QuizResultsScreen(
                categoryId = route?.categoryId,
                score = route?.score ?: -1,
                total = route?.total ?: 0,
                onContinue = {
                    navController.safeNavigate(QuizCategoriesRoute) {
                        popUpTo<QuizCategoriesRoute> { inclusive = false }
                    }
                },
                onRestart = {
                    navController.safeNavigate(QuizRoute(categoryId = route?.categoryId.orEmpty())) {
                        popUpTo<QuizResultsRoute> { inclusive = true }
                    }
                },
                onMenu = { navController.toMenu() }
            )
        }

        composable<LevelsRoute> {
            LevelsScreen(
                onLevel = { level -> navController.safeNavigate(PairsRoute(level = level)) },
                onRules = { navController.safeNavigate(RulesRoute) },
                onBack = { navController.safeBack() }
            )
        }

        composable<PairsRoute> { entry ->
            val route = remember(entry) { entry.safeRoute<PairsRoute>() }
            PairsScreen(
                level = route?.level,
                onFinished = { level, won, matched, total ->
                    navController.safeNavigate(
                        PairsResultsRoute(level = level, won = won, matched = matched, total = total)
                    ) {
                        popUpTo<PairsRoute> { inclusive = true }
                    }
                },
                onMenu = { navController.toMenu() },
                onBack = { navController.safeBack() }
            )
        }

        composable<PairsResultsRoute> { entry ->
            val route = remember(entry) { entry.safeRoute<PairsResultsRoute>() }
            val level = route?.level ?: 0
            PairsResultsScreen(
                level = level,
                won = route?.won ?: false,
                matched = route?.matched ?: -1,
                total = route?.total ?: 0,
                onContinue = {
                    val next = if (route?.won == true) GameLogic.nextLevel(level) else null
                    if (next != null) {
                        navController.safeNavigate(PairsRoute(level = next)) {
                            popUpTo<PairsResultsRoute> { inclusive = true }
                        }
                    } else {
                        navController.safeNavigate(LevelsRoute) {
                            popUpTo<LevelsRoute> { inclusive = false }
                        }
                    }
                },
                onRestart = {
                    navController.safeNavigate(PairsRoute(level = level)) {
                        popUpTo<PairsResultsRoute> { inclusive = true }
                    }
                },
                onMenu = { navController.toMenu() }
            )
        }

        composable<SummaryRoute> {
            ResultsSummaryScreen(onBack = { navController.safeBack() })
        }

        composable<SettingsRoute> {
            SettingsScreen(onBack = { navController.safeBack() })
        }

        composable<RulesRoute> {
            RulesScreen(onBack = { navController.safeBack() })
        }
    }
}
