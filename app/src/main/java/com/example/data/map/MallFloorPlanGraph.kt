package com.example.data.map

import com.example.data.model.IndoorRoutePlan
import com.example.data.model.NavEdge
import com.example.data.model.NavNode
import com.example.data.model.NavNodeType
import com.example.data.model.RouteGuidanceStep
import com.example.data.model.StoreEntity
import java.util.PriorityQueue
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * 896 x 1200 (3:4) wide uncropped world coordinate system & multi-floor pathfinding graph
 * aligned 1:1 with the real aerial layout and 2.5D game map of El Abed Mall (Toukh).
 */
object MallFloorPlanGraph {
    const val MAP_WIDTH = 896f
    const val MAP_HEIGHT = 1200f

    const val MALL_CENTER_LAT = 30.389391380976612
    const val MALL_CENTER_LNG = 31.19232959902437
    const val MALL_GOOGLE_MAPS_URL = "https://maps.app.goo.gl/P4fr3VoyMxJupns2A"

    const val METERS_PER_UNIT = 0.21f

    val nodes: List<NavNode> = listOf(
        // --- Ground Floor (0) Network ---
        NavNode("g_gate1", 335f, 635f, 0, NavNodeType.GATE, "بوابة رقم ١", "gate_1"),
        NavNode("g_alex_bank", 305f, 605f, 0, NavNodeType.POI, "بنك الإسكندرية", "alex_bank"),
        NavNode("g_banque_misr", 285f, 520f, 0, NavNodeType.POI, "بنك مصر", "banque_misr"),
        NavNode("g_parking", 285f, 345f, 0, NavNodeType.POI, "موقف السيارات", "parking_west"),
        NavNode("g_west_plaza", 355f, 610f, 0, NavNodeType.CORRIDOR, "ساحة المدخل الغربي"),
        NavNode("g_abk_atm", 365f, 535f, 0, NavNodeType.POI, "البنك الأهلي الكويتي ABK", "abk_atm"),
        NavNode("g_vodafone", 395f, 675f, 0, NavNodeType.POI, "فودافون", "vodafone"),
        NavNode("g_kfc", 395f, 585f, 0, NavNodeType.POI, "كنتاكي KFC", "kfc_toukh"),
        NavNode("g_pizza", 465f, 620f, 0, NavNodeType.POI, "بيزا بريمو", "pizza_primo"),
        NavNode("g_cust_svc", 490f, 700f, 0, NavNodeType.POI, "خدمة العملاء", "customer_service"),
        NavNode("g_fountain", 505f, 560f, 0, NavNodeType.POI, "الساحة الوسطى المفتوحة", "fountain_plaza"),
        NavNode("g_nw_walk", 395f, 430f, 0, NavNodeType.CORRIDOR, "ممر الجناح الشمالي"),
        NavNode("g_aaib", 415f, 355f, 0, NavNodeType.POI, "البنك العربي الأفريقي", "aaib_atm"),
        NavNode("g_hyper", 405f, 235f, 0, NavNodeType.POI, "هايبر ماركت العابد", "hypermarket"),
        NavNode("g_samsung", 535f, 335f, 0, NavNodeType.POI, "توكيل سامسونج", "samsung"),
        NavNode("g_faisal", 595f, 430f, 0, NavNodeType.POI, "بنك فيصل الإسلامي", "faisal_bank"),
        NavNode("g_escalator_0", 495f, 455f, 0, NavNodeType.VERTICAL_TRANSIT, "السلم المتحرك الرئيسي", "escalator_central"),
        NavNode("g_tecno", 565f, 545f, 0, NavNodeType.POI, "توكيل تكنو", "tecno_store"),
        NavNode("g_united", 575f, 740f, 0, NavNodeType.POI, "الشركة المتحدة", "united_electronics"),
        NavNode("g_east_entry", 605f, 615f, 0, NavNodeType.CORRIDOR, "مدخل الرواق الشرقي"),
        NavNode("g_elevator_0", 610f, 520f, 0, NavNodeType.VERTICAL_TRANSIT, "المصعد الكهربائي", "elevator_east"),
        NavNode("g_elabed_cafe", 645f, 645f, 0, NavNodeType.POI, "العابد كافيه", "elabed_cafe"),
        NavNode("g_pingo", 710f, 535f, 0, NavNodeType.POI, "بينجو العابد", "pingo_toys"),
        NavNode("g_cairo_bank", 785f, 615f, 0, NavNodeType.POI, "بنك القاهرة", "banque_du_caire"),
        NavNode("g_restrooms_0", 835f, 570f, 0, NavNodeType.POI, "دورات المياه (الأرضي)", "restrooms_g"),
        NavNode("g_hadouta", 765f, 735f, 0, NavNodeType.POI, "كافيه حدوتة مصرية", "hadouta_masreya"),
        NavNode("g_tutu", 765f, 965f, 0, NavNodeType.POI, "توتو كيدز", "tutu_kids"),

        // --- First Floor (1) Network ---
        NavNode("f1_escalator_1", 495f, 455f, 1, NavNodeType.VERTICAL_TRANSIT, "السلم المتحرك (الدور الأول)", "escalator_central"),
        NavNode("f1_nw_corridor", 480f, 365f, 1, NavNodeType.CORRIDOR, "ممر الأثاث العلوي"),
        NavNode("f1_furniture", 465f, 290f, 1, NavNodeType.POI, "أثاث للبيت", "furniture_home"),
        NavNode("f1_cafe_terrace", 440f, 540f, 1, NavNodeType.POI, "تراس الكافيهات", "cafe_terrace"),
        NavNode("f1_elevator_1", 610f, 520f, 1, NavNodeType.VERTICAL_TRANSIT, "المصعد الكهربائي (الأول)", "elevator_east"),
        NavNode("f1_kids_rides", 585f, 395f, 1, NavNodeType.POI, "منطقة ألعاب الأطفال", "kids_rides"),
        NavNode("f1_east_arcade", 685f, 635f, 1, NavNodeType.CORRIDOR, "القوس الشرقي العلوي"),
        NavNode("f1_kids_fashion", 595f, 735f, 1, NavNodeType.POI, "عالم الطفل", "kids_fashion"),
        NavNode("f1_perfumes", 760f, 645f, 1, NavNodeType.POI, "دار العطور والهدايا", "perfumes_oud"),
        NavNode("f1_restrooms_1", 830f, 570f, 1, NavNodeType.POI, "دورات المياه والمصلى", "restrooms_1")
    )

    private val rawConnections = listOf(
        // Ground Floor
        "g_gate1" to "g_alex_bank",
        "g_gate1" to "g_west_plaza",
        "g_alex_bank" to "g_banque_misr",
        "g_alex_bank" to "g_west_plaza",
        "g_banque_misr" to "g_parking",
        "g_banque_misr" to "g_abk_atm",
        "g_parking" to "g_nw_walk",
        "g_parking" to "g_aaib",
        "g_west_plaza" to "g_abk_atm",
        "g_west_plaza" to "g_vodafone",
        "g_abk_atm" to "g_kfc",
        "g_vodafone" to "g_kfc",
        "g_vodafone" to "g_cust_svc",
        "g_kfc" to "g_pizza",
        "g_kfc" to "g_nw_walk",
        "g_pizza" to "g_fountain",
        "g_pizza" to "g_cust_svc",
        "g_cust_svc" to "g_united",
        "g_cust_svc" to "g_east_entry",
        "g_fountain" to "g_escalator_0",
        "g_fountain" to "g_tecno",
        "g_fountain" to "g_east_entry",
        "g_nw_walk" to "g_aaib",
        "g_nw_walk" to "g_escalator_0",
        "g_aaib" to "g_hyper",
        "g_hyper" to "g_samsung",
        "g_samsung" to "g_escalator_0",
        "g_samsung" to "g_faisal",
        "g_faisal" to "g_escalator_0",
        "g_faisal" to "g_elevator_0",
        "g_tecno" to "g_escalator_0",
        "g_tecno" to "g_elevator_0",
        "g_tecno" to "g_east_entry",
        "g_united" to "g_east_entry",
        "g_united" to "g_hadouta",
        "g_east_entry" to "g_elabed_cafe",
        "g_east_entry" to "g_elevator_0",
        "g_elevator_0" to "g_pingo",
        "g_elevator_0" to "g_elabed_cafe",
        "g_pingo" to "g_cairo_bank",
        "g_elabed_cafe" to "g_cairo_bank",
        "g_cairo_bank" to "g_restrooms_0",
        "g_cairo_bank" to "g_hadouta",
        "g_hadouta" to "g_tutu",
        "g_restrooms_0" to "g_hadouta",

        // First Floor
        "f1_escalator_1" to "f1_nw_corridor",
        "f1_nw_corridor" to "f1_furniture",
        "f1_escalator_1" to "f1_cafe_terrace",
        "f1_escalator_1" to "f1_elevator_1",
        "f1_escalator_1" to "f1_kids_rides",
        "f1_cafe_terrace" to "f1_elevator_1",
        "f1_elevator_1" to "f1_kids_rides",
        "f1_elevator_1" to "f1_east_arcade",
        "f1_kids_rides" to "f1_restrooms_1",
        "f1_east_arcade" to "f1_kids_fashion",
        "f1_east_arcade" to "f1_perfumes",
        "f1_perfumes" to "f1_restrooms_1"
    )

    private val verticalConnections = listOf(
        "g_escalator_0" to "f1_escalator_1",
        "g_elevator_0" to "f1_elevator_1"
    )

    val edges: List<NavEdge> by lazy {
        val nodeMap = nodes.associateBy { it.id }
        val built = mutableListOf<NavEdge>()
        rawConnections.forEach { (a, b) ->
            val na = nodeMap[a] ?: return@forEach
            val nb = nodeMap[b] ?: return@forEach
            val w = hypot(na.x - nb.x, na.y - nb.y)
            built.add(NavEdge(a, b, w, false))
            built.add(NavEdge(b, a, w, false))
        }
        verticalConnections.forEach { (a, b) ->
            val cost = 55f
            built.add(NavEdge(a, b, cost, true))
            built.add(NavEdge(b, a, cost, true))
        }
        built
    }

    fun nearestNode(x: Float, y: Float, floor: Int): NavNode {
        return nodes
            .filter { it.floor == floor }
            .minBy { hypot(it.x - x, it.y - y) }
    }

    fun nodeForPlace(place: StoreEntity): NavNode {
        return nodes.firstOrNull { it.linkedPlaceId == place.id && it.floor == place.floor }
            ?: nearestNode(place.mapX, place.mapY, place.floor)
    }

    fun computeRoute(
        startX: Float,
        startY: Float,
        startFloor: Int,
        destination: StoreEntity
    ): IndoorRoutePlan {
        val startNode = nearestNode(startX, startY, startFloor)
        val targetNode = nodeForPlace(destination)

        if (startNode.id == targetNode.id) {
            val distM = (hypot(destination.mapX - startX, destination.mapY - startY) * METERS_PER_UNIT).roundToInt()
            return IndoorRoutePlan(
                nodes = listOf(startNode),
                steps = listOf(
                    RouteGuidanceStep(
                        instructionAr = "أنت قريب جداً من ${destination.nameAr}",
                        distanceMeters = distM,
                        floor = startFloor,
                        nodeId = startNode.id
                    )
                ),
                totalDistanceMeters = distM,
                requiresFloorChange = false,
                transitionNode = null,
                destinationStore = destination
            )
        }

        val adjacency = edges.groupBy { it.fromId }
        val dist = mutableMapOf<String, Float>().withDefault { Float.POSITIVE_INFINITY }
        val prev = mutableMapOf<String, String>()
        val pq = PriorityQueue<Pair<String, Float>>(compareBy { it.second })

        dist[startNode.id] = 0f
        pq.add(startNode.id to 0f)

        while (pq.isNotEmpty()) {
            val (currentId, currentDist) = pq.poll() ?: break
            if (currentId == targetNode.id) break
            if (currentDist > dist.getValue(currentId)) continue

            adjacency[currentId].orEmpty().forEach { edge ->
                val nextDist = currentDist + edge.weight
                if (nextDist < dist.getValue(edge.toId)) {
                    dist[edge.toId] = nextDist
                    prev[edge.toId] = currentId
                    pq.add(edge.toId to nextDist)
                }
            }
        }

        val pathIds = mutableListOf<String>()
        var cursor: String? = targetNode.id
        while (cursor != null) {
            pathIds.add(cursor)
            cursor = prev[cursor]
        }
        pathIds.reverse()

        val nodeMap = nodes.associateBy { it.id }
        val pathNodes = pathIds.mapNotNull { nodeMap[it] }
        val transitionIndex = pathNodes.zipWithNext().indexOfFirst { (a, b) -> a.floor != b.floor }
        val transitionNode = if (transitionIndex >= 0) pathNodes[transitionIndex] else null

        val steps = buildGuidanceSteps(pathNodes, destination)
        val rawLength = pathNodes.zipWithNext().sumOf { (a, b) ->
            if (a.floor != b.floor) 18.0 else hypot(a.x - b.x, a.y - b.y).toDouble()
        }.toFloat()
        val totalMeters = (rawLength * METERS_PER_UNIT).roundToInt().coerceAtLeast(3)

        return IndoorRoutePlan(
            nodes = pathNodes,
            steps = steps,
            totalDistanceMeters = totalMeters,
            requiresFloorChange = transitionNode != null,
            transitionNode = transitionNode,
            destinationStore = destination
        )
    }

    private fun buildGuidanceSteps(
        pathNodes: List<NavNode>,
        destination: StoreEntity
    ): List<RouteGuidanceStep> {
        if (pathNodes.isEmpty()) return emptyList()
        if (pathNodes.size == 1) {
            return listOf(
                RouteGuidanceStep(
                    instructionAr = "اتجه إلى ${destination.nameAr}",
                    distanceMeters = 4,
                    floor = destination.floor,
                    nodeId = pathNodes.first().id
                )
            )
        }

        val steps = mutableListOf<RouteGuidanceStep>()
        for (i in 0 until pathNodes.lastIndex) {
            val from = pathNodes[i]
            val to = pathNodes[i + 1]
            if (from.floor != to.floor) {
                val action = if (to.floor > from.floor) "اصعد" else "انزل"
                val floorLabel = if (to.floor == 1) "الدور الأول" else "الدور الأرضي"
                steps.add(
                    RouteGuidanceStep(
                        instructionAr = "$action عبر ${from.labelAr} إلى $floorLabel",
                        distanceMeters = 5,
                        floor = from.floor,
                        isTransitionStep = true,
                        targetFloor = to.floor,
                        nodeId = from.id
                    )
                )
            } else {
                val distM = (hypot(to.x - from.x, to.y - from.y) * METERS_PER_UNIT)
                    .roundToInt()
                    .coerceAtLeast(3)
                val dirLabel = compassDirectionAr(from.x, from.y, to.x, to.y)
                val targetName = if (i + 1 == pathNodes.lastIndex) destination.nameAr else to.labelAr
                steps.add(
                    RouteGuidanceStep(
                        instructionAr = "اتجه $dirLabel نحو $targetName",
                        distanceMeters = distM,
                        floor = from.floor,
                        nodeId = to.id
                    )
                )
            }
        }
        return steps
    }

    private fun compassDirectionAr(x1: Float, y1: Float, x2: Float, y2: Float): String {
        val dx = x2 - x1
        val dy = y2 - y1
        val angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
        return when {
            angleDeg in -35.0..35.0 -> "شرقاً"
            angleDeg in 35.0..145.0 -> "جنوباً"
            angleDeg in -145.0..-35.0 -> "شمالاً"
            else -> "غرباً"
        }
    }

    fun geoToMallCoords(lat: Double, lng: Double): Pair<Pair<Float, Float>, Boolean> {
        val latMeters = (lat - MALL_CENTER_LAT) * 111_132.0
        val lngMeters = (lng - MALL_CENTER_LNG) * 111_320.0 * cos(Math.toRadians(MALL_CENTER_LAT))
        val distanceFromCenter = hypot(latMeters, lngMeters)
        val isInside = distanceFromCenter <= 210.0

        val mappedX = (485.0 + (lngMeters / METERS_PER_UNIT)).toFloat().coerceIn(210f, 845f)
        val mappedY = (555.0 - (latMeters / METERS_PER_UNIT)).toFloat().coerceIn(170f, 1030f)
        return (mappedX to mappedY) to isInside
    }

    fun clampToWalkableArea(x: Float, y: Float): Pair<Float, Float> {
        val clampedX = x.coerceIn(210f, 845f)
        val clampedY = y.coerceIn(170f, 1030f)
        return clampedX to clampedY
    }

    fun calculateHeadingDegrees(dx: Float, dy: Float): Float {
        if (abs(dx) < 0.01f && abs(dy) < 0.01f) return 0f
        return Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    }
}
