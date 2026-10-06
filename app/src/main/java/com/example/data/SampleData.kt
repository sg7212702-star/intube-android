package com.example.data

import com.example.model.CommentItem
import com.example.model.NotificationItem
import com.example.model.NotificationType
import com.example.model.ReelItem
import com.example.model.UserProfile

object SampleData {

    val currentUser = UserProfile(
        uid = "user_me",
        displayName = "Aarav Sharma",
        handle = "@aarav_vids",
        avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80",
        bio = "✨ Digital Creator | Reels & Visual Vibes 🚀\n💎 INTUBE Premium Member\n📍 Mumbai • Tokyo",
        followersCount = 28400,
        followingCount = 312,
        likesCount = 452100,
        isPremium = true,
        isVerified = true
    )

    // Royalty-free public short video streams (H.264 MP4)
    val initialReels = listOf(
        ReelItem(
            id = "reel_1",
            videoUrl = "https://raw.githubusercontent.com/intel-iot-devkit/sample-videos/master/person-bicycle-car-detection.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80",
            caption = "Neon city nights in Neo-Tokyo. Nothing beats midnight drives under holographic billboards! ✨🏎️",
            tags = listOf("tokyo", "cyberpunk", "aesthetic", "neon"),
            musicTitle = "Midnight Drive (Slowed & Reverb)",
            musicArtist = "Kavinsky & Lorn",
            creatorId = "creator_1",
            creatorName = "Elena Rostova",
            creatorHandle = "@elena_visuals",
            creatorAvatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&auto=format&fit=crop&q=80",
            isVerified = true,
            likesCount = 142890,
            commentsCount = 1240,
            sharesCount = 8900,
            isLiked = false,
            isSaved = false,
            isFollowingCreator = false
        ),
        ReelItem(
            id = "reel_2",
            videoUrl = "https://raw.githubusercontent.com/intel-iot-devkit/sample-videos/master/face-demographics-walking-and-pause.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&auto=format&fit=crop&q=80",
            caption = "Hidden waterfalls discovered in the mist mountains of Norway 🏔️💧 Would you take a swim here?",
            tags = listOf("travel", "nature", "wanderlust", "norway"),
            musicTitle = "Wild Spirit Echoes",
            musicArtist = "Aurora Waves",
            creatorId = "creator_2",
            creatorName = "Leo Tanaka",
            creatorHandle = "@tanaka_explores",
            creatorAvatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=400&auto=format&fit=crop&q=80",
            isVerified = true,
            likesCount = 89340,
            commentsCount = 680,
            sharesCount = 4310,
            isLiked = true,
            isSaved = true,
            isFollowingCreator = true
        ),
        ReelItem(
            id = "reel_3",
            videoUrl = "https://raw.githubusercontent.com/intel-iot-devkit/sample-videos/master/classroom.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=800&auto=format&fit=crop&q=80",
            caption = "Street dance battle final round in Paris! The crowd went crazy when the beat dropped 🔥🕺",
            tags = listOf("dance", "streetstyle", "hiphop", "energy"),
            musicTitle = "Bounce with It - Remix",
            musicArtist = "DJ Pulse x Skream",
            creatorId = "creator_3",
            creatorName = "Chloe Bennett",
            creatorHandle = "@chloe_moves",
            creatorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80",
            isVerified = false,
            likesCount = 215400,
            commentsCount = 3450,
            sharesCount = 18200,
            isLiked = false,
            isSaved = false,
            isFollowingCreator = false
        ),
        ReelItem(
            id = "reel_4",
            videoUrl = "https://raw.githubusercontent.com/intel-iot-devkit/sample-videos/master/head-pose-face-detection-female.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800&auto=format&fit=crop&q=80",
            caption = "Authentic artisan ramen crafted over 18 hours in Kyoto. The broth is heavenly rich 🍜🥢",
            tags = listOf("foodie", "ramen", "japan", "delicious"),
            musicTitle = "Kyoto Cafe Lo-Fi",
            musicArtist = "Chillhop Music",
            creatorId = "creator_4",
            creatorName = "Chef Kenji",
            creatorHandle = "@kenji_kitchen",
            creatorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80",
            isVerified = true,
            likesCount = 67200,
            commentsCount = 520,
            sharesCount = 3100,
            isLiked = false,
            isSaved = false,
            isFollowingCreator = false
        ),
        ReelItem(
            id = "reel_5",
            videoUrl = "https://raw.githubusercontent.com/intel-iot-devkit/sample-videos/master/head-pose-face-detection-male.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=800&auto=format&fit=crop&q=80",
            caption = "Insane clutch moment during the championship finals! Rate this 1-10 🎮⚡",
            tags = listOf("gaming", "esports", "clutch", "epic"),
            musicTitle = "Cyber Horizon Synth",
            musicArtist = "RetroDrive",
            creatorId = "creator_5",
            creatorName = "Marcus Vance",
            creatorHandle = "@vance_gaming",
            creatorAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop&q=80",
            isVerified = true,
            likesCount = 310450,
            commentsCount = 4290,
            sharesCount = 25400,
            isLiked = false,
            isSaved = false,
            isFollowingCreator = true
        )
    )

    val sampleComments = mapOf(
        "reel_1" to listOf(
            CommentItem(
                id = "c1",
                reelId = "reel_1",
                userId = "u10",
                userName = "Maya Lin",
                userHandle = "@mayalin",
                userAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop&q=80",
                commentText = "The color grading on this is out of this world! What camera did you shoot with? 😍",
                timeAgo = "2h ago",
                likesCount = 245,
                isLiked = true
            ),
            CommentItem(
                id = "c2",
                reelId = "reel_1",
                userId = "u11",
                userName = "David K.",
                userHandle = "@davidk_tech",
                userAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop&q=80",
                commentText = "INTUBE's bitrate is so sharp, this looks like pure 4K OLED demo! 🔥🔥🔥",
                timeAgo = "5h ago",
                likesCount = 89,
                isLiked = false
            ),
            CommentItem(
                id = "c3",
                reelId = "reel_1",
                userId = "u12",
                userName = "Sora Takahashi",
                userHandle = "@sora_jp",
                userAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&auto=format&fit=crop&q=80",
                commentText = "Welcome to Tokyo! Shinjuku crossing is magic at 2 AM ✨",
                timeAgo = "1d ago",
                likesCount = 34,
                isLiked = false
            )
        ),
        "reel_2" to listOf(
            CommentItem(
                id = "c4",
                reelId = "reel_2",
                userId = "u13",
                userName = "Sarah Jenkins",
                userHandle = "@sarah_j",
                userAvatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200&auto=format&fit=crop&q=80",
                commentText = "Adding this to my bucket list immediately!! The sound of the water is therapeutic 🌊",
                timeAgo = "3h ago",
                likesCount = 112,
                isLiked = true
            )
        ),
        "reel_3" to listOf(
            CommentItem(
                id = "c5",
                reelId = "reel_3",
                userId = "u14",
                userName = "BreakBeat Pro",
                userHandle = "@bboy_flex",
                userAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&auto=format&fit=crop&q=80",
                commentText = "That freeze at 0:04 was completely illegal!! She definitely won that battle 🏆💯",
                timeAgo = "1h ago",
                likesCount = 432,
                isLiked = true
            )
        )
    )

    val sampleNotifications = listOf(
        NotificationItem(
            id = "notif_1",
            type = NotificationType.LIKE,
            userName = "Elena Rostova",
            userAvatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200&auto=format&fit=crop&q=80",
            content = "liked your recent reel \"Midnight Neon\"",
            timeAgo = "5m ago",
            reelThumbnail = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=200&auto=format&fit=crop&q=80",
            isRead = false
        ),
        NotificationItem(
            id = "notif_2",
            type = NotificationType.COMMENT,
            userName = "Chloe Bennett",
            userAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&auto=format&fit=crop&q=80",
            content = "commented: \"Love the transition effect! 🔥\"",
            timeAgo = "32m ago",
            reelThumbnail = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=200&auto=format&fit=crop&q=80",
            isRead = false
        ),
        NotificationItem(
            id = "notif_3",
            type = NotificationType.FOLLOW,
            userName = "Leo Tanaka",
            userAvatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200&auto=format&fit=crop&q=80",
            content = "started following you",
            timeAgo = "2h ago",
            isRead = false
        ),
        NotificationItem(
            id = "notif_4",
            type = NotificationType.TRENDING,
            userName = "InTube Staff",
            userAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop&q=80",
            content = "Your video reached 10,000 views on the #neon hashtag feed! 💎🎉",
            timeAgo = "1d ago",
            isRead = true
        )
    )

    val exploreCategories = listOf(
        "🔥 Trending",
        "🎵 Music",
        "💃 Dance",
        "💻 Tech & AI",
        "🎮 Gaming",
        "🍕 Food",
        "✈️ Travel",
        "🎭 Comedy",
        "⚡ Sports"
    )
}
