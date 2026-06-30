package com.example.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MyApplicationTheme

class BlockedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val blockedApp = intent.getStringExtra("BLOCKED_APP") ?: "App"
        val guardType = intent.getStringExtra("GUARD_TYPE") ?: "Morning Guard"
        val guardEndTime = intent.getStringExtra("guard_end_time") ?: "09:00 AM"
        
        var appName = blockedApp
        try {
            val pm = packageManager
            val info = pm.getApplicationInfo(blockedApp, 0)
            appName = pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            val parts = blockedApp.split(".")
            val namePart = if (parts.size >= 2 && parts.last() == "android") {
                parts[parts.size - 2]
            } else {
                parts.lastOrNull() ?: "App"
            }
            appName = namePart.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.US) else it.toString() }
        }
        
        setContent {
            MyApplicationTheme {
                Surface(
                    color = Color(0xFF141414), // Fully opaque dark background
                    modifier = Modifier.fillMaxSize()
                ) {
                    BlockedAppScreen(
                        blockedApp = appName,
                        guardType = guardType,
                        guardEndTime = guardEndTime,
                        onClose = { 
                            val homeIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                                addCategory(android.content.Intent.CATEGORY_HOME)
                                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(homeIntent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}
