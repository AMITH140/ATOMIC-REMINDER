package com.example.ui.screens

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.liquidGlass

@Composable
fun PremiumScreen() {
    val habitState = com.example.ui.state.LocalHabitState.current
    var rewardedAd by remember { mutableStateOf<RewardedAd?>(null) }
    var isLoadingAd by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    var currentContext = context
    var activity: Activity? = null
    while (currentContext is android.content.ContextWrapper) {
        if (currentContext is Activity) {
            activity = currentContext
            break
        }
        currentContext = currentContext.baseContext
    }
    
    val adUnitId = "ca-app-pub-7414823704847076/4333226745" // User's Ad Unit ID
    // For test: "ca-app-pub-3940256099942544/5224354917"

    fun loadAd() {
        if (rewardedAd != null || isLoadingAd) return
        isLoadingAd = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("AdMob", "Ad failed to load: ${adError.message}")
                rewardedAd = null
                isLoadingAd = false
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d("AdMob", "Ad loaded")
                rewardedAd = ad
                isLoadingAd = false
            }
        })
    }

    LaunchedEffect(Unit) {
        loadAd()
    }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val daysLeft = habitState.premiumDaysRemaining.intValue
        val isZero = daysLeft <= 0

        Surface(
            modifier = Modifier.padding(bottom = 24.dp).liquidGlass(RoundedCornerShape(50)),
            color = Color.Transparent,
            shape = RoundedCornerShape(50)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Schedule, contentDescription = null, tint = if (isZero) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("$daysLeft days remaining", style = MaterialTheme.typography.labelMedium, color = if (isZero) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        
        Text(
            text = if (isZero) "Subscription\nExpired" else "Keep Your Streak\nAlive",
            style = MaterialTheme.typography.displayMedium,
            color = if (isZero) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Earn free Premium days to maintain your focus and keep building those essential habits.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Watch 3 ads, earn 1 free day",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Support the app and extend your Premium experience.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (i in 1..3) {
                        if (i <= habitState.adsWatched.intValue) {
                            Box(
                                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        } else {
                            Box(
                                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                LiquidGlassButton(
                    onClick = {
                        if (rewardedAd != null && activity != null) {
                            rewardedAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    rewardedAd = null
                                    loadAd()
                                }
                                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                                    rewardedAd = null
                                }
                            }
                            rewardedAd?.show(activity) { rewardItem ->
                                // Reward granted
                                habitState.adsWatched.intValue++
                                if (habitState.adsWatched.intValue >= 3) {
                                    habitState.premiumDaysRemaining.intValue++
                                    habitState.adsWatched.intValue = 0
                                }
                                val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                                prefs.edit()
                                    .putInt("ads_watched", habitState.adsWatched.intValue)
                                    .putInt("premium_days", habitState.premiumDaysRemaining.intValue)
                                    .apply()
                            }
                        } else if (rewardedAd == null && !isLoadingAd) {
                            loadAd()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(50),
                    enabled = rewardedAd != null || !isLoadingAd
                ) {
                    if (isLoadingAd) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Filled.Movie, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (rewardedAd != null) "Watch an ad" else "Load Ad", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

