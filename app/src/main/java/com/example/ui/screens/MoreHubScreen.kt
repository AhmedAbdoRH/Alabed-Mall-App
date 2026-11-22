package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R

@Composable
fun MoreHubScreen(
    unreadNotifications: Int,
    onOpenAccess: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenVisitorServices: () -> Unit,
    onOpenContentManager: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_elabed_mall),
                            contentDescription = "شعار العابد مول",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(54.dp)
                                .padding(4.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "خدمات ومعلومات العابد مول",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "الوصول للمول، الإشعارات، خدمة الزوار، وإدارة المحتوى",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                HubTileCard(
                    title = "الوصول إلى مول العابد",
                    subtitle = "خريطة Google Maps، البوابات، المواعيد وطريق الإسكندرية الزراعي",
                    icon = Icons.Filled.DirectionsCar,
                    iconBg = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = onOpenAccess,
                    modifier = Modifier.testTag("more_access_tile")
                )
            }

            item {
                HubTileCard(
                    title = "الإشعارات وإعداداتها",
                    subtitle = "عروض، افتتاحات جديدة، فعاليات، والتحكم في التنبيهات",
                    icon = Icons.Filled.NotificationsActive,
                    iconBg = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                    badgeCount = unreadNotifications,
                    onClick = onOpenNotifications,
                    modifier = Modifier.testTag("more_notifications_tile")
                )
            }

            item {
                HubTileCard(
                    title = "خدمة الزوار والاستعلامات",
                    subtitle = "مواقع الخدمات الأساسية، إرسال استفسار أو ملاحظة أو بلاغ",
                    icon = Icons.Filled.SupportAgent,
                    iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = onOpenVisitorServices,
                    modifier = Modifier.testTag("more_services_tile")
                )
            }

            item {
                HubTileCard(
                    title = "إدارة محتوى المول (لفريق العمل)",
                    subtitle = "تحديث المحلات ومواقع الخريطة، العروض، الفعاليات، وإرسال إشعارات",
                    icon = Icons.Filled.EditLocationAlt,
                    iconBg = MaterialTheme.colorScheme.surfaceVariant,
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onOpenContentManager,
                    modifier = Modifier.testTag("more_cms_tile")
                )
            }

            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "عنوان المول الرسمي",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "مول العابد • كيلو ٣٧، طريق الإسكندرية الزراعي، طوخ، محافظة القليوبية",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "الرمز البريدي / Plus Code: 95QR+PW طوخ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HubTileCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    badgeCount: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                    if (badgeCount > 0) {
                        Badge { Text("$badgeCount") }
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
