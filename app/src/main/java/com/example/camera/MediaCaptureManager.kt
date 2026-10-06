package com.example.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class MediaCaptureManager(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var currentRecording: Recording? = null

    private var currentLensFacing = CameraSelector.DEFAULT_BACK_CAMERA

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationSeconds = MutableStateFlow(0)
    val recordingDurationSeconds: StateFlow<Int> = _recordingDurationSeconds.asStateFlow()

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    private val _isFrontCamera = MutableStateFlow(false)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null

    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onInitialized: () -> Unit = {}
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(lifecycleOwner, previewView)
                onInitialized()
            } catch (e: Exception) {
                Log.e("MediaCaptureManager", "Failed to bind camera use cases", e)
                _errorMessage.value = "Camera initialization failed: ${e.message}"
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val provider = cameraProvider ?: return

        try {
            provider.unbindAll()

            // 1. Preview Use Case
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            // 2. Video Capture Use Case with Recorder with fallback quality selector
            try {
                val qualitySelector = QualitySelector.fromOrderedList(
                    listOf(Quality.HD, Quality.SD, Quality.LOWEST),
                    androidx.camera.video.FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
                )

                val recorder = Recorder.Builder()
                    .setQualitySelector(qualitySelector)
                    .build()

                val capture = VideoCapture.withOutput(recorder)
                videoCapture = capture

                // 3. Bind to Lifecycle with VideoCapture
                camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    currentLensFacing,
                    preview,
                    capture
                )
            } catch (videoError: Exception) {
                Log.w("MediaCaptureManager", "Binding VideoCapture failed, falling back to Preview only: ${videoError.message}")
                videoCapture = null
                // Fallback: Bind preview only so camera viewfinder still works smoothly
                camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    currentLensFacing,
                    preview
                )
            }

            _isTorchOn.value = false
        } catch (e: Exception) {
            Log.e("MediaCaptureManager", "Use case binding failed", e)
            _errorMessage.value = e.message
        }
    }

    fun switchCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        currentLensFacing = if (currentLensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        _isFrontCamera.value = currentLensFacing == CameraSelector.DEFAULT_FRONT_CAMERA
        bindCameraUseCases(lifecycleOwner, previewView)
    }

    fun toggleTorch() {
        val cam = camera ?: return
        if (cam.cameraInfo.hasFlashUnit()) {
            val nextState = !_isTorchOn.value
            cam.cameraControl.enableTorch(nextState)
            _isTorchOn.value = nextState
        }
    }

    fun startRecording(
        outputFile: File,
        maxDurationSeconds: Int = 15,
        onRecordingFinished: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        val capture = videoCapture
        if (capture == null) {
            onError("Camera recorder is not ready")
            return
        }

        val outputOptions = FileOutputOptions.Builder(outputFile).build()
        val hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        var pendingRecording = capture.output.prepareRecording(context, outputOptions)
        if (hasAudioPermission) {
            pendingRecording = pendingRecording.withAudioEnabled()
        }

        _recordingDurationSeconds.value = 0
        startDurationTimer(maxDurationSeconds) {
            stopRecording()
        }

        currentRecording = pendingRecording.start(ContextCompat.getMainExecutor(context)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    _isRecording.value = true
                }
                is VideoRecordEvent.Finalize -> {
                    _isRecording.value = false
                    stopDurationTimer()
                    if (!event.hasError()) {
                        val savedUri = Uri.fromFile(outputFile)
                        onRecordingFinished(savedUri)
                    } else {
                        Log.e("MediaCaptureManager", "Video recording error: ${event.error}")
                        onError("Recording failed: ${event.error}")
                    }
                    currentRecording = null
                }
            }
        }
    }

    fun stopRecording() {
        currentRecording?.stop()
        currentRecording = null
        stopDurationTimer()
        _isRecording.value = false
    }

    private fun startDurationTimer(maxSeconds: Int, onMaxReached: () -> Unit) {
        stopDurationTimer()
        timerRunnable = object : Runnable {
            override fun run() {
                val current = _recordingDurationSeconds.value + 1
                _recordingDurationSeconds.value = current
                if (current >= maxSeconds) {
                    onMaxReached()
                } else {
                    mainHandler.postDelayed(this, 1000)
                }
            }
        }
        mainHandler.postDelayed(timerRunnable!!, 1000)
    }

    private fun stopDurationTimer() {
        timerRunnable?.let { mainHandler.removeCallbacks(it) }
        timerRunnable = null
    }

    fun createVideoOutputFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val storageDir = context.cacheDir
        return File(storageDir, "INTUBE_REC_${timeStamp}.mp4")
    }

    fun release() {
        stopDurationTimer()
        currentRecording?.stop()
        currentRecording = null
        cameraProvider?.unbindAll()
    }
}
