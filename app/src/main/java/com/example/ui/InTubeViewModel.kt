package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.InTubeRepository
import com.example.model.CommentItem
import com.example.model.NotificationItem
import com.example.model.ReelItem
import com.example.model.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class InTubeTab {
    HOME,
    EXPLORE,
    CREATE,
    NOTIFICATIONS,
    PROFILE
}

enum class FeedMode {
    FOR_YOU,
    FOLLOWING
}

data class InTubeUiState(
    val currentTab: InTubeTab = InTubeTab.HOME,
    val feedMode: FeedMode = FeedMode.FOR_YOU,
    val reels: List<ReelItem> = com.example.data.SampleData.initialReels,
    val filteredReels: List<ReelItem> = com.example.data.SampleData.initialReels,
    val currentReelIndex: Int = 0,
    val isMuted: Boolean = false,
    val activeCommentsReelId: String? = null,
    val comments: List<CommentItem> = emptyList(),
    val notifications: List<NotificationItem> = com.example.data.SampleData.sampleNotifications,
    val unreadCount: Int = com.example.data.SampleData.sampleNotifications.size,
    val currentUser: UserProfile? = com.example.data.SampleData.currentUser,
    val followedCreatorIds: Set<String> = setOf("creator_2", "creator_5"),
    val searchQuery: String = "",
    val selectedCategory: String = "🔥 Trending",
    val isCreateSheetVisible: Boolean = false,
    val isEditProfileVisible: Boolean = false,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val toastMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class InTubeViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: InTubeRepository = InTubeRepository(application)
) : AndroidViewModel(application) {

    private val _currentTab = MutableStateFlow(InTubeTab.HOME)
    private val _feedMode = MutableStateFlow(FeedMode.FOR_YOU)
    private val _currentReelIndex = MutableStateFlow(0)
    private val _isMuted = MutableStateFlow(false)
    private val _activeCommentsReelId = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("🔥 Trending")
    private val _isCreateSheetVisible = MutableStateFlow(false)
    private val _isEditProfileVisible = MutableStateFlow(false)
    private val _isUploading = MutableStateFlow(false)
    private val _uploadProgress = MutableStateFlow(0f)
    private val _toastMessage = MutableStateFlow<String?>(null)

    // Event to show heart burst animation on reel
    private val _heartBurstEvents = MutableSharedFlow<String>()
    val heartBurstEvents: SharedFlow<String> = _heartBurstEvents.asSharedFlow()

    // Real-time comments flow dynamically reacting to selected reel
    private val _realtimeComments = _activeCommentsReelId.flatMapLatest { reelId ->
        if (reelId != null) repository.observeComments(reelId)
        else flowOf(emptyList())
    }

    val uiState: StateFlow<InTubeUiState> = combine(
        combine(
            _currentTab,
            _feedMode,
            _currentReelIndex,
            _isMuted,
            _activeCommentsReelId
        ) { tab, mode, index, muted, commentsId ->
            Tuple5(tab, mode, index, muted, commentsId)
        },
        combine(
            _searchQuery,
            _selectedCategory,
            _isCreateSheetVisible,
            _isEditProfileVisible,
            _isUploading
        ) { search, cat, createVis, editVis, uploading ->
            Tuple5(search, cat, createVis, editVis, uploading)
        },
        combine(
            _uploadProgress,
            _toastMessage,
            repository.observeEnrichedReels(),
            _realtimeComments,
            repository.notifications
        ) { upProg, toast, allReels, commentsList, notifs ->
            Tuple5(upProg, toast, allReels, commentsList, notifs)
        },
        combine(
            repository.observeCurrentUser(),
            repository.observeFollows()
        ) { user, follows ->
            Pair(user, follows)
        }
    ) { t1, t2, t3, t4 ->
        val (tab, mode, index, muted, commentsId) = t1
        val (search, cat, createVis, editVis, uploading) = t2
        val (upProg, toast, allReels, commentsList, notifs) = t3
        val (user, follows) = t4

        val filtered = when (mode) {
            FeedMode.FOR_YOU -> allReels
            FeedMode.FOLLOWING -> allReels.filter { it.isFollowingCreator || follows.contains(it.creatorId) }
        }

        val displayReels = if (filtered.isEmpty()) allReels else filtered
        val unread = notifs.count { !it.isRead }

        InTubeUiState(
            currentTab = tab,
            feedMode = mode,
            reels = displayReels,
            filteredReels = filterExploreReels(allReels, search, cat),
            currentReelIndex = index.coerceIn(0, (displayReels.size - 1).coerceAtLeast(0)),
            isMuted = muted,
            activeCommentsReelId = commentsId,
            comments = commentsList,
            notifications = notifs,
            unreadCount = unread,
            currentUser = user,
            followedCreatorIds = follows,
            searchQuery = search,
            selectedCategory = cat,
            isCreateSheetVisible = createVis,
            isEditProfileVisible = editVis,
            isUploading = uploading,
            uploadProgress = upProg,
            toastMessage = toast
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InTubeUiState()
    )

    fun setTab(tab: InTubeTab) {
        if (tab == InTubeTab.CREATE) {
            _isCreateSheetVisible.value = true
        } else {
            _currentTab.value = tab
            if (tab == InTubeTab.NOTIFICATIONS) {
                repository.markNotificationsAsRead()
            }
        }
    }

    fun setFeedMode(FeedMode: FeedMode) {
        _feedMode.value = FeedMode
    }

    fun setCurrentReelIndex(index: Int) {
        _currentReelIndex.value = index
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    fun toggleLike(reelId: String) {
        repository.toggleLike(reelId)
    }

    fun onDoubleTapLike(reelId: String) {
        repository.setLiked(reelId, true)
        viewModelScope.launch {
            _heartBurstEvents.emit(reelId)
        }
    }

    fun toggleSave(reelId: String) {
        repository.toggleSave(reelId)
        showToast("Saved status updated 🔖")
    }

    fun toggleFollow(creatorId: String) {
        val wasFollowing = uiState.value.followedCreatorIds.contains(creatorId)
        repository.toggleFollow(creatorId)
        val reel = uiState.value.reels.find { it.creatorId == creatorId }
        val name = reel?.creatorHandle ?: "creator"
        if (!wasFollowing) {
            showToast("Now following $name ✨")
        } else {
            showToast("Unfollowed $name")
        }
    }

    fun openComments(reelId: String) {
        _activeCommentsReelId.value = reelId
    }

    fun closeComments() {
        _activeCommentsReelId.value = null
    }

    fun addComment(reelId: String, text: String) {
        if (text.isBlank()) return
        repository.addComment(reelId, text.trim())
    }

    fun toggleCommentLike(reelId: String, commentId: String) {
        repository.toggleCommentLike(reelId, commentId)
    }

    fun observeReelDocument(reelId: String) = repository.observeReelDocument(reelId)

    fun shareReel(reel: ReelItem, context: Context) {
        repository.incrementShareCount(reel.id)

        val videoLink = reel.videoUrl?.takeIf { it.isNotBlank() } ?: "https://intube.app/reel/${reel.id}"
        val shareMessage = buildString {
            append("Check out this reel on InTube 💎\n\n")
            if (reel.caption.isNotBlank()) {
                append("\"${reel.caption}\"\n")
            }
            append("by ${reel.creatorName} (${reel.creatorHandle})\n\n")
            append("Watch Video: $videoLink\n")
            if (reel.tags.isNotEmpty()) {
                append("\n${reel.tags.joinToString(" ")}")
            }
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "InTube: ${reel.caption.ifBlank { "Watch Reel" }}")
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }
        val chooserIntent = Intent.createChooser(sendIntent, "Share Video Link").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            showToast("Unable to open share menu")
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun openCreateSheet() {
        _isCreateSheetVisible.value = true
    }

    fun closeCreateSheet() {
        _isCreateSheetVisible.value = false
    }

    fun openEditProfile() {
        _isEditProfileVisible.value = true
    }

    fun closeEditProfile() {
        _isEditProfileVisible.value = false
    }

    fun saveProfile(displayName: String, handle: String, bio: String) {
        repository.updateProfile(displayName, handle, bio)
        _isEditProfileVisible.value = false
        showToast("Profile synced to Firestore ✨")
    }

    fun publishNewReel(caption: String, mediaUri: String?, tags: List<String>, musicTitle: String) {
        viewModelScope.launch {
            _isUploading.value = true
            _uploadProgress.value = 0.2f
            delay(300)
            _uploadProgress.value = 0.6f
            delay(400)
            _uploadProgress.value = 0.9f
            delay(300)
            _uploadProgress.value = 1.0f
            delay(200)

            repository.addReel(
                caption = caption,
                mediaUriString = mediaUri,
                thumbnailUriString = mediaUri,
                tags = tags,
                musicTitle = musicTitle
            )

            _isUploading.value = false
            _uploadProgress.value = 0f
            _isCreateSheetVisible.value = false
            _currentTab.value = InTubeTab.HOME
            _currentReelIndex.value = 0
            showToast("Reel posted to InTube! 🚀")
        }
    }

    fun showToast(message: String) {
        _toastMessage.value = message
        viewModelScope.launch {
            delay(2500)
            if (_toastMessage.value == message) {
                _toastMessage.value = null
            }
        }
    }

    fun dismissToast() {
        _toastMessage.value = null
    }

    private fun filterExploreReels(reels: List<ReelItem>, query: String, category: String): List<ReelItem> {
        val q = query.trim().lowercase()
        return reels.filter { reel ->
            val matchesQuery = if (q.isEmpty()) true else {
                reel.caption.lowercase().contains(q) ||
                    reel.creatorName.lowercase().contains(q) ||
                    reel.creatorHandle.lowercase().contains(q) ||
                    reel.tags.any { it.lowercase().contains(q) } ||
                    reel.musicTitle.lowercase().contains(q)
            }
            val matchesCat = if (category == "🔥 Trending") true else {
                val cleanCat = category.substringAfter(" ").lowercase()
                reel.tags.any { it.lowercase().contains(cleanCat) } ||
                    reel.caption.lowercase().contains(cleanCat)
            }
            matchesQuery && matchesCat
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E
)
