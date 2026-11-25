package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.map.MallFloorPlanGraph
import com.example.data.model.AvatarState
import com.example.data.model.OfferEntity
import com.example.data.model.StoreEntity
import com.example.ui.theme.StatusOpenGreen

@Composable
fun MapMiniRadarBadge(
    avatar: AvatarState,
    selectedFloor: Int,
    places: List<StoreEntity>,
    modifier: Modifier = Modifier
) {
    val nearestLandmark = remember(avatar.x, avatar.y, avatar.floor, places) {
        val node = MallFloorPlanGraph.nearestNode(avatar.x, avatar.y, avatar.floor)
        node.labelAr
    }

    Surface(
        modifier = modifier.testTag("map_mini_radar"),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xE00D1322),
        tonalElevation = 4.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Canvas(modifier = Modifier.size(22.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val r = size.minDimension / 2f - 1.dp.toPx()
                drawCircle(color = Color(0xFF172B6B), radius = r, center = center)
                drawCircle(
                    color = Color(0xFF4DB4A9),
                    radius = r,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                )
                rotate(degrees = avatar.headingDegrees, pivot = center) {
                    val needle = androidx.compose.ui.graphics.Path().apply {
                        moveTo(center.x + r * 0.72f, center.y)
                        lineTo(center.x - r * 0.35f, center.y - r * 0.38f)
                        lineTo(center.x - r * 0.15f, center.y)
                        lineTo(center.x - r * 0.35f, center.y + r * 0.38f)
                        close()
                    }
                    drawPath(needle, Color(0xFFEB8C28))
                }
            }
            Column {
                Text(
                    text = if (selectedFloor == 0) "الدور الأرضي" else "الدور الأول",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4DB4A9),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "بالقرب من: $nearestLandmark",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFF8FAFC),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MapInspectedPlaceCard(
    place: StoreEntity,
    onDismiss: () -> Unit,
    onStartNavigation: () -> Unit,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBadgeIcon(category = place.category)
                Column(modifier = Modifier.weight(1f)) {
                    Text(place.nameAr, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${place.floorLabelAr} • ${place.zoneAr}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "إغلاق")
                }
            }
            Text(
                text = place.descriptionAr,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartNavigation,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ابدأ التوجيه")
                }
                OutlinedButton(
                    onClick = onOpenDetails,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("التفاصيل")
                }
            }
        }
    }
}

@Composable
fun MapArrivalCard(
    destination: StoreEntity,
    offers: List<OfferEntity>,
    onOpenDetails: () -> Unit,
    onClearNavigation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("map_arrival_card"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = StatusOpenGreen,
                    modifier = Modifier.size(28.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "وصلت إلى وجهتك!",
                        style = MaterialTheme.typography.labelLarge,
                        color = StatusOpenGreen
                    )
                    Text(
                        text = destination.nameAr,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                FloorPill(floor = destination.floor)
            }
            Text(
                text = destination.descriptionAr,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (offers.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = "عرض متاح: ${offers.first().titleAr} (${offers.first().discountBadge})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenDetails,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("صفحة المحل والعروض")
                }
                OutlinedButton(
                    onClick = onClearNavigation,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("إنهاء التوجيه")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapDestinationPickerSheet(
    places: List<StoreEntity>,
    initialQuery: String = "",
    onDismiss: () -> Unit,
    onDestinationSelected: (StoreEntity) -> Unit
) {
    var filterQuery by remember(initialQuery) { mutableStateOf(initialQuery) }
    val filtered = remember(places, filterQuery) {
        places.filter {
            filterQuery.isBlank() ||
                it.nameAr.contains(filterQuery, ignoreCase = true) ||
                it.category.labelAr.contains(filterQuery, ignoreCase = true) ||
                it.zoneAr.contains(filterQuery, ignoreCase = true)
        }
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "اختر وجهتك داخل مول العابد",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = filterQuery,
                onValueChange = { filterQuery = it },
                placeholder = { Text("ابحث عن محل، مطعم، بوابة، مصعد...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            LazyColumn(
                modifier = Modifier.height(340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.id }) { place ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDestinationSelected(place) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryBadgeIcon(category = place.category)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    place.nameAr,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${place.category.labelAr} • ${place.zoneAr}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            FloorPill(floor = place.floor)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
