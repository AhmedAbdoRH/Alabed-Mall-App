package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.OfferEntity
import com.example.data.model.StoreEntity
import com.example.ui.components.FloorPill
import com.example.ui.components.StoreLogoBadge
import com.example.ui.theme.ElAbedSurfaceVariant
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailScreen(
    store: StoreEntity?,
    storeOffers: List<OfferEntity>,
    onBack: () -> Unit,
    onOpenIndoorMap: (String) -> Unit,
    onShareStore: (StoreEntity) -> Unit = {},
    onShareOffer: (OfferEntity) -> Unit = {},
    onAddStoreToTrip: (StoreEntity) -> Unit = {},
    onOpenCustomerService: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(store?.nameAr ?: "تفاصيل المكان", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("store_detail_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (store == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("لم يتم العثور على بيانات المحل", style = MaterialTheme.typography.titleMedium)
            }
            return@Scaffold
        }

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
                    .widthIn(max = 780.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StoreLogoBadge(
                                    store = store,
                                    size = 56.dp
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = store.nameAr,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${store.nameEn} • ${store.category.labelAr}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FloorPill(floor = store.floor)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = store.zoneAr,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = store.descriptionAr,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "مواعيد العمل: ${store.hoursAr}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "${store.floorLabelAr} — ${store.zoneAr}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onOpenIndoorMap(store.id) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("store_detail_map_button")
                                ) {
                                    Icon(Icons.Filled.Navigation, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("توجيه على الخريطة", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                }

                                if (store.phone.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            val dialIntent = Intent(
                                                Intent.ACTION_DIAL,
                                                Uri.parse("tel:${store.phone}")
                                            )
                                            context.startActivity(dialIntent)
                                        },
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("store_detail_call_button")
                                    ) {
                                        Icon(Icons.Filled.Call, contentDescription = "اتصال")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("اتصال", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        onAddStoreToTrip(store)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("تمت إضافة ${store.nameAr} إلى قائمة المشوار")
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("store_detail_trip_button")
                                ) {
                                    Icon(Icons.Filled.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("أضف للمشوار", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { onShareStore(store) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("store_detail_share_button")
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("مشاركة المحل", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = onOpenCustomerService,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("store_detail_cs_button")
                            ) {
                                Icon(Icons.Filled.SupportAgent, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("استفسار خدمة عملاء المول", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (storeOffers.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ElAbedSurfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "لا توجد عروض نشطة لهذا المحل حالياً",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "تابع قسم العروض والفعاليات للاطلاع على أحدث الخصومات والمهرجانات",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "العروض المتاحة في ${store.nameAr}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(storeOffers, key = { it.id }) { offer ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
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
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = offer.titleAr,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = offer.descriptionAr,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "كود الخصم: ${offer.couponCode}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "الشروط: ${offer.termsAr}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                FilledTonalButton(
                                    onClick = { onShareOffer(offer) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("مشاركة العرض بصورة ورابط", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
