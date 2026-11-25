package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.EventEntity
import com.example.data.model.MallPlaceCategory
import com.example.data.model.NotificationCategory
import com.example.data.model.OfferEntity
import com.example.data.model.PromoBannerConfig
import com.example.data.model.StoreEntity
import com.example.ui.components.PromoBannerImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentManagerScreen(
    places: List<StoreEntity>,
    offers: List<OfferEntity>,
    events: List<EventEntity>,
    promoBanner: PromoBannerConfig = PromoBannerConfig(),
    onBack: () -> Unit,
    onSaveStore: (
        id: String?,
        nameAr: String,
        nameEn: String,
        category: MallPlaceCategory,
        floor: Int,
        zoneAr: String,
        descriptionAr: String,
        hoursAr: String,
        phone: String,
        mapX: Float,
        mapY: Float,
        isNewOpening: Boolean
    ) -> Unit,
    onDeleteStore: (String) -> Unit,
    onAddOffer: (
        storeId: String,
        storeNameAr: String,
        titleAr: String,
        descriptionAr: String,
        discountBadge: String,
        couponCode: String,
        validUntilAr: String,
        termsAr: String
    ) -> Unit,
    onAddEvent: (
        titleAr: String,
        descriptionAr: String,
        dateRangeAr: String,
        timeAr: String,
        locationPlaceId: String,
        locationLabelAr: String,
        floor: Int,
        isNewOpening: Boolean
    ) -> Unit,
    onBroadcastNotification: (
        titleAr: String,
        bodyAr: String,
        category: NotificationCategory,
        linkedTargetId: String?
    ) -> Unit,
    onSavePromoBanner: (
        pickedImageUri: Uri?,
        presetDrawableKey: String?,
        titleAr: String,
        subtitleAr: String,
        badgeAr: String,
        linkedTargetId: String,
        isVisible: Boolean
    ) -> Unit = { _, _, _, _, _, _, _ -> },
    onResetPromoBanner: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("إدارة محتوى المول") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("cms_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
            ) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 12.dp
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("المحلات") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("العروض") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("الفعاليات") })
                    Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("الإشعارات") })
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = { Text("البنر الترويجي") },
                        modifier = Modifier.testTag("cms_tab_promo_banner")
                    )
                }

                when (selectedTab) {
                    0 -> StoreCmsTab(
                        places = places,
                        onSaveStore = { nameAr, nameEn, cat, floor, zone, desc, hours, phone, mx, my, isNew ->
                            onSaveStore(null, nameAr, nameEn, cat, floor, zone, desc, hours, phone, mx, my, isNew)
                            scope.launch { snackbarHostState.showSnackbar("تمت إضافة/تحديث المكان على الدليل والخريطة") }
                        },
                        onDeleteStore = onDeleteStore
                    )
                    1 -> OfferCmsTab(
                        places = places,
                        offers = offers,
                        onAddOffer = { sId, sName, title, desc, badge, code, valid, terms ->
                            onAddOffer(sId, sName, title, desc, badge, code, valid, terms)
                            scope.launch { snackbarHostState.showSnackbar("تم نشر العرض الجديد وإرسال الإشعار للمهتمين") }
                        }
                    )
                    2 -> EventCmsTab(
                        places = places,
                        events = events,
                        onAddEvent = { title, desc, date, time, pId, pLabel, floor, isNew ->
                            onAddEvent(title, desc, date, time, pId, pLabel, floor, isNew)
                            scope.launch { snackbarHostState.showSnackbar("تم نشر الفعالية بنجاح") }
                        }
                    )
                    3 -> NotificationCmsTab(
                        places = places,
                        onBroadcast = { title, body, cat, targetId ->
                            onBroadcastNotification(title, body, cat, targetId)
                            scope.launch { snackbarHostState.showSnackbar("تم إرسال الإشعار للزوار") }
                        }
                    )
                    else -> PromoBannerCmsTab(
                        places = places,
                        currentBanner = promoBanner,
                        onSaveBanner = { uri, presetKey, title, subtitle, badge, targetId, visible ->
                            onSavePromoBanner(uri, presetKey, title, subtitle, badge, targetId, visible)
                            scope.launch { snackbarHostState.showSnackbar("تم تحديث الصورة الترويجية في الرئيسية") }
                        },
                        onResetBanner = {
                            onResetPromoBanner()
                            scope.launch { snackbarHostState.showSnackbar("تمت استعادة البنر الافتراضي") }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreCmsTab(
    places: List<StoreEntity>,
    onSaveStore: (String, String, MallPlaceCategory, Int, String, String, String, String, Float, Float, Boolean) -> Unit,
    onDeleteStore: (String) -> Unit
) {
    var nameAr by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var zoneAr by remember { mutableStateOf("") }
    var descAr by remember { mutableStateOf("") }
    var hoursAr by remember { mutableStateOf("١٠:٠٠ ص - ١٢:٠٠ ص") }
    var phone by remember { mutableStateOf("0132460100") }
    var floor by remember { mutableIntStateOf(0) }
    var category by remember { mutableStateOf(MallPlaceCategory.FASHION) }
    var mapX by remember { mutableFloatStateOf(640f) }
    var mapY by remember { mutableFloatStateOf(380f) }
    var isNewOpening by remember { mutableStateOf(true) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("إضافة محل أو خدمة وتحديد موقعه على الخريطة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = nameAr, onValueChange = { nameAr = it }, label = { Text("اسم المحل بالعربية *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nameEn, onValueChange = { nameEn = it }, label = { Text("الاسم بالإنجليزية") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = floor == 0, onClick = { floor = 0 }, label = { Text("الدور الأرضي") })
                        FilterChip(selected = floor == 1, onClick = { floor = 1 }, label = { Text("الدور الأول") })
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(MallPlaceCategory.entries) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat.labelAr) }
                            )
                        }
                    }

                    OutlinedTextField(value = zoneAr, onValueChange = { zoneAr = it }, label = { Text("المنطقة (مثال: الرواق الشرقي)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = descAr, onValueChange = { descAr = it }, label = { Text("وصف النشاط *") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = hoursAr, onValueChange = { hoursAr = it }, label = { Text("مواعيد العمل") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("رقم التواصل") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                    Text("إحداثي X على الخريطة (${mapX.toInt()} / 1000)", style = MaterialTheme.typography.labelMedium)
                    Slider(value = mapX, onValueChange = { mapX = it }, valueRange = 100f..880f)
                    Text("إحداثي Y على الخريطة (${mapY.toInt()} / 700)", style = MaterialTheme.typography.labelMedium)
                    Slider(value = mapY, onValueChange = { mapY = it }, valueRange = 100f..590f)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isNewOpening, onCheckedChange = { isNewOpening = it })
                        Text("وسم كـ (افتتاح جديد)")
                    }

                    Button(
                        onClick = {
                            if (nameAr.isNotBlank() && descAr.isNotBlank()) {
                                onSaveStore(nameAr, nameEn, category, floor, zoneAr, descAr, hoursAr, phone, mapX, mapY, isNewOpening)
                                nameAr = ""
                                nameEn = ""
                                zoneAr = ""
                                descAr = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Text("حفظ وإضافة للخريطة والدليل")
                    }
                }
            }
        }

        items(places, key = { it.id }) { place ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(place.nameAr, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${place.floorLabelAr} • (${place.mapX.toInt()}, ${place.mapY.toInt()})", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onDeleteStore(place.id) }) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "حذف")
                    }
                }
            }
        }
    }
}

@Composable
private fun OfferCmsTab(
    places: List<StoreEntity>,
    offers: List<OfferEntity>,
    onAddOffer: (String, String, String, String, String, String, String, String) -> Unit
) {
    val stores = remember(places) { places.filter { !it.isServiceFacility } }
    var selectedStore by remember(stores) { mutableStateOf(stores.firstOrNull()) }
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("خصم ٢٠٪") }
    var coupon by remember { mutableStateOf("ELABED20") }
    var validUntil by remember { mutableStateOf("ساري لنهاية الأسبوع") }
    var terms by remember { mutableStateOf("يطبق داخل الفرع بالمول.") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("إضافة عرض أو كوبون جديد", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(stores, key = { it.id }) { s ->
                            FilterChip(
                                selected = selectedStore?.id == s.id,
                                onClick = { selectedStore = s },
                                label = { Text(s.nameAr) }
                            )
                        }
                    }
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان العرض *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("تفاصيل العرض *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = discount, onValueChange = { discount = it }, label = { Text("شارة الخصم") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = coupon, onValueChange = { coupon = it }, label = { Text("كود الكوبون") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = validUntil, onValueChange = { validUntil = it }, label = { Text("صلاحية العرض") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = terms, onValueChange = { terms = it }, label = { Text("شروط الاستفادة") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            val st = selectedStore
                            if (st != null && title.isNotBlank() && desc.isNotBlank()) {
                                onAddOffer(st.id, st.nameAr, title, desc, discount, coupon, validUntil, terms)
                                title = ""
                                desc = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("نشر العرض")
                    }
                }
            }
        }
        items(offers, key = { it.id }) { offer ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(offer.titleAr, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("${offer.storeNameAr} • ${offer.discountBadge}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun EventCmsTab(
    places: List<StoreEntity>,
    events: List<EventEntity>,
    onAddEvent: (String, String, String, String, String, String, Int, Boolean) -> Unit
) {
    var selectedPlace by remember(places) { mutableStateOf(places.firstOrNull { it.id == "fountain_plaza" } ?: places.firstOrNull()) }
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var dateRange by remember { mutableStateOf("الجمعة والسبت") }
    var timeRange by remember { mutableStateOf("٦:٠٠ م - ١٠:٠٠ م") }
    var isNewOpening by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("إضافة فعالية أو افتتاح فرع جديد", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(places, key = { it.id }) { p ->
                            FilterChip(
                                selected = selectedPlace?.id == p.id,
                                onClick = { selectedPlace = p },
                                label = { Text(p.nameAr) }
                            )
                        }
                    }
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان الفعالية أو الافتتاح *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("التفاصيل *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = dateRange, onValueChange = { dateRange = it }, label = { Text("التاريخ / الأيام") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = timeRange, onValueChange = { timeRange = it }, label = { Text("المواعيد") }, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isNewOpening, onCheckedChange = { isNewOpening = it })
                        Text("إعلان افتتاح جديد")
                    }
                    Button(
                        onClick = {
                            val pl = selectedPlace
                            if (pl != null && title.isNotBlank() && desc.isNotBlank()) {
                                onAddEvent(title, desc, dateRange, timeRange, pl.id, "${pl.zoneAr} • ${pl.floorLabelAr}", pl.floor, isNewOpening)
                                title = ""
                                desc = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("نشر الفعالية")
                    }
                }
            }
        }
        items(events, key = { it.id }) { ev ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(ev.titleAr, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("${ev.dateRangeAr} • ${ev.locationLabelAr}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun NotificationCmsTab(
    places: List<StoreEntity>,
    onBroadcast: (String, String, NotificationCategory, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(NotificationCategory.OFFER) }
    var linkedPlace by remember { mutableStateOf<StoreEntity?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("إرسال إشعار جديد لزوار المول", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(NotificationCategory.entries) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat.labelAr) }
                            )
                        }
                    }
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان الإشعار *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("نص الإشعار *") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    Text("ربط بمكان على الخريطة (اختياري):", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(places.take(10), key = { it.id }) { p ->
                            FilterChip(
                                selected = linkedPlace?.id == p.id,
                                onClick = { linkedPlace = if (linkedPlace?.id == p.id) null else p },
                                label = { Text(p.nameAr) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && body.isNotBlank()) {
                                onBroadcast(title, body, category, linkedPlace?.id)
                                title = ""
                                body = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("إرسال الإشعار الآن")
                    }
                }
            }
        }
    }
}

@Composable
private fun PromoBannerCmsTab(
    places: List<StoreEntity>,
    currentBanner: PromoBannerConfig,
    onSaveBanner: (
        pickedImageUri: Uri?,
        presetDrawableKey: String?,
        titleAr: String,
        subtitleAr: String,
        badgeAr: String,
        linkedTargetId: String,
        isVisible: Boolean
    ) -> Unit,
    onResetBanner: () -> Unit
) {
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var presetKey by remember(currentBanner.imagePathOrKey) {
        mutableStateOf(
            if (currentBanner.imagePathOrKey.startsWith("drawable:")) currentBanner.imagePathOrKey else null
        )
    }
    var title by remember(currentBanner.titleAr) { mutableStateOf(currentBanner.titleAr) }
    var subtitle by remember(currentBanner.subtitleAr) { mutableStateOf(currentBanner.subtitleAr) }
    var badge by remember(currentBanner.badgeAr) { mutableStateOf(currentBanner.badgeAr) }
    var linkedTargetId by remember(currentBanner.linkedTargetId) { mutableStateOf(currentBanner.linkedTargetId) }
    var isVisible by remember(currentBanner.isVisible) { mutableStateOf(currentBanner.isVisible) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pickedUri = uri
            presetKey = null
        }
    }

    val presets = listOf(
        PromoBannerConfig.DEFAULT_DRAWABLE_HYPERMARKET to "عروض الهايبر",
        PromoBannerConfig.DEFAULT_DRAWABLE_EVENTS to "ساحة الفعاليات",
        PromoBannerConfig.DEFAULT_DRAWABLE_HERO to "واجهة المول"
    )

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "تحكم بالصورة الترويجية في الرئيسية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        if (pickedUri != null) {
                            AsyncImage(
                                model = pickedUri,
                                contentDescription = "معاينة الصورة المختارة",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            PromoBannerImage(
                                imagePathOrKey = presetKey ?: currentBanner.imagePathOrKey,
                                contentDescription = "معاينة البنر الحالي",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("cms_pick_banner_image_button")
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اختيار صورة من معرض الجهاز", fontWeight = FontWeight.Bold)
                    }

                    Text("أو اختر من خلفيات المول الجاهزة:", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presets) { (key, label) ->
                            FilterChip(
                                selected = pickedUri == null && (presetKey == key || (presetKey == null && currentBanner.imagePathOrKey == key)),
                                onClick = {
                                    pickedUri = null
                                    presetKey = key
                                },
                                label = { Text(label) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = badge,
                        onValueChange = { badge = it },
                        label = { Text("الشارة الترويجية (مثال: عرض الأسبوع)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان البنر الترويجي") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = subtitle,
                        onValueChange = { subtitle = it },
                        label = { Text("الوصف المختصر") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("توجيه عند الضغط على البنر:", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(places.filter { !it.isServiceFacility }.take(8), key = { it.id }) { place ->
                            FilterChip(
                                selected = linkedTargetId == place.id,
                                onClick = { linkedTargetId = place.id },
                                label = { Text(place.nameAr) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("إظهار البنر للزوار في الرئيسية", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = isVisible, onCheckedChange = { isVisible = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onSaveBanner(pickedUri, presetKey, title, subtitle, badge, linkedTargetId, isVisible)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("cms_save_banner_button")
                        ) {
                            Text("حفظ ونشر البنر", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                pickedUri = null
                                onResetBanner()
                            },
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("استعادة الافتراضي")
                        }
                    }
                }
            }
        }
    }
}
