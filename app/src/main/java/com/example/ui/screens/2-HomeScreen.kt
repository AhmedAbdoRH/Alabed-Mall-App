package com.example.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MallPlaceCategory
import com.example.ui.components.OpenStatusChip
import com.example.ui.components.SectionHeader
import com.example.ui.theme.ElAbedRoyalBlue
import com.example.ui.theme.ElAbedTextPrimary
import com.example.ui.theme.ElAbedWarmOrange

private data class HomeCategoryTileSpec(
    val id: String,
    val titleAr: String,
    @DrawableRes val imageRes: Int,
    val surfaceColor: Color,
    val borderColor: Color,
    val category: MallPlaceCategory? = null
)

@Composable
fun HomeScreen(
    unreadNotifications: Int,
    onOpenDirectory: () -> Unit,
    onSearchWithQuery: (String?, String?) -> Unit,
    onOpenCategory: (MallPlaceCategory) -> Unit,
    onOpenIndoorMap: (String?) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenMoreHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
            putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث باسم المحل أو المنتج...")
        }
        try {
            voiceSearchLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "البحث الصوتي غير مدعوم على هذا الجهاز، يمكنك البحث بالكتابة.",
                Toast.LENGTH_SHORT
            ).show()
            onOpenDirectory()
        }
    }

    val categoryTiles = listOf(
        HomeCategoryTileSpec(
            id = "market",
            titleAr = "الماركت",
            imageRes = R.drawable.img_cat_market,
            surfaceColor = Color(0xFFEAF7EF),
            borderColor = Color(0xFFC2E8D1),
            category = MallPlaceCategory.HYPERMARKET
        ),
        HomeCategoryTileSpec(
            id = "electronics",
            titleAr = "أجهزة كهربائية",
            imageRes = R.drawable.img_cat_electronics,
            surfaceColor = Color(0xFFEAF2FE),
            borderColor = Color(0xFFC7DAFA),
            category = MallPlaceCategory.ELECTRONICS
        ),
        HomeCategoryTileSpec(
            id = "fashion",
            titleAr = "ملابس",
            imageRes = R.drawable.img_cat_fashion,
            surfaceColor = Color(0xFFFFF1E5),
            borderColor = Color(0xFFF6D3B8),
            category = MallPlaceCategory.FASHION
        ),
        HomeCategoryTileSpec(
            id = "dining",
            titleAr = "مطاعم وكافيهات",
            imageRes = R.drawable.img_cat_dining,
            surfaceColor = Color(0xFFFFF6E8),
            borderColor = Color(0xFFF3DBB4),
            category = MallPlaceCategory.DINING
        ),
        HomeCategoryTileSpec(
            id = "finance",
            titleAr = "خدمات مالية",
            imageRes = R.drawable.img_cat_finance,
            surfaceColor = Color(0xFFE5F6F4),
            borderColor = Color(0xFFB8E4DF),
            category = MallPlaceCategory.SERVICES
        ),
        HomeCategoryTileSpec(
            id = "other_services",
            titleAr = "خدمات أخرى",
            imageRes = R.drawable.img_cat_services,
            surfaceColor = Color(0xFFEEEFFA),
            borderColor = Color(0xFFCCD0F2),
            category = null
        )
    )

    val topRowTiles = categoryTiles.take(3)
    val bottomRowTiles = categoryTiles.drop(3)

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
            contentPadding = PaddingValues(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Compact Top Mall Facade Hero Banner with Search Bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(198.dp)
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
                                        Color(0x4D14255C),
                                        Color(0x2814255C),
                                        Color(0xE614255C)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                            .size(34.dp)
                                            .padding(3.dp)
                                    )
                                }
                                OpenStatusChip()
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
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

                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xEBFFFFFF),
                                    shadowElevation = 2.dp
                                ) {
                                    IconButton(
                                        onClick = onOpenMoreHub,
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("home_settings_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Settings,
                                            contentDescription = "الإعدادات",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "العابد مول • طوخ",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "طريق الإسكندرية الزراعي • كل اللي نفسك وأكثر",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFEFF4FF),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
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
                                        .padding(start = 14.dp, end = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "بحث",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "ابحث عن محل أو منتج...",
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

            // 2. Interactive 2.5D Mall Map Banner
            item {
                InteractiveMapHeroBanner(
                    onOpenMap = { onOpenIndoorMap(null) },
                    onExploreKidsOrFirstFloor = { onOpenIndoorMap("kids_rides") },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 3. Clean 6-Card Mall Sections Grid (3 Top + 3 Bottom)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionHeader(
                        title = "أقسام وخدمات المول",
                        subtitle = "اختر القسم اللي محتاجه بكل بساطة"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        topRowTiles.forEach { tile ->
                            HomeCategoryShowcaseCard(
                                spec = tile,
                                onClick = {
                                    val cat = tile.category
                                    if (cat != null) {
                                        onOpenCategory(cat)
                                    } else {
                                        onOpenMoreHub()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        bottomRowTiles.forEach { tile ->
                            HomeCategoryShowcaseCard(
                                spec = tile,
                                onClick = {
                                    val cat = tile.category
                                    if (cat != null) {
                                        onOpenCategory(cat)
                                    } else {
                                        onOpenMoreHub()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeCategoryShowcaseCard(
    spec: HomeCategoryTileSpec,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag("home_category_card_${spec.id}"),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, spec.borderColor),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.06f)
                    .background(spec.surfaceColor)
            ) {
                Image(
                    painter = painterResource(id = spec.imageRes),
                    contentDescription = spec.titleAr,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.BottomCenter,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
                    .background(Color.White)
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = spec.titleAr,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 13.sp,
                        lineHeight = 17.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = ElAbedTextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
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
            .height(148.dp)
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
                        text = "اعرف مكان أي محل أو خدمة ووصل له خطوة بخطوة.",
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
