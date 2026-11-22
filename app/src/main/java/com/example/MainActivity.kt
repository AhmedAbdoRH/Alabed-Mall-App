package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.ui.navigation.AccessRoute
import com.example.ui.navigation.ContentManagerRoute
import com.example.ui.navigation.DirectoryRoute
import com.example.ui.navigation.HomeRoute
import com.example.ui.navigation.IndoorMapRoute
import com.example.ui.navigation.MoreHubRoute
import com.example.ui.navigation.NotificationsRoute
import com.example.ui.navigation.OffersEventsRoute
import com.example.ui.navigation.StoreDetailRoute
import com.example.ui.navigation.VisitorServicesRoute
import com.example.ui.screens.AccessScreen
import com.example.ui.screens.ContentManagerScreen
import com.example.ui.screens.DirectoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IndoorMapScreen
import com.example.ui.screens.MoreHubScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OffersEventsScreen
import com.example.ui.screens.StoreDetailScreen
import com.example.ui.screens.VisitorServicesScreen
import com.example.ui.theme.ElAbedMallTheme
import com.example.ui.theme.ElAbedOutline
import com.example.ui.theme.ElAbedRoyalBlue
import com.example.ui.theme.ElAbedRoyalBlueContainer
import com.example.ui.theme.ElAbedTextSecondary
import com.example.ui.theme.ElAbedWarmOrange
import com.example.ui.viewmodel.MallViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.White.toArgb(),
                Color.White.toArgb()
            )
        )
        setContent {
            ElAbedMallTheme(darkTheme = false) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    ElAbedMallApp()
                }
            }
        }
    }
}

private data class TopLevelDestination(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: Any,
    val testTag: String
)

@Composable
fun ElAbedMallApp(
    viewModel: MallViewModel = viewModel()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = backStackEntry?.destination

    val places by viewModel.places.collectAsStateWithLifecycle()
    val offers by viewModel.offers.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val notifPrefs by viewModel.notificationPreferences.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val tickets by viewModel.tickets.collectAsStateWithLifecycle()
    val mapState by viewModel.mapState.collectAsStateWithLifecycle()

    val topDestinations = listOf(
        TopLevelDestination("الرئيسية", Icons.Filled.Home, Icons.Outlined.Home, HomeRoute, "nav_home"),
        TopLevelDestination("الدليل", Icons.Filled.Storefront, Icons.Outlined.Storefront, DirectoryRoute(), "nav_directory"),
        TopLevelDestination("الخريطة", Icons.Filled.Map, Icons.Outlined.Map, IndoorMapRoute(), "nav_map"),
        TopLevelDestination("العروض", Icons.Filled.LocalOffer, Icons.Outlined.LocalOffer, OffersEventsRoute(), "nav_offers"),
        TopLevelDestination("المزيد", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz, MoreHubRoute, "nav_more")
    )

    val showTopLevelNav = currentDest?.let { dest ->
        dest.hasRoute<HomeRoute>() ||
            dest.hasRoute<DirectoryRoute>() ||
            dest.hasRoute<IndoorMapRoute>() ||
            dest.hasRoute<OffersEventsRoute>() ||
            dest.hasRoute<MoreHubRoute>()
    } ?: true

    fun navigateTopLevel(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useRail && showTopLevelNav) {
                NavigationRail(
                    containerColor = Color.White
                ) {
                    topDestinations.forEach { item ->
                        val selected = isTopDestinationSelected(currentDest, item.route)
                        NavigationRailItem(
                            selected = selected,
                            onClick = { navigateTopLevel(item.route) },
                            icon = {
                                BadgedNavIcon(
                                    icon = if (selected) item.selectedIcon else item.unselectedIcon,
                                    label = item.label,
                                    badgeCount = if (item.route == MoreHubRoute) unreadCount else 0
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = ElAbedRoyalBlue,
                                selectedTextColor = ElAbedRoyalBlue,
                                indicatorColor = ElAbedRoyalBlueContainer,
                                unselectedIconColor = ElAbedTextSecondary,
                                unselectedTextColor = ElAbedTextSecondary
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                containerColor = Color.White,
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    if (!useRail && showTopLevelNav) {
                        Surface(
                            color = Color.White,
                            shadowElevation = 10.dp
                        ) {
                            Column {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = ElAbedOutline.copy(alpha = 0.7f)
                                )
                                NavigationBar(
                                    containerColor = Color.White,
                                    tonalElevation = 0.dp,
                                    windowInsets = WindowInsets.navigationBars
                                ) {
                                    topDestinations.forEach { item ->
                                        val selected = isTopDestinationSelected(currentDest, item.route)
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { navigateTopLevel(item.route) },
                                            icon = {
                                                BadgedNavIcon(
                                                    icon = if (selected) item.selectedIcon else item.unselectedIcon,
                                                    label = item.label,
                                                    badgeCount = if (item.route == MoreHubRoute) unreadCount else 0
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = item.label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = ElAbedRoyalBlue,
                                                selectedTextColor = ElAbedRoyalBlue,
                                                indicatorColor = ElAbedRoyalBlueContainer,
                                                unselectedIconColor = ElAbedTextSecondary,
                                                unselectedTextColor = ElAbedTextSecondary
                                            ),
                                            modifier = Modifier.testTag(item.testTag)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = HomeRoute,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    composable<HomeRoute> {
                        HomeScreen(
                            places = places,
                            offers = offers,
                            events = events,
                            unreadNotifications = unreadCount,
                            onOpenDirectory = { navigateTopLevel(DirectoryRoute()) },
                            onOpenIndoorMap = { targetId ->
                                navController.navigate(IndoorMapRoute(targetPlaceId = targetId))
                            },
                            onOpenAccessMap = { navController.navigate(AccessRoute) },
                            onOpenOffers = { navController.navigate(OffersEventsRoute(initialTab = 0)) },
                            onOpenEvents = { navController.navigate(OffersEventsRoute(initialTab = 1)) },
                            onOpenNotifications = { navController.navigate(NotificationsRoute) },
                            onOpenVisitorServices = { navController.navigate(VisitorServicesRoute) },
                            onOpenStoreDetail = { storeId ->
                                navController.navigate(StoreDetailRoute(storeId = storeId))
                            }
                        )
                    }

                    composable<DirectoryRoute> { entry ->
                        val args = entry.toRoute<DirectoryRoute>()
                        DirectoryScreen(
                            places = places,
                            offers = offers,
                            initialCategoryName = args.initialCategory,
                            onOpenStoreDetail = { storeId ->
                                navController.navigate(StoreDetailRoute(storeId = storeId))
                            },
                            onNavigateOnMap = { placeId ->
                                navController.navigate(IndoorMapRoute(targetPlaceId = placeId))
                            }
                        )
                    }

                    composable<StoreDetailRoute> { entry ->
                        val args = entry.toRoute<StoreDetailRoute>()
                        val store = places.firstOrNull { it.id == args.storeId }
                        val storeOffers = offers.filter { it.storeId == args.storeId }
                        StoreDetailScreen(
                            store = store,
                            storeOffers = storeOffers,
                            onBack = { navController.popBackStack() },
                            onOpenIndoorMap = { targetId ->
                                navController.navigate(IndoorMapRoute(targetPlaceId = targetId))
                            }
                        )
                    }

                    composable<IndoorMapRoute> { entry ->
                        val args = entry.toRoute<IndoorMapRoute>()
                        IndoorMapScreen(
                            mapState = mapState,
                            places = places,
                            offers = offers,
                            initialTargetPlaceId = args.targetPlaceId,
                            onSelectFloor = viewModel::selectFloor,
                            onSwitchMode = viewModel::switchMovementMode,
                            onLocationPermissionResult = viewModel::onLocationPermissionResult,
                            onInspectPlace = viewModel::inspectPlace,
                            onStartNavigation = viewModel::startNavigationToPlace,
                            onClearNavigation = viewModel::clearNavigation,
                            onMoveJoystick = viewModel::moveAvatarByJoystick,
                            onSwitchAvatarFloor = viewModel::switchAvatarFloor,
                            onResetToGate1 = viewModel::resetAvatarToGate1,
                            onOpenStoreDetail = { storeId ->
                                navController.navigate(StoreDetailRoute(storeId = storeId))
                            }
                        )
                    }

                    composable<OffersEventsRoute> { entry ->
                        val args = entry.toRoute<OffersEventsRoute>()
                        OffersEventsScreen(
                            offers = offers,
                            events = events,
                            initialTab = args.initialTab,
                            onOpenStoreDetail = { storeId ->
                                navController.navigate(StoreDetailRoute(storeId = storeId))
                            },
                            onOpenIndoorMap = { targetId ->
                                navController.navigate(IndoorMapRoute(targetPlaceId = targetId))
                            }
                        )
                    }

                    composable<MoreHubRoute> {
                        MoreHubScreen(
                            unreadNotifications = unreadCount,
                            onOpenAccess = { navController.navigate(AccessRoute) },
                            onOpenNotifications = { navController.navigate(NotificationsRoute) },
                            onOpenVisitorServices = { navController.navigate(VisitorServicesRoute) },
                            onOpenContentManager = { navController.navigate(ContentManagerRoute) }
                        )
                    }

                    composable<AccessRoute> {
                        AccessScreen(
                            onBack = { navController.popBackStack() },
                            onOpenIndoorMap = { targetId ->
                                navController.navigate(IndoorMapRoute(targetPlaceId = targetId))
                            }
                        )
                    }

                    composable<NotificationsRoute> {
                        NotificationsScreen(
                            notifications = notifications,
                            preferences = notifPrefs,
                            onBack = { navController.popBackStack() },
                            onMarkRead = viewModel::markNotificationRead,
                            onMarkAllRead = viewModel::markAllNotificationsRead,
                            onToggleOffers = viewModel::toggleOffersNotif,
                            onToggleOpenings = viewModel::toggleOpeningsNotif,
                            onToggleEvents = viewModel::toggleEventsNotif,
                            onToggleAlerts = viewModel::toggleAlertsNotif,
                            onSendTestNotification = {
                                viewModel.sendTestNotification(
                                    title = "مرحباً بك في مول العابد",
                                    body = "تصفح عروض الهايبر ماركت وابدأ التوجيه التفاعلي على الخريطة."
                                )
                            },
                            onOpenLinkedTarget = { notif ->
                                val target = notif.linkedTargetId
                                if (target != null) {
                                    if (notif.linkedType == "STORE") {
                                        navController.navigate(StoreDetailRoute(storeId = target))
                                    } else {
                                        navController.navigate(IndoorMapRoute(targetPlaceId = target))
                                    }
                                }
                            }
                        )
                    }

                    composable<VisitorServicesRoute> {
                        VisitorServicesScreen(
                            places = places,
                            tickets = tickets,
                            onBack = { navController.popBackStack() },
                            onOpenIndoorMap = { targetId ->
                                navController.navigate(IndoorMapRoute(targetPlaceId = targetId))
                            },
                            onSubmitTicket = viewModel::submitVisitorTicket
                        )
                    }

                    composable<ContentManagerRoute> {
                        ContentManagerScreen(
                            places = places,
                            offers = offers,
                            events = events,
                            onBack = { navController.popBackStack() },
                            onSaveStore = viewModel::saveStoreFromCms,
                            onDeleteStore = viewModel::deleteStore,
                            onAddOffer = viewModel::addOfferFromCms,
                            onAddEvent = viewModel::addEventFromCms,
                            onBroadcastNotification = viewModel::broadcastNotificationFromCms
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgedNavIcon(
    icon: ImageVector,
    label: String,
    badgeCount: Int
) {
    if (badgeCount > 0) {
        BadgedBox(
            badge = {
                Badge(
                    containerColor = ElAbedWarmOrange,
                    contentColor = Color.White
                ) {
                    Text("$badgeCount")
                }
            }
        ) {
            Icon(imageVector = icon, contentDescription = label)
        }
    } else {
        Icon(imageVector = icon, contentDescription = label)
    }
}

private fun isTopDestinationSelected(
    dest: androidx.navigation.NavDestination?,
    route: Any
): Boolean {
    if (dest == null) return false
    return when (route) {
        is HomeRoute -> dest.hasRoute<HomeRoute>()
        is DirectoryRoute -> dest.hasRoute<DirectoryRoute>()
        is IndoorMapRoute -> dest.hasRoute<IndoorMapRoute>()
        is OffersEventsRoute -> dest.hasRoute<OffersEventsRoute>()
        is MoreHubRoute -> dest.hasRoute<MoreHubRoute>()
        else -> false
    }
}
