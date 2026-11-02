package com.example.recentdock

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
import com.example.recentdock.ui.RecentDockLibraryScreen
import com.example.recentdock.ui.theme.RecentDockTheme

class MainActivity : ComponentActivity() {
    private var resumeTick by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecentDockTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RecentDockLibraryScreen(
                        initialCategory = AppCategory.RECENTLY_INSTALLED,
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
}
