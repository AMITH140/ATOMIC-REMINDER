package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MyApplicationTheme

class WidgetSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        setContent {
            MyApplicationTheme {
                val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                var transparency by remember { mutableStateOf(prefs.getInt("widget_transparency", 255).toFloat()) }

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
                                Text("Widget Transparency", style = MaterialTheme.typography.titleLarge, color = Color.White)
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Slider(
                                    value = transparency,
                                    onValueChange = { transparency = it },
                                    valueRange = 0f..255f,
                                    steps = 255
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { finish() }) {
                                        Text("Cancel", color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(onClick = {
                                        prefs.edit().putInt("widget_transparency", transparency.toInt()).apply()
                                        
                                        val appWidgetManager = AppWidgetManager.getInstance(this@WidgetSettingsActivity)
                                        TodoWidgetProvider.updateAppWidget(this@WidgetSettingsActivity, appWidgetManager, appWidgetId)
                                        
                                        finish()
                                    }) {
                                        Text("Save")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
