package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.map.HypermarketMapCatalog
import com.example.data.map.HypermarketSection
import com.example.data.model.ShoppingTaskEntity
import com.example.ui.theme.ElAbedRoyalBlue
import com.example.ui.theme.ElAbedRoyalBlueContainer
import com.example.ui.theme.ElAbedSurfaceVariant

@Composable
fun HypermarketMapView(
    tasks: List<ShoppingTaskEntity>,
    onQuickSelectSection: (HypermarketSection) -> Unit,
    modifier: Modifier = Modifier
) {
    val sections = HypermarketMapCatalog.sections
    var selectedSectionId by remember { mutableStateOf(sections.first().id) }
    val selectedSection = HypermarketMapCatalog.sectionById(selectedSectionId) ?: sections.first()

    val tasksBySection = remember(tasks) {
        tasks.groupBy { HypermarketMapCatalog.sectionForTask(it)?.id }
    }
    val activeSectionsOrdered = remember(tasksBySection) {
        sections.filter { section ->
            tasksBySection[section.id].orEmpty().any { !it.isChecked }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = ElAbedRoyalBlueContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Route,
                    contentDescription = null,
                    tint = ElAbedRoyalBlue,
                    modifier = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "خريطة أقسام هايبر ماركت العابد (بيانات توضيحية)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ElAbedRoyalBlue
                    )
                    Text(
                        text = if (activeSectionsOrdered.isEmpty()) {
                            "أضف مشترياتك وسيرتّب التطبيق مسار التسوق تلقائياً من المدخل للكاشير"
                        } else {
                            "مسار مقترح لـ ${activeSectionsOrdered.size} أقسام بالترتيب حتى الكاشير"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = BorderStroke(1.dp, Color(0xFFC3CBDE)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hypermarket_map_canvas")
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE2E8F0)
                    ) {
                        Text(
                            text = "⬅ مخرج الكاشير",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = "مدخل الهايبر ⬇",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                ) {
                    val mapWidth = maxWidth
                    val mapHeight = maxHeight

                    // Walking route path connecting active sections (or full loop outline)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val routeSections = if (activeSectionsOrdered.size >= 2) {
                            activeSectionsOrdered
                        } else {
                            sections
                        }
                        val centers = routeSections.map { s ->
                            Offset(
                                x = (s.normX + s.normW / 2f) * size.width,
                                y = (s.normY + s.normH / 2f) * size.height
                            )
                        }
                        val pathColor = if (activeSectionsOrdered.size >= 2) {
                            ElAbedRoyalBlue.copy(alpha = 0.72f)
                        } else {
                            Color(0xFFCBD5E1)
                        }
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        for (i in 0 until centers.lastIndex) {
                            drawLine(
                                color = pathColor,
                                start = centers[i],
                                end = centers[i + 1],
                                strokeWidth = if (activeSectionsOrdered.size >= 2) 5f else 3f,
                                cap = StrokeCap.Round,
                                pathEffect = dashEffect
                            )
                        }
                    }

                    // Render 10 interactive section blocks
                    sections.forEach { section ->
                        val pendingInSection = tasksBySection[section.id].orEmpty().count { !it.isChecked }
                        val totalInSection = tasksBySection[section.id].orEmpty().size
                        val isSelected = selectedSection.id == section.id

                        val xDp = mapWidth * section.normX
                        val yDp = mapHeight * section.normY
                        val wDp = mapWidth * section.normW
                        val hDp = mapHeight * section.normH

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) section.softBgColor else section.softBgColor.copy(alpha = 0.72f),
                            border = BorderStroke(
                                width = if (isSelected || pendingInSection > 0) 2.dp else 1.dp,
                                color = if (isSelected) ElAbedRoyalBlue else section.accentColor.copy(alpha = 0.65f)
                            ),
                            shadowElevation = if (isSelected) 4.dp else 0.dp,
                            modifier = Modifier
                                .offset(x = xDp, y = yDp)
                                .size(width = wDp, height = hDp)
                                .clickable { selectedSectionId = section.id }
                                .testTag("hyper_section_${section.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                            ) {
                                // Route order number badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(section.accentColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${section.routeOrder}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Pending items count badge if any
                                if (totalInSection > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (pendingInSection > 0) ElAbedRoyalBlue else Color(0xFF16A34A))
                                            .padding(horizontal = 5.dp, vertical = 1.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (pendingInSection > 0) "$pendingInSection" else "✓",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Text(
                                    text = section.nameAr,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(top = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Selected section details card
        val sectionTasks = tasksBySection[selectedSection.id].orEmpty()
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = selectedSection.softBgColor.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, selectedSection.accentColor.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(selectedSection.accentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${selectedSection.routeOrder}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                text = selectedSection.nameAr,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedSection.aisleLabelAr,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { onQuickSelectSection(selectedSection) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElAbedRoyalBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("hyper_section_add_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة لهذا القسم", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = "أصناف شائعة (بيانات توضيحية): ${selectedSection.sampleItemsPlaceholderAr}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (sectionTasks.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "في قائمتك داخل هذا القسم (${sectionTasks.size}):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ElAbedRoyalBlue
                            )
                            sectionTasks.forEach { t ->
                                Text(
                                    text = if (t.isChecked) "✓ ${t.itemName} (تم)" else "• ${t.itemName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (t.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
