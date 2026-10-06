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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.ReelItem
import com.example.model.UserProfile
import com.example.ui.theme.InTubeCardBorder
import com.example.ui.theme.InTubeDarkBg
import com.example.ui.theme.InTubeGold
import com.example.ui.theme.InTubePink
import com.example.ui.theme.InTubeSurface
import com.example.ui.theme.InTubeSurfaceHighlight
import com.example.ui.theme.InTubeSurfaceVariant
import com.example.ui.theme.InTubeTextMuted
import com.example.ui.theme.InTubeTextPrimary
import com.example.ui.theme.InTubeTextSecondary

@Composable
fun ProfileScreen(
    user: UserProfile?,
    allReels: List<ReelItem>,
    isEditProfileOpen: Boolean,
    onOpenEditProfile: () -> Unit,
    onCloseEditProfile: () -> Unit,
    onSaveProfile: (displayName: String, handle: String, bio: String) -> Unit,
    onSelectReel: (ReelItem) -> Unit,
    onShareProfile: () -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (user == null) return

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("My Reels", "Liked", "Saved")

    val myReels = remember(allReels, user.uid) {
        allReels.filter { it.creatorId == user.uid }
    }
    val likedReels = remember(allReels) {
        allReels.filter { it.isLiked }
    }
    val savedReels = remember(allReels) {
        allReels.filter { it.isSaved }
    }

    val displayReels = when (selectedTabIndex) {
        0 -> myReels
        1 -> likedReels
        2 -> savedReels
        else -> myReels
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(InTubeDarkBg)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Top Bar with Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x22FFFFFF))
                    .testTag("profile_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = user.handle,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = InTubeTextPrimary
            )

            IconButton(
                onClick = onShareProfile,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x22FFFFFF))
                    .testTag("profile_share_header_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Profile",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Profile Info Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar in Gradient Ring with Premium Badge
            Box(
                modifier = Modifier.size(92.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(InTubePink, InTubeGold)
                            )
                        )
                        .padding(3.dp)
                        .clip(CircleShape)
                ) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = user.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Premium Gem Badge
                if (user.isPremium) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(InTubeGold)
                            .border(2.dp, Color.Black, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "💎", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Name + Verified Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user.displayName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = InTubeTextPrimary
                )
                if (user.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = InTubeGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = user.handle,
                fontSize = 13.sp,
                color = InTubePink,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stats Row: Posts, Followers, Following, Likes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileStatItem(label = "Reels", value = "${myReels.size}")
                ProfileStatItem(label = "Followers", value = formatCount(user.followersCount))
                ProfileStatItem(label = "Following", value = formatCount(user.followingCount))
                ProfileStatItem(label = "Likes", value = formatCount(user.likesCount))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bio
            Text(
                text = user.bio,
                fontSize = 13.sp,
                color = InTubeTextSecondary,
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Edit Profile, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenEditProfile,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("edit_profile_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InTubeSurfaceVariant,
                        contentColor = InTubeTextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, InTubeCardBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = InTubePink,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Edit Profile", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onShareProfile,
                    modifier = Modifier
                        .height(42.dp)
                        .testTag("share_profile_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = InTubeTextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, InTubeCardBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Profile",
                        tint = InTubeGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Tab Row: My Reels, Liked, Saved
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = InTubeDarkBg,
            contentColor = InTubePink,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = InTubePink,
                    height = 2.5.dp
                )
            },
            divider = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(InTubeCardBorder)
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.GridOn
                                    1 -> Icons.Default.Favorite
                                    else -> Icons.Default.Bookmark
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTabIndex == index) InTubePink else InTubeTextMuted
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTabIndex == index) InTubeTextPrimary else InTubeTextMuted
                            )
                        }
                    }
                )
            }
        }

        // Reels Grid for selected tab
        if (displayReels.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when (selectedTabIndex) {
                            0 -> "📹"
                            1 -> "❤️"
                            else -> "🔖"
                        },
                        fontSize = 40.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = when (selectedTabIndex) {
                            0 -> "No reels posted yet"
                            1 -> "No liked reels"
                            else -> "No saved reels"
                        },
                        fontWeight = FontWeight.Bold,
                        color = InTubeTextPrimary,
                        fontSize = 16.sp
                    )
                    Text(
                        text = when (selectedTabIndex) {
                            0 -> "Tap ＋ at the bottom to publish your first reel!"
                            1 -> "Double tap reels in the feed to like them"
                            else -> "Tap the bookmark icon on any reel to save it here"
                        },
                        color = InTubeTextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(displayReels, key = { it.id }) { reel ->
                    ProfileReelThumbnail(
                        reel = reel,
                        onClick = { onSelectReel(reel) }
                    )
                }
            }
        }
    }

    // Edit Profile Dialog
    if (isEditProfileOpen) {
        EditProfileDialog(
            user = user,
            onDismiss = onCloseEditProfile,
            onSave = onSaveProfile
        )
    }
}

@Composable
private fun ProfileStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = InTubeTextPrimary
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = InTubeTextMuted
        )
    }
}

@Composable
private fun ProfileReelThumbnail(
    reel: ReelItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag("profile_reel_${reel.id}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = InTubeSurfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = reel.thumbnailUrl,
                contentDescription = reel.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Views / Likes indicator
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .background(Color(0x99000000), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${reel.likesCount}",
                    fontSize = 9.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EditProfileDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onSave: (displayName: String, handle: String, bio: String) -> Unit
) {
    var name by remember { mutableStateOf(user.displayName) }
    var handle by remember { mutableStateOf(user.handle) }
    var bio by remember { mutableStateOf(user.bio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = InTubeSurface,
        title = {
            Text(
                text = "Edit Profile ✏️",
                fontWeight = FontWeight.Bold,
                color = InTubeTextPrimary,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name", color = InTubeTextMuted) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InTubeSurfaceVariant,
                        unfocusedContainerColor = InTubeSurfaceVariant,
                        focusedBorderColor = InTubePink,
                        unfocusedBorderColor = InTubeCardBorder,
                        focusedTextColor = InTubeTextPrimary,
                        unfocusedTextColor = InTubeTextPrimary
                    )
                )

                OutlinedTextField(
                    value = handle,
                    onValueChange = { handle = it },
                    label = { Text("Handle", color = InTubeTextMuted) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_handle_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InTubeSurfaceVariant,
                        unfocusedContainerColor = InTubeSurfaceVariant,
                        focusedBorderColor = InTubePink,
                        unfocusedBorderColor = InTubeCardBorder,
                        focusedTextColor = InTubeTextPrimary,
                        unfocusedTextColor = InTubeTextPrimary
                    )
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio", color = InTubeTextMuted) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_bio_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InTubeSurfaceVariant,
                        unfocusedContainerColor = InTubeSurfaceVariant,
                        focusedBorderColor = InTubePink,
                        unfocusedBorderColor = InTubeCardBorder,
                        focusedTextColor = InTubeTextPrimary,
                        unfocusedTextColor = InTubeTextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, handle, bio)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = InTubePink,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = InTubeTextSecondary)
            }
        }
    )
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000f)
        count >= 1_000 -> String.format("%.1fK", count / 1_000f)
        else -> count.toString()
    }
}
