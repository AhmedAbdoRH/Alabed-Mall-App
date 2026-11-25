package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
data class DirectoryRoute(
    val initialCategory: String? = null,
    val initialQuery: String? = null,
    val initialSearchTab: String? = null
)

@Serializable
data class StoreDetailRoute(val storeId: String)

@Serializable
data class IndoorMapRoute(val targetPlaceId: String? = null)

@Serializable
data class OffersEventsRoute(val initialTab: Int = 0)

@Serializable
object MoreHubRoute

@Serializable
object AccessRoute

@Serializable
object NotificationsRoute

@Serializable
object VisitorServicesRoute

@Serializable
object ContentManagerRoute

@Serializable
object TripPlannerRoute
