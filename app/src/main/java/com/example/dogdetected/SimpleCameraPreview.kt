package com.example.dogdetected

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

@Composable
fun SimpleCameraPreview() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                Log.d("SimpleCameraPreview", "Creating PreviewView")
                
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    try {
                        Log.d("SimpleCameraPreview", "Camera provider listener triggered")
                        val cameraProvider = cameraProviderFuture.get()
                        
                        val preview = Preview.Builder().build()
                        preview.setSurfaceProvider(surfaceProvider)
                        
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, 
                            cameraSelector, 
                            preview
                        )
                        
                        Log.d("SimpleCameraPreview", "Camera bound successfully")
                        
                    } catch (exc: Exception) {
                        Log.e("SimpleCameraPreview", "Camera binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))
            }
        },
        modifier = Modifier.fillMaxSize()
    )
} 