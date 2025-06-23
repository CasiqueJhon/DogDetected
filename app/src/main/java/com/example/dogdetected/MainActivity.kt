package com.example.dogdetected

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.dogdetected.ui.theme.DogDetectedTheme

class MainActivity : ComponentActivity() {
    
    private var permissionCallback: ((Boolean) -> Unit)? = null
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, proceed
            permissionCallback?.invoke(true)
        } else {
            Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
            permissionCallback?.invoke(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            DogDetectedTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var hasPermission by remember {
                        mutableStateOf(
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                        )
                    }

                    var permissionRequested by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        val currentPermission = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        
                        hasPermission = currentPermission
                        
                        if (!currentPermission && !permissionRequested) {
                            permissionRequested = true
                            permissionCallback = { granted ->
                                hasPermission = granted
                                if (!granted) {
                                    finish()
                                }
                            }
                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }

                    if (hasPermission) {
                        // Use full CameraPreviewScreen with object detection
                        CameraPreviewScreen()
                    } else {
                        // You can add a permission request UI here if needed
                    }
                }
            }
        }
    }
}