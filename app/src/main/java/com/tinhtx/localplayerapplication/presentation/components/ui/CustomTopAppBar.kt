package com.tinhtx.localplayerapplication.presentation.components.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.components.image.CircularProfileImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: ImageVector? = Icons.Default.ArrowBack,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    profileImageUrl: String? = null,
    onProfileClick: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    showDivider: Boolean = false
) {
    Column {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            modifier = modifier,
            navigationIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Navigation icon
                    if (navigationIcon != null && onNavigationClick != null) {
                        IconButton(onClick = onNavigationClick) {
                            Icon(
                                imageVector = navigationIcon,
                                contentDescription = "Navigation",
                                tint = contentColor
                            )
                        }
                    }
                    
                    // Profile image (when no navigation icon)
                    if (navigationIcon == null && profileImageUrl != null && onProfileClick != null) {
                        CircularProfileImage(
                            imageUrl = profileImageUrl,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { onProfileClick() },
                            size = 32.dp
                        )
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = containerColor,
                titleContentColor = contentColor,
                navigationIconContentColor = contentColor,
                actionIconContentColor = contentColor
            ),
            scrollBehavior = scrollBehavior
        )
        
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    userName: String,
    greeting: String,
    profileImageUrl: String?,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    hasNotifications: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        modifier = modifier,
        navigationIcon = {
            CircularProfileImage(
                imageUrl = profileImageUrl,
                contentDescription = "Profile",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable { onProfileClick() },
                size = 40.dp,
                borderWidth = 2.dp,
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        },
        actions = {
            // Search button
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Notification button with badge
            Box {
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                if (hasNotifications) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp),
                        containerColor = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerTopAppBar(
    songTitle: String,
    onBackClick: () -> Unit,
    onQueueClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    TopAppBar(
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Playing from Library",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
                
                Text(
                    text = songTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Close player",
                    tint = contentColor
                )
            }
        },
        actions = {
            IconButton(onClick = onQueueClick) {
                Icon(
                    imageVector = Icons.Default.QueueMusic,
                    contentDescription = "Show queue",
                    tint = contentColor
                )
            }
            
            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = contentColor
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Tìm kiếm bài hát, nghệ sĩ, album...",
    isActive: Boolean = false
) {
    TopAppBar(
        title = {
            SearchTextField(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onClearClick = onClearClick,
                placeholder = placeholder,
                isActive = isActive,
                modifier = Modifier.fillMaxWidth()
            )
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    isActive: Boolean = false
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search"
                    )
                }
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
    )
}

@Composable
fun LibraryTopAppBar(
    selectedTab: String,
    onTabsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSortClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tabs button
        OutlinedButton(
            onClick = onTabsClick,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Text(
                text = selectedTab,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
        
        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
            
            IconButton(onClick = onSortClick) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Sort"
                )
            }
        }
    }
}
