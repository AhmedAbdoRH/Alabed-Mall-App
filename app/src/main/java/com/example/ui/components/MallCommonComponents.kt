package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Attractions
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.MallPlaceCategory
import com.example.data.model.PromoBannerConfig
import com.example.data.model.StoreEntity
import com.example.ui.theme.ElAbedOutline
import com.example.ui.theme.ElAbedRoyalBlue
import com.example.ui.theme.ElAbedRoyalBlueContainer
import com.example.ui.theme.ElAbedTurquoise
import com.example.ui.theme.ElAbedTurquoiseContainer
import com.example.ui.theme.ElAbedVibrantGreen
import com.example.ui.theme.ElAbedVibrantGreenContainer
import com.example.ui.theme.ElAbedWarmOrange
import com.example.ui.theme.ElAbedWarmOrangeContainer
import com.example.ui.theme.StatusOpenContainer
import com.example.ui.theme.StatusOpenGreen
import java.io.File

fun categoryIcon(category: MallPlaceCategory): ImageVector {
    return when (category) {
        MallPlaceCategory.HYPERMARKET -> Icons.Filled.LocalGroceryStore
        MallPlaceCategory.ELECTRONICS -> Icons.Filled.Devices
        MallPlaceCategory.FASHION -> Icons.Filled.Checkroom
        MallPlaceCategory.FURNITURE -> Icons.Filled.Chair
        MallPlaceCategory.DINING -> Icons.Filled.Restaurant
        MallPlaceCategory.ENTERTAINMENT -> Icons.Filled.Attractions
        MallPlaceCategory.SERVICES -> Icons.Filled.SupportAgent
        MallPlaceCategory.FACILITY -> Icons.AutoMirrored.Filled.DirectionsWalk
    }
}

@Composable
fun CategoryBadgeIcon(
    category: MallPlaceCategory,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = categoryIcon(category),
            contentDescription = category.labelAr,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun StoreLogoBadge(
    store: StoreEntity,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp
) {
    val usesMallLogo = store.id in setOf(
        "hypermarket", "elabed_cafe", "customer_service", "gate_1"
    )
    if (usesMallLogo) {
        Surface(
            modifier = modifier.size(size),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = BorderStroke(1.2.dp, ElAbedRoyalBlue.copy(alpha = 0.25f)),
            shadowElevation = 1.dp
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_elabed_mall),
                contentDescription = "شعار ${store.nameAr}",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            )
        }
    } else {
        val (bgColor, fgColor, brandMonogram) = when (store.id) {
            "kfc_toukh" -> Triple(Color(0xFFC81E1E), Color.White, "KFC")
            "pizza_primo" -> Triple(Color(0xFFEA580C), Color.White, "PRIMO")
            "hadithah_cafe" -> Triple(Color(0xFF78350F), Color.White, "قهوة")
            "cafe_terrace" -> Triple(Color(0xFF0F766E), Color.White, "تراس")
            "samsung" -> Triple(Color(0xFF1428A0), Color.White, "SAM")
            "tecno_store" -> Triple(Color(0xFF0284C7), Color.White, "TECNO")
            "united_electronics" -> Triple(Color(0xFF1E40AF), Color.White, "UE")
            "vodafone" -> Triple(Color(0xFFE60000), Color.White, "VF")
            "banque_misr" -> Triple(Color(0xFF881337), Color.White, "BM")
            "alex_bank" -> Triple(Color(0xFF0369A1), Color.White, "ALEX")
            "abk_atm" -> Triple(Color(0xFF1E3A8A), Color.White, "ABK")
            "aaib_atm" -> Triple(Color(0xFF0E7490), Color.White, "AAIB")
            "faisal_bank" -> Triple(Color(0xFF15803D), Color.White, "FIB")
            "banque_du_caire" -> Triple(Color(0xFFD97706), Color.White, "BDC")
            "pingo_toys" -> Triple(Color(0xFF16A34A), Color.White, "PINGO")
            "tutu_kids" -> Triple(Color(0xFF7C3AED), Color.White, "TUTU")
            "kids_fashion" -> Triple(Color(0xFFDB2777), Color.White, "KIDS")
            "perfumes_oud" -> Triple(Color(0xFFB45309), Color.White, "OUD")
            "furniture_home" -> Triple(Color(0xFF0D9488), Color.White, "HOME")
            else -> when (store.category) {
                MallPlaceCategory.ELECTRONICS -> Triple(ElAbedRoyalBlue, Color.White, null)
                MallPlaceCategory.DINING -> Triple(ElAbedWarmOrange, Color.White, null)
                MallPlaceCategory.ENTERTAINMENT -> Triple(ElAbedVibrantGreen, Color.White, null)
                MallPlaceCategory.FASHION -> Triple(Color(0xFFC026D3), Color.White, null)
                MallPlaceCategory.FURNITURE -> Triple(Color(0xFF0F766E), Color.White, null)
                MallPlaceCategory.SERVICES -> Triple(ElAbedTurquoise, Color.White, null)
                else -> Triple(Color(0xFF475569), Color.White, null)
            }
        }

        Surface(
            modifier = modifier.size(size),
            shape = RoundedCornerShape(14.dp),
            color = bgColor,
            shadowElevation = 1.dp
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (brandMonogram != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon(store.category),
                            contentDescription = store.nameAr,
                            tint = fgColor.copy(alpha = 0.92f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = brandMonogram,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = fgColor
                        )
                    }
                } else {
                    Icon(
                        imageVector = categoryIcon(store.category),
                        contentDescription = store.nameAr,
                        tint = fgColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PromoBannerImage(
    imagePathOrKey: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val drawableRes = when (imagePathOrKey) {
        PromoBannerConfig.DEFAULT_DRAWABLE_HYPERMARKET -> R.drawable.img_hypermarket_banner
        PromoBannerConfig.DEFAULT_DRAWABLE_EVENTS -> R.drawable.img_events_banner
        PromoBannerConfig.DEFAULT_DRAWABLE_HERO -> R.drawable.img_mall_hero
        else -> null
    }
    if (drawableRes != null) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        val file = File(imagePathOrKey)
        if (file.exists()) {
            AsyncImage(
                model = file,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_hypermarket_banner),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
    }
}

@Composable
fun FloorPill(
    floor: Int,
    modifier: Modifier = Modifier
) {
    val label = if (floor == 0) "الدور الأرضي" else "الدور الأول"
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (floor == 0) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.tertiaryContainer
        }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (floor == 0) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onTertiaryContainer
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun OpenStatusChip(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = StatusOpenContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(StatusOpenGreen)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "مفتوح اليوم • ٩ ص - ١ ص",
                style = MaterialTheme.typography.labelSmall,
                color = StatusOpenGreen,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionTestTag: String? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (subtitle != null) 34.dp else 22.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(ElAbedRoyalBlue, ElAbedWarmOrange)
                        )
                    )
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (actionLabel != null && onActionClick != null) {
            val tagModifier = if (actionTestTag != null) {
                Modifier
                    .minimumInteractiveComponentSize()
                    .testTag(actionTestTag)
            } else {
                Modifier.minimumInteractiveComponentSize()
            }
            Surface(
                onClick = onActionClick,
                shape = RoundedCornerShape(50),
                color = ElAbedRoyalBlueContainer.copy(alpha = 0.65f),
                modifier = tagModifier
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElAbedRoyalBlue
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = ElAbedRoyalBlue,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
