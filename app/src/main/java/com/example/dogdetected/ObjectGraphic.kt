package com.example.dogdetected

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import com.example.dogdetected.GraphicOverlay.Graphic
import com.google.mlkit.vision.objects.DetectedObject
import java.lang.Math.*
import java.util.Locale

/** Draw the detected object info in preview.  */
class ObjectGraphic constructor(
    overlay: GraphicOverlay,
    private val detectedObject: DetectedObject
) : Graphic(overlay) {

    private val numColors = COLORS.size

    private val boxPaints = Array(numColors) { Paint() }
    private val textPaints = Array(numColors) { Paint() }
    private val labelPaints = Array(numColors) { Paint() }

    init {
        for (i in 0 until numColors) {
            textPaints[i] = Paint()
            textPaints[i].color = COLORS[i][0]
            textPaints[i].textSize = TEXT_SIZE
            boxPaints[i] = Paint()
            boxPaints[i].color = COLORS[i][1]
            boxPaints[i].style = Paint.Style.STROKE
            boxPaints[i].strokeWidth = STROKE_WIDTH
            labelPaints[i] = Paint()
            labelPaints[i].color = COLORS[i][1]
            labelPaints[i].style = Paint.Style.FILL
        }
    }

    override fun draw(canvas: Canvas) {
        // Decide color based on object tracking ID


        val colorID =
            if (detectedObject.trackingId == null) 0
            else abs(detectedObject.trackingId!! % NUM_COLORS)
        var textWidth =
            textPaints[colorID].measureText("Tracking ID: " + detectedObject.trackingId)
        val lineHeight = TEXT_SIZE + STROKE_WIDTH
        var yLabelOffset = -lineHeight


        for (label in detectedObject.labels) {
            // Mostrar todos los objetos detectados (filtro desactivado)
            //if(label.text.equals("perro")){

            textWidth = max(textWidth, textPaints[colorID].measureText(label.text))
            textWidth = max(textWidth, textPaints[colorID].measureText(
                    String.format(
                        Locale.US,
                        LABEL_FORMAT,
                        label.confidence * 100,
                        label.index
                    )
                )
            )
            yLabelOffset -= 2 * lineHeight

            // Draws the bounding box.
            val rect = RectF(detectedObject.boundingBox)
            val x0 = translateX(rect.left)
            val x1 = translateX(rect.right)
            rect.left = min(x0, x1)
            rect.right = max(x0, x1)
            rect.top = translateY(rect.top)
            rect.bottom = translateY(rect.bottom)
            canvas.drawRect(rect, boxPaints[colorID])

            // Draws other object info.
            canvas.drawRect(
                rect.left - STROKE_WIDTH,
                rect.top + yLabelOffset,
                rect.left + textWidth + 2 * STROKE_WIDTH,
                rect.top,
                labelPaints[colorID]
            )
            yLabelOffset += TEXT_SIZE
            canvas.drawText(
                "Tracking ID: " + detectedObject.trackingId,
                rect.left,
                rect.top + yLabelOffset,
                textPaints[colorID]
            )

            yLabelOffset += lineHeight



            canvas.drawText(
                label.text + " (index: " + label.index + ")",
                rect.left,
                rect.top + yLabelOffset,
                textPaints[colorID]
            )
            yLabelOffset += lineHeight
            canvas.drawText(
                String.format(
                    Locale.US,
                    LABEL_FORMAT,
                    label.confidence * 100,
                    label.index
                ),
                rect.left,
                rect.top + yLabelOffset,
                textPaints[colorID]
            )
            yLabelOffset += lineHeight

        }
    //}
    }

    companion object {
        private const val TEXT_SIZE = 54.0f
        private const val STROKE_WIDTH = 4.0f
        private const val NUM_COLORS = 10
        private val COLORS =
            arrayOf(
                intArrayOf(Color.WHITE, Color.BLUE),
                intArrayOf(Color.WHITE, Color.RED),
                intArrayOf(Color.BLACK, Color.YELLOW),
                intArrayOf(Color.BLACK, Color.WHITE),
                intArrayOf(Color.WHITE, Color.MAGENTA),
                intArrayOf(Color.BLACK, Color.LTGRAY),
                intArrayOf(Color.WHITE, Color.DKGRAY),
                intArrayOf(Color.BLACK, Color.CYAN),
                intArrayOf(Color.WHITE, Color.BLACK),
                intArrayOf(Color.BLACK, Color.GREEN)
            )
        private const val LABEL_FORMAT = "%.2f%% confidence (index: %d)"
    }
} 