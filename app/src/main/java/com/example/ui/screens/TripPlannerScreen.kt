package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.map.HypermarketMapCatalog
import com.example.data.model.ShoppingTaskEntity
import com.example.sharing.MallShareHelper
import com.example.ui.components.FloorPill
import com.example.ui.theme.ElAbedOutline
import com.example.ui.theme.ElAbedRoyalBlue
import com.example.ui.theme.ElAbedRoyalBlueContainer
import com.example.ui.theme.ElAbedSurfaceVariant
import com.example.ui.theme.ElAbedVibrantGreen
import com.example.ui.theme.ElAbedVibrantGreenContainer
import com.example.ui.theme.ElAbedWarmOrange
import com.example.ui.theme.ElAbedWarmOrangeContainer
import com.example.ui.viewmodel.OrderedTripStop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripPlannerScreen(
    orderedStops: List<OrderedTripStop>,
    shoppingTasks: List<ShoppingTaskEntity>,
    onBack: () -> Unit,
    onOpenIndoorMap: (String) -> Unit,
    onOpenDirectory: () -> Unit,
    onOpenOffers: () -> Unit,
    onToggleTripStopVisited: (String, Boolean) -> Unit,
    onRemoveTripStop: (String) -> Unit,
    onClearVisitedStops: () -> Unit,
    onAddShoppingTask: (String, String) -> Unit,
    onToggleShoppingTask: (Long, Boolean) -> Unit,
    onDeleteShoppingTask: (Long) -> Unit,
    onClearCheckedTasks: () -> Unit,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 1)) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "قائمة المشوار ومشتريات الهايبر",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("trip_planner_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
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
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "مسار المشوار (${orderedStops.size})",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.testTag("trip_tab_route")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "مشتريات الهايبر (${shoppingTasks.size})",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.testTag("trip_tab_hyper")
                    )
                }

                if (selectedTab == 0) {
                    TripRouteTab(
                        orderedStops = orderedStops,
                        onOpenIndoorMap = onOpenIndoorMap,
                        onOpenDirectory = onOpenDirectory,
                        onOpenOffers = onOpenOffers,
                        onToggleVisited = onToggleTripStopVisited,
                        onRemoveStop = onRemoveTripStop,
                        onClearVisited = onClearVisitedStops
                    )
                } else {
                    HypermarketTasksTab(
                        tasks = shoppingTasks,
                        onAddTask = onAddShoppingTask,
                        onToggleTask = onToggleShoppingTask,
                        onDeleteTask = onDeleteShoppingTask,
                        onClearChecked = onClearCheckedTasks,
                        onNavigateToHypermarket = { onOpenIndoorMap("hypermarket") }
                    )
                }
            }
        }
    }
}

@Composable
private fun TripRouteTab(
    orderedStops: List<OrderedTripStop>,
    onOpenIndoorMap: (String) -> Unit,
    onOpenDirectory: () -> Unit,
    onOpenOffers: () -> Unit,
    onToggleVisited: (String, Boolean) -> Unit,
    onRemoveStop: (String) -> Unit,
    onClearVisited: () -> Unit
) {
    val unvisitedStops = orderedStops.filter { !it.stop.isVisited }
    val visitedCount = orderedStops.count { it.stop.isVisited }
    val totalDistanceMeters = unvisitedStops.sumOf { it.stepDistanceMeters }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ElAbedRoyalBlueContainer.copy(alpha = 0.45f)),
                border = BorderStroke(1.dp, ElAbedRoyalBlue.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElAbedRoyalBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Route, contentDescription = null, tint = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ترتيب الزيارة التلقائي بالمول",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ElAbedRoyalBlue
                            )
                            Text(
                                text = "يبدأ المسار من بوابة ١ ثم الدور الأرضي فالدور الأول لتقليل المسافة (${totalDistanceMeters} م تقريباً)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val nextStop = unvisitedStops.firstOrNull()
                    if (nextStop != null) {
                        Button(
                            onClick = { onOpenIndoorMap(nextStop.stop.storeId) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("trip_start_next_stop_button")
                        ) {
                            Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ابدأ بالمحطة التالية: ${nextStop.stop.titleAr}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (visitedCount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تمت زيارة $visitedCount من أصل ${orderedStops.size} محطات",
                                style = MaterialTheme.typography.labelLarge,
                                color = ElAbedVibrantGreen,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = onClearVisited) {
                                Text("مسح المكتمل", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }

        if (orderedStops.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ElAbedSurfaceVariant),
                    border = BorderStroke(1.dp, ElAbedOutline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingBag,
                            contentDescription = null,
                            tint = ElAbedRoyalBlue,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "قائمة المشوار فارغة حالياً",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "أضف المحلات أو العروض من الدليل أو قسم العروض، وسنرتب لك خط السير تلقائياً حسب الأدوار.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onOpenDirectory,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("دليل المحلات", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onOpenOffers,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("تصفح العروض", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            items(orderedStops, key = { it.stop.id }) { item ->
                val stop = item.stop
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (stop.isVisited) {
                            ElAbedVibrantGreenContainer.copy(alpha = 0.25f)
                        } else {
                            Color.White
                        }
                    ),
                    border = BorderStroke(1.dp, ElAbedOutline),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (stop.isVisited) 0.dp else 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (stop.isVisited) ElAbedVibrantGreen else ElAbedRoyalBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                if (stop.isVisited) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "تمت الزيارة",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${item.orderNumber}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = stop.titleAr,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (stop.isVisited) TextDecoration.LineThrough else null
                                )
                                Text(
                                    text = stop.subtitleAr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            FloorPill(floor = stop.floor)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (stop.badgeAr.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = ElAbedWarmOrangeContainer
                                    ) {
                                        Text(
                                            text = stop.badgeAr,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF8A4300),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                if (!stop.isVisited && item.stepDistanceMeters > 0) {
                                    Text(
                                        text = "مسافة الخطوة ~${item.stepDistanceMeters} م",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Checkbox(
                                    checked = stop.isVisited,
                                    onCheckedChange = { onToggleVisited(stop.id, it) }
                                )
                                OutlinedButton(
                                    onClick = { onOpenIndoorMap(stop.storeId) },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("توجيه", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                                IconButton(
                                    onClick = { onRemoveStop(stop.id) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Filled.DeleteOutline, contentDescription = "حذف المحطة")
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
private fun HypermarketTasksTab(
    tasks: List<ShoppingTaskEntity>,
    onAddTask: (String, String) -> Unit,
    onToggleTask: (Long, Boolean) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onClearChecked: () -> Unit,
    onNavigateToHypermarket: () -> Unit
) {
    val context = LocalContext.current
    var itemInput by remember { mutableStateOf("") }
    val sections = HypermarketMapCatalog.sections
    var selectedSectionId by remember { mutableStateOf(sections.first().id) }
    val selectedSection = HypermarketMapCatalog.sectionById(selectedSectionId) ?: sections.first()
    val checkedCount = tasks.count { it.isChecked }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ElAbedOutline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Icon(
                                imageVector = Icons.Filled.LocalGroceryStore,
                                contentDescription = null,
                                tint = ElAbedWarmOrange
                            )
                            Text(
                                text = "اكتب مشترياتك ومهامك بالهايبر",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedTextField(
                        value = itemInput,
                        onValueChange = { itemInput = it },
                        label = { Text("الصنف أو المهمة (مثال: ٢ كيلو لحم مفروم، لبن، شامبو)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hyper_task_input")
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(sections, key = { it.id }) { sec ->
                            FilterChip(
                                selected = selectedSection.id == sec.id,
                                onClick = { selectedSectionId = sec.id },
                                label = {
                                    Text(
                                        "${sec.routeOrder}. ${sec.nameAr}",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = sec.softBgColor,
                                    selectedLabelColor = sec.accentColor
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (itemInput.isNotBlank()) {
                                    val autoSec = HypermarketMapCatalog.resolveSectionForInput(itemInput, "")
                                    onAddTask(itemInput, (autoSec ?: selectedSection).nameAr)
                                    itemInput = ""
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("hyper_task_add_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("أضف للقائمة", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onNavigateToHypermarket,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("hyper_task_map_button")
                        ) {
                            Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("الهايبر على الخريطة", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (tasks.isNotEmpty()) {
                        FilledTonalButton(
                            onClick = {
                                val text = MallShareHelper.buildFormattedShoppingListText(tasks)
                                MallShareHelper.shareFormattedTextToWhatsApp(
                                    context = context,
                                    subject = "قائمة مشتريات مول العابد",
                                    formattedText = text
                                )
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = ElAbedVibrantGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مشاركة القائمة عبر واتساب", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (checkedCount > 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تم شراء $checkedCount من ${tasks.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElAbedVibrantGreen,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onClearChecked) {
                        Text("مسح المشتريات المنجزة", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        if (tasks.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ElAbedSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "اكتب قائمة طلباتك ومهام التسوق في هايبر ماركت العابد لمتابعتها بسهولة أثناء التسوق.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (task.isChecked) {
                            ElAbedVibrantGreenContainer.copy(alpha = 0.28f)
                        } else {
                            Color.White
                        }
                    ),
                    border = BorderStroke(1.dp, ElAbedOutline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleTask(task.id, !task.isChecked) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = task.isChecked,
                            onCheckedChange = { onToggleTask(task.id, it) }
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = task.itemName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = if (task.isChecked) TextDecoration.LineThrough else null
                            )
                            Text(
                                text = task.departmentTag,
                                style = MaterialTheme.typography.labelSmall,
                                color = ElAbedRoyalBlue
                            )
                        }
                        IconButton(
                            onClick = { onDeleteTask(task.id) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "حذف الصنف")
                        }
                    }
                }
            }
        }
    }
}
