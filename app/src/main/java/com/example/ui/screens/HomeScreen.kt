package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.EventEntity
import com.example.data.model.OfferEntity
import com.example.data.model.StoreEntity
import com.example.ui.components.HomeEventCard
import com.example.ui.components.HomeFacilityQuickPill
import com.example.ui.components.HomeFeaturedStoreCard
import com.example.ui.components.HomeOfferTicketCard
import com.example.ui.components.HomeVisitorBentoCard
import com.example.ui.components.OpenStatusChip
import com.example.ui.components.SectionHeader
import com.example.ui.theme.ElAbedOutline
import com.example.ui.theme.ElAbedRoyalBlue
import com.example.ui.theme.ElAbedRoyalBlueContainer
import com.example.ui.theme.ElAbedSurfaceVariant
import com.example.ui.theme.ElAbedVibrantGreen
import com.example.ui.theme.ElAbedVibrantGreenContainer
import com.example.ui.theme.ElAbedWarmOrange

@Composable
fun HomeScreen(
    places: List<StoreEntity>,
    offers: List<OfferEntity>,
    events: List<EventEntity>,
    unreadNotifications: Int,
    onOpenDirectory: () -> Unit,
    onOpenIndoorMap: (String?) -> Unit,
    onOpenAccessMap: () -> Unit,
    onOpenOffers: () -> Unit,
    onOpenEvents: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenVisitorServices: () -> Unit,
    onOpenStoreDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickServices = places.filter { it.isServiceFacility }.take(6)
    val featuredStores = places.filter { !it.isServiceFacility && it.isFeatured }
    val storeCount = places.count { !it.isServiceFacility }

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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Top Sunlit Mall Facade Hero Header (Preserved)
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
                                    .height(48.dp)
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
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "بحث",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "ابحث عن محل، مطعم، هايبر أو خدمة...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Prominent Interactive 2.5D Map Hero Showcase Card (Preserved)
            item {
                InteractiveMapHeroBanner(
                    onOpenMap = { onOpenIndoorMap(null) },
                    onExploreKidsOrFirstFloor = { onOpenIndoorMap("kids_rides") },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 3. Featured Offers & Coupons Carousel (Immediately below the two top banners)
            if (offers.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(
                            title = "أبرز العروض والكوبونات",
                            subtitle = "${offers.size} عروض حصرية نشطة الآن بالمول",
                            actionLabel = "عرض الكل",
                            onActionClick = onOpenOffers,
                            actionTestTag = "home_action_offers",
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            itemsIndexed(offers.take(6), key = { _, offer -> offer.id }) { idx, offer ->
                                HomeOfferTicketCard(
                                    offer = offer,
                                    index = idx,
                                    onOpenStoreDetail = { onOpenStoreDetail(offer.storeId) },
                                    onOpenMapRoute = { onOpenIndoorMap(offer.storeId) }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Smart Visitor Bento Grid (Access + Visitor Services + Full Directory Pill)
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionHeader(
                        title = "خدماتك السريعة بالمول",
                        subtitle = "تنقل أسهل من بيتك لحد المحل"
                    )
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
                            title = "خدمة الزوار",
                            subtitle = "استعلامات ومفقودات",
                            badgeLabel = "مساعدة",
                            icon = Icons.Filled.SupportAgent,
                            accentColor = ElAbedVibrantGreen,
                            tintBackground = ElAbedVibrantGreenContainer.copy(alpha = 0.42f),
                            onClick = onOpenVisitorServices,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_action_services")
                        )
                    }

                    // Sleek full-width Directory Quick Dock Bar
                    Surface(
                        onClick = onOpenDirectory,
                        shape = RoundedCornerShape(18.dp),
                        color = ElAbedSurfaceVariant,
                        border = BorderStroke(1.dp, ElAbedOutline.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("home_action_directory")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
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
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ElAbedWarmOrange),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Storefront,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "دليل المحلات والأقسام",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$storeCount متجر ومطعم وخدمة مصنفة حسب الدور",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 1.dp
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "فتح الدليل",
                                    tint = ElAbedRoyalBlue,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 5. Quick Mall Facilities & ATMs Strip
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

            // 6. Events & New Openings
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

            // 7. Featured Stores Strip
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
                                    onOpenMapRoute = { onOpenIndoorMap(store.id) }
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
private fun InteractiveMapHeroBanner(
    onOpenMap: () -> Unit,
    onExploreKidsOrFirstFloor: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(215.dp)
            .clickable(onClick = onOpenMap)
            .testTag("home_action_map"),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    .padding(18.dp),
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
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Explore,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "خريطة 2.5D تفاعلية • دورين",
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
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "تجول داخل العابد مول كأنك في عالم حي!",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "حرّك الشخصية التفاعلية لاستكشاف الهايبر والمطاعم والألعاب، أو احصل على المسار المباشر.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFEAF0FF),
                        maxLines = 2,
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
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "افتح الخريطة الآن",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x33FFFFFF),
                        modifier = Modifier.clickable(onClick = onExploreKidsOrFirstFloor)
                    ) {
                        Text(
                            text = "مسار الدور الأول",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                        )
                    }
                }
            }
        }
    }
}
