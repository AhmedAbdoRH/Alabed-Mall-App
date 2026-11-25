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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.sharing.MallShareContent
import com.example.ui.components.ShareSheetDialog
import com.example.ui.navigation.AccessRoute
import com.example.ui.navigation.ContentManagerRoute
import com.example.ui.navigation.DirectoryRoute
import com.example.ui.navigation.HomeRoute
import com.example.ui.navigation.IndoorMapRoute
import com.example.ui.navigation.MoreHubRoute
import com.example.ui.navigation.NotificationsRoute
import com.example.ui.navigation.OffersEventsRoute
import com.example.ui.navigation.StoreDetailRoute
import com.example.ui.navigation.TasksShoppingRoute
import com.example.ui.navigation.TripPlannerRoute
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
import com.example.ui.screens.TasksShoppingScreen
import com.example.ui.screens.TripPlannerScreen
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
    val interestMatchedOffers by viewModel.interestMatchedOffers.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val notifPrefs by viewModel.notificationPreferences.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val tickets by viewModel.tickets.collectAsStateWithLifecycle()
    val mapState by viewModel.mapState.collectAsStateWithLifecycle()
    val promoBanner by viewModel.promoBanner.collectAsStateWithLifecycle()
    val orderedTripStops by viewModel.orderedTripStops.collectAsStateWithLifecycle()
    val shoppingTasks by viewModel.shoppingTasks.collectAsStateWithLifecycle()

    var pendingShareContent by remember { mutableStateOf<MallShareContent?>(null) }

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

    val showGlobalTasksFab = currentDest?.let { dest ->
        !dest.hasRoute<IndoorMapRoute>() &&
            !dest.hasRoute<TasksShoppingRoute>() &&
            !dest.hasRoute<ContentManagerRoute>()
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
                floatingActionButton = {
                    if (showGlobalTasksFab) {
                        FloatingActionButton(
                            onClick = { navController.navigate(TasksShoppingRoute) },
                            containerColor = ElAbedRoyalBlue,
                            contentColor = Color.White,
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                            modifier = Modifier.testTag("global_add_task_fab")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "صفحة المهام والمشتريات"
                            )
                        }
                    }
                },
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
                            unreadNotifications = unreadCount,
                            onOpenDirectory = { navigateTopLevel(DirectoryRoute()) },
                            onSearchWithQuery = { q, tabName ->
                                navController.navigate(
                                    DirectoryRoute(
                                        initialQuery = q,
                                        initialSearchTab = tabName
                                    )
                                )
                            },
                            onOpenCategory = { category ->
                                navController.navigate(
                                    DirectoryRoute(initialCategory = category.name)
                                )
                            },
                            onOpenIndoorMap = { targetId ->
                                navController.navigate(IndoorMapRoute(targetPlaceId = targetId))
                            },
                            onOpenNotifications = { navController.navigate(NotificationsRoute) },
                            onOpenMoreHub = { navigateTopLevel(MoreHubRoute) }
                        )
                    }

                    composable<DirectoryRoute> { entry ->
                        val args = entry.toRoute<DirectoryRoute>()
                        DirectoryScreen(
                            places = places,
                            offers = offers,
                            initialCategoryName = args.initialCategory,
                            initialQuery = args.initialQuery,
                            initialSearchTabName = args.initialSearchTab,
                            onOpenStoreDetail = { storeId ->
                                navController.navigate(StoreDetailRoute(storeId = storeId))
                            },
                            onNavigateOnMap = { placeId ->
                                navController.navigate(IndoorMapRoute(targetPlaceId = placeId))
                            },
                            onShareStore = { store ->
                                pendingShareContent = MallShareContent.forStore(store)
                            },
                            onAddStoreToTrip = viewModel::addStoreToTrip
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
                            },
                            onShareStore = { targetStore ->
                                pendingShareContent = MallShareContent.forStore(targetStore)
                            },
                            onShareOffer = { offer ->
                                pendingShareContent = MallShareContent.forOffer(offer, store)
                            },
                            onAddStoreToTrip = viewModel::addStoreToTrip,
                            onOpenCustomerService = { navController.navigate(VisitorServicesRoute) }
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
                            offers = interestMatchedOffers,
                            events = events,
                            initialTab = args.initialTab,
                            onOpenStoreDetail = { storeId ->
                                navController.navigate(StoreDetailRoute(storeId = storeId))
                            },
                            onOpenIndoorMap = { targetId ->
                                navController.navigate(IndoorMapRoute(targetPlaceId = targetId))
                            },
                            onShareOffer = { offer ->
                                val store = places.firstOrNull { it.id == offer.storeId }
                                pendingShareContent = MallShareContent.forOffer(offer, store)
                            },
                            onAddOfferToTrip = viewModel::addOfferToTrip
                        )
                    }

                    composable<MoreHubRoute> {
                        MoreHubScreen(
                            unreadNotifications = unreadCount,
                            tripStopCount = orderedTripStops.size,
                            onOpenAccess = { navController.navigate(AccessRoute) },
                            onOpenNotifications = { navController.navigate(NotificationsRoute) },
                            onOpenVisitorServices = { navController.navigate(VisitorServicesRoute) },
                            onOpenTripPlanner = { navController.navigate(TripPlannerRoute) },
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
                            onToggleInterest = viewModel::toggleVisitorInterest,
                            onSendTestNotification = {
                                viewModel.sendInterestTestOfferNotification()
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

                    composable<TripPlannerRoute> {
                        TripPlannerScreen(
                            orderedStops = orderedTripStops,
                            shoppingTasks = shoppingTasks,
                            onBack = { navController.popBackStack() },
                            onOpenIndoorMap = { targetId ->
                                navController.navigate(IndoorMapRoute(targetPlaceId = targetId))
                            },
                            onOpenDirectory = { navigateTopLevel(DirectoryRoute()) },
                            onOpenOffers = { navigateTopLevel(OffersEventsRoute(initialTab = 0)) },
                            onToggleTripStopVisited = viewModel::toggleTripStopVisited,
                            onRemoveTripStop = viewModel::removeTripStop,
                            onClearVisitedStops = viewModel::clearVisitedTripStops,
                            onAddShoppingTask = { name, dept ->
                                viewModel.addShoppingTask(
                                    itemName = name,
                                    departmentTag = dept,
                                    store = places.firstOrNull { it.id == "hypermarket" }
                                )
                            },
                            onToggleShoppingTask = viewModel::toggleShoppingTask,
                            onDeleteShoppingTask = viewModel::deleteShoppingTask,
                            onClearCheckedTasks = viewModel::clearCheckedShoppingTasks
                        )
                    }

                    composable<TasksShoppingRoute> {
                        TasksShoppingScreen(
                            tasks = shoppingTasks,
                            places = places,
                            onBack = { navController.popBackStack() },
                            onAddTask = { itemName, departmentTag, selectedStore ->
                                viewModel.addShoppingTask(itemName, departmentTag, selectedStore)
                            },
                            onToggleTask = viewModel::toggleShoppingTask,
                            onDeleteTask = viewModel::deleteShoppingTask,
                            onClearChecked = viewModel::clearCheckedShoppingTasks,
                            onOpenStoreDetail = { storeId ->
                                navController.navigate(StoreDetailRoute(storeId = storeId))
                            },
                            onOpenIndoorMap = { targetId ->
                                navController.navigate(IndoorMapRoute(targetPlaceId = targetId))
                            }
                        )
                    }

                    composable<ContentManagerRoute> {
                        ContentManagerScreen(
                            places = places,
                            offers = offers,
                            events = events,
                            promoBanner = promoBanner,
                            onBack = { navController.popBackStack() },
                            onSaveStore = viewModel::saveStoreFromCms,
                            onDeleteStore = viewModel::deleteStore,
                            onAddOffer = viewModel::addOfferFromCms,
                            onAddEvent = viewModel::addEventFromCms,
                            onBroadcastNotification = viewModel::broadcastNotificationFromCms,
                            onSavePromoBanner = viewModel::savePromoBannerFromAdmin,
                            onResetPromoBanner = viewModel::resetPromoBanner
                        )
                    }
                }
            }
        }

        ShareSheetDialog(
            content = pendingShareContent,
            onDismiss = { pendingShareContent = null }
        )
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
