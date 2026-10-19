package com.jokinojewels.game.ui.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.toRoute

/**
 * Navigates only from a RESUMED entry (ignores double taps and taps during transitions)
 * and never lets a navigation error crash the app.
 */
fun NavController.safeNavigate(route: Any, builder: NavOptionsBuilder.() -> Unit = {}) {
    val current = currentBackStackEntry ?: return
    if (current.lifecycle.currentState != Lifecycle.State.RESUMED) return
    try {
        navigate(route) {
            launchSingleTop = true
            builder()
        }
    } catch (e: IllegalArgumentException) {
        // Unknown destination — stay where we are.
    } catch (e: IllegalStateException) {
        // Graph not ready — stay where we are.
    }
}

/** Pops one screen; if there is nothing below, goes to Main instead of leaving a blank host. */
fun NavController.safeBack() {
    val current = currentBackStackEntry ?: return
    if (current.lifecycle.currentState != Lifecycle.State.RESUMED) return
    if (previousBackStackEntry != null) {
        popBackStack()
    } else {
        try {
            navigate(MainRoute) {
                popUpTo(graph.id) { inclusive = true }
                launchSingleTop = true
            }
        } catch (e: IllegalArgumentException) {
            // Already at the root.
        } catch (e: IllegalStateException) {
            // Graph not ready.
        }
    }
}

/** Returns to the Grand Menu, clearing any game / results screens above it. */
fun NavController.toMenu() = safeNavigate(MenuRoute) {
    popUpTo<MenuRoute> { inclusive = false }
}

/** Decodes typed args without ever throwing; null means "invalid", show a fallback. */
inline fun <reified T : Any> NavBackStackEntry.safeRoute(): T? = try {
    toRoute<T>()
} catch (e: Exception) {
    null
}
