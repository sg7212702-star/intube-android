package com.example.ui.components

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.model.ReelItem
import com.example.ui.theme.InTubePink

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerView(
    reel: ReelItem,
    isActive: Boolean,
    isMuted: Boolean,
    modifier: Modifier = Modifier,
    onSingleTap: () -> Unit = {}
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(false) }
    var hasPlaybackError by remember { mutableStateOf(false) }
    var showPlayIcon by remember { mutableStateOf(false) }

    val videoUri = remember(reel) {
        when {
            !reel.localUriString.isNullOrBlank() -> Uri.parse(reel.localUriString)
            !reel.videoUrl.isNullOrBlank() -> Uri.parse(reel.videoUrl)
            else -> null
        }
    }

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    LaunchedEffect(videoUri, isActive) {
        if (isActive && videoUri != null) {
            hasPlaybackError = false
            try {
                val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                    .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) InTube/1.0")
                    .setAllowCrossProtocolRedirects(true)
                    .setConnectTimeoutMs(15000)
                    .setReadTimeoutMs(15000)

                val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
                val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

                val player = ExoPlayer.Builder(context)
                    .setMediaSourceFactory(mediaSourceFactory)
                    .build()
                    .apply {
                        val mediaItem = MediaItem.fromUri(videoUri)
                        setMediaItem(mediaItem)
                        repeatMode = Player.REPEAT_MODE_ONE
                        volume = if (isMuted) 0f else 1f
                        setWakeMode(androidx.media3.common.C.WAKE_MODE_NONE)
                        prepare()
                        playWhenReady = true

                        addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(playbackState: Int) {
                                isBuffering = playbackState == Player.STATE_BUFFERING
                            }

                            override fun onIsPlayingChanged(playing: Boolean) {
                                isPlaying = playing
                            }

                            override fun onPlayerError(error: PlaybackException) {
                                android.util.Log.w("VideoPlayerView", "Playback error for reel ${reel.id}: ${error.message}")
                                hasPlaybackError = true
                                isBuffering = false
                            }
                        })
                    }
                exoPlayer = player
            } catch (e: Throwable) {
                android.util.Log.e("VideoPlayerView", "ExoPlayer setup failed", e)
                hasPlaybackError = true
                exoPlayer = null
            }
        } else {
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    LaunchedEffect(isMuted) {
        exoPlayer?.volume = if (isMuted) 0f else 1f
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                exoPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.pause()
                        showPlayIcon = true
                    } else {
                        player.play()
                        showPlayIcon = false
                    }
                }
                onSingleTap()
            }
    ) {
        // Thumbnail backdrop
        AsyncImage(
            model = reel.thumbnailUrl,
            contentDescription = reel.caption,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // ExoPlayer Surface View
        if (isActive && videoUri != null && exoPlayer != null && !hasPlaybackError) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        player = exoPlayer
                    }
                },
                update = { view ->
                    if (view.player != exoPlayer) {
                        view.player = exoPlayer
                    }
                },
                onRelease = { view ->
                    view.player = null
                },
                onReset = { view ->
                    view.player = null
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Buffering spinner
        if (isBuffering && isActive) {
            CircularProgressIndicator(
                color = InTubePink,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(44.dp)
                    .align(Alignment.Center)
            )
        }

        // Tap to pause overlay icon
        AnimatedVisibility(
            visible = showPlayIcon,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0x99000000), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
    }
}
