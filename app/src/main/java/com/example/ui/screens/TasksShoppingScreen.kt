package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ShoppingTaskEntity
import com.example.data.model.StoreEntity
import com.example.data.search.MallTaskPlaceSuggester
import com.example.ui.components.FloorPill
import com.example.ui.components.StoreLogoBadge
import com.example.ui.theme.ElAbedOutline
import com.example.ui.theme.ElAbedRoyalBlue
import com.example.ui.theme.ElAbedRoyalBlueContainer
import com.example.ui.theme.ElAbedSurfaceVariant
import com.example.ui.theme.ElAbedVibrantGreen
import com.example.ui.theme.ElAbedVibrantGreenContainer
import com.example.ui.theme.ElAbedWarmOrange
import com.example.ui.theme.ElAbedWarmOrangeContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksShoppingScreen(
    tasks: List<ShoppingTaskEntity>,
    places: List<StoreEntity>,
    onBack: () -> Unit,
    onAddTask: (itemName: String, departmentTag: String, selectedStore: StoreEntity?) -> Unit,
    onToggleTask: (Long, Boolean) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onClearChecked: () -> Unit,
    onOpenStoreDetail: (String) -> Unit,
    onOpenIndoorMap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var itemInput by remember { mutableStateOf("") }
    var selectedTaskType by remember { mutableStateOf("مشتريات") }
    var selectedStore by remember { mutableStateOf<StoreEntity?>(null) }
    var filterTab by remember { mutableIntStateOf(0) } // 0 = الكل, 1 = قيد التنفيذ, 2 = تمت

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val placeSuggestions = remember(itemInput, places) {
        MallTaskPlaceSuggester.suggestPlacesForTask(
            query = itemInput,
            places = places,
            limit = 8
        )
    }

    val placeMap = remember(places) { places.associateBy { it.id } }

    val filteredTasks = remember(tasks, filterTab) {
        when (filterTab) {
            1 -> tasks.filter { !it.isChecked }
            2 -> tasks.filter { it.isChecked }
            else -> tasks
        }
    }
    val pendingCount = tasks.count { !it.isChecked }
    val checkedCount = tasks.count { it.isChecked }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "المهام والمشتريات",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("tasks_shopping_back")) {
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Summary Strip
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(ElAbedRoyalBlue, Color(0xFF3E5CB8))
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "خطط مهامك ومشترياتك في العابد",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "اكتب الطلب وسنقترح لك البراندات والأماكن المناسبة داخل المول",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFEAF0FF)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White.copy(alpha = 0.18f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "$pendingCount",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "متبقي",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFEAF0FF)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Add Task / Purchase Card with Smart Mall Place Suggestions
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, ElAbedOutline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlaylistAddCheck,
                                    contentDescription = null,
                                    tint = ElAbedRoyalBlue
                                )
                                Text(
                                    text = "إضافة مهمة أو مشتريات جديدة",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Type selector: مشتريات / مهمة
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(
                                    "مشتريات" to Icons.Filled.ShoppingBag,
                                    "مهمة" to Icons.Filled.TaskAlt
                                ).forEach { (typeLabel, icon) ->
                                    val selected = selectedTaskType == typeLabel
                                    FilterChip(
                                        selected = selected,
                                        onClick = { selectedTaskType = typeLabel },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = typeLabel,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ElAbedRoyalBlueContainer,
                                            selectedLabelColor = ElAbedRoyalBlue,
                                            selectedLeadingIconColor = ElAbedRoyalBlue
                                        ),
                                        modifier = Modifier.testTag("task_type_chip_$typeLabel")
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = itemInput,
                                onValueChange = { itemInput = it },
                                label = { Text("اكتب المهمة أو الصنف (مثال: لحوم، شاحن سامسونج، عطر، سحب نقدي)") },
                                singleLine = true,
                                trailingIcon = {
                                    if (itemInput.isNotEmpty()) {
                                        IconButton(onClick = { itemInput = "" }) {
                                            Icon(Icons.Filled.Clear, contentDescription = "مسح النص")
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tasks_item_input")
                            )

                            // Smart Suggested Places inside El Abed Mall
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = if (itemInput.isBlank()) {
                                        "أماكن وبراندات مقترحة في العابد مول:"
                                    } else {
                                        "اقتراحات الأماكن المناسبة لـ «${itemInput.trim()}»:"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ElAbedRoyalBlue
                                )
                                Text(
                                    text = "اختر المكان الذي ستشتري منه أو تنفذ فيه المهمة (اختياري)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(placeSuggestions, key = { it.store.id }) { suggestion ->
                                    val isChosen = selectedStore?.id == suggestion.store.id
                                    Surface(
                                        onClick = {
                                            selectedStore = if (isChosen) null else suggestion.store
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isChosen) {
                                            ElAbedRoyalBlueContainer.copy(alpha = 0.65f)
                                        } else {
                                            ElAbedSurfaceVariant
                                        },
                                        border = BorderStroke(
                                            width = if (isChosen) 1.5.dp else 1.dp,
                                            color = if (isChosen) ElAbedRoyalBlue else ElAbedOutline
                                        ),
                                        modifier = Modifier
                                            .width(210.dp)
                                            .testTag("task_place_suggestion_${suggestion.store.id}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                StoreLogoBadge(store = suggestion.store, size = 38.dp)
                                                if (isChosen) {
                                                    Icon(
                                                        imageVector = Icons.Filled.CheckCircle,
                                                        contentDescription = "مكان مختار",
                                                        tint = ElAbedRoyalBlue,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                } else {
                                                    FloorPill(floor = suggestion.store.floor)
                                                }
                                            }
                                            Text(
                                                text = suggestion.store.nameAr,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = if (isChosen) ElAbedRoyalBlue else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = suggestion.reasonAr,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            AnimatedVisibility(visible = selectedStore != null) {
                                selectedStore?.let { chosen ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = ElAbedWarmOrangeContainer.copy(alpha = 0.65f),
                                        border = BorderStroke(1.dp, ElAbedWarmOrange.copy(alpha = 0.45f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.LocationOn,
                                                    contentDescription = null,
                                                    tint = Color(0xFF8A4300),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "المكان المحدد: ${chosen.nameAr} (${chosen.floorLabelAr})",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF6B3200),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            IconButton(
                                                onClick = { selectedStore = null },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Clear,
                                                    contentDescription = "إلغاء تحديد المكان",
                                                    tint = Color(0xFF6B3200),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (itemInput.isNotBlank()) {
                                        val tag = selectedStore?.let {
                                            "$selectedTaskType • ${it.category.labelAr}"
                                        } ?: selectedTaskType
                                        onAddTask(itemInput, tag, selectedStore)
                                        val addedPlaceName = selectedStore?.nameAr
                                        itemInput = ""
                                        selectedStore = null
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                if (addedPlaceName != null) {
                                                    "تمت الإضافة وربطها بمكان: $addedPlaceName"
                                                } else {
                                                    "تمت الإضافة إلى قائمة المهام والمشتريات"
                                                }
                                            )
                                        }
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("اكتب اسم المهمة أو الصنف أولاً")
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElAbedRoyalBlue,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("tasks_add_button")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "إضافة للقائمة",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Filter Strip + Clear Completed
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                0 to "الكل (${tasks.size})",
                                1 to "قيد التنفيذ ($pendingCount)",
                                2 to "تمت ($checkedCount)"
                            ).forEach { (idx, label) ->
                                FilterChip(
                                    selected = filterTab == idx,
                                    onClick = { filterTab = idx },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (filterTab == idx) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ElAbedRoyalBlueContainer,
                                        selectedLabelColor = ElAbedRoyalBlue
                                    )
                                )
                            }
                        }

                        if (checkedCount > 0) {
                            TextButton(onClick = onClearChecked) {
                                Text("مسح المكتمل", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }

                // Empty State or Tasks List
                if (filteredTasks.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = ElAbedSurfaceVariant),
                            border = BorderStroke(1.dp, ElAbedOutline)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlaylistAddCheck,
                                    contentDescription = null,
                                    tint = ElAbedRoyalBlue,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = if (tasks.isEmpty()) {
                                        "قائمة المهام والمشتريات فارغة"
                                    } else {
                                        "لا توجد عناصر في هذا التصنيف"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "ابدأ بكتابة أي غرض أو مهمة، واختر البراند أو المكان المقترح داخل العابد مول للوصول إليه بسهولة.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredTasks, key = { it.id }) { task ->
                        val linkedStore = task.storeId?.let { placeMap[it] }
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (task.isChecked) {
                                    ElAbedVibrantGreenContainer.copy(alpha = 0.28f)
                                } else {
                                    Color.White
                                }
                            ),
                            border = BorderStroke(1.dp, ElAbedOutline),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (task.isChecked) 0.dp else 1.5.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleTask(task.id, !task.isChecked) }
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = task.isChecked,
                                        onCheckedChange = { onToggleTask(task.id, it) }
                                    )
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = task.itemName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = if (task.isChecked) TextDecoration.LineThrough else null,
                                            color = if (task.isChecked) {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = ElAbedRoyalBlueContainer.copy(alpha = 0.6f)
                                        ) {
                                            Text(
                                                text = task.departmentTag,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = ElAbedRoyalBlue,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { onDeleteTask(task.id) },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(Icons.Filled.DeleteOutline, contentDescription = "حذف المهمة")
                                    }
                                }

                                // Linked Mall Brand / Place Row
                                if (!task.storeNameAr.isNullOrBlank() && !task.storeId.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = ElAbedSurfaceVariant,
                                        border = BorderStroke(1.dp, ElAbedOutline.copy(alpha = 0.7f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { onOpenStoreDetail(task.storeId) }
                                            ) {
                                                if (linkedStore != null) {
                                                    StoreLogoBadge(store = linkedStore, size = 34.dp)
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Filled.Storefront,
                                                        contentDescription = null,
                                                        tint = ElAbedRoyalBlue,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = task.storeNameAr,
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = ElAbedRoyalBlue,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    val floorText = when (task.floor) {
                                                        0 -> "الدور الأرضي"
                                                        1 -> "الدور الأول"
                                                        else -> linkedStore?.floorLabelAr ?: "داخل العابد مول"
                                                    }
                                                    Text(
                                                        text = floorText,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            OutlinedButton(
                                                onClick = { onOpenIndoorMap(task.storeId) },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier
                                                    .height(40.dp)
                                                    .testTag("task_navigate_${task.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Navigation,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "توجيه",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
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
