package com.example.model

data class ReelItem(
    val id: String = "",
    val videoUrl: String? = null,
    val localUriString: String? = null,
    val thumbnailUrl: String = "",
    val caption: String = "",
    val tags: List<String> = emptyList(),
    val musicTitle: String = "Original Sound",
    val musicArtist: String = "InTube Creator",
    val creatorId: String = "",
    val creatorName: String = "",
    val creatorHandle: String = "",
    val creatorAvatarUrl: String = "",
    val isVerified: Boolean = false,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val isFollowingCreator: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
