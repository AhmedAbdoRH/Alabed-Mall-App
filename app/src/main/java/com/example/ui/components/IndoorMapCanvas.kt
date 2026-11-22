package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.R
import com.example.data.map.MallFloorPlanGraph
import com.example.data.model.AvatarState
import com.example.data.model.IndoorRoutePlan
import com.example.data.model.StoreEntity
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.hypot

@Composable
fun IndoorMapCanvas(
    selectedFloor: Int,
    isEveningMode: Boolean,
    places: List<StoreEntity>,
    avatar: AvatarState,
    routePlan: IndoorRoutePlan?,
    inspectedPlaceId: String?,
    zoomScale: Float,
    panOffset: Offset,
    onZoomPanChange: (Float, Offset) -> Unit,
    onPlaceTapped: (StoreEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val palette = remember(isEveningMode) { MapWorldPalette.create(isEveningMode) }

    val groundMapBitmap = ImageBitmap.imageResource(id = R.drawable.img_mall_game_map_f0)
    val firstFloorMapBitmap = ImageBitmap.imageResource(id = R.drawable.img_mall_game_map_f1)
    val activeWorldBitmap = if (selectedFloor == 0) groundMapBitmap else firstFloorMapBitmap

    val infiniteTransition = rememberInfiniteTransition(label = "game_world_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.56f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 52f,
        animationSpec = infiniteRepeatable(
            animation = tween(820, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dash_phase"
    )
    val trafficPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(11500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "traffic_phase"
    )
    val walkPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "walk_phase"
    )

    val visiblePlaces = remember(places, selectedFloor) {
        places.filter {
            it.floor == selectedFloor ||
                it.id == "escalator_central" ||
                it.id == "elevator_east"
        }
    }

    var currentCanvasSize by remember { mutableStateOf(Size.Zero) }
    val latestZoom by rememberUpdatedState(zoomScale)
    val latestPan by rememberUpdatedState(panOffset)
    val latestPlaces by rememberUpdatedState(visiblePlaces)
    val latestOnZoomPan by rememberUpdatedState(onZoomPanChange)
    val latestOnPlaceTapped by rememberUpdatedState(onPlaceTapped)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.canvasBackdrop)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newZoom = (latestZoom * zoom).coerceIn(1.0f, 3.6f)
                    val viewport = Size(size.width.toFloat(), size.height.toFloat())
                    val clampedPan = clampPanOffset(viewport, newZoom, latestPan + pan)
                    latestOnZoomPan(newZoom, clampedPan)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    if (currentCanvasSize.width <= 0f || currentCanvasSize.height <= 0f) return@detectTapGestures
                    val transform = computeMapTransform(currentCanvasSize, latestZoom, latestPan)
                    val mapX = (tapOffset.x - transform.offsetX) / transform.scale
                    val mapY = (tapOffset.y - transform.offsetY) / transform.scale

                    val tapped = latestPlaces
                        .filter { p ->
                            abs(mapX - p.mapX) <= 52f &&
                                mapY >= (p.mapY - 46f) &&
                                mapY <= (p.mapY + 34f)
                        }
                        .minByOrNull { p ->
                            hypot(mapX - p.mapX, mapY - (p.mapY - 10f))
                        }
                        ?: latestPlaces.minByOrNull { hypot(mapX - it.mapX, mapY - it.mapY) }?.takeIf {
                            hypot(mapX - it.mapX, mapY - it.mapY) <= 62f
                        }

                    if (tapped != null) {
                        latestOnPlaceTapped(tapped)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            currentCanvasSize = size
            val t = computeMapTransform(size, zoomScale, panOffset)

            fun toScreen(mx: Float, my: Float): Offset {
                return Offset(t.offsetX + mx * t.scale, t.offsetY + my * t.scale)
            }

            fun toSize(mw: Float, mh: Float): Size {
                return Size(mw * t.scale, mh * t.scale)
            }

            val worldTL = toScreen(0f, 0f)
            val worldSz = toSize(MallFloorPlanGraph.MAP_WIDTH, MallFloorPlanGraph.MAP_HEIGHT)

            // 1. Full-Screen Edge-to-Edge 2.5D Illustrated Game World Map (Ground or Cutaway First Floor)
            drawImage(
                image = activeWorldBitmap,
                dstOffset = IntOffset(floor(worldTL.x).toInt(), floor(worldTL.y).toInt()),
                dstSize = IntSize(ceil(worldSz.width).toInt() + 1, ceil(worldSz.height).toInt() + 1),
                filterQuality = FilterQuality.High
            )

            // 2. Live Animated Cars Gliding on the Agricultural Highway
            drawAnimatedHighwayTraffic(
                trafficPhase = trafficPhase,
                isEvening = isEveningMode,
                toScreen = ::toScreen,
                scale = t.scale
            )

            // 3. Evening Twilight Lighting & Warm Courtyard Lantern Pools
            if (isEveningMode) {
                drawEveningAtmosphere(
                    selectedFloor = selectedFloor,
                    toScreen = ::toScreen,
                    toSize = ::toSize,
                    scale = t.scale
                )
            }

            // 4. Subtle Walkable Corridor Path Network
            val nodeMap = MallFloorPlanGraph.nodes.associateBy { it.id }
            MallFloorPlanGraph.edges
                .filter { !it.isVerticalTransition }
                .forEach { edge ->
                    val n1 = nodeMap[edge.fromId]
                    val n2 = nodeMap[edge.toId]
                    if (n1 != null && n2 != null && n1.floor == selectedFloor && n2.floor == selectedFloor) {
                        drawLine(
                            color = Color(0xFF22D3EE).copy(alpha = if (isEveningMode) 0.14f else 0.10f),
                            start = toScreen(n1.x, n1.y),
                            end = toScreen(n2.x, n2.y),
                            strokeWidth = 5f * t.scale,
                            cap = StrokeCap.Round
                        )
                    }
                }

            // 5. Active Game-Style Glowing Navigation Ribbon
            if (routePlan != null && routePlan.nodes.isNotEmpty()) {
                drawGameNavigationPath(
                    selectedFloor = selectedFloor,
                    avatar = avatar,
                    routePlan = routePlan,
                    dashPhase = dashPhase,
                    pulseScale = pulseScale,
                    toScreen = ::toScreen,
                    scale = t.scale
                )
            }

            // 6. 2.5D Floating Game Marker Pins & Zoom-Adaptive Store Labels
            drawGameStorePins(
                places = visiblePlaces,
                palette = palette,
                routePlan = routePlan,
                inspectedPlaceId = inspectedPlaceId,
                zoomScale = zoomScale,
                pulseScale = pulseScale,
                toScreen = ::toScreen,
                scale = t.scale,
                textMeasurer = textMeasurer
            )

            // 7. Articulated 2.5D Top-Down Explorer Avatar
            if (avatar.floor == selectedFloor) {
                drawTopDownWalkerAvatar(
                    avatar = avatar,
                    walkPhase = walkPhase,
                    pulseScale = pulseScale,
                    toScreen = ::toScreen,
                    scale = t.scale
                )
            }
        }
    }
}

internal data class MapTransform(val scale: Float, val offsetX: Float, val offsetY: Float)

/**
 * Computes a full-screen edge-to-edge cover scale (`maxOf`) and strictly clamps pan offsets
 * inside the world map bounds so blank or cut-off screen margins can never appear.
 */
internal fun computeMapTransform(canvasSize: Size, zoom: Float, panOffset: Offset): MapTransform {
    val clampedZoom = zoom.coerceIn(1.0f, 3.6f)
    val baseScaleX = canvasSize.width / MallFloorPlanGraph.MAP_WIDTH
    val baseScaleY = canvasSize.height / MallFloorPlanGraph.MAP_HEIGHT
    val baseScale = maxOf(baseScaleX, baseScaleY)
    val effectiveScale = baseScale * clampedZoom
    val contentW = MallFloorPlanGraph.MAP_WIDTH * effectiveScale
    val contentH = MallFloorPlanGraph.MAP_HEIGHT * effectiveScale
    val clampedPan = clampPanOffset(canvasSize, clampedZoom, panOffset)
    val ox = (canvasSize.width - contentW) / 2f + clampedPan.x
    val oy = (canvasSize.height - contentH) / 2f + clampedPan.y
    return MapTransform(effectiveScale, ox, oy)
}

internal fun clampPanOffset(canvasSize: Size, zoom: Float, desiredPan: Offset): Offset {
    if (canvasSize.width <= 0f || canvasSize.height <= 0f) return Offset.Zero
    val clampedZoom = zoom.coerceIn(1.0f, 3.6f)
    val baseScale = maxOf(
        canvasSize.width / MallFloorPlanGraph.MAP_WIDTH,
        canvasSize.height / MallFloorPlanGraph.MAP_HEIGHT
    )
    val effectiveScale = baseScale * clampedZoom
    val contentW = MallFloorPlanGraph.MAP_WIDTH * effectiveScale
    val contentH = MallFloorPlanGraph.MAP_HEIGHT * effectiveScale
    val maxPanX = ((contentW - canvasSize.width) / 2f).coerceAtLeast(0f)
    val maxPanY = ((contentH - canvasSize.height) / 2f).coerceAtLeast(0f)
    return Offset(
        x = desiredPan.x.coerceIn(-maxPanX, maxPanX),
        y = desiredPan.y.coerceIn(-maxPanY, maxPanY)
    )
}
