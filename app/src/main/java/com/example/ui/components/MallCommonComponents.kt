package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.MallPlaceCategory
import com.example.ui.theme.ElAbedRoyalBlue
import com.example.ui.theme.ElAbedRoyalBlueContainer
import com.example.ui.theme.ElAbedWarmOrange
import com.example.ui.theme.StatusOpenContainer
import com.example.ui.theme.StatusOpenGreen

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
