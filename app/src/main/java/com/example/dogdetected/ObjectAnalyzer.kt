package com.example.dogdetected

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions


class ObjectAnalyzer(graphicOverlay: GraphicOverlay) : ImageAnalysis.Analyzer {

    val localModel = LocalModel.Builder()
        .setAssetFilePath("model_meta.tflite")
        .build()

    // Live detection and tracking
    val customObjectDetectorOptions =
        CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .setClassificationConfidenceThreshold(0.5f) // Reducir umbral para más detecciones
            .setMaxPerObjectLabelCount(3)
            .build()

    val objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)
    
    init {
        Log.d("ObjectAnalyzer", "ObjectAnalyzer initialized with model: model_meta.tflite")
        Log.d("ObjectAnalyzer", "Confidence threshold: 0.5f")
    }


    val overlay = graphicOverlay
    private val lensFacing = CameraSelector.LENS_FACING_BACK

    @SuppressLint("UnsafeExperimentalUsageError")
    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        
        Log.d("ObjectAnalyzer", "Analyzing frame - Size: ${imageProxy.width}x${imageProxy.height}, Rotation: $rotationDegrees")
        
        if (rotationDegrees == 0 || rotationDegrees == 180) {
            overlay.setImageSourceInfo(
                imageProxy.width, imageProxy.height, isImageFlipped
            )
        } else {
            overlay.setImageSourceInfo(
                imageProxy.height, imageProxy.width, isImageFlipped
            )
        }
        
        val frame = InputImage.fromMediaImage(
            imageProxy.image!!,
            imageProxy.imageInfo.rotationDegrees
        )
        
        Log.d("ObjectAnalyzer", "Processing frame with MLKit...")
        
        objectDetector.process(frame)
                .addOnSuccessListener { detectedObjects ->
                    Log.d("ObjectAnalyzer", "Detection completed - Found ${detectedObjects.size} objects")
                    
                    // Task completed successfully
                    overlay.clear()
                    
                    for (detectedObject in detectedObjects) {
                        Log.d("ObjectAnalyzer", "Object detected:")
                        Log.d("ObjectAnalyzer", "  - Tracking ID: ${detectedObject.trackingId}")
                        Log.d("ObjectAnalyzer", "  - Bounding box: ${detectedObject.boundingBox}")
                        Log.d("ObjectAnalyzer", "  - Labels count: ${detectedObject.labels.size}")
                        
                        for (label in detectedObject.labels) {
                            Log.d("ObjectAnalyzer", "    Label: ${label.text}, Confidence: ${label.confidence}, Index: ${label.index}")
                        }
                        
                        val objGraphic = ObjectGraphic(this.overlay, detectedObject)
                        this.overlay.add(objGraphic)
                    }
                    this.overlay.postInvalidate()
                }

                .addOnFailureListener { e ->
                    Log.e("ObjectAnalyzer", "Object detection failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
    }

} 