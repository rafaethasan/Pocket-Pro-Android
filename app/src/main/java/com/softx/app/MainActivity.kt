package com.softx.app

import android.content.Intent
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.softx.app.data.PocketNativePreferencesStore
import com.softx.app.parser.PocketSmartIntentHolder
import com.softx.app.ui.PocketProNativeApp
import com.softx.app.ui.theme.PocketProNativeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PocketSmartIntentHolder.handleIntent(intent)
        PocketNativePreferencesStore(this).applySavedLanguage()
        PocketReminderScheduler.ensureDailyReminder(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            PocketProNativeTheme {
                PocketProNativeApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        PocketSmartIntentHolder.handleIntent(intent)
    }
}
