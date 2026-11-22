package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.MapMovementMode
import com.example.data.model.OfferEntity
import com.example.data.model.StoreEntity
import com.example.ui.components.IndoorMapCanvas
import com.example.ui.components.MapArrivalCard
import com.example.ui.components.MapDestinationPickerSheet
import com.example.ui.components.MapInspectedPlaceCard
import com.example.ui.components.MapMiniRadarBadge
import com.example.ui.components.OpenStatusChip
import com.example.ui.components.VirtualJoystick
import com.example.ui.components.computeMapTransform
import com.example.ui.theme.StatusOpenContainer
import com.example.ui.theme.StatusOpenGreen
import com.example.ui.viewmodel.IndoorMapUiState

@Composable
fun IndoorMapScreen(
    mapState: IndoorMapUiState,
    places: List<StoreEntity>,
    offers: List<OfferEntity>,
    initialTargetPlaceId: String?,
    onSelectFloor: (Int) -> Unit,
    onSwitchMode: (MapMovementMode) -> Unit,
    onLocationPermissionResult: (Boolean) -> Unit,
    onInspectPlace: (String?) -> Unit,
    onStartNavigation: (String) -> Unit,
    onClearNavigation: () -> Unit,
    onMoveJoystick: (Float, Float, Float) -> Unit,
    onSwitchAvatarFloor: (Int) -> Unit,
    onResetToGate1: () -> Unit,
    onOpenStoreDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Full-screen game camera focused smoothly closer to the hero explorer (1.42x)
    var zoomScale by remember { mutableFloatStateOf(1.42f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var followAvatar by remember { mutableStateOf(true) }
    var isEveningMode by remember { mutableStateOf(false) }
    var showDestinationPicker by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onLocationPermissionResult(granted)
    }

    LaunchedEffect(initialTargetPlaceId) {
        if (!initialTargetPlaceId.isNullOrBlank()) {
            onStartNavigation(initialTargetPlaceId)
            followAvatar = true
            if (zoomScale < 1.4f) zoomScale = 1.45f
        }
    }

    val inspectedPlace = remember(places, mapState.inspectedPlaceId) {
        places.firstOrNull { it.id == mapState.inspectedPlaceId }
    }
    val destOffers = remember(offers, mapState.routePlan) {
        val destId = mapState.routePlan?.destinationStore?.id ?: return@remember emptyList()
        offers.filter { it.storeId == destId }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val density = LocalDensity.current
        val viewportSize = remember(maxWidth, maxHeight, density) {
            with(density) { Size(maxWidth.toPx(), maxHeight.toPx()) }
        }

        // Smooth camera follow clamped within world bounds so the map always fills the entire screen
        LaunchedEffect(
            mapState.avatar.x,
            mapState.avatar.y,
            followAvatar,
            zoomScale,
            viewportSize
        ) {
            if (followAvatar && viewportSize.width > 0f && viewportSize.height > 0f) {
                val t = computeMapTransform(viewportSize, zoomScale, Offset.Zero)
                val avBaseX = t.offsetX + mapState.avatar.x * t.scale
                val avBaseY = t.offsetY + mapState.avatar.y * t.scale
                val desired = Offset(
                    x = viewportSize.width * 0.5f - avBaseX,
                    y = viewportSize.height * 0.56f - avBaseY
                )
                panOffset = com.example.ui.components.clampPanOffset(viewportSize, zoomScale, desired)
            }
        }

        // 1. Wide Uncropped 2.5D Illustrated Game World Canvas
        IndoorMapCanvas(
            selectedFloor = mapState.selectedFloor,
            isEveningMode = isEveningMode,
            places = places,
            avatar = mapState.avatar,
            routePlan = mapState.routePlan,
            inspectedPlaceId = mapState.inspectedPlaceId,
            zoomScale = zoomScale,
            panOffset = panOffset,
            onZoomPanChange = { z, p ->
                followAvatar = false
                zoomScale = z
                panOffset = p
            },
            onPlaceTapped = { place ->
                onInspectPlace(place.id)
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Official Brand & Game HUD Controls
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 4.dp,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Brand Row: Official Logo + Name + Open Status + Day/Evening Lighting
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
                                shadowElevation = 1.dp
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_elabed_mall),
                                    contentDescription = "شعار مول العابد",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .padding(2.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "مول العابد • طوخ",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "الخريطة التفاعلية الذكية",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OpenStatusChip()
                            FilledTonalIconButton(
                                onClick = { isEveningMode = !isEveningMode },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("map_lighting_toggle")
                            ) {
                                Icon(
                                    imageVector = if (isEveningMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                    contentDescription = if (isEveningMode) "الوضع النهاري" else "الوضع المسائي",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Movement Mode Segmented Switcher
                    val modes = listOf(
                        MapMovementMode.MANUAL_EXPLORE,
                        MapMovementMode.LIVE_GUIDANCE
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        modes.forEachIndexed { idx, mode ->
                            SegmentedButton(
                                selected = mapState.movementMode == mode,
                                onClick = {
                                    onSwitchMode(mode)
                                    if (mode == MapMovementMode.LIVE_GUIDANCE && !mapState.liveGpsStatus.hasPermission) {
                                        locationPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = idx, count = modes.size),
                                icon = {
                                    Icon(
                                        imageVector = if (mode == MapMovementMode.MANUAL_EXPLORE) {
                                            Icons.Filled.Gamepad
                                        } else {
                                            Icons.Filled.MyLocation
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                },
                                modifier = Modifier.testTag("map_mode_${mode.name}")
                            ) {
                                Text(
                                    text = mode.labelAr,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Floor Switcher + Brand Warm Orange CTA Destination Picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val floors = listOf(0 to "الأرضي", 1 to "الأول")
                        floors.forEach { (floorIdx, label) ->
                            val hasAvatar = mapState.avatar.floor == floorIdx
                            val hasDest = mapState.routePlan?.destinationStore?.floor == floorIdx
                            val suffix = buildString {
                                if (hasAvatar) append(" • أنت")
                                if (hasDest) append(" • وجهة")
                            }
                            FilterChip(
                                selected = mapState.selectedFloor == floorIdx,
                                onClick = { onSelectFloor(floorIdx) },
                                label = {
                                    Text(
                                        text = "$label$suffix",
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1
                                    )
                                },
                                modifier = Modifier.testTag("map_floor_chip_$floorIdx")
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = { showDestinationPicker = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("map_pick_destination_button")
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ابحث عن وجهة", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Live Compass & Nearby Landmark Radar Strip
            MapMiniRadarBadge(
                avatar = mapState.avatar,
                selectedFloor = mapState.selectedFloor,
                places = places
            )

            // Live GPS Status strip when in Live Guidance Mode
            AnimatedVisibility(visible = mapState.movementMode == MapMovementMode.LIVE_GUIDANCE) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = if (mapState.liveGpsStatus.isInsideMallBounds) {
                        StatusOpenContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mapState.liveGpsStatus.statusMessageAr,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (mapState.liveGpsStatus.isInsideMallBounds) {
                                StatusOpenGreen
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            },
                            modifier = Modifier.weight(1f)
                        )
                        if (!mapState.liveGpsStatus.hasPermission) {
                            FilledTonalButton(
                                onClick = {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("منح الإذن", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // Turn-by-turn Guidance Card when route is active
            val activePlan = mapState.routePlan
            AnimatedVisibility(visible = activePlan != null && !mapState.hasArrived) {
                activePlan?.let { plan ->
                    val currentStep = plan.steps.firstOrNull { it.floor == mapState.avatar.floor }
                        ?: plan.steps.firstOrNull()
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                        imageVector = Icons.Filled.Navigation,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = currentStep?.instructionAr ?: "اتجه نحو ${plan.destinationStore.nameAr}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "الوجهة: ${plan.destinationStore.nameAr} • المتبقي ≈ ${plan.totalDistanceMeters} م",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.88f)
                                        )
                                    }
                                }
                                IconButton(onClick = onClearNavigation) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "إلغاء التوجيه",
                                        tint = Color.White
                                    )
                                }
                            }

                            val prompt = mapState.floorTransitionPrompt
                            if (prompt != null) {
                                FilledTonalButton(
                                    onClick = { onSwitchAvatarFloor(plan.destinationStore.floor) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.Stairs, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(prompt, style = MaterialTheme.typography.labelLarge)
                                }
                            } else if (plan.requiresFloorChange && mapState.avatar.floor != plan.destinationStore.floor) {
                                Text(
                                    text = "الوجهة في ${plan.destinationStore.floorLabelAr} — تحرك نحو ${plan.transitionNode?.labelAr ?: "المصعد أو السلم المتحرك"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFFE5CC)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Bottom-End Camera & Follow Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            if (mapState.avatar.floor != mapState.selectedFloor) {
                FilledTonalButton(
                    onClick = {
                        onSelectFloor(mapState.avatar.floor)
                        followAvatar = true
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("عودة لدورك", style = MaterialTheme.typography.labelSmall)
                }
            }
            FilledTonalIconButton(
                onClick = {
                    followAvatar = true
                    if (zoomScale < 1.5f) zoomScale = 1.65f
                },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("map_follow_avatar_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.CenterFocusStrong,
                    contentDescription = "تتبع الشخصية",
                    tint = if (followAvatar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalIconButton(
                onClick = {
                    followAvatar = false
                    zoomScale = 1.0f
                    panOffset = Offset.Zero
                },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("map_overview_button")
            ) {
                Icon(Icons.Filled.ZoomOutMap, contentDescription = "نظرة شاملة للمول")
            }
            FilledTonalIconButton(
                onClick = {
                    onResetToGate1()
                    followAvatar = true
                    zoomScale = 1.42f
                },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("map_reset_gate1_button")
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "العودة لبوابة ١")
            }
        }

        // 4. Virtual Joystick Overlay in Manual Explore Mode (Bottom-Start)
        AnimatedVisibility(
            visible = mapState.movementMode == MapMovementMode.MANUAL_EXPLORE &&
                inspectedPlace == null &&
                !mapState.hasArrived,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            VirtualJoystick(
                onMoveTick = { dx, dy, dt ->
                    if (dx != 0f || dy != 0f) {
                        followAvatar = true
                        if (zoomScale < 1.35f) {
                            zoomScale = 1.45f
                        }
                    }
                    onMoveJoystick(dx, dy, dt)
                }
            )
        }

        // 5. Inspected Place Quick Card
        AnimatedVisibility(
            visible = inspectedPlace != null && !mapState.hasArrived,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            inspectedPlace?.let { place ->
                MapInspectedPlaceCard(
                    place = place,
                    onDismiss = { onInspectPlace(null) },
                    onStartNavigation = {
                        onStartNavigation(place.id)
                        followAvatar = true
                        if (zoomScale < 1.35f) zoomScale = 1.45f
                    },
                    onOpenDetails = { onOpenStoreDetail(place.id) }
                )
            }
        }

        // 6. Arrival Destination Card
        AnimatedVisibility(
            visible = mapState.hasArrived && mapState.routePlan != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            mapState.routePlan?.destinationStore?.let { dest ->
                MapArrivalCard(
                    destination = dest,
                    offers = destOffers,
                    onOpenDetails = { onOpenStoreDetail(dest.id) },
                    onClearNavigation = onClearNavigation
                )
            }
        }
    }

    if (showDestinationPicker) {
        MapDestinationPickerSheet(
            places = places,
            onDismiss = { showDestinationPicker = false },
            onDestinationSelected = { place ->
                showDestinationPicker = false
                followAvatar = true
                if (zoomScale < 1.35f) zoomScale = 1.45f
                onStartNavigation(place.id)
            }
        )
    }
}
