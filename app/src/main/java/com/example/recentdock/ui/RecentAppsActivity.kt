package com.example.recentdock.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.recentdock.data.AppCategory
import com.example.recentdock.ui.theme.RecentDockTheme

class RecentAppsActivity : ComponentActivity() {
    private var resumeTick by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialCategory = AppCategory.fromStored(intent.getStringExtra(EXTRA_CATEGORY))
        setContent {
            RecentDockTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RecentDockLibraryScreen(
                        initialCategory = initialCategory,
                        resumeTick = resumeTick,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resumeTick++
    }

    companion object {
        const val EXTRA_CATEGORY = "recentdock_category"
    }
}
