package com.example.dogdetected

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraPreviewScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var graphicOverlay by remember { mutableStateOf<GraphicOverlay?>(null) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    
    // Initialize camera when both views are ready
    LaunchedEffect(previewView, graphicOverlay) {
        if (previewView != null && graphicOverlay != null) {
            try {
                Log.d("CameraPreview", "Starting camera initialization")
                
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                val cameraProvider = withContext(Dispatchers.Main) {
                    cameraProviderFuture.get()
                }
                
                Log.d("CameraPreview", "Camera provider obtained")
                
                // Preview
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView!!.surfaceProvider)
                    }

                // Image Analysis
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, ObjectAnalyzer(graphicOverlay!!))
                    }

                // Camera selector
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalyzer
                )
                
                Log.d("CameraPreview", "Camera initialized successfully")

            } catch (exc: Exception) {
                Log.e("CameraPreview", "Camera initialization failed", exc)
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                Log.d("CameraPreview", "Creating PreviewView")
                PreviewView(ctx).also { view ->
                    previewView = view
                    Log.d("CameraPreview", "PreviewView created")
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Graphic Overlay
        AndroidView(
            factory = { ctx ->
                Log.d("CameraPreview", "Creating GraphicOverlay")
                GraphicOverlay(ctx, null).also { overlay ->
                    graphicOverlay = overlay
                    Log.d("CameraPreview", "GraphicOverlay created")
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
} 