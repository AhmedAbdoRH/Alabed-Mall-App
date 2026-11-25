package com.example.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.EventEntity
import com.example.data.model.MallPlaceCategory
import com.example.data.model.OfferEntity
import com.example.data.model.PromoBannerConfig
import com.example.data.model.StoreEntity
import com.example.data.search.MallSearchEngine
import com.example.ui.components.HomeEventCard
import com.example.ui.components.HomeFacilityQuickPill
import com.example.ui.components.HomeFeaturedStoreCard
import com.example.ui.components.HomeOfferTicketCard
import com.example.ui.components.HomeVisitorBentoCard
import com.example.ui.components.OpenStatusChip
import com.example.ui.components.PromoBannerImage
import com.example.ui.components.SectionHeader
import com.example.ui.theme.ElAbedOutline
import com.example.ui.theme.ElAbedRoyalBlue
import com.example.ui.theme.ElAbedRoyalBlueContainer
import com.example.ui.theme.ElAbedSurfaceVariant
import com.example.ui.theme.ElAbedVibrantGreen
import com.example.ui.theme.ElAbedVibrantGreenContainer
import com.example.ui.theme.ElAbedWarmOrange
import com.example.ui.theme.ElAbedWarmOrangeContainer

@Composable
fun HomeScreen(
    places: List<StoreEntity>,
    offers: List<OfferEntity>,
    events: List<EventEntity>,
    promoBanner: PromoBannerConfig,
    selectedInterests: Set<String>,
    tripStopCount: Int,
    shoppingTaskCount: Int,
    unreadNotifications: Int,
    onOpenDirectory: () -> Unit,
    onSearchWithQuery: (String?, String?) -> Unit,
    onOpenIndoorMap: (String?) -> Unit,
    onOpenAccessMap: () -> Unit,
    onOpenOffers: () -> Unit,
    onOpenEvents: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenVisitorServices: () -> Unit,
    onOpenTripPlanner: () -> Unit,
    onOpenStoreDetail: (String) -> Unit,
    onToggleInterest: (MallPlaceCategory) -> Unit,
    onShareOffer: (OfferEntity) -> Unit,
    onShareStore: (StoreEntity) -> Unit,
    onAddOfferToTrip: (OfferEntity) -> Unit,
    onAddStoreToTrip: (StoreEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val quickServices = places.filter { it.isServiceFacility }.take(6)
    val featuredStores = places.filter { !it.isServiceFacility && it.isFeatured }
    val storeCount = places.count { !it.isServiceFacility }
    val placeCategoryMap = remember(places) { places.associate { it.id to it.categoryName } }

    val voiceSearchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.trim()
            if (!spoken.isNullOrBlank()) {
                onSearchWithQuery(spoken, null)
            }
        }
    }

    fun launchVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-EG")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث باسم المحل أو المنتج للبحث...")
        }
        try {
            voiceSearchLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "البحث الصوتي غير متوفر على هذا الجهاز، يمكنك البحث بالكتابة.",
                Toast.LENGTH_SHORT
            ).show()
            onOpenDirectory()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Top Sunlit Mall Facade Hero Header + Voice Search
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(255.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_mall_hero),
                        contentDescription = "واجهة العابد مول بطوخ",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x5514255C),
                                        Color(0x2814255C),
                                        Color(0xE614255C)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
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
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White,
                                    shadowElevation = 2.dp
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.logo_elabed_mall),
                                        contentDescription = "شعار العابد مول",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .padding(3.dp)
                                    )
                                }
                                OpenStatusChip()
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color(0xEBFFFFFF),
                                shadowElevation = 2.dp
                            ) {
                                IconButton(
                                    onClick = onOpenNotifications,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("home_notifications_button")
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (unreadNotifications > 0) {
                                                Badge(
                                                    containerColor = ElAbedWarmOrange,
                                                    contentColor = Color.White
                                                ) { Text("$unreadNotifications") }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Notifications,
                                            contentDescription = "الإشعارات",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "العابد مول • طوخ",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "كيلو ٣٧، طريق الإسكندرية الزراعي • كل اللي نفسك وأكثر",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFEFF4FF)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onOpenDirectory() }
                                    .testTag("home_search_bar"),
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                shadowElevation = 3.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(start = 14.dp, end = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "بحث",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "ابحث بالاسم أو الهاشتاج أو الصوت...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { launchVoiceSearch() },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("home_voice_search_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Mic,
                                            contentDescription = "بحث بالصوت",
                                            tint = ElAbedRoyalBlue
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 1b. Quick Search Hashtags Strip
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(MallSearchEngine.popularHashtags, key = { it.tag }) { hashtag ->
                        Surface(
                            onClick = { onSearchWithQuery(hashtag.tag, hashtag.targetTab.name) },
                            shape = RoundedCornerShape(50),
                            color = ElAbedRoyalBlueContainer.copy(alpha = 0.55f),
                            border = BorderStroke(1.dp, ElAbedRoyalBlue.copy(alpha = 0.22f)),
                            modifier = Modifier.testTag("home_hashtag_${hashtag.tag}")
                        ) {
                            Text(
                                text = hashtag.tag,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = ElAbedRoyalBlue,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // 2. Admin-Editable Promotional Image Banner
            if (promoBanner.isVisible) {
                item {
                    AdminPromoBannerCard(
                        promoBanner = promoBanner,
                        onClick = {
                            val target = promoBanner.linkedTargetId
                            val hasStore = places.any { it.id == target }
                            if (hasStore) {
                                onOpenStoreDetail(target)
                            } else {
                                onOpenOffers()
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // 3. Prominent Interactive 2.5D Map Hero Showcase Card (Preserved)
            item {
                InteractiveMapHeroBanner(
                    onOpenMap = { onOpenIndoorMap(null) },
                    onExploreKidsOrFirstFloor = { onOpenIndoorMap("kids_rides") },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 4. Featured Offers & Visitor Interest Filter Carousel
            if (offers.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(
                            title = "عروض حسب اهتماماتك",
                            subtitle = if (selectedInterests.isEmpty()) {
                                "${offers.size} عروض حصرية نشطة • اختر اهتماماتك لتخصيصها"
                            } else {
                                "مخصصة لاهتماماتك المختارة (${selectedInterests.size})"
                            },
                            actionLabel = "عرض الكل",
                            onActionClick = onOpenOffers,
                            actionTestTag = "home_action_offers",
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Interactive Visitor Interests Picker
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(MallPlaceCategory.interestCategories, key = { it.name }) { cat ->
                                val selected = cat.name in selectedInterests
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
                                    modifier = Modifier.testTag("home_interest_chip_${cat.name}")
                                )
                            }
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            itemsIndexed(offers.take(8), key = { _, offer -> offer.id }) { idx, offer ->
                                val storeCat = placeCategoryMap[offer.storeId]
                                val isMatch = selectedInterests.isNotEmpty() && storeCat != null && storeCat in selectedInterests
                                HomeOfferTicketCard(
                                    offer = offer,
                                    index = idx,
                                    isInterestMatch = isMatch,
                                    onOpenStoreDetail = { onOpenStoreDetail(offer.storeId) },
                                    onOpenMapRoute = { onOpenIndoorMap(offer.storeId) },
                                    onShareOffer = { onShareOffer(offer) },
                                    onAddToTrip = { onAddOfferToTrip(offer) }
                                )
                            }
                        }
                    }
                }
            }

            // 5. Trip Planner & Hypermarket List + Customer Service Contact + Visitor Bento
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionHeader(
                        title = "مساعد زيارتك وخدمة العملاء",
                        subtitle = "خطط مشوارك وتواصل فوراً مع خدمة عملاء المول"
                    )

                    // Trip Planner & Hypermarket Tasks Card
                    Surface(
                        onClick = onOpenTripPlanner,
                        shape = RoundedCornerShape(18.dp),
                        color = ElAbedRoyalBlueContainer.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, ElAbedRoyalBlue.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("home_action_trip_planner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ElAbedRoyalBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Route,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "قائمة المشوار ومشتريات الهايبر",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ElAbedRoyalBlue
                                    )
                                    Text(
                                        text = "$tripStopCount محطات مرتبة بالخريطة • $shoppingTaskCount أصناف للهايبر",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "فتح قائمة المشوار",
                                tint = ElAbedRoyalBlue
                            )
                        }
                    }

                    // Dedicated Customer Service Contact & Inquiry Card
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = ElAbedVibrantGreenContainer.copy(alpha = 0.45f)),
                        border = BorderStroke(1.dp, ElAbedVibrantGreen.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ElAbedVibrantGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.SupportAgent,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "خدمة عملاء مول العابد",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "للاستفسار عن المحلات والعروض والمفقودات طوال اليوم",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onOpenVisitorServices,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("home_customer_service_inquiry_button")
                                ) {
                                    Icon(Icons.Filled.SupportAgent, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تواصل واستفسار", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0132460105"))
                                        context.startActivity(dialIntent)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("home_customer_service_call_button")
                                ) {
                                    Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("اتصال مباشر", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HomeVisitorBentoCard(
                            title = "الوصول للمول",
                            subtitle = "خرائط Google • ك٣٧",
                            badgeLabel = "مسار سريع",
                            icon = Icons.Filled.Directions,
                            accentColor = ElAbedRoyalBlue,
                            tintBackground = ElAbedRoyalBlueContainer.copy(alpha = 0.35f),
                            onClick = onOpenAccessMap,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_action_access")
                        )
                        HomeVisitorBentoCard(
                            title = "دليل المحلات",
                            subtitle = "$storeCount متجر ومطعم",
                            badgeLabel = "الأقسام",
                            icon = Icons.Filled.Storefront,
                            accentColor = ElAbedWarmOrange,
                            tintBackground = ElAbedWarmOrangeContainer.copy(alpha = 0.45f),
                            onClick = onOpenDirectory,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_action_directory")
                        )
                    }
                }
            }

            // 6. Quick Mall Facilities & ATMs Strip
            if (quickServices.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(
                            title = "مرافق وخدمات فورية",
                            subtitle = "بوابات، بنوك، مصلى وخدمات على الخريطة",
                            actionLabel = "الخريطة",
                            onActionClick = { onOpenIndoorMap(null) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(quickServices, key = { it.id }) { service ->
                                HomeFacilityQuickPill(
                                    place = service,
                                    onClick = { onOpenIndoorMap(service.id) }
                                )
                            }
                        }
                    }
                }
            }

            // 7. Events & New Openings
            if (events.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SectionHeader(
                            title = "الفعاليات والافتتاحات الجديدة",
                            subtitle = "أحدث الأنشطة والعروض الحية بالساحة",
                            actionLabel = "المزيد",
                            onActionClick = onOpenEvents
                        )
                        events.take(3).forEachIndexed { idx, event ->
                            HomeEventCard(
                                event = event,
                                isFeatured = idx == 0,
                                onClick = { onOpenIndoorMap(event.locationPlaceId) }
                            )
                        }
                    }
                }
            }

            // 8. Featured Stores Strip
            if (featuredStores.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(
                            title = "وجهات مميزة بالمول",
                            subtitle = "الأكثر زيارة وتقييماً",
                            actionLabel = "دليل المحلات",
                            onActionClick = onOpenDirectory,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(featuredStores, key = { it.id }) { store ->
                                HomeFeaturedStoreCard(
                                    store = store,
                                    onOpenDetails = { onOpenStoreDetail(store.id) },
                                    onOpenMapRoute = { onOpenIndoorMap(store.id) },
                                    onShareStore = { onShareStore(store) },
                                    onAddToTrip = { onAddStoreToTrip(store) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminPromoBannerCard(
    promoBanner: PromoBannerConfig,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(156.dp)
            .clickable(onClick = onClick)
            .testTag("home_admin_promo_banner"),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            PromoBannerImage(
                imagePathOrKey = promoBanner.imagePathOrKey,
                contentDescription = promoBanner.titleAr,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x33000000),
                                Color(0xCC141926)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = ElAbedWarmOrange
                ) {
                    Text(
                        text = promoBanner.badgeAr,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = promoBanner.titleAr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = promoBanner.subtitleAr,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFEFF4FF),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun InteractiveMapHeroBanner(
    onOpenMap: () -> Unit,
    onExploreKidsOrFirstFloor: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable(onClick = onOpenMap)
            .testTag("home_action_map"),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_mall_game_map_f0),
                contentDescription = "معاينة الخريطة التفاعلية لمول العابد",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xEE18317A),
                                Color(0xCC28459B),
                                Color(0x55102657)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF4DB4A9)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Explore,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "خريطة 2.5D تفاعلية",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xCCFFFFFF)
                    ) {
                        Text(
                            text = "محاكي + GPS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "تجول داخل العابد مول تفاعلياً!",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "استكشف الهايبر والمطاعم والألعاب، أو احصل على المسار المباشر.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFEAF0FF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onOpenMap,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "افتح الخريطة",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x33FFFFFF),
                        modifier = Modifier.clickable(onClick = onExploreKidsOrFirstFloor)
                    ) {
                        Text(
                            text = "الدور الأول",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
