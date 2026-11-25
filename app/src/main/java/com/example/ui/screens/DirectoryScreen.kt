package com.example.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.StoreMallDirectory
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.ui.components.CompactBrandPlaceCard
import com.example.ui.components.ProductSuggestionPillCard
import com.example.ui.theme.ElAbedRoyalBlue
import com.example.ui.theme.ElAbedRoyalBlueContainer
import com.example.ui.theme.ElAbedTextPrimary
import com.example.ui.theme.ElAbedTextSecondary
import com.example.ui.theme.ElAbedVibrantGreen
import com.example.ui.theme.ElAbedVibrantGreenContainer
import com.example.ui.theme.OnElAbedRoyalBlueContainer
import com.example.ui.theme.OnElAbedVibrantGreenContainer
import kotlinx.coroutines.launch

private enum class VoiceSearchTarget {
    PLACES,
    PRODUCTS
}

private data class DirectorySuggestionChip(
    val labelAr: String,
    val category: MallPlaceCategory?
)

private val PlaceSectionSoftBlue = Color(0xFFF3F6FF)
private val PlaceSectionBorderBlue = Color(0xFFBFD0F5)
private val PlaceItemOutlineBlue = Color(0xFFC5D2EE)

private val ProductSectionSoftGreen = Color(0xFFF1FBF5)
private val ProductSectionBorderGreen = Color(0xFFB3E3C7)
private val ProductItemOutlineGreen = Color(0xFFB3DEC5)

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
    val initialCategory = remember(initialCategoryName) {
        MallPlaceCategory.entries.firstOrNull { it.name == initialCategoryName }
    }

    var placesQuery by remember(initialQuery, initialSearchTabName) {
        mutableStateOf(
            if (initialSearchTabName == SearchPathTab.PRODUCTS.name) "" else initialQuery.orEmpty()
        )
    }
    var selectedPlaceCategory by remember(initialCategory) {
        mutableStateOf(initialCategory)
    }

    var productsQuery by remember(initialQuery, initialSearchTabName) {
        mutableStateOf(
            if (initialSearchTabName == SearchPathTab.BRANDS.name) "" else initialQuery.orEmpty()
        )
    }
    var selectedProductCategory by remember(initialCategory) {
        mutableStateOf(initialCategory)
    }

    var voiceTarget by remember { mutableStateOf(VoiceSearchTarget.PLACES) }
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
                when (voiceTarget) {
                    VoiceSearchTarget.PLACES -> placesQuery = spoken
                    VoiceSearchTarget.PRODUCTS -> productsQuery = spoken
                }
            }
        }
    }

    fun launchVoiceSearch(target: VoiceSearchTarget) {
        voiceTarget = target
        val promptText = if (target == VoiceSearchTarget.PLACES) {
            "تحدث باسم البراند أو المحل أو المرفق..."
        } else {
            "تحدث باسم المنتج أو الخدمة المطلوبة..."
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-EG")
            putExtra(RecognizerIntent.EXTRA_PROMPT, promptText)
        }
        try {
            voiceLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            scope.launch {
                snackbarHostState.showSnackbar("البحث الصوتي غير متاح على هذا الجهاز، يمكنك الكتابة في خانة البحث.")
            }
        }
    }

    val placeChips = remember {
        listOf(DirectorySuggestionChip(labelAr = "الكل", category = null)) +
            MallPlaceCategory.entries.map { DirectorySuggestionChip(labelAr = it.labelAr, category = it) }
    }

    val productChips = remember {
        listOf(
            DirectorySuggestionChip("الكل", null),
            DirectorySuggestionChip("بقالة ولحوم", MallPlaceCategory.HYPERMARKET),
            DirectorySuggestionChip("أجهزة وإلكترونيات", MallPlaceCategory.ELECTRONICS),
            DirectorySuggestionChip("ملابس وعطور", MallPlaceCategory.FASHION),
            DirectorySuggestionChip("أثاث ومفروشات", MallPlaceCategory.FURNITURE),
            DirectorySuggestionChip("وجبات ومشروبات", MallPlaceCategory.DINING),
            DirectorySuggestionChip("ألعاب أطفال", MallPlaceCategory.ENTERTAINMENT),
            DirectorySuggestionChip("بنوك وخدمات", MallPlaceCategory.SERVICES),
            DirectorySuggestionChip("مواقف وبوابات", MallPlaceCategory.FACILITY)
        )
    }

    val filteredPlaces = remember(places, offers, placesQuery, selectedPlaceCategory) {
        MallSearchEngine.filterPlaces(
            places = places,
            offers = offers,
            rawQuery = placesQuery,
            searchTab = SearchPathTab.ALL,
            selectedCategory = selectedPlaceCategory,
            selectedFloor = -1
        ).sortedWith(
            compareBy<StoreEntity> { it.isServiceFacility }
                .thenByDescending { it.isFeatured }
                .thenBy { it.floor }
        )
    }

    val filteredProducts = remember(productsQuery, selectedProductCategory) {
        MallProductCatalog.filterProducts(
            rawQuery = productsQuery,
            selectedCategory = selectedProductCategory,
            selectedFloor = -1
        )
    }

    val placeRows = remember(filteredPlaces) { filteredPlaces.chunked(2) }
    val productRows = remember(filteredProducts) { filteredProducts.chunked(2) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "دليل العابد مول",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ElAbedTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Top Half Section: Brands & Places (Soft Blue Card)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("directory_places_section"),
                    shape = RoundedCornerShape(16.dp),
                    color = PlaceSectionSoftBlue,
                    border = BorderStroke(1.dp, PlaceSectionBorderBlue),
                    shadowElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DirectorySectionHeader(
                            title = "البراندات والأماكن",
                            countLabel = "${filteredPlaces.size} مكان",
                            icon = Icons.Filled.Storefront,
                            accentColor = ElAbedRoyalBlue,
                            badgeContainerColor = ElAbedRoyalBlueContainer,
                            badgeTextColor = OnElAbedRoyalBlueContainer
                        )

                        DirectorySectionSearchField(
                            query = placesQuery,
                            onQueryChange = { placesQuery = it },
                            placeholder = "ابحث عن براند أو محل أو مرفق...",
                            accentColor = ElAbedRoyalBlue,
                            accentContainerColor = ElAbedRoyalBlueContainer,
                            idleBorderColor = PlaceItemOutlineBlue,
                            inputTestTag = "directory_places_search_input",
                            voiceTestTag = "directory_places_voice_btn",
                            onVoiceClick = { launchVoiceSearch(VoiceSearchTarget.PLACES) }
                        )

                        DirectorySuggestionChipRow(
                            chips = placeChips,
                            selectedCategory = selectedPlaceCategory,
                            onSelectCategory = { cat ->
                                selectedPlaceCategory = if (cat != null && selectedPlaceCategory == cat) null else cat
                            },
                            selectedContainerColor = ElAbedRoyalBlue,
                            idleBorderColor = PlaceItemOutlineBlue,
                            rowTestTag = "directory_places_chips"
                        )

                        if (placeRows.isEmpty()) {
                            DirectorySectionEmptyState(
                                message = "لا توجد أماكن مطابقة لبحثك",
                                accentColor = ElAbedRoyalBlue,
                                accentContainerColor = ElAbedRoyalBlueContainer,
                                onReset = {
                                    placesQuery = ""
                                    selectedPlaceCategory = null
                                }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .testTag("directory_places_list"),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = placeRows,
                                    key = { row -> row.joinToString("_") { it.id } }
                                ) { rowPlaces ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowPlaces.forEach { place ->
                                            CompactBrandPlaceCard(
                                                place = place,
                                                onOpenStoreDetail = { onOpenStoreDetail(place.id) },
                                                onNavigateOnMap = { onNavigateOnMap(place.id) },
                                                borderColor = PlaceItemOutlineBlue,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowPlaces.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Half Section: Products & Services (Soft Green Card)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("directory_products_section"),
                    shape = RoundedCornerShape(16.dp),
                    color = ProductSectionSoftGreen,
                    border = BorderStroke(1.dp, ProductSectionBorderGreen),
                    shadowElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DirectorySectionHeader(
                            title = "المنتجات والخدمات",
                            countLabel = "${filteredProducts.size} منتج",
                            icon = Icons.Filled.ShoppingBag,
                            accentColor = ElAbedVibrantGreen,
                            badgeContainerColor = ElAbedVibrantGreenContainer,
                            badgeTextColor = OnElAbedVibrantGreenContainer
                        )

                        DirectorySectionSearchField(
                            query = productsQuery,
                            onQueryChange = { productsQuery = it },
                            placeholder = "ابحث عن منتج، لحوم، أجهزة، خدمات...",
                            accentColor = ElAbedVibrantGreen,
                            accentContainerColor = ElAbedVibrantGreenContainer,
                            idleBorderColor = ProductItemOutlineGreen,
                            inputTestTag = "directory_products_search_input",
                            voiceTestTag = "directory_products_voice_btn",
                            onVoiceClick = { launchVoiceSearch(VoiceSearchTarget.PRODUCTS) }
                        )

                        DirectorySuggestionChipRow(
                            chips = productChips,
                            selectedCategory = selectedProductCategory,
                            onSelectCategory = { cat ->
                                selectedProductCategory = if (cat != null && selectedProductCategory == cat) null else cat
                            },
                            selectedContainerColor = ElAbedVibrantGreen,
                            idleBorderColor = ProductItemOutlineGreen,
                            rowTestTag = "directory_products_chips"
                        )

                        if (productRows.isEmpty()) {
                            DirectorySectionEmptyState(
                                message = "لا توجد منتجات أو خدمات مطابقة",
                                accentColor = ElAbedVibrantGreen,
                                accentContainerColor = ElAbedVibrantGreenContainer,
                                onReset = {
                                    productsQuery = ""
                                    selectedProductCategory = null
                                }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .testTag("directory_products_grid"),
                                contentPadding = PaddingValues(bottom = 56.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = productRows,
                                    key = { row -> row.joinToString("_") { it.id } }
                                ) { rowProducts ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowProducts.forEach { product ->
                                            ProductSuggestionPillCard(
                                                product = product,
                                                onOpenStore = { onOpenStoreDetail(product.storeId) },
                                                onNavigateMap = { onNavigateOnMap(product.storeId) },
                                                borderColor = ProductItemOutlineGreen,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowProducts.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
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

@Composable
private fun DirectorySectionHeader(
    title: String,
    countLabel: String,
    icon: ImageVector,
    accentColor: Color,
    badgeContainerColor: Color,
    badgeTextColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = badgeContainerColor,
                modifier = Modifier.size(30.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ElAbedTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Surface(
            shape = RoundedCornerShape(50),
            color = badgeContainerColor
        ) {
            Text(
                text = countLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = badgeTextColor,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun DirectorySectionSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    accentColor: Color,
    accentContainerColor: Color,
    idleBorderColor: Color,
    inputTestTag: String,
    voiceTestTag: String,
    onVoiceClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(
            width = if (query.isNotBlank()) 1.4.dp else 1.dp,
            color = if (query.isNotBlank()) accentColor else idleBorderColor
        ),
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodySmall,
                        color = ElAbedTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = ElAbedTextPrimary,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(accentColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(inputTestTag)
                )
            }
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "مسح البحث",
                        tint = ElAbedTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(9.dp),
                color = accentContainerColor
            ) {
                IconButton(
                    onClick = onVoiceClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag(voiceTestTag)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "البحث بالصوت",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DirectorySuggestionChipRow(
    chips: List<DirectorySuggestionChip>,
    selectedCategory: MallPlaceCategory?,
    onSelectCategory: (MallPlaceCategory?) -> Unit,
    selectedContainerColor: Color,
    idleBorderColor: Color,
    rowTestTag: String
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(rowTestTag),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(chips, key = { it.labelAr }) { chip ->
            val selected = selectedCategory == chip.category
            Surface(
                onClick = { onSelectCategory(chip.category) },
                shape = RoundedCornerShape(10.dp),
                color = if (selected) selectedContainerColor else Color.White,
                border = if (selected) null else BorderStroke(1.dp, idleBorderColor),
                modifier = Modifier.heightIn(min = 32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = chip.labelAr,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Color.White else ElAbedTextPrimary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun DirectorySectionEmptyState(
    message: String,
    accentColor: Color,
    accentContainerColor: Color,
    onReset: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accentContainerColor,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.StoreMallDirectory,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = ElAbedTextPrimary
            )
            Surface(
                onClick = onReset,
                shape = RoundedCornerShape(50),
                color = accentColor
            ) {
                Text(
                    text = "عرض الكل",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
