package com.example.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ReelItem
import com.example.ui.components.BottomNavBar
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.CreateReelSheet
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.InTubeDarkBg
import com.example.ui.theme.InTubePink
import com.example.ui.theme.InTubeSurfaceVariant

@Composable
fun InTubeApp(
    viewModel: InTubeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Handle back button when not on Home tab
    if (uiState.currentTab != InTubeTab.HOME) {
        BackHandler {
            viewModel.setTab(InTubeTab.HOME)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InTubeDarkBg)
    ) {
        // Main Screen Body
        when (uiState.currentTab) {
            InTubeTab.HOME -> {
                FeedScreen(
                    reels = uiState.reels,
                    feedMode = uiState.feedMode,
                    isMuted = uiState.isMuted,
                    unreadNotifications = uiState.unreadCount,
                    userAvatarUrl = uiState.currentUser?.avatarUrl,
                    heartBurstEvents = viewModel.heartBurstEvents,
                    initialIndex = uiState.currentReelIndex,
                    onObserveReelDocument = { reelId -> viewModel.observeReelDocument(reelId) },
                    onFeedModeChanged = { viewModel.setFeedMode(it) },
                    onCurrentIndexChanged = { viewModel.setCurrentReelIndex(it) },
                    onToggleLike = { viewModel.toggleLike(it) },
                    onDoubleTapLike = { viewModel.onDoubleTapLike(it) },
                    onToggleSave = { viewModel.toggleSave(it) },
                    onToggleFollow = { viewModel.toggleFollow(it) },
                    onOpenComments = { viewModel.openComments(it) },
                    onShareReel = { reel ->
                        viewModel.shareReel(reel, context)
                    },
                    onToggleMute = { viewModel.toggleMute() },
                    onNotificationsClick = { viewModel.setTab(InTubeTab.NOTIFICATIONS) },
                    onProfileClick = { viewModel.setTab(InTubeTab.PROFILE) }
                )
            }
            InTubeTab.EXPLORE -> {
                ExploreScreen(
                    searchQuery = uiState.searchQuery,
                    selectedCategory = uiState.selectedCategory,
                    reels = uiState.filteredReels,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onCategorySelected = { viewModel.selectCategory(it) },
                    onReelSelected = { selectedReel ->
                        val index = uiState.reels.indexOfFirst { it.id == selectedReel.id }
                        if (index >= 0) {
                            viewModel.setCurrentReelIndex(index)
                        }
                        viewModel.setTab(InTubeTab.HOME)
                    }
                )
            }
            InTubeTab.CREATE -> {
                // Modal sheet handled below
            }
            InTubeTab.NOTIFICATIONS -> {
                NotificationsScreen(
                    notifications = uiState.notifications,
                    onBack = { viewModel.setTab(InTubeTab.HOME) }
                )
            }
            InTubeTab.PROFILE -> {
                ProfileScreen(
                    user = uiState.currentUser,
                    allReels = uiState.reels,
                    isEditProfileOpen = uiState.isEditProfileVisible,
                    onOpenEditProfile = { viewModel.openEditProfile() },
                    onCloseEditProfile = { viewModel.closeEditProfile() },
                    onSaveProfile = { name, handle, bio ->
                        viewModel.saveProfile(name, handle, bio)
                    },
                    onSelectReel = { selectedReel ->
                        val index = uiState.reels.indexOfFirst { it.id == selectedReel.id }
                        if (index >= 0) {
                            viewModel.setCurrentReelIndex(index)
                        }
                        viewModel.setTab(InTubeTab.HOME)
                    },
                    onShareProfile = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Follow ${uiState.currentUser?.displayName} (${uiState.currentUser?.handle}) on InTube 💎")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Profile")
                        context.startActivity(shareIntent)
                    },
                    onBack = { viewModel.setTab(InTubeTab.HOME) }
                )
            }
        }

        // Floating Bottom Navigation Bar
        BottomNavBar(
            currentTab = uiState.currentTab,
            unreadNotifications = uiState.unreadCount,
            onTabSelected = { viewModel.setTab(it) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Comments Bottom Sheet
        if (uiState.activeCommentsReelId != null) {
            CommentsBottomSheet(
                reelId = uiState.activeCommentsReelId!!,
                comments = uiState.comments,
                onDismiss = { viewModel.closeComments() },
                onPostComment = { text ->
                    uiState.activeCommentsReelId?.let { id ->
                        viewModel.addComment(id, text)
                    }
                },
                onToggleLikeComment = { commentId ->
                    uiState.activeCommentsReelId?.let { id ->
                        viewModel.toggleCommentLike(id, commentId)
                    }
                }
            )
        }

        // Create / Upload Reel Sheet
        if (uiState.isCreateSheetVisible) {
            CreateReelSheet(
                isUploading = uiState.isUploading,
                uploadProgress = uiState.uploadProgress,
                onDismiss = { viewModel.closeCreateSheet() },
                onPublish = { caption, mediaUri, tags, music ->
                    viewModel.publishNewReel(caption, mediaUri, tags, music)
                }
            )
        }

        // Custom Floating InTube Toast
        AnimatedVisibility(
            visible = uiState.toastMessage != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 80.dp, start = 32.dp, end = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(InTubeSurfaceVariant)
                    .border(1.dp, InTubePink.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = uiState.toastMessage ?: "",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
