package com.example.model

enum class NotificationType {
    LIKE,
    COMMENT,
    FOLLOW,
    TRENDING
}

data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val userName: String,
    val userAvatarUrl: String,
    val content: String,
    val timeAgo: String,
    val reelThumbnail: String? = null,
    val isRead: Boolean = false
)
