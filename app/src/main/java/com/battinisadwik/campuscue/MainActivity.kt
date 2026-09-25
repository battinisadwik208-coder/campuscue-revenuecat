package com.battinisadwik.campuscue

import android.app.Activity
import android.os.Bundle

import com.revenuecat.purchases.PurchaseParams

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.models.Package

private val Ink = Color(0xFF101225)
private val Lavender = Color(0xFF635BFF)
private val Mint = Color(0xFFDFF8EE)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureRevenueCat()
        setContent { CampusCueApp(this@MainActivity) }
    }

    private fun configureRevenueCat() {
        val key = BuildConfig.REVENUECAT_PUBLIC_SDK_KEY
        if (key.startsWith("REPLACE_")) return
        Purchases.configure(PurchasesConfiguration.Builder(this, key).build())
    }
}

private data class Cue(val title: String, val course: String, val minutes: Int, val done: Boolean = false)

@androidx.compose.runtime.Composable
private fun CampusCueApp(activity: Activity) {
    var premiumUnlocked by rememberSaveable { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf("Today") }
    var notice by remember { mutableStateOf("Your day is clear. Pick one cue and start small.") }
    var cues by remember {
        mutableStateOf(
            listOf(
                Cue("Review neural networks", "AI/ML", 25),
                Cue("Finish database exercise", "Data Systems", 40),
                Cue("Walk + reset", "Wellbeing", 15)
            )
        )
    }
    var activePackage by remember { mutableStateOf<Package?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF7F7FB)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Ink)
                    .padding(horizontal = 22.dp, vertical = 26.dp)
            ) {
                Text("CampusCue", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Turn campus chaos into one calm next step.", color = Color(0xFFBFC3E8), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Today’s focus", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(10.dp))
                    Text("3 cues · 80 min", color = Color(0xFFBFC3E8))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Today", "Plan", "Premium").forEach { tab ->
                    FilterChip(selected = selectedTab == tab, onClick = { selectedTab = tab }, label = { Text(tab) })
                }
            }

            when (selectedTab) {
                "Premium" -> PremiumPanel(
                    unlocked = premiumUnlocked,
                    activePackage = activePackage,
                    notice = notice,
                    onLoad = {
                        if (BuildConfig.REVENUECAT_PUBLIC_SDK_KEY.startsWith("REPLACE_")) {
                            notice = "Add your RevenueCat public SDK key in local.properties to load the Test Store offering."
                        } else {
                            Purchases.sharedInstance.getOfferingsWith(
                                onError = { error -> notice = "RevenueCat: ${error.message}" },
                                onSuccess = { offerings ->
                                    activePackage = offerings.current?.availablePackages?.firstOrNull()
                                    notice = if (activePackage == null) "No current offering found. Create campuscue_plus_monthly in RevenueCat." else "Offering loaded from RevenueCat Test Store."
                                }
                            )
                        }
                    },
                    onPurchase = {
                        val packageToBuy = activePackage
                        if (packageToBuy == null) {
                            notice = "Load the RevenueCat offering first."
                        } else {
                            val purchaseParams = PurchaseParams.Builder(activity, packageToBuy).build()
                            Purchases.sharedInstance.purchaseWith(
                                purchaseParams = purchaseParams,
                                onError = { error, cancelled -> notice = if (cancelled) "Purchase cancelled safely." else "Purchase failed: ${error.message}" },
                                onSuccess = { _, info ->
                                    premiumUnlocked = info.entitlements["CampusCue Plus"]?.isActive == true
                                    notice = if (premiumUnlocked) "CampusCue Plus unlocked. Your focus plan is ready." else "Purchase completed; entitlement is still syncing."
                                }
                            )
                        }
                    },
                    onRestore = {
                        Purchases.sharedInstance.restorePurchasesWith(
                            onError = { error -> notice = "Restore failed: ${error.message}" },
                            onSuccess = { info ->
                                premiumUnlocked = info.entitlements["CampusCue Plus"]?.isActive == true
                                notice = if (premiumUnlocked) "CampusCue Plus restored." else "No active CampusCue Plus entitlement found."
                            }
                        )
                    }
                )
                "Plan" -> PlanPanel(cues = cues, onToggle = { title -> cues = cues.map { if (it.title == title) it.copy(done = !it.done) else it } })
                else -> TodayPanel(cues = cues, notice = notice, onStart = { title -> notice = "Focus session started: $title" })
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun TodayPanel(cues: List<Cue>, notice: String, onStart: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(notice, modifier = Modifier.padding(bottom = 2.dp), color = Color(0xFF555A76)) }
        items(cues) { cue ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cue.title, fontWeight = FontWeight.SemiBold, color = Ink)
                        Text("${cue.course} · ${cue.minutes} min", color = Color(0xFF6E7187))
                    }
                    Button(onClick = { onStart(cue.title) }) { Text("Start") }
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Mint), shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Small steps compound.", fontWeight = FontWeight.Bold, color = Ink)
                    Spacer(Modifier.height(4.dp))
                    Text("CampusCue keeps planning local and makes premium focus tools optional.", color = Color(0xFF3F5F54))
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun PlanPanel(cues: List<Cue>, onToggle: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Text("Your plan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(8.dp))
        cues.forEach { cue ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cue.title, fontWeight = FontWeight.SemiBold, color = if (cue.done) Color(0xFF7A7D8E) else Ink)
                        Text(if (cue.done) "Done" else "Queued · ${cue.minutes} min", color = Color(0xFF6E7187))
                    }
                    OutlinedButton(onClick = { onToggle(cue.title) }) { Text(if (cue.done) "Undo" else "Done") }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun PremiumPanel(
    unlocked: Boolean,
    activePackage: Package?,
    notice: String,
    onLoad: () -> Unit,
    onPurchase: () -> Unit,
    onRestore: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Text("CampusCue Plus", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(6.dp))
        Text("A gentle upgrade for students who want deeper focus rituals.", color = Color(0xFF555A76))
        Spacer(Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("What unlocks", fontWeight = FontWeight.Bold, color = Ink)
                Spacer(Modifier.height(8.dp))
                Text("• Unlimited focus sessions\n• Weekly reflection history\n• Exam-week planning prompts", color = Color(0xFF555A76))
                Spacer(Modifier.height(16.dp))
                if (unlocked) {
                    Text("CampusCue Plus is active.", color = Color(0xFF287A5A), fontWeight = FontWeight.Bold)
                } else {
                    Button(onClick = onLoad, modifier = Modifier.fillMaxWidth()) { Text(if (activePackage == null) "Load Test Store offering" else "Offering loaded") }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onPurchase, modifier = Modifier.fillMaxWidth(), enabled = activePackage != null) { Text("Unlock CampusCue Plus") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = onRestore, modifier = Modifier.fillMaxWidth()) { Text("Restore purchases") }
                }
                Spacer(Modifier.height(12.dp))
                Text(notice, color = Color(0xFF6E7187), style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("CampusCue uses RevenueCat for the purchase and entitlement flow. No payment is required in the included Test Store demo.", color = Color(0xFF6E7187), style = MaterialTheme.typography.bodySmall)
    }
}
