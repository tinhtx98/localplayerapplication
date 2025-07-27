package com.tinhtx.localplayerapplication.presentation.components.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.components.common.*
import com.tinhtx.localplayerapplication.presentation.navigation.BottomNavItem

@Composable
fun AdaptiveNavigation(
    items: List<BottomNavItem>,
    selectedItem: String,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    hasNotification: Map<String, Boolean> = emptyMap(),
    content: @Composable () -> Unit
) {
    val responsiveInfo = rememberResponsiveInfo()
    val navigationType = rememberNavigationType()
    
    when (navigationType) {
        NavigationType.BottomNavigation -> {
            // Phone layout với bottom navigation
            Column(
                modifier = modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    content()
                }
                
                BottomNavigationBar(
                    items = items,
                    selectedItem = selectedItem,
                    onItemClick = onItemClick,
                    hasNotification = hasNotification,
                    style = when (responsiveInfo.screenSize) {
                        ScreenSize.Compact -> BottomNavStyle.Default
                        else -> BottomNavStyle.Floating
                    }
                )
            }
        }
        
        NavigationType.NavigationRail -> {
            // Tablet layout với navigation rail
            Row(
                modifier = modifier.fillMaxSize()
            ) {
                NavigationRail(
                    items = items,
                    selectedItem = selectedItem,
                    onItemClick = onItemClick,
                    hasNotification = hasNotification,
                    showLabels = responsiveInfo.screenSize >= ScreenSize.Expanded,
                    modifier = Modifier.width(
                        if (responsiveInfo.screenSize >= ScreenSize.Expanded) 120.dp else 80.dp
                    )
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {
                    content()
                }
            }
        }
        
        NavigationType.NavigationDrawer -> {
            // Desktop layout với navigation drawer
            val drawerState = rememberDrawerState(DrawerValue.Open)
            
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(
                        modifier = Modifier.width(280.dp)
                    ) {
                        NavigationDrawerContent(
                            items = items,
                            selectedItem = selectedItem,
                            onItemClick = onItemClick,
                            hasNotification = hasNotification
                        )
                    }
                },
                modifier = modifier
            ) {
                content()
            }
        }
    }
}

@Composable
fun ResponsiveNavigationLayout(
    items: List<BottomNavItem>,
    selectedItem: String,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    hasNotification: Map<String, Boolean> = emptyMap(),
    topBar: (@Composable () -> Unit)? = null,
    floatingActionButton: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val responsiveInfo = rememberResponsiveInfo()
    
    when (responsiveInfo.deviceType) {
        DeviceType.Phone -> {
            // Phone layout
            Scaffold(
                topBar = topBar ?: {},
                bottomBar = {
                    BottomNavigationBar(
                        items = items,
                        selectedItem = selectedItem,
                        onItemClick = onItemClick,
                        hasNotification = hasNotification,
                        style = BottomNavStyle.Default
                    )
                },
                floatingActionButton = floatingActionButton ?: {},
                modifier = modifier
            ) { paddingValues ->
                content(paddingValues)
            }
        }
        
        DeviceType.Tablet -> {
            // Tablet layout
            Scaffold(
                topBar = topBar ?: {},
                floatingActionButton = floatingActionButton ?: {},
                modifier = modifier
            ) { paddingValues ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    NavigationRail(
                        items = items,
                        selectedItem = selectedItem,
                        onItemClick = onItemClick,
                        hasNotification = hasNotification,
                        showLabels = true,
                        modifier = Modifier.width(120.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                    ) {
                        content(PaddingValues(0.dp))
                    }
                }
            }
        }
        
        DeviceType.Desktop -> {
            // Desktop layout
            val drawerState = rememberDrawerState(DrawerValue.Open)
            
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(
                        modifier = Modifier.width(300.dp)
                    ) {
                        NavigationDrawerContent(
                            items = items,
                            selectedItem = selectedItem,
                            onItemClick = onItemClick,
                            hasNotification = hasNotification,
                            showHeaders = true
                        )
                    }
                },
                modifier = modifier
            ) {
                Scaffold(
                    topBar = topBar ?: {},
                    floatingActionButton = floatingActionButton ?: {}
                ) { paddingValues ->
                    content(paddingValues)
                }
            }
        }
    }
}

@Composable
private fun NavigationDrawerContent(
    items: List<BottomNavItem>,
    selectedItem: String,
    onItemClick: (String) -> Unit,
    hasNotification: Map<String, Boolean>,
    showHeaders: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // App header
        if (showHeaders) {
            Text(
                text = "LocalPlayer",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
        
        // Navigation items
        items.forEach { item ->
            val isSelected = selectedItem == item.route
            val hasNotif = hasNotification[item.route] == true
            
            NavigationDrawerItem(
                selected = isSelected,
                onClick = { onItemClick(item.route) },
                icon = {
                    Box {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label
                        )
                        
                        // Notification badge
                        if (hasNotif) {
                            Box(
                                modifier = Modifier
                                    .offset(x = 12.dp, y = (-4).dp)
                                    .size(8.dp)
                                    .background(
                                        MaterialTheme.colorScheme.error,
                                        androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Footer
        if (showHeaders) {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = "v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun DynamicNavigation(
    items: List<BottomNavItem>,
    selectedItem: String,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    hasNotification: Map<String, Boolean> = emptyMap(),
    forceNavigationType: NavigationType? = null,
    content: @Composable () -> Unit
) {
    val navigationType = forceNavigationType ?: rememberNavigationType()
    var isRailExpanded by remember { mutableStateOf(false) }
    
    when (navigationType) {
        NavigationType.BottomNavigation -> {
            Column(modifier = modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    content()
                }
                
                AdaptiveBottomNavigation(
                    items = items,
                    selectedItem = selectedItem,
                    onItemClick = onItemClick,
                    hasNotification = hasNotification
                )
            }
        }
        
        NavigationType.NavigationRail -> {
            Row(modifier = modifier.fillMaxSize()) {
                ExpandableNavigationRail(
                    items = items,
                    selectedItem = selectedItem,
                    onItemClick = onItemClick,
                    expanded = isRailExpanded,
                    onToggleExpanded = { isRailExpanded = !isRailExpanded },
                    hasNotification = hasNotification
                )
                
                Box(modifier = Modifier.weight(1f)) {
                    content()
                }
            }
        }
        
        NavigationType.NavigationDrawer -> {
            val drawerState = rememberDrawerState(DrawerValue.Open)
            
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        NavigationDrawerContent(
                            items = items,
                            selectedItem = selectedItem,
                            onItemClick = onItemClick,
                            hasNotification = hasNotification,
                            showHeaders = true
                        )
                    }
                }
            ) {
                content()
            }
        }
    }
}

@Composable
fun SmartNavigation(
    items: List<BottomNavItem>,
    selectedItem: String,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    hasNotification: Map<String, Boolean> = emptyMap(),
    autoHideNavigation: Boolean = false,
    content: @Composable () -> Unit
) {
    val responsiveInfo = rememberResponsiveInfo()
    var isNavigationVisible by remember { mutableStateOf(!autoHideNavigation) }
    
    // Auto-hide logic for immersive content
    LaunchedEffect(autoHideNavigation) {
        if (autoHideNavigation) {
            kotlinx.coroutines.delay(3000) // Hide after 3 seconds
            isNavigationVisible = false
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        content()
        
        // Show navigation based on screen size and visibility state
        AnimatedVisibility(
            visible = isNavigationVisible || !autoHideNavigation,
            enter = when (rememberNavigationType()) {
                NavigationType.BottomNavigation -> slideInVertically(initialOffsetY = { it })
                NavigationType.NavigationRail -> slideInHorizontally(initialOffsetX = { -it })
                NavigationType.NavigationDrawer -> fadeIn()
            },
            exit = when (rememberNavigationType()) {
                NavigationType.BottomNavigation -> slideOutVertically(targetOffsetY = { it })
                NavigationType.NavigationRail -> slideOutHorizontally(targetOffsetX = { -it })
                NavigationType.NavigationDrawer -> fadeOut()
            }
        ) {
            AdaptiveNavigation(
                items = items,
                selectedItem = selectedItem,
                onItemClick = onItemClick,
                hasNotification = hasNotification
            ) {
                // Empty content since we're overlaying
            }
        }
        
        // Toggle button for auto-hide mode
        if (autoHideNavigation && !isNavigationVisible) {
            FloatingActionButton(
                onClick = { isNavigationVisible = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = when (rememberNavigationType()) {
                        NavigationType.BottomNavigation -> Icons.Default.KeyboardArrowUp
                        NavigationType.NavigationRail -> Icons.Default.KeyboardArrowRight
                        NavigationType.NavigationDrawer -> Icons.Default.Menu
                    },
                    contentDescription = "Show navigation"
                )
            }
        }
    }
}
