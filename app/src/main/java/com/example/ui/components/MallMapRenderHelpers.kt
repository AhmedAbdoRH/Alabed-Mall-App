package com.example.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.model.AvatarState
import com.example.data.model.IndoorRoutePlan
import com.example.data.model.MallPlaceCategory
import com.example.data.model.StoreEntity
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ElAbedRoyalBlue
import com.example.ui.theme.ElAbedTurquoise
import com.example.ui.theme.ElAbedVibrantGreen
import com.example.ui.theme.ElAbedWarmOrange
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

internal data class MapWorldPalette(
    val isEvening: Boolean,
    val canvasBackdrop: Color,
    val labelColor: Color,
    val labelBg: Color,
    val pinShadow: Color
) {
    companion object {
        fun create(isEvening: Boolean): MapWorldPalette {
            return if (isEvening) {
                MapWorldPalette(
                    isEvening = true,
                    canvasBackdrop = Color(0xFF0D1322),
                    labelColor = Color(0xFFF8FAFC),
                    labelBg = Color(0xE6141C2F),
                    pinShadow = Color(0x88000000)
                )
            } else {
                MapWorldPalette(
                    isEvening = false,
                    canvasBackdrop = Color(0xFFFFFFFF),
                    labelColor = Color(0xFF141926),
                    labelBg = Color(0xF5FFFFFF),
                    pinShadow = Color(0x4512181C)
                )
            }
        }
    }
}

private val overviewPriorityIds = setOf(
    "gate_1",
    "hypermarket",
    "samsung",
    "tecno_store",
    "kfc_toukh",
    "pizza_primo",
    "pingo_toys",
    "tutu_kids",
    "fountain_plaza",
    "escalator_central",
    "elevator_east",
    "parking_west",
    "furniture_home",
    "kids_rides",
    "cafe_terrace",
    "perfumes_oud"
)

private data class TrafficCarSpec(
    val laneIndex: Int, // 0 = southbound left lane, 1 = northbound right lane
    val phaseOffset: Float,
    val speedMultiplier: Float,
    val bodyColor: Color,
    val lengthUnits: Float,
    val widthUnits: Float
)

private val highwayCars = listOf(
    TrafficCarSpec(0, 0.06f, 1.0f, Color(0xFFF8FAFC), 22f, 11f),
    TrafficCarSpec(0, 0.31f, 1.0f, ElAbedRoyalBlue, 24f, 12f),
    TrafficCarSpec(0, 0.58f, 1.0f, ElAbedWarmOrange, 21f, 11f),
    TrafficCarSpec(0, 0.84f, 1.0f, Color(0xFF334155), 22f, 11f),
    TrafficCarSpec(1, 0.14f, 1.0f, Color(0xFFDC2626), 22f, 11f),
    TrafficCarSpec(1, 0.44f, 1.0f, ElAbedTurquoise, 24f, 12f),
    TrafficCarSpec(1, 0.72f, 1.0f, ElAbedVibrantGreen, 22f, 11f),
    TrafficCarSpec(1, 0.93f, 1.0f, Color(0xFFE2E8F0), 21f, 11f)
)

/**
 * Animates top-down miniature cars gliding along the Cairo-Alexandria Agricultural Highway
 * on the left side of the 896x1200 wide world map, smoothly passing beneath the overpass bridge.
 */
internal fun DrawScope.drawAnimatedHighwayTraffic(
    trafficPhase: Float,
    isEvening: Boolean,
    toScreen: (Float, Float) -> Offset,
    scale: Float
) {
    val sbStart = Offset(28f, -40f)
    val sbEnd = Offset(152f, 1240f)
    val nbStart = Offset(214f, 1240f)
    val nbEnd = Offset(86f, -40f)

    highwayCars.forEach { car ->
        val progress = (trafficPhase * car.speedMultiplier + car.phaseOffset) % 1f
        val p0 = if (car.laneIndex == 0) sbStart else nbStart
        val p1 = if (car.laneIndex == 0) sbEnd else nbEnd

        val mapX = p0.x + (p1.x - p0.x) * progress
        val mapY = p0.y + (p1.y - p0.y) * progress

        // Fade cleanly beneath the pedestrian bridge deck at y = 200..255
        val bridgeAlpha = when {
            mapY in 204f..252f -> 0f
            mapY in 186f..204f -> (204f - mapY) / 18f
            mapY in 252f..270f -> (mapY - 252f) / 18f
            else -> 1f
        }
        if (bridgeAlpha <= 0.02f) return@forEach

        val screenPos = toScreen(mapX, mapY)
        val angleDeg = Math.toDegrees(atan2((p1.y - p0.y).toDouble(), (p1.x - p0.x).toDouble())).toFloat()
        val len = car.lengthUnits * scale
        val wid = car.widthUnits * scale

        rotate(degrees = angleDeg, pivot = screenPos) {
            if (isEvening) {
                val beam = Path().apply {
                    moveTo(screenPos.x + len * 0.45f, screenPos.y - wid * 0.3f)
                    lineTo(screenPos.x + len * 2.1f, screenPos.y - wid * 0.95f)
                    lineTo(screenPos.x + len * 2.1f, screenPos.y + wid * 0.95f)
                    lineTo(screenPos.x + len * 0.45f, screenPos.y + wid * 0.3f)
                    close()
                }
                drawPath(
                    path = beam,
                    color = Color(0xFFFFF59D).copy(alpha = 0.28f * bridgeAlpha)
                )
            }

            drawRoundRect(
                color = Color(0x4A000000).copy(alpha = 0.35f * bridgeAlpha),
                topLeft = Offset(screenPos.x - len / 2f + 2f * scale, screenPos.y - wid / 2f + 3f * scale),
                size = Size(len, wid),
                cornerRadius = CornerRadius(4f * scale, 4f * scale)
            )
            drawRoundRect(
                color = car.bodyColor.copy(alpha = bridgeAlpha),
                topLeft = Offset(screenPos.x - len / 2f, screenPos.y - wid / 2f),
                size = Size(len, wid),
                cornerRadius = CornerRadius(4f * scale, 4f * scale)
            )
            drawRoundRect(
                color = Color(0xFF1E293B).copy(alpha = 0.88f * bridgeAlpha),
                topLeft = Offset(screenPos.x - len * 0.16f, screenPos.y - wid * 0.34f),
                size = Size(len * 0.42f, wid * 0.68f),
                cornerRadius = CornerRadius(2.5f * scale, 2.5f * scale)
            )
            drawCircle(
                color = Color(0xFFEF4444).copy(alpha = 0.9f * bridgeAlpha),
                radius = 1.6f * scale,
                center = Offset(screenPos.x - len * 0.46f, screenPos.y - wid * 0.28f)
            )
            drawCircle(
                color = Color(0xFFEF4444).copy(alpha = 0.9f * bridgeAlpha),
                radius = 1.6f * scale,
                center = Offset(screenPos.x - len * 0.46f, screenPos.y + wid * 0.28f)
            )
        }
    }
}

/**
 * Renders atmospheric twilight lighting and glowing plaza lantern pools across the wide 896x1200 world
 * when Evening Mode is enabled.
 */
internal fun DrawScope.drawEveningAtmosphere(
    selectedFloor: Int,
    toScreen: (Float, Float) -> Offset,
    toSize: (Float, Float) -> Size,
    scale: Float
) {
    drawRect(
        color = Color(0xFF0A1428).copy(alpha = 0.40f),
        topLeft = toScreen(0f, 0f),
        size = toSize(896f, 1200f)
    )

    val pools = buildList {
        add(Triple(Offset(335f, 635f), 85f, ElAbedWarmOrange))
        add(Triple(Offset(445f, 595f), 95f, ElAbedWarmOrange))
        add(Triple(Offset(505f, 565f), 110f, Color(0xFFFFCA7A)))
        add(Triple(Offset(685f, 610f), 95f, Color(0xFFFFCC80)))
        add(Triple(Offset(765f, 965f), 120f, Color(0xFFF472B6)))
        if (selectedFloor == 0) {
            add(Triple(Offset(405f, 235f), 105f, Color(0xFFFFD54F)))
            add(Triple(Offset(535f, 335f), 85f, ElAbedRoyalBlue))
        } else {
            add(Triple(Offset(465f, 290f), 125f, Color(0xFFFFE082)))
            add(Triple(Offset(585f, 395f), 115f, Color(0xFFE040FB)))
        }
    }

    pools.forEach { (mapPt, radiusUnits, glowColor) ->
        val center = toScreen(mapPt.x, mapPt.y)
        val r = radiusUnits * scale
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor.copy(alpha = 0.34f),
                    glowColor.copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = center,
                radius = r
            ),
            radius = r,
            center = center
        )
    }
}

/**
 * Draws the glowing navigation ribbon in official Royal Blue + Turquoise + Warm Orange energy dashes.
 */
internal fun DrawScope.drawGameNavigationPath(
    selectedFloor: Int,
    avatar: AvatarState,
    routePlan: IndoorRoutePlan,
    dashPhase: Float,
    pulseScale: Float,
    toScreen: (Float, Float) -> Offset,
    scale: Float
) {
    val floorNodes = routePlan.nodes.filter { it.floor == selectedFloor }
    if (floorNodes.size >= 2) {
        val path = Path()
        val firstPt = toScreen(floorNodes.first().x, floorNodes.first().y)
        path.moveTo(firstPt.x, firstPt.y)
        for (i in 1 until floorNodes.size) {
            val pt = toScreen(floorNodes[i].x, floorNodes[i].y)
            path.lineTo(pt.x, pt.y)
        }

        if (avatar.floor == selectedFloor) {
            drawLine(
                color = ElAbedTurquoise.copy(alpha = 0.85f),
                start = toScreen(avatar.x, avatar.y),
                end = firstPt,
                strokeWidth = 5.5f * scale,
                cap = StrokeCap.Round
            )
        }

        drawPath(
            path = path,
            color = ElAbedRoyalBlue.copy(alpha = 0.34f),
            style = Stroke(width = 16f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = path,
            color = ElAbedWarmOrange.copy(alpha = 0.45f),
            style = Stroke(width = 10f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = path,
            color = Color(0xFFFFD54F),
            style = Stroke(
                width = 4.8f * scale,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(16f * scale, 10f * scale),
                    phase = -dashPhase
                )
            )
        )

        floorNodes.forEach { n ->
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = 3.2f * scale,
                center = toScreen(n.x, n.y)
            )
        }
    }

    routePlan.transitionNode?.let { tNode ->
        if (tNode.floor == selectedFloor) {
            val pt = toScreen(tNode.x, tNode.y)
            drawCircle(
                color = ElAbedWarmOrange.copy(alpha = 0.45f),
                radius = 23f * scale * pulseScale,
                center = pt
            )
            drawCircle(
                color = ElAbedWarmOrange,
                radius = 10f * scale,
                center = pt
            )
            drawCircle(
                color = Color.White,
                radius = 4f * scale,
                center = pt
            )
        }
    }

    val dest = routePlan.destinationStore
    if (dest.floor == selectedFloor) {
        val destPt = toScreen(dest.mapX, dest.mapY)
        drawCircle(
            color = ElAbedWarmOrange.copy(alpha = 0.38f),
            radius = 26f * scale * pulseScale,
            center = destPt
        )
        drawCircle(
            color = ElAbedWarmOrange,
            radius = 9f * scale,
            center = destPt
        )
        drawCircle(
            color = Color.White,
            radius = 4f * scale,
            center = destPt
        )
    }
}

/**
 * Draws sleek floating 2.5D game pins using the official Royal Blue / Warm Orange / Vibrant Green / Turquoise system.
 */
internal fun DrawScope.drawGameStorePins(
    places: List<StoreEntity>,
    palette: MapWorldPalette,
    routePlan: IndoorRoutePlan?,
    inspectedPlaceId: String?,
    zoomScale: Float,
    pulseScale: Float,
    toScreen: (Float, Float) -> Offset,
    scale: Float,
    textMeasurer: TextMeasurer
) {
    val visibleInCurrentZoom = places.filter { place ->
        val isDest = routePlan?.destinationStore?.id == place.id
        val isInspected = inspectedPlaceId == place.id
        zoomScale >= 1.22f ||
            isDest ||
            isInspected ||
            place.isFeatured ||
            place.id in overviewPriorityIds
    }

    visibleInCurrentZoom.forEach { place ->
        val isDest = routePlan?.destinationStore?.id == place.id
        val isInspected = inspectedPlaceId == place.id
        val accent = placeAccentColor(place)
        val anchor = toScreen(place.mapX, place.mapY)

        val hoverHeight = if (isDest || isInspected) 20f * scale else 14f * scale
        val pinCenter = Offset(anchor.x, anchor.y - hoverHeight)
        val tokenRadius = (if (isDest || isInspected) 12.2f else 9.5f) * scale.coerceIn(0.68f, 1.35f)

        drawCircle(
            color = palette.pinShadow,
            radius = 4.8f * scale.coerceIn(0.6f, 1.3f),
            center = Offset(anchor.x + 1.5f * scale, anchor.y + 2f * scale)
        )
        drawLine(
            color = if (isDest) ElAbedWarmOrange else Color.White.copy(alpha = 0.88f),
            start = anchor,
            end = pinCenter,
            strokeWidth = 2f * scale,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = if (isDest) ElAbedWarmOrange else accent,
            radius = 3.1f * scale,
            center = anchor
        )

        if (isDest || isInspected) {
            drawCircle(
                color = (if (isDest) ElAbedWarmOrange else accent).copy(alpha = 0.36f),
                radius = tokenRadius * 1.45f * pulseScale,
                center = pinCenter
            )
        }

        drawCircle(
            color = palette.pinShadow,
            radius = tokenRadius,
            center = Offset(pinCenter.x + 2f * scale, pinCenter.y + 3.2f * scale)
        )
        drawCircle(
            color = if (isDest) ElAbedWarmOrange else accent,
            radius = tokenRadius,
            center = pinCenter
        )
        drawCircle(
            color = Color.White,
            radius = tokenRadius,
            center = pinCenter,
            style = Stroke(width = (if (isDest) 2.4f else 1.8f) * scale)
        )

        drawPinCategoryGlyph(
            place = place,
            center = pinCenter,
            radius = tokenRadius * 0.56f
        )

        val showLabel = zoomScale >= 1.15f || isDest || isInspected || place.isFeatured || place.id == "gate_1"
        if (showLabel) {
            val label = shortStoreLabel(place.nameAr)
            val fontSizeSp = (8.2f * scale.coerceIn(0.82f, 1.25f)).sp
            val textLayout = textMeasurer.measure(
                text = label,
                style = TextStyle(
                    fontFamily = CairoFontFamily,
                    fontSize = fontSizeSp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDest) Color.White else palette.labelColor
                )
            )
            val padH = 6.5f * scale.coerceIn(0.75f, 1.25f)
            val padV = 2.6f * scale.coerceIn(0.75f, 1.25f)
            val pillW = textLayout.size.width + padH * 2f
            val pillH = textLayout.size.height + padV * 2f
            val pillTopY = anchor.y + 4.5f * scale
            val pillTL = Offset(anchor.x - pillW / 2f, pillTopY)

            drawRoundRect(
                color = palette.pinShadow,
                topLeft = Offset(pillTL.x + 1.5f * scale, pillTL.y + 2f * scale),
                size = Size(pillW, pillH),
                cornerRadius = CornerRadius(7f * scale, 7f * scale)
            )
            drawRoundRect(
                color = if (isDest) ElAbedWarmOrange else palette.labelBg,
                topLeft = pillTL,
                size = Size(pillW, pillH),
                cornerRadius = CornerRadius(7f * scale, 7f * scale)
            )
            drawRoundRect(
                color = if (isDest) Color.White.copy(alpha = 0.85f) else accent.copy(alpha = 0.72f),
                topLeft = pillTL,
                size = Size(pillW, pillH),
                cornerRadius = CornerRadius(7f * scale, 7f * scale),
                style = Stroke(width = 1.1f * scale)
            )
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(
                    anchor.x - textLayout.size.width / 2f,
                    pillTopY + padV
                )
            )
        }
    }
}

private fun DrawScope.drawPinCategoryGlyph(
    place: StoreEntity,
    center: Offset,
    radius: Float
) {
    val stroke = Stroke(width = (radius * 0.34f).coerceAtLeast(1.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)
    when {
        place.id == "escalator_central" || place.id == "elevator_east" -> {
            val path = Path().apply {
                moveTo(center.x - radius * 0.55f, center.y + radius * 0.45f)
                lineTo(center.x - radius * 0.1f, center.y + radius * 0.45f)
                lineTo(center.x - radius * 0.1f, center.y)
                lineTo(center.x + radius * 0.4f, center.y)
                lineTo(center.x + radius * 0.4f, center.y - radius * 0.45f)
            }
            drawPath(path, Color.White, style = stroke)
        }
        place.id == "gate_1" -> {
            drawArc(
                color = Color.White,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - radius * 0.55f, center.y - radius * 0.45f),
                size = Size(radius * 1.1f, radius * 1.1f),
                style = stroke
            )
            drawLine(
                color = Color.White,
                start = Offset(center.x - radius * 0.55f, center.y + radius * 0.1f),
                end = Offset(center.x - radius * 0.55f, center.y + radius * 0.55f),
                strokeWidth = stroke.width
            )
            drawLine(
                color = Color.White,
                start = Offset(center.x + radius * 0.55f, center.y + radius * 0.1f),
                end = Offset(center.x + radius * 0.55f, center.y + radius * 0.55f),
                strokeWidth = stroke.width
            )
        }
        place.category == MallPlaceCategory.DINING -> {
            drawCircle(color = Color.White, radius = radius * 0.58f, center = center, style = stroke)
            drawCircle(color = Color.White, radius = radius * 0.22f, center = center)
        }
        place.category == MallPlaceCategory.ENTERTAINMENT -> {
            val star = Path().apply {
                moveTo(center.x, center.y - radius * 0.85f)
                lineTo(center.x + radius * 0.25f, center.y - radius * 0.25f)
                lineTo(center.x + radius * 0.85f, center.y)
                lineTo(center.x + radius * 0.25f, center.y + radius * 0.25f)
                lineTo(center.x, center.y + radius * 0.85f)
                lineTo(center.x - radius * 0.25f, center.y + radius * 0.25f)
                lineTo(center.x - radius * 0.85f, center.y)
                lineTo(center.x - radius * 0.25f, center.y - radius * 0.25f)
                close()
            }
            drawPath(star, Color.White)
        }
        place.category == MallPlaceCategory.HYPERMARKET -> {
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(center.x - radius * 0.6f, center.y - radius * 0.4f),
                size = Size(radius * 1.2f, radius * 0.9f),
                cornerRadius = CornerRadius(radius * 0.25f, radius * 0.25f),
                style = stroke
            )
        }
        else -> {
            val diamond = Path().apply {
                moveTo(center.x, center.y - radius * 0.68f)
                lineTo(center.x + radius * 0.68f, center.y)
                lineTo(center.x, center.y + radius * 0.68f)
                lineTo(center.x - radius * 0.68f, center.y)
                close()
            }
            drawPath(diamond, Color.White)
        }
    }
}

internal fun DrawScope.drawTopDownWalkerAvatar(
    avatar: AvatarState,
    walkPhase: Float,
    pulseScale: Float,
    toScreen: (Float, Float) -> Offset,
    scale: Float
) {
    val center = toScreen(avatar.x, avatar.y)

    // 1. Official Brand-Colored Pulsing Locator Halo
    drawCircle(
        color = ElAbedTurquoise.copy(alpha = 0.28f),
        radius = 24f * scale * pulseScale,
        center = center
    )
    drawCircle(
        color = ElAbedWarmOrange.copy(alpha = 0.9f),
        radius = 17f * scale,
        center = center,
        style = Stroke(width = 2.2f * scale)
    )

    // 2. Soft ground shadow
    drawCircle(
        color = Color(0x55000000),
        radius = 13.5f * scale,
        center = Offset(center.x + 2.5f * scale, center.y + 4f * scale)
    )

    // 3. Articulated Top-Down Character rotated by headingDegrees
    val strideOffset = if (avatar.isMoving) sin(walkPhase * 2.0 * PI).toFloat() * 5.8f * scale else 0f

    rotate(degrees = avatar.headingDegrees, pivot = center) {
        val cone = Path().apply {
            moveTo(center.x + 34f * scale, center.y)
            lineTo(center.x + 8f * scale, center.y - 14f * scale)
            lineTo(center.x + 8f * scale, center.y + 14f * scale)
            close()
        }
        drawPath(path = cone, color = ElAbedTurquoise.copy(alpha = 0.36f))

        drawCircle(
            color = Color(0xFF1E293B),
            radius = 3.9f * scale,
            center = Offset(center.x + strideOffset, center.y - 6.2f * scale)
        )
        drawCircle(
            color = Color(0xFF1E293B),
            radius = 3.9f * scale,
            center = Offset(center.x - strideOffset, center.y + 6.2f * scale)
        )

        drawCircle(
            color = Color(0xFFE6A16B),
            radius = 3.5f * scale,
            center = Offset(center.x - strideOffset * 0.8f, center.y - 10.8f * scale)
        )
        drawCircle(
            color = Color(0xFFE6A16B),
            radius = 3.5f * scale,
            center = Offset(center.x + strideOffset * 0.8f, center.y + 10.8f * scale)
        )

        // Orange Explorer Backpack
        drawRoundRect(
            color = ElAbedWarmOrange,
            topLeft = Offset(center.x - 10.5f * scale, center.y - 7f * scale),
            size = Size(5.5f * scale, 14f * scale),
            cornerRadius = CornerRadius(3f * scale, 3f * scale)
        )

        // Royal Blue Jacket with Green-Turquoise trim
        drawRoundRect(
            color = ElAbedRoyalBlue,
            topLeft = Offset(center.x - 7.5f * scale, center.y - 10.5f * scale),
            size = Size(15f * scale, 21f * scale),
            cornerRadius = CornerRadius(7.5f * scale, 7.5f * scale)
        )
        drawRoundRect(
            color = ElAbedTurquoise,
            topLeft = Offset(center.x - 7.5f * scale, center.y - 10.5f * scale),
            size = Size(15f * scale, 21f * scale),
            cornerRadius = CornerRadius(7.5f * scale, 7.5f * scale),
            style = Stroke(width = 1.8f * scale)
        )

        // Head & cap
        drawCircle(
            color = Color(0xFF1A2226),
            radius = 6.4f * scale,
            center = Offset(center.x + 1.5f * scale, center.y)
        )
        drawCircle(
            color = ElAbedVibrantGreen,
            radius = 2.8f * scale,
            center = Offset(center.x + 4.8f * scale, center.y)
        )
    }
}

internal fun placeAccentColor(place: StoreEntity): Color {
    return when {
        place.id == "escalator_central" || place.id == "elevator_east" -> ElAbedWarmOrange
        place.id == "fountain_plaza" -> ElAbedTurquoise
        place.category == MallPlaceCategory.FACILITY -> Color(0xFF475569)
        place.category == MallPlaceCategory.HYPERMARKET -> ElAbedRoyalBlue
        place.category == MallPlaceCategory.DINING -> ElAbedWarmOrange
        place.category == MallPlaceCategory.ENTERTAINMENT -> ElAbedVibrantGreen
        place.category == MallPlaceCategory.ELECTRONICS -> ElAbedRoyalBlue
        place.category == MallPlaceCategory.FURNITURE -> Color(0xFF8D5524)
        else -> ElAbedTurquoise
    }
}

private fun shortStoreLabel(nameAr: String): String {
    return nameAr
        .replace(" ومفروشات العابد", "")
        .replace(" (مدينة ملاهي)", "")
        .replace(" بمول العابد", "")
        .replace(" (الدور الأرضي)", "")
        .replace(" (الأرضي)", "")
        .replace(" (الأول)", "")
        .replace(" (المدخل الرئيسي)", "")
        .replace(" (الشرقي)", "")
        .replace(" - مول العابد", "")
        .take(18)
}
