package com.example.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.StoreMallDirectory
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.MallPlaceCategory
import com.example.data.model.OfferEntity
import com.example.data.model.StoreEntity
import com.example.data.search.MallProductCatalog
import com.example.data.search.MallSearchEngine
import com.example.data.search.SearchPathTab
import com.example.ui.components.BrandPlaceDirectoryCard
import com.example.ui.components.ProductSuggestionPillCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.ElAbedOutline
import com.example.ui.theme.ElAbedRoyalBlue
import com.example.ui.theme.ElAbedRoyalBlueContainer
import com.example.ui.theme.ElAbedSurfaceVariant
import com.example.ui.theme.ElAbedTextPrimary
import com.example.ui.theme.ElAbedTextSecondary
import kotlinx.coroutines.launch

private enum class DirectoryBrowseTab(
    val labelAr: String,
    val icon: ImageVector
) {
    PRODUCTS("المنتجات والخدمات", Icons.Filled.ShoppingBag),
    PLACES("البراندات والأماكن", Icons.Filled.Storefront)
}

@Composable
fun DirectoryScreen(
    places: List<StoreEntity>,
    offers: List<OfferEntity>,
    initialCategoryName: String? = null,
    initialQuery: String? = null,
    initialSearchTabName: String? = null,
    onOpenStoreDetail: (String) -> Unit,
    onNavigateOnMap: (String) -> Unit,
    onShareStore: (StoreEntity) -> Unit = {},
    onAddStoreToTrip: (StoreEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember(initialQuery) { mutableStateOf(initialQuery.orEmpty()) }
    var activeTab by remember(initialCategoryName, initialSearchTabName) {
        mutableStateOf(
            when {
                initialCategoryName != null -> DirectoryBrowseTab.PLACES
                initialSearchTabName == SearchPathTab.BRANDS.name -> DirectoryBrowseTab.PLACES
                else -> DirectoryBrowseTab.PRODUCTS
            }
        )
    }
    var selectedCategory by remember(initialCategoryName) {
        mutableStateOf(
            MallPlaceCategory.entries.firstOrNull { it.name == initialCategoryName }
        )
    }
    var selectedFloorFilter by remember { mutableIntStateOf(-1) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.trim()
            if (!spoken.isNullOrBlank()) {
                searchQuery = spoken
            }
        }
    }

    fun launchVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-EG")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث باسم المنتج أو البراند أو الخدمة...")
        }
        try {
            voiceLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            scope.launch {
                snackbarHostState.showSnackbar("البحث الصوتي غير مدعوم على هذا الجهاز، يمكنك الكتابة هنا.")
            }
        }
    }

    val offersByStore = remember(offers) { offers.groupBy { it.storeId } }

    val filteredProducts = remember(searchQuery, selectedCategory, selectedFloorFilter) {
        MallProductCatalog.filterProducts(
            rawQuery = searchQuery,
            selectedCategory = selectedCategory,
            selectedFloor = selectedFloorFilter
        )
    }

    val filteredPlaces = remember(places, offers, searchQuery, selectedCategory, selectedFloorFilter) {
        MallSearchEngine.filterPlaces(
            places = places,
            offers = offers,
            rawQuery = searchQuery,
            searchTab = SearchPathTab.ALL,
            selectedCategory = selectedCategory,
            selectedFloor = selectedFloorFilter
        ).sortedWith(
            compareBy<StoreEntity> { it.isServiceFacility }
                .thenByDescending { it.isFeatured }
                .thenBy { it.floor }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
            ) {
                // Header + Modern Search Bar + 2-Option Segmented Pill Toggle Directly Below Search
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "دليل العابد مول",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = ElAbedTextPrimary
                            )
                            Text(
                                text = "ابحث عن أي منتج أو براند أو خدمة داخل المول",
                                style = MaterialTheme.typography.bodySmall,
                                color = ElAbedTextSecondary
                            )
                        }

                        // Compact Floor Selector Pill in Header
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F4FA),
                            border = BorderStroke(1.dp, ElAbedOutline.copy(alpha = 0.7f))
                        ) {
                            Row(
                                modifier = Modifier.padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                listOf(-1 to "الكل", 0 to "أرضي", 1 to "أول").forEach { (floorVal, label) ->
                                    val selected = selectedFloorFilter == floorVal
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9.dp))
                                            .background(if (selected) ElAbedRoyalBlue else Color.Transparent)
                                            .clickable { selectedFloorFilter = floorVal }
                                            .padding(horizontal = 9.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) Color.White else ElAbedTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Wide Spacious Search Input with Voice Search Button
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFF3F6FC),
                        border = BorderStroke(
                            1.2.dp,
                            if (searchQuery.isNotBlank()) ElAbedRoyalBlue else Color(0xFFC3CBDE)
                        ),
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 14.dp, end = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = ElAbedRoyalBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = if (activeTab == DirectoryBrowseTab.PRODUCTS) {
                                            "ابحث عن منتج، لحوم، أجهزة، عطور..."
                                        } else {
                                            "ابحث باسم البراند، المطعم، البنك..."
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ElAbedTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = ElAbedTextPrimary,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(ElAbedRoyalBlue),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("directory_search_input")
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "مسح البحث",
                                        tint = ElAbedTextSecondary
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(13.dp),
                                color = ElAbedRoyalBlueContainer
                            ) {
                                IconButton(
                                    onClick = { launchVoiceSearch() },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .testTag("directory_voice_search_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Mic,
                                        contentDescription = "البحث بالصوت",
                                        tint = ElAbedRoyalBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Directly Beneath Search Bar: Crisp 2-Way Mode Toggle (المنتجات والخدمات | البراندات والأماكن)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF0F3FA),
                        border = BorderStroke(1.dp, Color(0xFFC3CBDE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DirectoryBrowseTab.entries.forEach { tab ->
                                val selected = activeTab == tab
                                Surface(
                                    onClick = { activeTab = tab },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) ElAbedRoyalBlue else Color.Transparent,
                                    shadowElevation = 0.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                          .height(40.dp)
                                        .testTag("directory_search_tab_${tab.name}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = null,
                                            tint = if (selected) Color.White else ElAbedTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = tab.labelAr,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (selected) Color.White else ElAbedTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Clean Category Filter Strip (No Hashtags)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = {
                                Text(
                                    text = "الكل",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selectedCategory == null) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElAbedRoyalBlue,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFEEF1F8)
                            )
                        )
                    }
                    items(MallPlaceCategory.entries) { cat ->
                        val isCatSelected = selectedCategory == cat
                        FilterChip(
                            selected = isCatSelected,
                            onClick = {
                                selectedCategory = if (isCatSelected) null else cat
                            },
                            label = {
                                Text(
                                    text = cat.labelAr,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElAbedRoyalBlue,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFEEF1F8)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Body Content Depending on Active Toggle
                if (activeTab == DirectoryBrowseTab.PRODUCTS) {
                    val productRows = remember(filteredProducts) { filteredProducts.chunked(2) }
                    if (productRows.isEmpty() && filteredPlaces.isEmpty()) {
                        EmptySearchPlaceholder(
                            onClear = {
                                searchQuery = ""
                                selectedCategory = null
                                selectedFloorFilter = -1
                            }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 92.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (productRows.isNotEmpty()) {
                                item {
                                    SectionHeader(
                                        title = if (searchQuery.isBlank()) "منتجات وخدمات مقترحة" else "نتائج المنتجات والخدمات",
                                        subtitle = "اضغط على أي منتج لمعرفة مكانه أو تفاصيله"
                                    )
                                }
                                items(productRows) { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowItems.forEach { product ->
                                            ProductSuggestionPillCard(
                                                product = product,
                                                onOpenStore = { onOpenStoreDetail(product.storeId) },
                                                onNavigateMap = { onNavigateOnMap(product.storeId) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowItems.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }

                            if (searchQuery.isNotBlank() && filteredPlaces.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    SectionHeader(
                                        title = "أماكن توفر طلبك (${filteredPlaces.size})",
                                        subtitle = "متاجر ومرافق مطابقة لكلمة البحث"
                                    )
                                }
                                items(filteredPlaces, key = { "match_${it.id}" }) { place ->
                                    BrandPlaceDirectoryCard(
                                        place = place,
                                        storeOffers = offersByStore[place.id].orEmpty(),
                                        onOpenStoreDetail = { onOpenStoreDetail(place.id) },
                                        onNavigateOnMap = { onNavigateOnMap(place.id) },
                                        onAddToTrip = {
                                            onAddStoreToTrip(place)
                                            scope.launch {
                                                snackbarHostState.showSnackbar("تمت إضافة ${place.nameAr} لقائمة المشوار")
                                            }
                                        },
                                        onShareStore = { onShareStore(place) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    if (filteredPlaces.isEmpty()) {
                        EmptySearchPlaceholder(
                            onClear = {
                                searchQuery = ""
                                selectedCategory = null
                                selectedFloorFilter = -1
                            }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 92.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                SectionHeader(
                                    title = "البراندات والأماكن (${filteredPlaces.size})",
                                    subtitle = "المحلات، المطاعم، البنوك والمرافق بشعاراتها"
                                )
                            }
                            items(filteredPlaces, key = { it.id }) { place ->
                                BrandPlaceDirectoryCard(
                                    place = place,
                                    storeOffers = offersByStore[place.id].orEmpty(),
                                    onOpenStoreDetail = { onOpenStoreDetail(place.id) },
                                    onNavigateOnMap = { onNavigateOnMap(place.id) },
                                    onAddToTrip = {
                                        onAddStoreToTrip(place)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("تمت إضافة ${place.nameAr} لقائمة المشوار")
                                        }
                                    },
                                    onShareStore = { onShareStore(place) }
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
private fun EmptySearchPlaceholder(
    onClear: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(ElAbedRoyalBlueContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.StoreMallDirectory,
                    contentDescription = null,
                    tint = ElAbedRoyalBlue,
                    modifier = Modifier.size(34.dp)
                )
            }
            Text(
                text = "لا توجد نتائج مطابقة للبحث",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "جرّب البحث بكلمة أخرى أو أعد ضبط الفلاتر",
                style = MaterialTheme.typography.bodySmall,
                color = ElAbedTextSecondary
            )
            Surface(
                onClick = onClear,
                shape = RoundedCornerShape(50),
                color = ElAbedRoyalBlue
            ) {
                Text(
                    text = "عرض الكل",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    }
}
