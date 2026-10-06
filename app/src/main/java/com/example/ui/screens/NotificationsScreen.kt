package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.NotificationItem
import com.example.model.NotificationType
import com.example.ui.theme.InTubeCardBorder
import com.example.ui.theme.InTubeDarkBg
import com.example.ui.theme.InTubeGold
import com.example.ui.theme.InTubePink
import com.example.ui.theme.InTubeSurfaceHighlight
import com.example.ui.theme.InTubeSurfaceVariant
import com.example.ui.theme.InTubeTextMuted
import com.example.ui.theme.InTubeTextPrimary
import com.example.ui.theme.InTubeTextSecondary

@Composable
fun NotificationsScreen(
    notifications: List<NotificationItem>,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filterTabs = remember { listOf("All", "Likes", "Comments", "Followers") }

    val filteredList = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            "Likes" -> notifications.filter { it.type == NotificationType.LIKE }
            "Comments" -> notifications.filter { it.type == NotificationType.COMMENT }
            "Followers" -> notifications.filter { it.type == NotificationType.FOLLOW }
            else -> notifications
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(InTubeDarkBg)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Header with Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x22FFFFFF))
                    .testTag("notifications_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Feed",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Activity",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = InTubeTextPrimary
                )
                Text(
                    text = "Recent interactions and alerts",
                    fontSize = 12.sp,
                    color = InTubeTextMuted
                )
            }
        }

        // Filter Pills
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterTabs) { tab ->
                val isSelected = selectedFilter == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) InTubePink else InTubeSurfaceVariant)
                        .border(1.dp, if (isSelected) InTubePink else InTubeCardBorder, RoundedCornerShape(20.dp))
                        .clickable { selectedFilter = tab }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else InTubeTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Notifications List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🔔", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No activity yet",
                        fontWeight = FontWeight.Bold,
                        color = InTubeTextPrimary,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Likes, comments, and follows will appear here",
                        color = InTubeTextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("notifications_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    NotificationRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(item: NotificationItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(InTubeSurfaceVariant)
            .border(1.dp, InTubeCardBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar + Action Type Badge
        Box(
            modifier = Modifier.size(46.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = item.userAvatarUrl,
                contentDescription = item.userName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
            )

            // Small badge icon
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(
                        when (item.type) {
                            NotificationType.LIKE -> InTubePink
                            NotificationType.COMMENT -> InTubeGold
                            NotificationType.FOLLOW -> Color(0xFF38EF7D)
                            NotificationType.TRENDING -> Color(0xFF00E5FF)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (item.type) {
                        NotificationType.LIKE -> Icons.Default.Favorite
                        NotificationType.COMMENT -> Icons.Default.Comment
                        NotificationType.FOLLOW -> Icons.Default.PersonAdd
                        NotificationType.TRENDING -> Icons.Default.TrendingUp
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.userName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = InTubeTextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.timeAgo,
                    fontSize = 11.sp,
                    color = InTubeTextMuted
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.content,
                fontSize = 13.sp,
                color = InTubeTextSecondary,
                lineHeight = 17.sp
            )
        }

        // Optional Reel Thumbnail Preview
        if (item.reelThumbnail != null) {
            Spacer(modifier = Modifier.width(10.dp))
            AsyncImage(
                model = item.reelThumbnail,
                contentDescription = "Reel Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
}
