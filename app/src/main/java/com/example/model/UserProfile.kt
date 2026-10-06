package com.example.model

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val handle: String = "",
    val avatarUrl: String = "",
    val bio: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val likesCount: Int = 0,
    val isPremium: Boolean = true,
    val isVerified: Boolean = true
)
