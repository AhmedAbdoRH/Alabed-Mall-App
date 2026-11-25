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
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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

    val total = tasks.size
    val progress = if (total == 0) 0f else checkedCount.toFloat() / total

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
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
                .background(Color.White)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Progress summary
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = ElAbedRoyalBlueContainer,
                        modifier = Modifier.fillMaxWidth()
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (total == 0) "ابدأ قائمتك" else "$checkedCount من $total تمت",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ElAbedRoyalBlue
                                    )
                                    Text(
                                        text = "اكتب الطلب وسنقترح لك المكان المناسب داخل المول",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "$pendingCount",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ElAbedRoyalBlue
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "متبقي",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (total > 0) LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = ElAbedVibrantGreen,
                                trackColor = Color.White
                            )
                        }
                    }
                }

                // Add card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFC3CBDE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
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

                            val submit = {
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
                                                "تمت الإضافة إلى القائمة"
                                            }
                                        )
                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("اكتب اسم المهمة أو الصنف أولاً")
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = itemInput,
                                    onValueChange = { itemInput = it },
                                    placeholder = {
                                        Text(
                                            "مثال: لحوم، شاحن، عطر، سحب نقدي",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    singleLine = true,
                                    trailingIcon = {
                                        if (itemInput.isNotEmpty()) {
                                            IconButton(onClick = { itemInput = "" }) {
                                                Icon(Icons.Filled.Clear, contentDescription = "مسح النص")
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color(0xFFC3CBDE),
                                        focusedBorderColor = ElAbedRoyalBlue
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("tasks_item_input")
                                )
                                Button(
                                    onClick = { submit() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElAbedRoyalBlue,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier
                                        .size(56.dp)
                                        .testTag("tasks_add_button")
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "إضافة للقائمة")
                                }
                            }

                            if (placeSuggestions.isNotEmpty()) {
                                Text(
                                    text = if (itemInput.isBlank()) {
                                        "أماكن مقترحة (اختياري)"
                                    } else {
                                        "أماكن مناسبة لـ «${itemInput.trim()}»"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(placeSuggestions, key = { it.store.id }) { suggestion ->
                                        val isChosen = selectedStore?.id == suggestion.store.id
                                        Surface(
                                            onClick = {
                                                selectedStore = if (isChosen) null else suggestion.store
                                            },
                                            shape = RoundedCornerShape(50),
                                            color = if (isChosen) ElAbedRoyalBlueContainer else Color(0xFFF3F5FA),
                                            border = BorderStroke(
                                                width = if (isChosen) 1.5.dp else 1.dp,
                                                color = if (isChosen) ElAbedRoyalBlue else Color(0xFFC3CBDE)
                                            ),
                                            modifier = Modifier.testTag("task_place_suggestion_${suggestion.store.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                if (isChosen) {
                                                    Icon(
                                                        imageVector = Icons.Filled.CheckCircle,
                                                        contentDescription = "مكان مختار",
                                                        tint = ElAbedRoyalBlue,
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                } else {
                                                    StoreLogoBadge(store = suggestion.store, size = 28.dp)
                                                }
                                                Text(
                                                    text = suggestion.store.nameAr,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.SemiBold,
                                                    color = if (isChosen) ElAbedRoyalBlue else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.widthIn(max = 150.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Filters
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                0 to "الكل ($total)",
                                1 to "متبقي ($pendingCount)",
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
                                        selectedContainerColor = ElAbedRoyalBlue,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFEEF1F8)
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

                if (filteredTasks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlaylistAddCheck,
                                contentDescription = null,
                                tint = ElAbedRoyalBlue.copy(alpha = 0.7f),
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = if (tasks.isEmpty()) "القائمة فارغة" else "لا توجد عناصر هنا",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "اكتب أي غرض أو مهمة وسنوصلك للمكان المناسب في العابد مول.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(filteredTasks, key = { it.id }) { task ->
                        val linkedStore = task.storeId?.let { placeMap[it] }
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (task.isChecked) ElAbedSurfaceVariant else Color.White
                            ),
                            border = BorderStroke(1.dp, Color(0xFFC3CBDE)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggleTask(task.id, !task.isChecked) },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Checkbox(
                                        checked = task.isChecked,
                                        onCheckedChange = { onToggleTask(task.id, it) },
                                        colors = CheckboxDefaults.colors(checkedColor = ElAbedVibrantGreen)
                                    )
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
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
                                        Text(
                                            text = task.departmentTag,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteTask(task.id) },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.DeleteOutline,
                                            contentDescription = "حذف المهمة",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (!task.storeNameAr.isNullOrBlank() && !task.storeId.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = ElAbedSurfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 4.dp, end = 4.dp, bottom = 4.dp)
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
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                                                        modifier = Modifier.size(22.dp)
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

                                            Button(
                                                onClick = { onOpenIndoorMap(task.storeId) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = ElAbedRoyalBlue,
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                                modifier = Modifier
                                                    .height(40.dp)
                                                    .testTag("task_navigate_${task.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Navigation,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "توجيه",
                                                    style = MaterialTheme.typography.labelLarge,
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
