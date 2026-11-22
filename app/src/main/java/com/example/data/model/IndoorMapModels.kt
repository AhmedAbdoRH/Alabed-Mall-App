package com.example.data.model

enum class NavNodeType {
    CORRIDOR,
    GATE,
    VERTICAL_TRANSIT,
    POI
}

data class NavNode(
    val id: String,
    val x: Float,
    val y: Float,
    val floor: Int,
    val type: NavNodeType,
    val labelAr: String,
    val linkedPlaceId: String? = null
)

data class NavEdge(
    val fromId: String,
    val toId: String,
    val weight: Float,
    val isVerticalTransition: Boolean = false
)

enum class MapMovementMode(val labelAr: String, val subtitleAr: String) {
    MANUAL_EXPLORE(
        labelAr = "استكشاف يدوي",
        subtitleAr = "تحريك الشخصية بعصا التحكم داخل المحاكي"
    ),
    LIVE_GUIDANCE(
        labelAr = "توجيه فعلي (GPS)",
        subtitleAr = "تحديث موقع الشخصية حسب موقع الهاتف"
    )
}

data class AvatarState(
    val x: Float = 335f,
    val y: Float = 635f,
    val floor: Int = 0,
    val headingDegrees: Float = 315f,
    val isMoving: Boolean = false
)

data class RouteGuidanceStep(
    val instructionAr: String,
    val distanceMeters: Int,
    val floor: Int,
    val isTransitionStep: Boolean = false,
    val targetFloor: Int? = null,
    val nodeId: String
)

data class IndoorRoutePlan(
    val nodes: List<NavNode>,
    val steps: List<RouteGuidanceStep>,
    val totalDistanceMeters: Int,
    val requiresFloorChange: Boolean,
    val transitionNode: NavNode?,
    val destinationStore: StoreEntity
)

data class LiveGpsStatus(
    val hasPermission: Boolean = false,
    val isReceivingSignal: Boolean = false,
    val accuracyMeters: Float? = null,
    val isInsideMallBounds: Boolean = false,
    val statusMessageAr: String = "في انتظار تفعيل خدمة الموقع"
)
