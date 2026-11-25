package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MallDatabase
import com.example.data.local.NotificationPreferences
import com.example.data.local.NotificationPreferencesManager
import com.example.data.map.MallFloorPlanGraph
import com.example.data.model.AvatarState
import com.example.data.model.EventEntity
import com.example.data.model.IndoorRoutePlan
import com.example.data.model.LiveGpsStatus
import com.example.data.model.MallNotificationEntity
import com.example.data.model.MallPlaceCategory
import com.example.data.model.MapMovementMode
import com.example.data.model.NotificationCategory
import com.example.data.model.OfferEntity
import com.example.data.model.PromoBannerConfig
import com.example.data.model.ShoppingTaskEntity
import com.example.data.model.StoreEntity
import com.example.data.model.TripStopEntity
import com.example.data.model.VisitorTicketEntity
import com.example.data.repository.MallRepository
import com.example.location.LocationStreamEvent
import com.example.location.MallLocationClient
import com.example.notifications.MallNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.hypot
import kotlin.math.roundToInt

data class OrderedTripStop(
    val stop: TripStopEntity,
    val orderNumber: Int,
    val stepDistanceMeters: Int
)

data class IndoorMapUiState(
    val selectedFloor: Int = 0,
    val movementMode: MapMovementMode = MapMovementMode.MANUAL_EXPLORE,
    val avatar: AvatarState = AvatarState(),
    val selectedDestinationId: String? = null,
    val inspectedPlaceId: String? = null,
    val routePlan: IndoorRoutePlan? = null,
    val hasArrived: Boolean = false,
    val liveGpsStatus: LiveGpsStatus = LiveGpsStatus(),
    val floorTransitionPrompt: String? = null
)

class MallViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MallDatabase.getInstance(application)
    private val prefsManager = NotificationPreferencesManager(application)
    private val repository = MallRepository(database.mallDao(), prefsManager)
    private val locationClient = MallLocationClient(application)

    val places: StateFlow<List<StoreEntity>> = repository.places
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offers: StateFlow<List<OfferEntity>> = repository.offers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val events: StateFlow<List<EventEntity>> = repository.events
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<MallNotificationEntity>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tickets: StateFlow<List<VisitorTicketEntity>> = repository.tickets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tripStops: StateFlow<List<TripStopEntity>> = repository.tripStops
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orderedTripStops: StateFlow<List<OrderedTripStop>> = tripStops
        .combine(places) { stops, _ ->
            sortTripStopsByFloorAndPath(stops)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shoppingTasks: StateFlow<List<ShoppingTaskEntity>> = repository.shoppingTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val promoBanner: StateFlow<PromoBannerConfig> = repository.promoBannerConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PromoBannerConfig())

    val notificationPreferences: StateFlow<NotificationPreferences> = repository.notificationPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationPreferences())

    val interestMatchedOffers: StateFlow<List<OfferEntity>> = combine(
        offers,
        places,
        notificationPreferences
    ) { offerList, placeList, prefs ->
        val selected = prefs.selectedInterests
        if (selected.isEmpty()) {
            offerList
        } else {
            val placeCategoryMap = placeList.associate { it.id to it.categoryName }
            offerList.sortedByDescending { offer ->
                val cat = placeCategoryMap[offer.storeId]
                cat != null && cat in selected
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationCount: StateFlow<Int> = notifications
        .combine(notificationPreferences) { list, prefs ->
            list.count { !it.isRead && isCategoryEnabled(it.category, prefs) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _mapState = MutableStateFlow(
        IndoorMapUiState(
            liveGpsStatus = LiveGpsStatus(
                hasPermission = locationClient.hasLocationPermission(),
                statusMessageAr = if (locationClient.hasLocationPermission()) {
                    "إذن الموقع متاح — فعّل وضع التوجيه الفعلي للبدء"
                } else {
                    "يتطلب وضع التوجيه الفعلي إذن الموقع من الجهاز"
                }
            )
        )
    )
    val mapState: StateFlow<IndoorMapUiState> = _mapState.asStateFlow()

    private var locationCollectionJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
        viewModelScope.launch {
            places.collectLatest { placeList ->
                val destId = _mapState.value.selectedDestinationId
                if (destId != null) {
                    val target = placeList.firstOrNull { it.id == destId }
                    if (target != null) {
                        recalculateRoute(target)
                    }
                }
            }
        }
    }

    private fun isCategoryEnabled(category: NotificationCategory, prefs: NotificationPreferences): Boolean {
        return when (category) {
            NotificationCategory.OFFER -> prefs.offersEnabled
            NotificationCategory.OPENING -> prefs.openingsEnabled
            NotificationCategory.EVENT -> prefs.eventsEnabled
            NotificationCategory.ALERT -> prefs.alertsEnabled
        }
    }

    fun selectFloor(floor: Int) {
        _mapState.update { it.copy(selectedFloor = floor.coerceIn(0, 1)) }
    }

    fun inspectPlace(placeId: String?) {
        _mapState.update { it.copy(inspectedPlaceId = placeId) }
    }

    fun startNavigationToPlace(placeId: String) {
        val target = places.value.firstOrNull { it.id == placeId } ?: return
        _mapState.update { state ->
            state.copy(
                selectedDestinationId = target.id,
                inspectedPlaceId = null,
                hasArrived = false,
                selectedFloor = state.avatar.floor
            )
        }
        recalculateRoute(target)
    }

    fun clearNavigation() {
        _mapState.update {
            it.copy(
                selectedDestinationId = null,
                routePlan = null,
                hasArrived = false,
                floorTransitionPrompt = null
            )
        }
    }

    fun resetAvatarToGate1() {
        _mapState.update {
            it.copy(
                avatar = AvatarState(x = 335f, y = 635f, floor = 0, headingDegrees = 315f, isMoving = false),
                selectedFloor = 0,
                hasArrived = false
            )
        }
        _mapState.value.selectedDestinationId?.let { id ->
            places.value.firstOrNull { it.id == id }?.let { recalculateRoute(it) }
        }
    }

    fun switchMovementMode(mode: MapMovementMode) {
        _mapState.update { it.copy(movementMode = mode) }
        viewModelScope.launch {
            repository.setDefaultMapMode(mode)
        }
        if (mode == MapMovementMode.LIVE_GUIDANCE) {
            startLiveGpsTracking()
        } else {
            stopLiveGpsTracking()
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _mapState.update { state ->
            state.copy(
                liveGpsStatus = state.liveGpsStatus.copy(
                    hasPermission = granted,
                    statusMessageAr = if (granted) {
                        "جاري استقبال إشارة الموقع عبر Google Play services..."
                    } else {
                        "تم رفض إذن الموقع — يمكنك الاستمرار بوضع الاستكشاف اليدوي"
                    }
                )
            )
        }
        if (granted && _mapState.value.movementMode == MapMovementMode.LIVE_GUIDANCE) {
            startLiveGpsTracking()
        }
    }

    private fun startLiveGpsTracking() {
        locationCollectionJob?.cancel()
        val hasPerm = locationClient.hasLocationPermission()
        if (!hasPerm) {
            _mapState.update {
                it.copy(
                    liveGpsStatus = LiveGpsStatus(
                        hasPermission = false,
                        statusMessageAr = "يرجى السماح بالوصول للموقع لتفعيل التوجيه الفعلي"
                    )
                )
            }
            return
        }

        locationCollectionJob = viewModelScope.launch {
            locationClient.observeLocationUpdates().collectLatest { event ->
                when (event) {
                    LocationStreamEvent.PermissionMissing -> {
                        _mapState.update {
                            it.copy(
                                liveGpsStatus = LiveGpsStatus(
                                    hasPermission = false,
                                    statusMessageAr = "إذن الموقع غير مفعل"
                                )
                            )
                        }
                    }

                    LocationStreamEvent.WaitingForSignal -> {
                        _mapState.update {
                            it.copy(
                                liveGpsStatus = it.liveGpsStatus.copy(
                                    hasPermission = true,
                                    isReceivingSignal = false,
                                    statusMessageAr = "جاري تحديد موقعك داخل المول..."
                                )
                            )
                        }
                    }

                    is LocationStreamEvent.LocationUpdated -> {
                        val loc = event.location
                        val (coords, isInside) = MallFloorPlanGraph.geoToMallCoords(
                            loc.latitude,
                            loc.longitude
                        )
                        val accuracy = if (loc.hasAccuracy()) loc.accuracy else null
                        val currentAvatar = _mapState.value.avatar

                        val nextX = if (isInside) coords.first else currentAvatar.x
                        val nextY = if (isInside) coords.second else currentAvatar.y
                        val heading = if (loc.hasBearing()) {
                            loc.bearing - 90f
                        } else {
                            currentAvatar.headingDegrees
                        }

                        val statusText = when {
                            !isInside -> "أنت خارج نطاق المول حالياً — استخدم الاستكشاف اليدوي للمعاينة"
                            accuracy != null && accuracy > 15f -> "تم رصد الموقع (دقة ±${accuracy.toInt()} م — قد تقل الدقة داخلياً)"
                            accuracy != null -> "التوجيه الفعلي نشط (دقة ±${accuracy.toInt()} م)"
                            else -> "التوجيه الفعلي نشط"
                        }

                        _mapState.update {
                            it.copy(
                                avatar = currentAvatar.copy(
                                    x = nextX,
                                    y = nextY,
                                    headingDegrees = heading,
                                    isMoving = false
                                ),
                                liveGpsStatus = LiveGpsStatus(
                                    hasPermission = true,
                                    isReceivingSignal = true,
                                    accuracyMeters = accuracy,
                                    isInsideMallBounds = isInside,
                                    statusMessageAr = statusText
                                )
                            )
                        }
                        refreshRouteProgress()
                    }

                    is LocationStreamEvent.Error -> {
                        _mapState.update {
                            it.copy(
                                liveGpsStatus = it.liveGpsStatus.copy(
                                    isReceivingSignal = false,
                                    statusMessageAr = "تعذر قراءة الموقع: ${event.message}"
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun stopLiveGpsTracking() {
        locationCollectionJob?.cancel()
        locationCollectionJob = null
    }

    /**
     * Moves avatar by joystick vector (jx, jy in -1f..1f) during Manual Explore mode.
     */
    fun moveAvatarByJoystick(jx: Float, jy: Float, deltaSeconds: Float) {
        if (_mapState.value.movementMode != MapMovementMode.MANUAL_EXPLORE) return
        val mag = hypot(jx, jy)
        if (mag < 0.08f) {
            if (_mapState.value.avatar.isMoving) {
                _mapState.update { it.copy(avatar = it.avatar.copy(isMoving = false)) }
            }
            return
        }

        val speedUnitsPerSec = 140f // ~25m/s simulated fast walk inside mall map
        val current = _mapState.value.avatar
        val (clampedX, clampedY) = MallFloorPlanGraph.clampToWalkableArea(
            current.x + jx * speedUnitsPerSec * deltaSeconds,
            current.y + jy * speedUnitsPerSec * deltaSeconds
        )
        val heading = MallFloorPlanGraph.calculateHeadingDegrees(jx, jy)

        _mapState.update {
            it.copy(
                avatar = current.copy(
                    x = clampedX,
                    y = clampedY,
                    headingDegrees = heading,
                    isMoving = true
                )
            )
        }
        refreshRouteProgress()
    }

    fun switchAvatarFloor(newFloor: Int) {
        val targetFloor = newFloor.coerceIn(0, 1)
        _mapState.update {
            it.copy(
                avatar = it.avatar.copy(floor = targetFloor),
                selectedFloor = targetFloor,
                floorTransitionPrompt = null
            )
        }
        refreshRouteProgress(forceRecompute = true)
    }

    private fun recalculateRoute(destination: StoreEntity) {
        val av = _mapState.value.avatar
        val plan = MallFloorPlanGraph.computeRoute(av.x, av.y, av.floor, destination)
        val transitionPrompt = checkVerticalTransitPrompt(av, plan)
        val arrived = isArrivedAtDestination(av, destination)
        _mapState.update {
            it.copy(
                routePlan = plan,
                floorTransitionPrompt = transitionPrompt,
                hasArrived = arrived
            )
        }
    }

    private fun refreshRouteProgress(forceRecompute: Boolean = false) {
        val state = _mapState.value
        val destId = state.selectedDestinationId ?: return
        val dest = places.value.firstOrNull { it.id == destId } ?: return
        val av = state.avatar

        if (isArrivedAtDestination(av, dest)) {
            _mapState.update {
                it.copy(
                    hasArrived = true,
                    floorTransitionPrompt = null
                )
            }
            return
        }

        val plan = if (forceRecompute || state.routePlan == null) {
            MallFloorPlanGraph.computeRoute(av.x, av.y, av.floor, dest)
        } else {
            MallFloorPlanGraph.computeRoute(av.x, av.y, av.floor, dest)
        }
        val prompt = checkVerticalTransitPrompt(av, plan)
        _mapState.update {
            it.copy(
                routePlan = plan,
                hasArrived = false,
                floorTransitionPrompt = prompt
            )
        }
    }

    private fun checkVerticalTransitPrompt(
        avatar: AvatarState,
        plan: IndoorRoutePlan
    ): String? {
        if (avatar.floor == plan.destinationStore.floor) return null
        val tNode = plan.transitionNode ?: return null
        val dist = hypot(avatar.x - tNode.x, avatar.y - tNode.y)
        return if (dist <= 48f) {
            val dir = if (plan.destinationStore.floor > avatar.floor) "الصعود إلى الدور الأول" else "النزول إلى الدور الأرضي"
            "وصلت عند ${tNode.labelAr} — اضغط للـ$dir"
        } else null
    }

    private fun isArrivedAtDestination(avatar: AvatarState, destination: StoreEntity): Boolean {
        if (avatar.floor != destination.floor) return false
        return hypot(avatar.x - destination.mapX, avatar.y - destination.mapY) <= 42f
    }

    // --- Notifications & Preferences ---
    fun markNotificationRead(id: String) {
        viewModelScope.launch { repository.markNotificationRead(id) }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch { repository.markAllNotificationsRead() }
    }

    fun toggleOffersNotif(enabled: Boolean) {
        viewModelScope.launch { repository.setOffersNotifEnabled(enabled) }
    }

    fun toggleOpeningsNotif(enabled: Boolean) {
        viewModelScope.launch { repository.setOpeningsNotifEnabled(enabled) }
    }

    fun toggleEventsNotif(enabled: Boolean) {
        viewModelScope.launch { repository.setEventsNotifEnabled(enabled) }
    }

    fun toggleAlertsNotif(enabled: Boolean) {
        viewModelScope.launch { repository.setAlertsNotifEnabled(enabled) }
    }

    fun toggleVisitorInterest(category: MallPlaceCategory) {
        viewModelScope.launch { repository.toggleInterest(category.name) }
    }

    fun setVisitorInterests(categories: Set<String>) {
        viewModelScope.launch { repository.setSelectedInterests(categories) }
    }

    fun sendTestNotification(title: String, body: String): Boolean {
        return MallNotificationHelper.showMallNotification(
            context = getApplication(),
            notificationId = (System.currentTimeMillis() % 10000).toInt(),
            title = title,
            body = body
        )
    }

    fun sendInterestTestOfferNotification(): Boolean {
        val prefs = notificationPreferences.value
        val selected = prefs.selectedInterests
        val placeMap = places.value.associateBy { it.id }
        val matchingOffer = offers.value.firstOrNull { offer ->
            val cat = placeMap[offer.storeId]?.categoryName
            selected.isEmpty() || (cat != null && cat in selected)
        }
        val title = if (matchingOffer != null) {
            "عرض يناسب اهتماماتك: ${matchingOffer.storeNameAr}"
        } else {
            "عروض مول العابد المحدثة"
        }
        val body = if (matchingOffer != null) {
            "${matchingOffer.titleAr} (${matchingOffer.discountBadge}) — كود: ${matchingOffer.couponCode}"
        } else {
            "تصفح أحدث العروض والكوبونات في مول العابد بطوخ."
        }
        val sent = MallNotificationHelper.showMallNotification(
            context = getApplication(),
            notificationId = (System.currentTimeMillis() % 10000).toInt(),
            title = title,
            body = body
        )
        viewModelScope.launch {
            repository.addNotification(
                MallNotificationEntity(
                    id = "notif_interest_${System.currentTimeMillis()}",
                    titleAr = title,
                    bodyAr = body,
                    categoryName = NotificationCategory.OFFER.name,
                    timestampLabelAr = "الآن",
                    linkedTargetId = matchingOffer?.storeId ?: "hypermarket",
                    linkedType = "STORE",
                    isRead = false
                )
            )
        }
        return sent
    }

    // --- Trip Planner & Hypermarket Shopping List ---
    fun addStoreToTrip(store: StoreEntity) {
        viewModelScope.launch {
            repository.upsertTripStop(
                TripStopEntity(
                    id = "store_${store.id}",
                    storeId = store.id,
                    offerId = null,
                    titleAr = store.nameAr,
                    subtitleAr = "${store.category.labelAr} • ${store.zoneAr}",
                    badgeAr = if (store.isNewOpening) "جديد" else "",
                    floor = store.floor,
                    mapX = store.mapX,
                    mapY = store.mapY
                )
            )
        }
    }

    fun addOfferToTrip(offer: OfferEntity) {
        val store = places.value.firstOrNull { it.id == offer.storeId }
        viewModelScope.launch {
            repository.upsertTripStop(
                TripStopEntity(
                    id = "offer_${offer.id}",
                    storeId = offer.storeId,
                    offerId = offer.id,
                    titleAr = offer.storeNameAr,
                    subtitleAr = offer.titleAr,
                    badgeAr = offer.discountBadge,
                    floor = store?.floor ?: 0,
                    mapX = store?.mapX ?: 405f,
                    mapY = store?.mapY ?: 235f
                )
            )
        }
    }

    fun toggleTripStopVisited(id: String, visited: Boolean) {
        viewModelScope.launch { repository.setTripStopVisited(id, visited) }
    }

    fun removeTripStop(id: String) {
        viewModelScope.launch { repository.deleteTripStop(id) }
    }

    fun clearVisitedTripStops() {
        viewModelScope.launch { repository.clearVisitedTripStops() }
    }

    fun addShoppingTask(
        itemName: String,
        departmentTag: String,
        store: StoreEntity? = null
    ) {
        val trimmed = itemName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.insertShoppingTask(
                ShoppingTaskEntity(
                    itemName = trimmed,
                    departmentTag = departmentTag.trim().ifEmpty {
                        store?.category?.labelAr ?: "عام"
                    },
                    storeId = store?.id,
                    storeNameAr = store?.nameAr,
                    floor = store?.floor
                )
            )
        }
    }

    fun toggleShoppingTask(id: Long, checked: Boolean) {
        viewModelScope.launch { repository.setShoppingTaskChecked(id, checked) }
    }

    fun deleteShoppingTask(id: Long) {
        viewModelScope.launch { repository.deleteShoppingTask(id) }
    }

    fun clearCheckedShoppingTasks() {
        viewModelScope.launch { repository.clearCheckedShoppingTasks() }
    }

    private fun sortTripStopsByFloorAndPath(stops: List<TripStopEntity>): List<OrderedTripStop> {
        if (stops.isEmpty()) return emptyList()
        val unvisited = stops.filter { !it.isVisited }
        val visited = stops.filter { it.isVisited }

        val ordered = mutableListOf<OrderedTripStop>()
        var currX = 335f // Gate 1 Main Entrance
        var currY = 635f
        var orderCounter = 1

        // Visit Ground Floor (0) first, then First Floor (1), using nearest-neighbor
        for (floor in listOf(0, 1)) {
            val remainingOnFloor = unvisited.filter { it.floor == floor }.toMutableList()
            if (floor == 1 && remainingOnFloor.isNotEmpty()) {
                // Transition via Central Escalator
                currX = 495f
                currY = 455f
            }
            while (remainingOnFloor.isNotEmpty()) {
                val nextIdx = remainingOnFloor.indices.minBy { idx ->
                    val s = remainingOnFloor[idx]
                    hypot(s.mapX - currX, s.mapY - currY)
                }
                val chosen = remainingOnFloor.removeAt(nextIdx)
                val distMeters = (hypot(chosen.mapX - currX, chosen.mapY - currY) *
                    MallFloorPlanGraph.METERS_PER_UNIT).roundToInt().coerceAtLeast(5)
                ordered.add(
                    OrderedTripStop(
                        stop = chosen,
                        orderNumber = orderCounter++,
                        stepDistanceMeters = distMeters
                    )
                )
                currX = chosen.mapX
                currY = chosen.mapY
            }
        }

        visited.forEach { done ->
            ordered.add(
                OrderedTripStop(
                    stop = done,
                    orderNumber = orderCounter++,
                    stepDistanceMeters = 0
                )
            )
        }
        return ordered
    }

    // --- Visitor Services ---
    fun submitVisitorTicket(type: String, name: String, phone: String, message: String) {
        viewModelScope.launch {
            repository.submitVisitorTicket(
                VisitorTicketEntity(
                    ticketType = type,
                    visitorName = name.trim().ifEmpty { "زائر مول العابد" },
                    visitorPhone = phone.trim(),
                    message = message.trim()
                )
            )
        }
    }

    // --- Mall Content Management (CMS) ---
    fun saveStoreFromCms(
        id: String?,
        nameAr: String,
        nameEn: String,
        category: MallPlaceCategory,
        floor: Int,
        zoneAr: String,
        descriptionAr: String,
        hoursAr: String,
        phone: String,
        mapX: Float,
        mapY: Float,
        isNewOpening: Boolean
    ) {
        val finalId = id ?: "store_${System.currentTimeMillis()}"
        viewModelScope.launch {
            repository.upsertStore(
                StoreEntity(
                    id = finalId,
                    nameAr = nameAr.trim(),
                    nameEn = nameEn.trim().ifEmpty { nameAr.trim() },
                    categoryName = category.name,
                    floor = floor.coerceIn(0, 1),
                    zoneAr = zoneAr.trim().ifEmpty { if (floor == 0) "الدور الأرضي" else "الدور الأول" },
                    descriptionAr = descriptionAr.trim(),
                    hoursAr = hoursAr.trim().ifEmpty { "١٠:٠٠ ص - ١٢:٠٠ ص" },
                    phone = phone.trim().ifEmpty { "0132460100" },
                    mapX = mapX.coerceIn(210f, 845f),
                    mapY = mapY.coerceIn(170f, 1030f),
                    isServiceFacility = category == MallPlaceCategory.FACILITY,
                    isFeatured = true,
                    isNewOpening = isNewOpening
                )
            )
        }
    }

    fun deleteStore(id: String) {
        viewModelScope.launch { repository.deleteStore(id) }
    }

    fun addOfferFromCms(
        storeId: String,
        storeNameAr: String,
        titleAr: String,
        descriptionAr: String,
        discountBadge: String,
        couponCode: String,
        validUntilAr: String,
        termsAr: String
    ) {
        viewModelScope.launch {
            val cleanTitle = titleAr.trim()
            val cleanBadge = discountBadge.trim().ifEmpty { "عرض خاص" }
            val cleanCode = couponCode.trim().uppercase().ifEmpty { "ELABED" }
            repository.upsertOffer(
                OfferEntity(
                    id = "offer_${System.currentTimeMillis()}",
                    storeId = storeId,
                    storeNameAr = storeNameAr,
                    titleAr = cleanTitle,
                    descriptionAr = descriptionAr.trim(),
                    discountBadge = cleanBadge,
                    couponCode = cleanCode,
                    validUntilAr = validUntilAr.trim().ifEmpty { "ساري لفترة محدودة" },
                    termsAr = termsAr.trim().ifEmpty { "يسري العرض داخل الفرع بالمول." },
                    isFeatured = true
                )
            )

            // Trigger personalized offer notification if visitor interests match (or none selected)
            val prefs = notificationPreferences.value
            val storeCategory = places.value.firstOrNull { it.id == storeId }?.categoryName
            val matchesInterest = prefs.selectedInterests.isEmpty() ||
                (storeCategory != null && storeCategory in prefs.selectedInterests)
            if (prefs.offersEnabled && matchesInterest) {
                val notifTitle = "عرض جديد في $storeNameAr ($cleanBadge)"
                val notifBody = "$cleanTitle — استخدم كود $cleanCode"
                repository.addNotification(
                    MallNotificationEntity(
                        id = "notif_offer_${System.currentTimeMillis()}",
                        titleAr = notifTitle,
                        bodyAr = notifBody,
                        categoryName = NotificationCategory.OFFER.name,
                        timestampLabelAr = "الآن",
                        linkedTargetId = storeId,
                        linkedType = "STORE",
                        isRead = false
                    )
                )
                MallNotificationHelper.showMallNotification(
                    context = getApplication(),
                    notificationId = (System.currentTimeMillis() % 10000).toInt(),
                    title = notifTitle,
                    body = notifBody
                )
            }
        }
    }

    fun savePromoBannerFromAdmin(
        pickedImageUri: Uri?,
        presetDrawableKey: String?,
        titleAr: String,
        subtitleAr: String,
        badgeAr: String,
        linkedTargetId: String,
        isVisible: Boolean
    ) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val currentBanner = promoBanner.value
            val resolvedImagePath = when {
                pickedImageUri != null -> withContext(Dispatchers.IO) {
                    copyPickedBannerImage(app, pickedImageUri) ?: currentBanner.imagePathOrKey
                }
                !presetDrawableKey.isNullOrBlank() -> presetDrawableKey
                else -> currentBanner.imagePathOrKey
            }

            repository.savePromoBanner(
                PromoBannerConfig(
                    imagePathOrKey = resolvedImagePath,
                    titleAr = titleAr.trim().ifEmpty { currentBanner.titleAr },
                    subtitleAr = subtitleAr.trim().ifEmpty { currentBanner.subtitleAr },
                    badgeAr = badgeAr.trim().ifEmpty { "عرض ترويجي" },
                    linkedTargetId = linkedTargetId.ifBlank { "hypermarket" },
                    isVisible = isVisible
                )
            )
        }
    }

    fun resetPromoBanner() {
        viewModelScope.launch {
            val app = getApplication<Application>()
            withContext(Dispatchers.IO) {
                app.filesDir.listFiles { f -> f.name.startsWith("promo_banner_") }?.forEach { it.delete() }
            }
            repository.resetPromoBanner()
        }
    }

    private fun copyPickedBannerImage(app: Application, uri: Uri): String? {
        return try {
            app.filesDir.listFiles { f -> f.name.startsWith("promo_banner_") }?.forEach { it.delete() }
            val outFile = File(app.filesDir, "promo_banner_${System.currentTimeMillis()}.jpg")
            app.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            outFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    fun addEventFromCms(
        titleAr: String,
        descriptionAr: String,
        dateRangeAr: String,
        timeAr: String,
        locationPlaceId: String,
        locationLabelAr: String,
        floor: Int,
        isNewOpening: Boolean
    ) {
        viewModelScope.launch {
            repository.upsertEvent(
                EventEntity(
                    id = "event_${System.currentTimeMillis()}",
                    titleAr = titleAr.trim(),
                    descriptionAr = descriptionAr.trim(),
                    dateRangeAr = dateRangeAr.trim().ifEmpty { "هذا الأسبوع" },
                    timeAr = timeAr.trim().ifEmpty { "٦:٠٠ م - ١٠:٠٠ م" },
                    locationPlaceId = locationPlaceId,
                    locationLabelAr = locationLabelAr,
                    floor = floor,
                    isNewOpening = isNewOpening,
                    badgeAr = if (isNewOpening) "افتتاح جديد" else "فعالية"
                )
            )
        }
    }

    fun broadcastNotificationFromCms(
        titleAr: String,
        bodyAr: String,
        category: NotificationCategory,
        linkedTargetId: String?
    ) {
        viewModelScope.launch {
            val id = "notif_${System.currentTimeMillis()}"
            repository.addNotification(
                MallNotificationEntity(
                    id = id,
                    titleAr = titleAr.trim(),
                    bodyAr = bodyAr.trim(),
                    categoryName = category.name,
                    timestampLabelAr = "الآن",
                    linkedTargetId = linkedTargetId,
                    linkedType = "MAP",
                    isRead = false
                )
            )
            val prefs = notificationPreferences.value
            if (isCategoryEnabled(category, prefs)) {
                MallNotificationHelper.showMallNotification(
                    context = getApplication(),
                    notificationId = (System.currentTimeMillis() % 10000).toInt(),
                    title = titleAr.trim(),
                    body = bodyAr.trim()
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLiveGpsTracking()
    }
}
