package com.jokinojewels.game

/** Static, offline app configuration. */
object AppConfig {

    /**
     * Optional public privacy policy URL (opened in the user's browser, the app itself has
     * no INTERNET permission). Leave empty to show only the built-in policy text.
     */
    const val PRIVACY_POLICY_URL: String = ""

    val PRIVACY_POLICY_TEXT: String = """
        Jokino Jewels works completely offline.

        • No account, no sign-in, no ads, no analytics, no tracking.
        • The app has no internet permission and sends nothing anywhere.
        • Your best quiz scores, cleared levels and the sound setting are stored only on this device (Android DataStore).
        • Nothing is included in cloud backups or device transfers.
        • You can erase everything at any time with Settings → Reset Progress, or by uninstalling the app.
    """.trimIndent()
}
