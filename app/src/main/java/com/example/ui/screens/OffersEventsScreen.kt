package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.EventEntity
import com.example.data.model.OfferEntity
import com.example.notifications.MallNotificationHelper
import com.example.notifications.OfferScheduleDateHelper
import com.example.ui.components.FloorPill
import com.example.ui.theme.ElAbedRoyalBlue
import com.example.ui.theme.ElAbedRoyalBlueContainer
import com.example.ui.theme.ElAbedVibrantGreen
import com.example.ui.theme.ElAbedVibrantGreenContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OffersEventsScreen(
    offers: List<OfferEntity>,
    events: List<EventEntity>,
    savedOfferIds: Set<String> = emptySet(),
    initialTab: Int = 0,
    onOpenStoreDetail: (String) -> Unit,
    onOpenIndoorMap: (String) -> Unit,
    onShareOffer: (OfferEntity) -> Unit = {},
    onAddOfferToTrip: (OfferEntity) -> Unit = {},
    onToggleSaveOffer: (OfferEntity) -> Boolean = { true },
    modifier: Modifier = Modifier
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    var showSavedOnly by remember { mutableStateOf(false) }
    var copiedOfferId by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        scope.launch {
            snackbarHostState.showSnackbar(
                if (granted) {
                    "تم تفعيل إذن الإشعارات وسيصلك التذكير قبل انتهاء العرض"
                } else {
                    "تم حفظ العرض داخل التطبيق — فعّل إذن الإشعارات لاحقاً لوصول تنبيه على الجهاز"
                }
            )
        }
    }

    val displayedOffers = remember(offers, savedOfferIds, showSavedOnly) {
        if (showSavedOnly) offers.filter { it.id in savedOfferIds } else offers
    }

    androidx.compose.runtime.LaunchedEffect(copiedOfferId) {
        if (copiedOfferId != null) {
            delay(1800)
            copiedOfferId = null
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
            ) {
                Text(
                    text = "العروض والفعاليات",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("العروض والكوبونات (${offers.size})") },
                        modifier = Modifier.testTag("tab_offers")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("الفعاليات والافتتاحات (${events.size})") },
                        modifier = Modifier.testTag("tab_events")
                    )
                }

                if (selectedTab == 0) {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Image(
                                painter = painterResource(id = R.drawable.img_hypermarket_banner),
                                contentDescription = "عروض هايبر ماركت ومحلات مول العابد",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(MaterialTheme.shapes.large)
                            )
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = !showSavedOnly,
                                    onClick = { showSavedOnly = false },
                                    label = { Text("كل العروض (${offers.size})", fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ElAbedRoyalBlueContainer,
                                        selectedLabelColor = ElAbedRoyalBlue
                                    ),
                                    modifier = Modifier.testTag("offers_filter_all")
                                )
                                FilterChip(
                                    selected = showSavedOnly,
                                    onClick = { showSavedOnly = true },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (showSavedOnly) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            "المحفوظة (${offers.count { it.id in savedOfferIds }})",
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ElAbedRoyalBlueContainer,
                                        selectedLabelColor = ElAbedRoyalBlue
                                    ),
                                    modifier = Modifier.testTag("offers_filter_saved")
                                )
                            }
                        }
                        if (displayedOffers.isEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "لا توجد عروض محفوظة حالياً. اضغط «حفظ وتذكير» على أي عرض ليصلك تنبيه قبل انتهائه.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(18.dp)
                                    )
                                }
                            }
                        }
                        items(displayedOffers, key = { it.id }) { offer ->
                            val isSaved = offer.id in savedOfferIds
                            Card(
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                text = offer.discountBadge,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                        Text(
                                            text = offer.validUntilAr,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = offer.storeNameAr,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = offer.titleAr,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = offer.descriptionAr,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "الشروط: ${offer.termsAr}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Coupon Code Box
                                     val isCopied = copiedOfferId == offer.id
                                     Surface(
                                         shape = RoundedCornerShape(12.dp),
                                         color = if (isCopied) ElAbedVibrantGreenContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
                                     ) {
                                         Row(
                                             modifier = Modifier
                                                 .fillMaxWidth()
                                                 .padding(horizontal = 14.dp, vertical = 8.dp),
                                             horizontalArrangement = Arrangement.SpaceBetween,
                                             verticalAlignment = Alignment.CenterVertically
                                         ) {
                                             Text(
                                                 text = "كوبون: ${offer.couponCode}",
                                                 style = MaterialTheme.typography.titleSmall,
                                                 fontWeight = FontWeight.Bold,
                                                 color = if (isCopied) ElAbedVibrantGreen else MaterialTheme.colorScheme.primary
                                             )
                                             Surface(
                                                 onClick = {
                                                     val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                     clipboard.setPrimaryClip(ClipData.newPlainText("Coupon", offer.couponCode))
                                                     copiedOfferId = offer.id
                                                     scope.launch {
                                                         snackbarHostState.showSnackbar("تم نسخ الكوبون ${offer.couponCode} بنجاح")
                                                     }
                                                 },
                                                 shape = RoundedCornerShape(8.dp),
                                                 color = if (isCopied) ElAbedVibrantGreen else MaterialTheme.colorScheme.primary
                                             ) {
                                                 Row(
                                                     modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                     verticalAlignment = Alignment.CenterVertically,
                                                     horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                 ) {
                                                     Icon(
                                                         imageVector = if (isCopied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                                                         contentDescription = "نسخ الكوبون",
                                                         tint = Color.White,
                                                         modifier = Modifier.size(14.dp)
                                                     )
                                                     Text(
                                                         text = if (isCopied) "تم النسخ ✓" else "نسخ الكود",
                                                         style = MaterialTheme.typography.labelSmall,
                                                         fontWeight = FontWeight.Bold,
                                                         color = Color.White
                                                     )
                                                 }
                                             }
                                         }
                                     }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = { onOpenStoreDetail(offer.storeId) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                        ) {
                                            Icon(Icons.Filled.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("المحل", style = MaterialTheme.typography.labelLarge)
                                        }
                                        FilledTonalButton(
                                            onClick = { onOpenIndoorMap(offer.storeId) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                        ) {
                                            Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("الخريطة", style = MaterialTheme.typography.labelLarge)
                                        }
                                    }

                                    if (isSaved) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = ElAbedVibrantGreenContainer.copy(alpha = 0.5f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.AlarmOn,
                                                    contentDescription = null,
                                                    tint = ElAbedVibrantGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                val expiryDateText = if (offer.expiresAtMillis > 0L) {
                                                    " • ينتهي ${OfferScheduleDateHelper.formatArabicDate(offer.expiresAtMillis)}"
                                                } else ""
                                                Text(
                                                    text = "مجدول تذكير محلي قبل الانتهاء بيوم$expiryDateText",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = ElAbedVibrantGreen
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val nowSaved = onToggleSaveOffer(offer)
                                                if (nowSaved) {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                                        !MallNotificationHelper.hasNotificationPermission(context)
                                                    ) {
                                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                    } else {
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("تم حفظ العرض وجدولة تذكير محلي قبل انتهائه بيوم")
                                                        }
                                                    }
                                                } else {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("تمت إزالة العرض من المحفوظات وإلغاء التذكير")
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSaved) ElAbedVibrantGreen else ElAbedRoyalBlue,
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .testTag("offer_save_btn_${offer.id}")
                                        ) {
                                            Icon(
                                                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isSaved) "محفوظ ✓" else "حفظ وتذكير",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                onAddOfferToTrip(offer)
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("تمت إضافة العرض لقائمة المشوار")
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .testTag("offer_trip_btn_${offer.id}")
                                        ) {
                                            Icon(Icons.Filled.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("أضف للمشوار", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    FilledTonalButton(
                                        onClick = { onShareOffer(offer) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("offer_share_btn_${offer.id}")
                                    ) {
                                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("مشاركة العرض", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Image(
                                painter = painterResource(id = R.drawable.img_events_banner),
                                contentDescription = "فعاليات ومهرجانات مول العابد",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(MaterialTheme.shapes.large)
                            )
                        }
                        items(events, key = { it.id }) { event ->
                            Card(
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (event.isNewOpening) {
                                                MaterialTheme.colorScheme.secondary
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            }
                                        ) {
                                            Text(
                                                text = event.badgeAr,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        FloorPill(floor = event.floor)
                                    }

                                    Text(
                                        text = event.titleAr,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Text(
                                        text = event.descriptionAr,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Event,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "${event.dateRangeAr} • ${event.timeAr}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    FilledTonalButton(
                                        onClick = { onOpenIndoorMap(event.locationPlaceId) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("عرض الموقع على الخريطة (${event.locationLabelAr})")
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
