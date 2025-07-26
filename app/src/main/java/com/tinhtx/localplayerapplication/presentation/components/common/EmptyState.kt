package com.tinhtx.localplayerapplication.presentation.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.MusicNote,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = actionText)
            }
        }
    }
}

@Composable
fun NoMusicFoundState(
    modifier: Modifier = Modifier,
    onScanClick: (() -> Unit)? = null
) {
    EmptyState(
        title = "Không tìm thấy nhạc",
        description = "Chúng tôi không thể tìm thấy bất kỳ tệp nhạc nào trên thiết bị của bạn. Hãy thử quét lại thư viện nhạc.",
        icon = Icons.Default.LibraryMusic,
        actionText = if (onScanClick != null) "Quét thư viện" else null,
        onActionClick = onScanClick,
        modifier = modifier
    )
}

@Composable
fun NoSearchResultsState(
    query: String,
    modifier: Modifier = Modifier
) {
    EmptyState(
        title = "Không có kết quả",
        description = "Không tìm thấy kết quả nào cho \"$query\". Hãy thử tìm kiếm với từ khóa khác.",
        icon = Icons.Default.SearchOff,
        modifier = modifier
    )
}

@Composable
fun NoPlaylistState(
    modifier: Modifier = Modifier,
    onCreatePlaylist: (() -> Unit)? = null
) {
    EmptyState(
        title = "Chưa có playlist",
        description = "Bạn chưa tạo playlist nào. Hãy tạo playlist đầu tiên để tổ chức nhạc của mình.",
        icon = Icons.Default.QueueMusic,
        actionText = if (onCreatePlaylist != null) "Tạo playlist" else null,
        onActionClick = onCreatePlaylist,
        modifier = modifier
    )
}

@Composable
fun NoFavoritesState(
    modifier: Modifier = Modifier
) {
    EmptyState(
        title = "Chưa có bài hát yêu thích",
        description = "Bạn chưa thêm bài hát nào vào danh sách yêu thích. Nhấn vào biểu tượng trái tim để thêm bài hát.",
        icon = Icons.Default.FavoriteBorder,
        modifier = modifier
    )
}

@Composable
fun EmptyPlaylistState(
    playlistName: String,
    modifier: Modifier = Modifier,
    onAddSongs: (() -> Unit)? = null
) {
    EmptyState(
        title = "Playlist trống",
        description = "\"$playlistName\" chưa có bài hát nào. Hãy thêm một số bài hát để bắt đầu nghe.",
        icon = Icons.Default.PlaylistAdd,
        actionText = if (onAddSongs != null) "Thêm bài hát" else null,
        onActionClick = onAddSongs,
        modifier = modifier
    )
}

@Composable
fun NoPermissionState(
    modifier: Modifier = Modifier,
    onGrantPermission: (() -> Unit)? = null
) {
    EmptyState(
        title = "Cần quyền truy cập",
        description = "Ứng dụng cần quyền truy cập bộ nhớ để đọc các tệp nhạc của bạn. Vui lòng cấp quyền để tiếp tục.",
        icon = Icons.Default.Security,
        actionText = if (onGrantPermission != null) "Cấp quyền" else null,
        onActionClick = onGrantPermission,
        modifier = modifier
    )
}
