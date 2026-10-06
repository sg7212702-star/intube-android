package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.InTubeCardBorder
import com.example.ui.theme.InTubeGold
import com.example.ui.theme.InTubePink
import com.example.ui.theme.InTubeSurface
import com.example.ui.theme.InTubeSurfaceHighlight
import com.example.ui.theme.InTubeSurfaceVariant
import com.example.ui.theme.InTubeTextMuted
import com.example.ui.theme.InTubeTextPrimary
import com.example.ui.theme.InTubeTextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateReelSheet(
    isUploading: Boolean,
    uploadProgress: Float,
    onDismiss: () -> Unit,
    onPublish: (caption: String, mediaUri: String?, tags: List<String>, musicTitle: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isCameraMode by remember { mutableStateOf(false) }
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var caption by remember { mutableStateOf("") }
    var musicTitle by remember { mutableStateOf("") }
    val selectedTags = remember { mutableStateListOf<String>("viral", "intube") }

    val presetTags = remember {
        listOf("viral", "intube", "aesthetic", "dance", "tech", "cinematic", "vlog", "funny")
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedMediaUri = uri
        }
    }

    if (isCameraMode) {
        CameraRecordingView(
            onVideoCaptured = { uri ->
                selectedMediaUri = uri
                isCameraMode = false
            },
            onChooseFromGallery = {
                isCameraMode = false
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
            },
            onClose = { isCameraMode = false }
        )
        return
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = InTubeSurface,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .windowInsetsPadding(WindowInsets.ime)
                .padding(horizontal = 20.dp)
        ) {
            // Drag handle pill
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .background(Color(0x40FFFFFF), CircleShape)
                    .align(Alignment.CenterHorizontally)
            )

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Create New Reel",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = InTubeTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "💎",
                        fontSize = 16.sp
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp),
                    enabled = !isUploading
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = InTubeTextSecondary
                    )
                }
            }

            // Scrollable form
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Media Picker Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(InTubeSurfaceVariant)
                        .border(1.dp, InTubeCardBorder, RoundedCornerShape(16.dp))
                        .clickable(enabled = !isUploading) {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedMediaUri != null) {
                        AsyncImage(
                            model = selectedMediaUri,
                            contentDescription = "Selected Media Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Tap to change overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .background(Color(0x80000000), CircleShape)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Change media",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                // Camera Button
                                Button(
                                    onClick = { isCameraMode = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = InTubePink),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.testTag("record_camera_button")
                                ) {
                                    Text("📹 Record Video", fontWeight = FontWeight.Bold)
                                }

                                // Gallery Button
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                        )
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.testTag("pick_gallery_button")
                                ) {
                                    Text("🖼️ Gallery")
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Record up to 15s or select from storage",
                                color = InTubeTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Caption Field
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    placeholder = {
                        Text("Write a captivating caption...", color = InTubeTextMuted, fontSize = 14.sp)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reel_caption_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InTubeSurfaceVariant,
                        unfocusedContainerColor = InTubeSurfaceVariant,
                        focusedBorderColor = InTubePink,
                        unfocusedBorderColor = InTubeCardBorder,
                        focusedTextColor = InTubeTextPrimary,
                        unfocusedTextColor = InTubeTextPrimary
                    ),
                    minLines = 3,
                    maxLines = 5,
                    enabled = !isUploading
                )

                // Sound / Music input
                OutlinedTextField(
                    value = musicTitle,
                    onValueChange = { musicTitle = it },
                    placeholder = {
                        Text("Soundtrack (e.g. Cyber Tokyo Beats)", color = InTubeTextMuted, fontSize = 14.sp)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = InTubeGold,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reel_music_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InTubeSurfaceVariant,
                        unfocusedContainerColor = InTubeSurfaceVariant,
                        focusedBorderColor = InTubeGold,
                        unfocusedBorderColor = InTubeCardBorder,
                        focusedTextColor = InTubeTextPrimary,
                        unfocusedTextColor = InTubeTextPrimary
                    ),
                    singleLine = true,
                    enabled = !isUploading
                )

                // Hashtags Selector
                Column {
                    Text(
                        text = "Add Hashtags",
                        fontWeight = FontWeight.SemiBold,
                        color = InTubeTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetTags.forEach { tag ->
                            val isSelected = selectedTags.contains(tag)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) InTubePink else InTubeSurfaceHighlight
                                    )
                                    .clickable(enabled = !isUploading) {
                                        if (isSelected) selectedTags.remove(tag)
                                        else selectedTags.add(tag)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = "#$tag",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) Color.White else InTubeTextSecondary
                                )
                            }
                        }
                    }
                }

                // Progress Bar (when uploading)
                AnimatedVisibility(visible = isUploading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Publishing to InTube...",
                                color = InTubePink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${(uploadProgress * 100).toInt()}%",
                                color = InTubeTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { uploadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = InTubePink,
                            trackColor = InTubeSurfaceVariant
                        )
                    }
                }
            }

            // Bottom action buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onPublish(
                            caption.ifBlank { "Check out my new reel on InTube! ✨" },
                            selectedMediaUri?.toString(),
                            selectedTags.toList(),
                            musicTitle.ifBlank { "Original Track" }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("publish_reel_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InTubePink,
                        contentColor = Color.White
                    ),
                    enabled = !isUploading
                ) {
                    Text(
                        text = if (isUploading) "Uploading Reel..." else "Post Reel 🚀",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = InTubeTextSecondary
                    ),
                    border = null,
                    enabled = !isUploading
                ) {
                    Text("Cancel", fontSize = 14.sp)
                }
            }
        }
    }
}
