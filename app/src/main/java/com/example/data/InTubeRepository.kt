package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.CommentItem
import com.example.model.NotificationItem
import com.example.model.NotificationType
import com.example.model.ReelItem
import com.example.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

private fun createFirestore(context: Context): FirebaseFirestore? {
    return try {
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    } catch (e: Exception) {
        null
    }
}

private fun createAuth(): FirebaseAuth? {
    return try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        null
    }
}

class InTubeRepository(
    private val db: FirebaseFirestore? = null,
    private val auth: FirebaseAuth? = null
) {
    // Primary constructor resolving the provisioned named database ID
    constructor(context: Context) : this(
        createFirestore(context),
        createAuth()
    )

    private val scope = CoroutineScope(Dispatchers.IO)

    // In-memory local state
    private val _localReels = MutableStateFlow(SampleData.initialReels)
    private val _localComments = MutableStateFlow<Map<String, List<CommentItem>>>(SampleData.sampleComments)
    private val _localNotifications = MutableStateFlow(SampleData.sampleNotifications)
    private val _localCurrentUser = MutableStateFlow(SampleData.currentUser)
    private val _localFollowedCreators = MutableStateFlow(setOf("creator_2", "creator_5"))
    private val _likedReelIds = MutableStateFlow(setOf("reel_2"))
    private val _savedReelIds = MutableStateFlow(setOf("reel_2"))

    val reels: StateFlow<List<ReelItem>> = _localReels.asStateFlow()
    val comments: StateFlow<Map<String, List<CommentItem>>> = _localComments.asStateFlow()
    val notifications: StateFlow<List<NotificationItem>> = _localNotifications.asStateFlow()
    val followedCreatorIds: StateFlow<Set<String>> = _localFollowedCreators.asStateFlow()
    val currentUser: StateFlow<UserProfile> = _localCurrentUser.asStateFlow()

    init {
        if (db != null) {
            seedInitialFirestoreDataIfNeeded()
        }
    }

    private fun currentUserId(): String {
        return auth?.currentUser?.uid ?: _localCurrentUser.value.uid
    }

    // 1. REAL-TIME VIDEO FEED OBSERVATION
    fun observeReels(): Flow<List<ReelItem>> {
        val firestore = db ?: return _localReels.asStateFlow()
        return callbackFlow {
            // Immediately emit current data so the UI renders without blocking
            trySend(_localReels.value)

            val path = "reels"
            val registration = firestore.collection(path)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, path)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            val item = doc.toObject(ReelItem::class.java)
                            item?.copy(id = doc.id)
                        }
                        _localReels.value = list
                        trySend(list)
                    }
                }

            awaitClose { registration.remove() }
        }.catch { error ->
            Log.w("InTubeRepository", "observeReels stream warning: ${error.message}")
            emit(_localReels.value)
        }
    }

    // 1a. REAL-TIME LISTENER ON AN INDIVIDUAL VIDEO DOCUMENT IN FIRESTORE
    fun observeReelDocument(reelId: String): Flow<ReelItem?> {
        val firestore = db ?: return flowOf(_localReels.value.find { it.id == reelId })
        val path = "reels/$reelId"
        return callbackFlow<ReelItem?> {
            val initialLocal = _localReels.value.find { it.id == reelId }
            if (initialLocal != null) {
                trySend(initialLocal)
            }

            val registration = firestore.collection("reels").document(reelId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.GET, path)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val remoteReel = snapshot.toObject(ReelItem::class.java)?.copy(id = snapshot.id)
                        if (remoteReel != null) {
                            // Sync back into local reels list so all observers update
                            _localReels.update { list ->
                                list.map { if (it.id == reelId) remoteReel else it }
                            }
                            trySend(remoteReel)
                        }
                    }
                }

            awaitClose { registration.remove() }
        }.catch { error ->
            Log.w("InTubeRepository", "observeReelDocument error for $reelId: ${error.message}")
            emit(_localReels.value.find { it.id == reelId })
        }
    }

    // 1b. REAL-TIME FOLLOWS COLLECTION OBSERVATION
    fun observeFollows(): Flow<Set<String>> {
        val firestore = db ?: return _localFollowedCreators.asStateFlow()
        val uid = currentUserId()
        val path = "follows"
        return callbackFlow {
            // Immediately emit current follows set
            trySend(_localFollowedCreators.value)

            val registration = firestore.collection(path)
                .whereEqualTo("followerId", uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, path)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val followingSet = snapshot.documents.mapNotNull { doc ->
                            doc.getString("followingId")
                        }.toSet()
                        _localFollowedCreators.value = followingSet
                        trySend(followingSet)
                    }
                }

            awaitClose { registration.remove() }
        }.catch { error ->
            Log.w("InTubeRepository", "observeFollows error: ${error.message}")
            emit(_localFollowedCreators.value)
        }
    }

    // Combined reels Flow enriched with user interaction states (isLiked, isSaved, isFollowing)
    fun observeEnrichedReels(): Flow<List<ReelItem>> {
        return combine(
            observeReels(),
            _likedReelIds,
            _savedReelIds,
            observeFollows()
        ) { reelsList, likes, saved, follows ->
            reelsList.map { reel ->
                reel.copy(
                    isLiked = likes.contains(reel.id),
                    isSaved = saved.contains(reel.id),
                    isFollowingCreator = follows.contains(reel.creatorId)
                )
            }
        }
    }

    // 2. REAL-TIME COMMENTS FOR A REEL
    fun observeComments(reelId: String): Flow<List<CommentItem>> {
        val firestore = db ?: return flowOf(_localComments.value[reelId] ?: emptyList())
        return callbackFlow {
            // Immediately emit local cached comments
            trySend(_localComments.value[reelId] ?: emptyList())

            val path = "reels/$reelId/comments"
            val registration = firestore.collection("reels").document(reelId)
                .collection("comments")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, path)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(CommentItem::class.java)?.copy(id = doc.id)
                        }
                        _localComments.update { it + (reelId to list) }
                        trySend(list)
                    }
                }

            awaitClose { registration.remove() }
        }.catch { error ->
            Log.w("InTubeRepository", "observeComments error for $reelId: ${error.message}")
            emit(_localComments.value[reelId] ?: emptyList())
        }
    }

    // 3. USER PROFILE OBSERVATION
    fun observeCurrentUser(): Flow<UserProfile> {
        val firestore = db ?: return _localCurrentUser.asStateFlow()
        val uid = currentUserId()
        val path = "users/$uid"
        return callbackFlow {
            // Immediately emit local user profile
            trySend(_localCurrentUser.value)

            val registration = firestore.collection("users").document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.GET, path)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val profile = snapshot.toObject(UserProfile::class.java)
                        if (profile != null) {
                            _localCurrentUser.value = profile
                            trySend(profile)
                        }
                    }
                }

            awaitClose { registration.remove() }
        }.catch {
            emit(_localCurrentUser.value)
        }
    }

    // 4. USER INTERACTIONS & MUTATIONS

    fun toggleLike(reelId: String) {
        val uid = currentUserId()
        val isCurrentlyLiked = _likedReelIds.value.contains(reelId)
        val newIsLiked = !isCurrentlyLiked

        // Optimistic local update
        _likedReelIds.update { set ->
            if (newIsLiked) set + reelId else set - reelId
        }

        _localReels.update { list ->
            list.map { reel ->
                if (reel.id == reelId) {
                    val newCount = if (newIsLiked) reel.likesCount + 1 else (reel.likesCount - 1).coerceAtLeast(0)
                    reel.copy(isLiked = newIsLiked, likesCount = newCount)
                } else reel
            }
        }

        val firestore = db ?: return
        scope.launch {
            try {
                val userLikeRef = firestore.collection("users").document(uid).collection("likes").document(reelId)
                val reelRef = firestore.collection("reels").document(reelId)

                if (newIsLiked) {
                    userLikeRef.set(
                        mapOf(
                            "reelId" to reelId,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                    reelRef.update("likesCount", FieldValue.increment(1)).await()
                } else {
                    userLikeRef.delete().await()
                    reelRef.update("likesCount", FieldValue.increment(-1)).await()
                }
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, "reels/$reelId")
            }
        }
    }

    fun setLiked(reelId: String, liked: Boolean) {
        if (_likedReelIds.value.contains(reelId) != liked) {
            toggleLike(reelId)
        }
    }

    fun toggleSave(reelId: String) {
        val uid = currentUserId()
        val isSaved = _savedReelIds.value.contains(reelId)
        val newIsSaved = !isSaved

        _savedReelIds.update { set ->
            if (newIsSaved) set + reelId else set - reelId
        }

        val firestore = db ?: return
        scope.launch {
            try {
                val userSavedRef = firestore.collection("users").document(uid).collection("saved").document(reelId)
                if (newIsSaved) {
                    userSavedRef.set(
                        mapOf(
                            "reelId" to reelId,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                } else {
                    userSavedRef.delete().await()
                }
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.WRITE, "users/$uid/saved/$reelId")
            }
        }
    }

    fun incrementShareCount(reelId: String) {
        _localReels.update { list ->
            list.map { reel ->
                if (reel.id == reelId) {
                    reel.copy(sharesCount = reel.sharesCount + 1)
                } else reel
            }
        }

        val firestore = db ?: return
        scope.launch {
            try {
                firestore.collection("reels").document(reelId)
                    .update("sharesCount", FieldValue.increment(1)).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, "reels/$reelId")
            }
        }
    }

    fun toggleFollow(creatorId: String) {
        val uid = currentUserId()
        val isFollowing = _localFollowedCreators.value.contains(creatorId)
        val newIsFollowing = !isFollowing
        val followDocId = "${uid}_${creatorId}"

        _localFollowedCreators.update { set ->
            if (newIsFollowing) set + creatorId else set - creatorId
        }

        // Also update local user following count
        _localCurrentUser.update { current ->
            val updatedCount = if (newIsFollowing) current.followingCount + 1 else (current.followingCount - 1).coerceAtLeast(0)
            current.copy(followingCount = updatedCount)
        }

        val firestore = db ?: return
        scope.launch {
            try {
                val followRef = firestore.collection("follows").document(followDocId)
                if (newIsFollowing) {
                    followRef.set(
                        mapOf(
                            "followerId" to uid,
                            "followingId" to creatorId,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    ).await()

                    // Increment following count on user profile
                    firestore.collection("users").document(uid)
                        .set(mapOf("followingCount" to FieldValue.increment(1)), SetOptions.merge())
                } else {
                    followRef.delete().await()

                    // Decrement following count on user profile
                    firestore.collection("users").document(uid)
                        .set(mapOf("followingCount" to FieldValue.increment(-1)), SetOptions.merge())
                }
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.WRITE, "follows/$followDocId")
            }
        }
    }

    fun addComment(reelId: String, text: String): CommentItem {
        val user = _localCurrentUser.value
        val commentId = UUID.randomUUID().toString()
        val newComment = CommentItem(
            id = commentId,
            reelId = reelId,
            userId = user.uid,
            userName = user.displayName,
            userHandle = user.handle,
            userAvatarUrl = user.avatarUrl,
            commentText = text,
            timeAgo = "Just now",
            likesCount = 0,
            isLiked = false
        )

        // Optimistic local update
        _localComments.update { map ->
            val existing = map[reelId] ?: emptyList()
            map + (reelId to (listOf(newComment) + existing))
        }

        _localReels.update { list ->
            list.map { reel ->
                if (reel.id == reelId) reel.copy(commentsCount = reel.commentsCount + 1) else reel
            }
        }

        val firestore = db ?: return newComment
        scope.launch {
            try {
                val commentRef = firestore.collection("reels").document(reelId).collection("comments").document(commentId)
                val reelRef = firestore.collection("reels").document(reelId)

                commentRef.set(
                    mapOf(
                        "id" to commentId,
                        "reelId" to reelId,
                        "userId" to user.uid,
                        "userName" to user.displayName,
                        "userHandle" to user.handle,
                        "userAvatarUrl" to user.avatarUrl,
                        "commentText" to text,
                        "likesCount" to 0,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()

                reelRef.update("commentsCount", FieldValue.increment(1)).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, "reels/$reelId/comments/$commentId")
            }
        }

        return newComment
    }

    fun toggleCommentLike(reelId: String, commentId: String) {
        _localComments.update { map ->
            val list = map[reelId] ?: return@update map
            val updated = list.map { c ->
                if (c.id == commentId) {
                    val newLiked = !c.isLiked
                    val newCount = if (newLiked) c.likesCount + 1 else (c.likesCount - 1).coerceAtLeast(0)
                    c.copy(isLiked = newLiked, likesCount = newCount)
                } else c
            }
            map + (reelId to updated)
        }
    }

    fun addReel(
        caption: String,
        mediaUriString: String?,
        thumbnailUriString: String?,
        tags: List<String>,
        musicTitle: String
    ): ReelItem {
        val user = _localCurrentUser.value
        val reelId = UUID.randomUUID().toString()
        val newReel = ReelItem(
            id = reelId,
            videoUrl = null,
            localUriString = mediaUriString,
            thumbnailUrl = thumbnailUriString ?: user.avatarUrl,
            caption = caption,
            tags = tags,
            musicTitle = musicTitle.ifBlank { "Original Audio - ${user.displayName}" },
            musicArtist = user.displayName,
            creatorId = user.uid,
            creatorName = user.displayName,
            creatorHandle = user.handle,
            creatorAvatarUrl = user.avatarUrl,
            isVerified = user.isVerified,
            likesCount = 1,
            commentsCount = 0,
            sharesCount = 0,
            isLiked = true,
            isSaved = false,
            isFollowingCreator = true,
            timestamp = System.currentTimeMillis()
        )

        _localReels.update { listOf(newReel) + it }
        _likedReelIds.update { it + reelId }

        val firestore = db ?: return newReel
        scope.launch {
            try {
                firestore.collection("reels").document(reelId).set(
                    mapOf(
                        "id" to reelId,
                        "videoUrl" to (newReel.videoUrl ?: ""),
                        "localUriString" to (newReel.localUriString ?: ""),
                        "thumbnailUrl" to newReel.thumbnailUrl,
                        "caption" to newReel.caption,
                        "tags" to newReel.tags,
                        "musicTitle" to newReel.musicTitle,
                        "musicArtist" to newReel.musicArtist,
                        "creatorId" to newReel.creatorId,
                        "creatorName" to newReel.creatorName,
                        "creatorHandle" to newReel.creatorHandle,
                        "creatorAvatarUrl" to newReel.creatorAvatarUrl,
                        "isVerified" to newReel.isVerified,
                        "likesCount" to 1,
                        "commentsCount" to 0,
                        "sharesCount" to 0,
                        "timestamp" to newReel.timestamp,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, "reels/$reelId")
            }
        }

        return newReel
    }

    fun markNotificationsAsRead() {
        _localNotifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
    }

    fun updateProfile(displayName: String, handle: String, bio: String) {
        val user = _localCurrentUser.value.copy(
            displayName = displayName.trim(),
            handle = if (handle.startsWith("@")) handle.trim() else "@${handle.trim()}",
            bio = bio.trim()
        )
        _localCurrentUser.value = user

        val firestore = db ?: return
        scope.launch {
            try {
                firestore.collection("users").document(user.uid).set(
                    mapOf(
                        "uid" to user.uid,
                        "displayName" to user.displayName,
                        "handle" to user.handle,
                        "avatarUrl" to user.avatarUrl,
                        "bio" to user.bio,
                        "followersCount" to user.followersCount,
                        "followingCount" to user.followingCount,
                        "likesCount" to user.likesCount,
                        "isPremium" to user.isPremium,
                        "isVerified" to user.isVerified,
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                ).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, "users/${user.uid}")
            }
        }
    }

    private fun seedInitialFirestoreDataIfNeeded() {
        val firestore = db ?: return
        scope.launch {
            try {
                val existing = firestore.collection("reels").limit(1).get().await()
                if (existing.isEmpty) {
                    SampleData.initialReels.forEach { reel ->
                        firestore.collection("reels").document(reel.id).set(
                            mapOf(
                                "id" to reel.id,
                                "videoUrl" to (reel.videoUrl ?: ""),
                                "thumbnailUrl" to reel.thumbnailUrl,
                                "caption" to reel.caption,
                                "tags" to reel.tags,
                                "musicTitle" to reel.musicTitle,
                                "musicArtist" to reel.musicArtist,
                                "creatorId" to reel.creatorId,
                                "creatorName" to reel.creatorName,
                                "creatorHandle" to reel.creatorHandle,
                                "creatorAvatarUrl" to reel.creatorAvatarUrl,
                                "isVerified" to reel.isVerified,
                                "likesCount" to reel.likesCount,
                                "commentsCount" to reel.commentsCount,
                                "sharesCount" to reel.sharesCount,
                                "timestamp" to reel.timestamp,
                                "createdAt" to FieldValue.serverTimestamp()
                            )
                        ).await()
                    }
                }
            } catch (e: Exception) {
                Log.w("InTubeRepository", "Initial seed check: ${e.message}")
            }
        }
    }
}
