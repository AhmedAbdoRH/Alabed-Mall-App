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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StoreMallDirectory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.MallPlaceCategory
import com.example.data.model.OfferEntity
import com.example.data.model.StoreEntity
import com.example.data.search.MallSearchEngine
import com.example.data.search.SearchPathTab
import com.example.ui.components.FloorPill
import com.example.ui.components.StoreLogoBadge
import com.example.ui.theme.ElAbedOutline
import com.example.ui.theme.ElAbedRoyalBlue
import com.example.ui.theme.ElAbedRoyalBlueContainer
import com.example.ui.theme.ElAbedSurfaceVariant
import kotlinx.coroutines.launch

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
    var selectedSearchTab by remember(initialSearchTabName) {
        mutableStateOf(
            SearchPathTab.entries.firstOrNull { it.name == initialSearchTabName }
                ?: SearchPathTab.ALL
        )
    }
    var selectedCategory by remember(initialCategoryName) {
        mutableStateOf(
            MallPlaceCategory.entries.firstOrNull { it.name == initialCategoryName }
        )
    }
    var selectedFloorFilter by remember { mutableIntStateOf(-1) }
    var showSuggestions by remember(initialQuery) { mutableStateOf(!initialQuery.isNullOrBlank()) }

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
                showSuggestions = true
            }
        }
    }

    fun launchVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-EG")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث باسم المحل أو المنتج...")
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

    val suggestions = remember(places, offers, searchQuery, selectedSearchTab) {
        MallSearchEngine.buildSuggestions(
            places = places,
            offers = offers,
            rawQuery = searchQuery,
            searchTab = selectedSearchTab,
            limit = 5
        )
    }

    val filteredPlaces = remember(places, offers, searchQuery, selectedSearchTab, selectedCategory, selectedFloorFilter) {
        MallSearchEngine.filterPlaces(
            places = places,
            offers = offers,
            rawQuery = searchQuery,
            searchTab = selectedSearchTab,
            selectedCategory = selectedCategory,
            selectedFloor = selectedFloorFilter
        )
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "دليل المحلات والبحث المتقدم",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Search Path Tabs: الكل / البراندات والمحلات / المنتجات والأقسام
                    TabRow(
                        selectedTabIndex = SearchPathTab.entries.indexOf(selectedSearchTab),
                        containerColor = Color.Transparent
                    ) {
                        SearchPathTab.entries.forEach { tab ->
                            val isSelected = selectedSearchTab == tab
                            Tab(
                                selected = isSelected,
                                onClick = { selectedSearchTab = tab },
                                text = {
                                    Text(
                                        text = tab.labelAr,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag("directory_search_tab_${tab.name}")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            showSuggestions = it.isNotBlank()
                        },
                        placeholder = {
                            Text(
                                text = when (selectedSearchTab) {
                                    SearchPathTab.BRANDS -> "ابحث باسم البراند أو المحل..."
                                    SearchPathTab.PRODUCTS -> "ابحث عن منتج، لحوم، هواتف، عطور..."
                                    SearchPathTab.ALL -> "ابحث بالاسم، الهاشتاج، أو بالصوت..."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = ElAbedRoyalBlue) },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { launchVoiceSearch() },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("directory_voice_search_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Mic,
                                        contentDescription = "البحث بالصوت",
                                        tint = ElAbedRoyalBlue
                                    )
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            searchQuery = ""
                                            showSuggestions = false
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(Icons.Filled.Clear, contentDescription = "مسح البحث")
                                    }
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("directory_search_input")
                    )

                    // Search Hashtags
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(MallSearchEngine.popularHashtags, key = { it.tag }) { hashtag ->
                            val isSelected = searchQuery.equals(hashtag.tag, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        searchQuery = ""
                                        showSuggestions = false
                                    } else {
                                        searchQuery = hashtag.tag
                                        selectedSearchTab = hashtag.targetTab
                                        showSuggestions = true
                                    }
                                },
                                label = {
                                    Text(
                                        text = hashtag.tag,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElAbedRoyalBlue,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("directory_hashtag_${hashtag.tag}")
                            )
                        }
                    }

                    // Instant Brand & Logo Suggestions Panel
                    if (showSuggestions && searchQuery.isNotBlank() && suggestions.isNotEmpty()) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, ElAbedRoyalBlue.copy(alpha = 0.25f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "اقتراحات سريعة (${suggestions.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ElAbedRoyalBlue
                                    )
                                    Text(
                                        text = "إخفاء",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .clickable { showSuggestions = false }
                                            .padding(4.dp)
                                    )
                                }
                                suggestions.forEachIndexed { idx, suggestion ->
                                    if (idx > 0) HorizontalDivider(color = ElAbedOutline.copy(alpha = 0.5f))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                showSuggestions = false
                                                onOpenStoreDetail(suggestion.store.id)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        StoreLogoBadge(store = suggestion.store, size = 38.dp)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = suggestion.store.nameAr,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = suggestion.matchReasonAr,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = ElAbedRoyalBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Floor filter
                    val floorOptions = listOf(-1 to "كل الأدوار", 0 to "الدور الأرضي", 1 to "الدور الأول")
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        floorOptions.forEachIndexed { idx, (floorVal, label) ->
                            SegmentedButton(
                                selected = selectedFloorFilter == floorVal,
                                onClick = { selectedFloorFilter = floorVal },
                                shape = SegmentedButtonDefaults.itemShape(index = idx, count = floorOptions.size),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = ElAbedRoyalBlueContainer,
                                    activeContentColor = ElAbedRoyalBlue,
                                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                    inactiveContentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selectedFloorFilter == floorVal) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Category chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("الكل", style = MaterialTheme.typography.labelLarge) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElAbedRoyalBlueContainer,
                                selectedLabelColor = ElAbedRoyalBlue
                            )
                        )
                    }
                    items(MallPlaceCategory.entries) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = {
                                selectedCategory = if (selectedCategory == cat) null else cat
                            },
                            label = { Text(cat.labelAr, style = MaterialTheme.typography.labelLarge) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElAbedRoyalBlueContainer,
                                selectedLabelColor = ElAbedRoyalBlue
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredPlaces.isEmpty()) {
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
                            Icon(
                                imageVector = Icons.Filled.StoreMallDirectory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = "لا توجد نتائج مطابقة للبحث",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "جرّب البحث باسم آخر أو إزالة فلتر الفئة والدور",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredPlaces, key = { it.id }) { place ->
                            val storeOffers = offersByStore[place.id].orEmpty()
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenStoreDetail(place.id) }
                                    .testTag("directory_item_${place.id}"),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = ElAbedSurfaceVariant.copy(alpha = 0.45f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        StoreLogoBadge(store = place)
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = place.nameAr,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                if (place.isNewOpening) {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = MaterialTheme.colorScheme.secondary
                                                    ) {
                                                        Text(
                                                            text = "جديد",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSecondary,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "${place.category.labelAr} • ${place.zoneAr}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        FloorPill(floor = place.floor)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (storeOffers.isNotEmpty()) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.tertiaryContainer
                                            ) {
                                                Text(
                                                    text = "${storeOffers.first().discountBadge} متاح الآن",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = "مواعيد: ${place.hoursAr}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    onAddStoreToTrip(place)
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("تمت إضافة ${place.nameAr} لقائمة المشوار")
                                                    }
                                                },
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .testTag("directory_trip_btn_${place.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.BookmarkAdd,
                                                    contentDescription = "أضف لقائمة المشوار",
                                                    tint = ElAbedRoyalBlue
                                                )
                                            }
                                            IconButton(
                                                onClick = { onShareStore(place) },
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .testTag("directory_share_btn_${place.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Share,
                                                    contentDescription = "مشاركة المحل",
                                                    tint = ElAbedRoyalBlue
                                                )
                                            }
                                            OutlinedButton(
                                                onClick = { onNavigateOnMap(place.id) },
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                                modifier = Modifier
                                                    .height(48.dp)
                                                    .testTag("directory_map_btn_${place.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.LocationOn,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("خريطة", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
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
    }
}
