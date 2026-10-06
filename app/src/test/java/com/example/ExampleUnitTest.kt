package com.example

import com.example.data.InTubeRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun toggleLike_updatesReelLikeStateAndCount() {
    val repo = InTubeRepository()
    val reel = repo.reels.value.first()
    val initialLikes = reel.likesCount
    val initialIsLiked = reel.isLiked

    repo.toggleLike(reel.id)

    val updatedReel = repo.reels.value.first { it.id == reel.id }
    assertEquals(!initialIsLiked, updatedReel.isLiked)
    val expectedCount = if (updatedReel.isLiked) initialLikes + 1 else initialLikes - 1
    assertEquals(expectedCount, updatedReel.likesCount)
  }

  @Test
  fun addComment_addsCommentAndIncrementsCount() {
    val repo = InTubeRepository()
    val reel = repo.reels.value.first()
    val initialCommentCount = reel.commentsCount

    val comment = repo.addComment(reel.id, "Epic video! 🔥")

    val updatedReel = repo.reels.value.first { it.id == reel.id }
    assertEquals(initialCommentCount + 1, updatedReel.commentsCount)
    val comments = repo.comments.value[reel.id] ?: emptyList()
    assertTrue(comments.any { it.id == comment.id && it.commentText == "Epic video! 🔥" })
  }

  @Test
  fun toggleFollow_updatesFollowedSet() {
    val repo = InTubeRepository()
    val creatorId = "creator_1"
    val initialFollowingCount = repo.currentUser.value.followingCount
    assertFalse(repo.followedCreatorIds.value.contains(creatorId))

    repo.toggleFollow(creatorId)
    assertTrue(repo.followedCreatorIds.value.contains(creatorId))
    assertEquals(initialFollowingCount + 1, repo.currentUser.value.followingCount)

    repo.toggleFollow(creatorId)
    assertFalse(repo.followedCreatorIds.value.contains(creatorId))
    assertEquals(initialFollowingCount, repo.currentUser.value.followingCount)
  }
}
