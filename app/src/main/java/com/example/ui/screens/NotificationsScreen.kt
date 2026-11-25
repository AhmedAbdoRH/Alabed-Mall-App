package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.local.NotificationPreferences
import com.example.data.model.MallNotificationEntity
import com.example.data.model.MallPlaceCategory
import com.example.ui.theme.ElAbedSurfaceVariant
import com.example.ui.theme.ElAbedWarmOrange
import com.example.ui.theme.ElAbedWarmOrangeContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotificationsScreen(
    notifications: List<MallNotificationEntity>,
    preferences: NotificationPreferences,
    onBack: () -> Unit,
    onMarkRead: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onToggleOffers: (Boolean) -> Unit,
    onToggleOpenings: (Boolean) -> Unit,
    onToggleEvents: (Boolean) -> Unit,
    onToggleAlerts: (Boolean) -> Unit,
    onToggleInterest: (MallPlaceCategory) -> Unit = {},
    onSendTestNotification: () -> Boolean,
    onOpenLinkedTarget: (MallNotificationEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var areSystemNotifsEnabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                areSystemNotifsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        areSystemNotifsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        scope.launch {
            if (granted && areSystemNotifsEnabled) {
                snackbarHostState.showSnackbar("تم منح إذن إشعارات الجهاز بنجاح")
            } else {
                snackbarHostState.showSnackbar("إشعارات الجهاز معطلة في النظام. يمكنك متابعة التنبيهات داخل التطبيق في أي وقت.")
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("الإشعارات وإعداداتها", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("notifications_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    TextButton(onClick = onMarkAllRead, modifier = Modifier.height(48.dp)) {
                        Icon(Icons.Filled.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("قراءة الكل", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // System notification permission status banner
                item {
                    if (!areSystemNotifsEnabled) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "إشعارات النظام معطلة حالياً",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = "الإشعارات الفورية متوقفة في إعدادات نظام الجهاز. يمكنك متابعة العروض والتنبيهات مباشرة من هذا القسم.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Preferences Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "تفضيلات الإشعارات داخل التطبيق",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "اختر أنواع التنبيهات التي ترغب في تلقيها ومتابعتها من مول العابد",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            PreferenceSwitchRow(
                                label = "عروض وخصومات جديدة",
                                checked = preferences.offersEnabled,
                                onCheckedChange = onToggleOffers
                            )
                            PreferenceSwitchRow(
                                label = "افتتاحات المحلات والفروع الجديدة",
                                checked = preferences.openingsEnabled,
                                onCheckedChange = onToggleOpenings
                            )
                            PreferenceSwitchRow(
                                label = "المهرجانات والفعاليات العائلية",
                                checked = preferences.eventsEnabled,
                                onCheckedChange = onToggleEvents
                            )
                            PreferenceSwitchRow(
                                label = "تنبيهات مهمة تخص المول وخدماته",
                                checked = preferences.alertsEnabled,
                                onCheckedChange = onToggleAlerts
                            )

                            FilledTonalButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !areSystemNotifsEnabled) {
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        areSystemNotifsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
                                        val sent = onSendTestNotification()
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                if (sent) "تم إرسال إشعار عرض تجريبي حسب اهتماماتك" else "تعذر إرسال الإشعار"
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("notifications_test_button")
                            ) {
                                Icon(Icons.Filled.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (areSystemNotifsEnabled) "إرسال إشعار تجريبي" else "السماح بإشعارات الجهاز",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Visitor Offer Interests Picker Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "اهتماماتك لإشعارات العروض",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (preferences.selectedInterests.isEmpty()) {
                                    "لم تحدد اهتمامات بعد — تصلك حالياً جميع عروض المول. اختر أقسامك المفضلة للتخصيص:"
                                } else {
                                    "تصلك إشعارات العروض للأقسام المختارة (${preferences.selectedInterests.size}) وتظهر أولاً في الرئيسية."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MallPlaceCategory.interestCategories.forEach { cat ->
                                    val selected = cat.name in preferences.selectedInterests
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onToggleInterest(cat) },
                                        label = {
                                            Text(
                                                text = cat.labelAr,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ElAbedWarmOrangeContainer,
                                            selectedLabelColor = Color(0xFF7D3B00)
                                        ),
                                        modifier = Modifier.testTag("notif_interest_${cat.name}")
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "أحدث الإشعارات والتنبيهات",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(notifications, key = { it.id }) { notif ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onMarkRead(notif.id)
                                onOpenLinkedTarget(notif)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!notif.isRead) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            } else {
                                ElAbedSurfaceVariant.copy(alpha = 0.45f)
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (!notif.isRead) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.secondary)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = notif.category.labelAr,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = notif.timestampLabelAr,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = notif.titleAr,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = notif.bodyAr,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
