package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MyApplicationTheme

class WidgetFilterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(32.dp).fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text("Select Filter", style = MaterialTheme.typography.titleLarge, color = Color.White)
                                Spacer(modifier = Modifier.height(16.dp))
                                val filters = listOf("Today", "Pending", "All", "Habits")
                                filters.forEach { filter ->
                                    Text(
                                        text = filter,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                                prefs.edit().putString("widget_filter", filter).apply()
                                                
                                                val appWidgetManager = AppWidgetManager.getInstance(this@WidgetFilterActivity)
                                                TodoWidgetProvider.updateAppWidget(this@WidgetFilterActivity, appWidgetManager, appWidgetId)
                                                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, com.example.R.id.widget_list_view)
                                                
                                                finish()
                                            }
                                            .padding(vertical = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
