package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.ReelItem
import com.example.ui.FeedMode
import com.example.ui.components.HeartBurstOverlay
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.InTubeGold
import com.example.ui.theme.InTubePink
import com.example.ui.theme.InTubeTextPrimary
import com.example.ui.theme.InTubeTextSecondary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FeedScreen(
    reels: List<ReelItem>,
    feedMode: FeedMode,
    isMuted: Boolean,
    unreadNotifications: Int,
    userAvatarUrl: String?,
    heartBurstEvents: SharedFlow<String>,
    initialIndex: Int = 0,
    onObserveReelDocument: ((String) -> Flow<ReelItem?>)? = null,
    onFeedModeChanged: (FeedMode) -> Unit,
    onCurrentIndexChanged: (Int) -> Unit,
    onToggleLike: (String) -> Unit,
    onDoubleTapLike: (String) -> Unit,
    onToggleSave: (String) -> Unit,
    onToggleFollow: (String) -> Unit,
    onOpenComments: (String) -> Unit,
    onShareReel: (ReelItem) -> Unit,
    onToggleMute: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (reels.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "💎", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Reels in this feed yet",
                    fontWeight = FontWeight.Bold,
                    color = InTubeTextPrimary,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Switch to 'For You' or post a new reel!",
                    color = InTubeTextSecondary,
                    fontSize = 14.sp
                )
            }
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (reels.size - 1).coerceAtLeast(0)),
        pageCount = { reels.size }
    )

    LaunchedEffect(pagerState.currentPage) {
        onCurrentIndexChanged(pagerState.currentPage)
    }

    LaunchedEffect(initialIndex) {
        if (initialIndex in reels.indices && pagerState.currentPage != initialIndex) {
            pagerState.scrollToPage(initialIndex)
        }
    }

    var heartBurstReelId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        heartBurstEvents.collectLatest { reelId ->
            heartBurstReelId = reelId
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        VerticalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val reel = reels[pageIndex]
            val isCurrentPage = pagerState.currentPage == pageIndex

            val pageOffsetProvider = remember(pageIndex, pagerState) {
                {
                    (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                }
            }

            ReelPageItem(
                reel = reel,
                isActive = isCurrentPage,
                isMuted = isMuted,
                pageOffsetProvider = pageOffsetProvider,
                showHeartBurst = heartBurstReelId == reel.id,
                onObserveReelDocument = onObserveReelDocument,
                onHeartBurstEnd = { heartBurstReelId = null },
                onSingleTap = {},
                onDoubleTap = { onDoubleTapLike(reel.id) },
                onToggleLike = { onToggleLike(reel.id) },
                onToggleSave = { onToggleSave(reel.id) },
                onToggleFollow = { onToggleFollow(reel.creatorId) },
                onOpenComments = { onOpenComments(reel.id) },
                onShare = { onShareReel(reel) },
                onToggleMute = onToggleMute,
                onCreatorClick = onProfileClick
            )
        }

        // Top Glass Navigation Bar
        FeedTopBar(
            feedMode = feedMode,
            unreadNotifications = unreadNotifications,
            userAvatarUrl = userAvatarUrl,
            onFeedModeChanged = onFeedModeChanged,
            onNotificationsClick = onNotificationsClick,
            onProfileClick = onProfileClick,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun ReelPageItem(
    reel: ReelItem,
    isActive: Boolean,
    isMuted: Boolean,
    pageOffsetProvider: () -> Float,
    showHeartBurst: Boolean,
    onObserveReelDocument: ((String) -> Flow<ReelItem?>)? = null,
    onHeartBurstEnd: () -> Unit,
    onSingleTap: () -> Unit,
    onDoubleTap: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleSave: () -> Unit,
    onToggleFollow: () -> Unit,
    onOpenComments: () -> Unit,
    onShare: () -> Unit,
    onToggleMute: () -> Unit,
    onCreatorClick: () -> Unit
) {
    // Real-time listener on this individual video document from Firestore
    val liveReelState = onObserveReelDocument?.let { observer ->
        remember(reel.id, observer) { observer(reel.id) }
            .collectAsStateWithLifecycle(initialValue = reel)
    }
    val currentReel = liveReelState?.value?.let { liveDoc ->
        // Preserve local user-specific flags (isLiked/isSaved/isFollowing) while updating real-time counts from Firestore document
        reel.copy(
            likesCount = liveDoc.likesCount,
            commentsCount = liveDoc.commentsCount,
            sharesCount = liveDoc.sharesCount
        )
    } ?: reel

    Box(
        modifier = Modifier
            .fillMaxSize()
            .reelEntranceExitAnimation(pageOffsetProvider)
            .pointerInput(currentReel.id) {
                detectTapGestures(
                    onDoubleTap = { onDoubleTap() },
                    onTap = { onSingleTap() }
                )
            }
    ) {
        // Video / Image Media View
        VideoPlayerView(
            reel = currentReel,
            isActive = isActive,
            isMuted = isMuted,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient shadow over bottom content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x99000000),
                            Color(0xEA050508)
                        )
                    )
                )
        )

        // Double-Tap Bursting Heart Animation
        HeartBurstOverlay(
            isVisible = showHeartBurst,
            onAnimationEnd = onHeartBurstEnd,
            modifier = Modifier.align(Alignment.Center)
        )

        // Right Action Column with Real-time Like and Comment counts
        ReelActionColumn(
            reel = currentReel,
            isMuted = isMuted,
            onToggleLike = onToggleLike,
            onToggleSave = onToggleSave,
            onToggleFollow = onToggleFollow,
            onOpenComments = onOpenComments,
            onShare = onShare,
            onToggleMute = onToggleMute,
            onCreatorClick = onCreatorClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 85.dp)
                .reelActionColumnEntranceExitAnimation(pageOffsetProvider)
        )

        // Bottom Left Creator & Caption Overlay
        ReelInfoOverlay(
            reel = currentReel,
            onToggleFollow = onToggleFollow,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 76.dp, bottom = 85.dp)
                .reelInfoOverlayEntranceExitAnimation(pageOffsetProvider)
        )
    }
}

@Composable
private fun ReelActionColumn(
    reel: ReelItem,
    isMuted: Boolean,
    onToggleLike: () -> Unit,
    onToggleSave: () -> Unit,
    onToggleFollow: () -> Unit,
    onOpenComments: () -> Unit,
    onShare: () -> Unit,
    onToggleMute: () -> Unit,
    onCreatorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Creator Avatar with Gradient Ring and Follow "+" button
        Box(
            modifier = Modifier.size(52.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(InTubePink, InTubeGold)
                        )
                    )
                    .padding(2.dp)
                    .clip(CircleShape)
                    .clickable { onCreatorClick() }
            ) {
                AsyncImage(
                    model = reel.creatorAvatarUrl,
                    contentDescription = reel.creatorName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            // Follow Badge (Plus when not following, Check when following)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 6.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (reel.isFollowingCreator) Color(0xFF28A745) else InTubePink)
                    .border(1.5.dp, Color.Black, CircleShape)
                    .clickable { onToggleFollow() }
                    .testTag("avatar_follow_badge_${reel.creatorId}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (reel.isFollowingCreator) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = if (reel.isFollowingCreator) "Following" else "Follow",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        // Like Button
        ActionIconButton(
            icon = if (reel.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            label = formatCount(reel.likesCount),
            isActive = reel.isLiked,
            activeColor = InTubePink,
            testTag = "like_button_${reel.id}",
            onClick = onToggleLike
        )

        // Comment Button
        ActionIconButton(
            icon = Icons.Default.Comment,
            label = formatCount(reel.commentsCount),
            isActive = false,
            testTag = "comment_button_${reel.id}",
            onClick = onOpenComments
        )

        // Save / Bookmark Button
        ActionIconButton(
            icon = if (reel.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
            label = if (reel.isSaved) "Saved" else "Save",
            isActive = reel.isSaved,
            activeColor = InTubeGold,
            testTag = "save_button_${reel.id}",
            onClick = onToggleSave
        )

        // Share Button
        ActionIconButton(
            icon = Icons.Default.Share,
            label = if (reel.sharesCount > 0) formatCount(reel.sharesCount) else "Share",
            isActive = false,
            contentDescription = "Share video link with other apps",
            testTag = "share_button_${reel.id}",
            onClick = onShare
        )

        // Mute / Unmute Button
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0x33FFFFFF))
                .border(1.dp, Color(0x33FFFFFF), CircleShape)
                .clickable { onToggleMute() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                contentDescription = if (isMuted) "Unmute" else "Mute",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        // Spinning Music Disc
        SpinningMusicDisc(thumbnailUrl = reel.thumbnailUrl)
    }
}

@Composable
private fun ActionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color = InTubePink,
    contentDescription: String? = null,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor else Color(0x33FFFFFF))
                .border(1.dp, Color(0x33FFFFFF), CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription ?: label.ifBlank { null },
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        if (label.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SpinningMusicDisc(thumbnailUrl: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "disc_spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_angle"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .rotate(angle)
            .clip(CircleShape)
            .background(Color(0xFF1E1E24))
            .border(2.dp, Color(0x40FFFFFF), CircleShape)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        )
    }
}

@Composable
private fun ReelInfoOverlay(
    reel: ReelItem,
    onToggleFollow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpandedCaption by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Creator Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = reel.creatorHandle,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )

            if (reel.isVerified) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified",
                    tint = InTubeGold,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Follow / Following pill button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (reel.isFollowingCreator) Color(0x33FFFFFF) else InTubePink)
                    .border(
                        1.dp,
                        if (reel.isFollowingCreator) Color(0x55FFFFFF) else InTubePink,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onToggleFollow() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("follow_pill_${reel.creatorId}"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (reel.isFollowingCreator) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = if (reel.isFollowingCreator) "Following" else "Follow",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Caption & Tags
        Text(
            text = reel.caption,
            fontSize = 13.sp,
            color = Color(0xFFF2F2F7),
            maxLines = if (isExpandedCaption) 6 else 2,
            lineHeight = 18.sp,
            modifier = Modifier.clickable { isExpandedCaption = !isExpandedCaption }
        )

        if (reel.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                reel.tags.take(3).forEach { tag ->
                    Text(
                        text = "#$tag",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InTubeGold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Music ticker audio row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x33000000))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${reel.musicTitle} • ${reel.musicArtist}",
                fontSize = 11.sp,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FeedTopBar(
    feedMode: FeedMode,
    unreadNotifications: Int,
    userAvatarUrl: String?,
    onFeedModeChanged: (FeedMode) -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // App Logo Left
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Text(
                text = "INTUBE",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                letterSpacing = 1.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "💎",
                fontSize = 15.sp
            )
        }

        // Center Feed Tabs: "Following" | "For You"
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FeedModeTab(
                label = "Following",
                isSelected = feedMode == FeedMode.FOLLOWING,
                onClick = { onFeedModeChanged(FeedMode.FOLLOWING) }
            )
            FeedModeTab(
                label = "For You",
                isSelected = feedMode == FeedMode.FOR_YOU,
                onClick = { onFeedModeChanged(FeedMode.FOR_YOU) }
            )
        }

        // Right Action: Notification bell & profile avatar
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Notification Bell with Badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
                    .clickable { onNotificationsClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )

                if (unreadNotifications > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                            .size(14.dp)
                            .background(Color(0xFFFF0055), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$unreadNotifications",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // User Avatar Thumbnail
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, InTubePink, CircleShape)
                    .clickable { onProfileClick() }
            ) {
                AsyncImage(
                    model = userAvatarUrl,
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun FeedModeTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0x99FFFFFF)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .size(width = 24.dp, height = 2.dp)
                .background(if (isSelected) InTubePink else Color.Transparent, CircleShape)
        )
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000f)
        count >= 1_000 -> String.format("%.1fK", count / 1_000f)
        else -> count.toString()
    }
}

/**
 * Modifier for video feed item entrance and exit animations during vertical scrolling.
 * Dynamically computes scale, alpha, 3D tilt (rotationX), subtle vertical parallax,
 * and adaptive corner rounding as the item approaches or recedes from viewport center.
 */
fun Modifier.reelEntranceExitAnimation(
    pageOffsetProvider: () -> Float
): Modifier = this.graphicsLayer {
    val pageOffset = pageOffsetProvider()
    val absOffset = kotlin.math.abs(pageOffset).coerceIn(0f, 1f)

    // 1. Scale Transition: Smoothly scales between 0.88f (exiting/entering) and 1.0f (centered)
    val scale = 1f - (absOffset * 0.12f)
    scaleX = scale
    scaleY = scale

    // 2. Alpha Transition: Smoothly fades between 0.40f and 1.0f
    alpha = 1f - (absOffset * 0.60f)

    // 3. 3D Perspective Tilt: Rotates on X axis simulating physical reel carousel motion
    rotationX = pageOffset * -12f
    cameraDistance = 16f

    // 4. Parallax Translation: Soft vertical depth offset
    translationY = size.height * (pageOffset * 0.08f)

    // 5. Adaptive Corner Radius: Elegantly rounds corners into an elevated card during transition
    if (absOffset > 0.005f) {
        clip = true
        shape = RoundedCornerShape((absOffset * 24f).dp)
    } else {
        clip = false
    }
}

/**
 * Modifier for right-side action column entrance and exit animations.
 * Slides horizontally off-screen to the right and fades out during scroll exit;
 * slides smoothly into view from the right on entrance.
 */
fun Modifier.reelActionColumnEntranceExitAnimation(
    pageOffsetProvider: () -> Float
): Modifier = this.graphicsLayer {
    val pageOffset = pageOffsetProvider()
    val absOffset = kotlin.math.abs(pageOffset).coerceIn(0f, 1f)

    // Slide out to the right during exit, slide in from right during entrance
    translationX = absOffset * 90.dp.toPx()

    // Smooth fade: exits quickly so actions don't clutter the transition
    alpha = (1f - absOffset * 2.2f).coerceIn(0f, 1f)

    // Subtle scale pop
    val colScale = 1f - (absOffset * 0.2f)
    scaleX = colScale
    scaleY = colScale
}

/**
 * Modifier for bottom-left creator info and caption overlay entrance and exit animations.
 * Slides down and fades out during exit; slides up from bottom on entrance.
 */
fun Modifier.reelInfoOverlayEntranceExitAnimation(
    pageOffsetProvider: () -> Float
): Modifier = this.graphicsLayer {
    val pageOffset = pageOffsetProvider()
    val absOffset = kotlin.math.abs(pageOffset).coerceIn(0f, 1f)

    // Slide down during exit, slide up during entrance
    translationY = absOffset * 70.dp.toPx()

    // Subtle slide inward from left
    translationX = -absOffset * 35.dp.toPx()

    // Smooth fade
    alpha = (1f - absOffset * 2.0f).coerceIn(0f, 1f)
}

