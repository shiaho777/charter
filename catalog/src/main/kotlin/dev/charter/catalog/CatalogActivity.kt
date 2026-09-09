package dev.charter.catalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.charter.catalog.ui.CatalogScreen
import dev.charter.core.designsystem.theme.CharterTheme

class CatalogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CharterTheme {
                CatalogScreen()
            }
        }
    }
}
