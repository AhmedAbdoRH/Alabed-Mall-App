package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OfferEntity
import com.example.data.model.StoreEntity
import com.example.data.search.MallProductSuggestion
import com.example.ui.theme.ElAbedOutline
import com.example.ui.theme.ElAbedTextSecondary
import com.example.ui.theme.ElAbedRoyalBlue
import com.example.ui.theme.ElAbedRoyalBlueContainer
import com.example.ui.theme.ElAbedTextPrimary
import com.example.ui.theme.ElAbedWarmOrangeContainer
import com.example.ui.theme.OnElAbedWarmOrangeContainer

@Composable
fun ProductSuggestionPillCard(
    product: MallProductSuggestion,
    onOpenStore: () -> Unit,
    onNavigateMap: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = Color(0xFF6CC794)
) {
    Surface(
        onClick = onOpenStore,
        modifier = modifier.testTag("directory_product_${product.id}"),
        shape = RoundedCornerShape(10.dp),
        color = product.softTintColor,
        border = BorderStroke(1.5.dp, borderColor),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(product.accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = product.icon,
                        contentDescription = product.nameAr,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = product.nameAr,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.sp,
                        lineHeight = 13.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = ElAbedTextPrimary,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.brandNameAr,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = ElAbedTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Surface(
                    onClick = onNavigateMap,
                    shape = RoundedCornerShape(50),
                    color = product.accentColor,
                    modifier = Modifier.testTag("directory_product_map_btn_${product.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Navigation,
                            contentDescription = "خريطة",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "خريطة",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactBrandPlaceCard(
    place: StoreEntity,
    onOpenStoreDetail: () -> Unit,
    onNavigateOnMap: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = Color(0xFF7C9DE8)
) {
    Surface(
        onClick = onOpenStoreDetail,
        modifier = modifier.testTag("directory_item_${place.id}"),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, borderColor),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StoreLogoBadge(store = place, size = 26.dp)
                Text(
                    text = place.nameAr,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.sp,
                        lineHeight = 13.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = ElAbedTextPrimary,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = place.floorLabelAr,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = ElAbedRoyalBlue,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Surface(
                    onClick = onNavigateOnMap,
                    shape = RoundedCornerShape(50),
                    color = ElAbedRoyalBlue,
                    modifier = Modifier.testTag("directory_map_btn_${place.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Navigation,
                            contentDescription = "خريطة ${place.nameAr}",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "خريطة",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BrandPlaceDirectoryCard(
    place: StoreEntity,
    storeOffers: List<OfferEntity>,
    onOpenStoreDetail: () -> Unit,
    onNavigateOnMap: () -> Unit,
    onAddToTrip: () -> Unit,
    onShareStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenStoreDetail)
            .testTag("directory_item_${place.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFC3CBDE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StoreLogoBadge(store = place, size = 54.dp)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = place.nameAr,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElAbedTextPrimary,
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
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${place.category.labelAr} • ${place.zoneAr}",
                        style = MaterialTheme.typography.bodySmall,
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
                        color = ElAbedWarmOrangeContainer
                    ) {
                        Text(
                            text = "${storeOffers.first().discountBadge} متاح الآن",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnElAbedWarmOrangeContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                } else {
                    Text(
                        text = "مواعيد: ${place.hoursAr}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onAddToTrip,
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
                        onClick = onShareStore,
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
                    FilledTonalButton(
                        onClick = onNavigateOnMap,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ElAbedRoyalBlueContainer,
                            contentColor = ElAbedRoyalBlue
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("directory_map_btn_${place.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "خريطة",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
