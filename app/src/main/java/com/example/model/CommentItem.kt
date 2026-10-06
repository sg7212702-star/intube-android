package com.example.model

data class CommentItem(
    val id: String = "",
    val reelId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userHandle: String = "",
    val userAvatarUrl: String = "",
    val commentText: String = "",
    val timeAgo: String = "Just now",
    val likesCount: Int = 0,
    val isLiked: Boolean = false
)
