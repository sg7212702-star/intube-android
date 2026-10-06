package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.camera.MediaCaptureManager
import com.example.ui.theme.InTubeGold
import com.example.ui.theme.InTubePink
import com.example.ui.theme.InTubeSurfaceVariant
import com.example.ui.theme.InTubeTextPrimary
import com.example.ui.theme.InTubeTextSecondary

@Composable
fun CameraRecordingView(
    onVideoCaptured: (Uri) -> Unit,
    onChooseFromGallery: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mediaCaptureManager = remember { MediaCaptureManager(context) }
    val isRecording by mediaCaptureManager.isRecording.collectAsStateWithLifecycle()
    val recordingDuration by mediaCaptureManager.recordingDurationSeconds.collectAsStateWithLifecycle()
    val isTorchOn by mediaCaptureManager.isTorchOn.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaCaptureManager.release()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!hasCameraPermission) {
            // Permission Request State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "📷", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera Permission Required",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = InTubeTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Grant camera and microphone permissions to record short-form video reels directly in InTube.",
                    fontSize = 13.sp,
                    color = InTubeTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = InTubePink)
                ) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onChooseFromGallery,
                    colors = ButtonDefaults.buttonColors(containerColor = InTubeSurfaceVariant)
                ) {
                    Text("Choose from Gallery Instead")
                }
            }
        } else {
            // CameraX Viewfinder
            var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        previewViewRef = this
                        mediaCaptureManager.bindCamera(lifecycleOwner, this)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Top Bar Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0x66000000), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Camera",
                        tint = Color.White
                    )
                }

                // Recording Timer Badge
                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xCCFF0055))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "00:${String.format("%02d", recordingDuration)} / 00:15",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Flash / Torch Toggle
                    IconButton(
                        onClick = { mediaCaptureManager.toggleTorch() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0x66000000), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Torch",
                            tint = if (isTorchOn) InTubeGold else Color.White
                        )
                    }

                    // Camera Switch (Front/Back)
                    IconButton(
                        onClick = {
                            previewViewRef?.let {
                                mediaCaptureManager.switchCamera(lifecycleOwner, it)
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0x66000000), CircleShape),
                        enabled = !isRecording
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Flip Camera",
                            tint = Color.White
                        )
                    }
                }
            }

            // Bottom Controls Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp, vertical = 40.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery Picker Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable(enabled = !isRecording) { onChooseFromGallery() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0x66000000), CircleShape)
                            .border(1.5.dp, Color(0x66FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Gallery", fontSize = 11.sp, color = Color.White)
                }

                // Shutter / Record Button
                Box(
                    modifier = Modifier.size(84.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRecording) {
                        val progress = recordingDuration / 15f
                        val animatedProgress by animateFloatAsState(
                            targetValue = progress,
                            animationSpec = tween(1000),
                            label = "record_progress"
                        )
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(84.dp),
                            color = InTubePink,
                            trackColor = Color(0x33FFFFFF),
                            strokeWidth = 4.dp
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .border(3.dp, Color.White, CircleShape)
                        )
                    }

                    // Record Action Center
                    Box(
                        modifier = Modifier
                            .size(if (isRecording) 36.dp else 64.dp)
                            .clip(if (isRecording) RoundedCornerShape(8.dp) else CircleShape)
                            .background(Color(0xFFFF1744))
                            .clickable {
                                if (isRecording) {
                                    mediaCaptureManager.stopRecording()
                                } else {
                                    val outputFile = mediaCaptureManager.createVideoOutputFile()
                                    mediaCaptureManager.startRecording(
                                        outputFile = outputFile,
                                        maxDurationSeconds = 15,
                                        onRecordingFinished = { savedUri ->
                                            onVideoCaptured(savedUri)
                                        },
                                        onError = { error ->
                                            // Handle error
                                        }
                                    )
                                }
                            }
                            .testTag("camera_shutter_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRecording) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop Recording",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Mode Indicator placeholder or effect spacer
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0x33000000), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "15s",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = InTubeGold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Short", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}
