package dev.charter.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dev.charter.app.ui.AppRoot
import dev.charter.core.designsystem.theme.CharterTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // targetSdk 35+ forces edge-to-edge; declare it, pad content by insets.
        enableEdgeToEdge()
        setContent {
            CharterTheme {
                AppRoot()
            }
        }
    }
}
