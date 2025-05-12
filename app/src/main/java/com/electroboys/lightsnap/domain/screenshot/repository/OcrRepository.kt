package com.electroboys.lightsnap.domain.screenshot.repository


import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.tasks.await

class OcrRepository {
    suspend fun recognizeText(bitmap: Bitmap): Result<Text> {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(
                ChineseTextRecognizerOptions.Builder().build()
            )
            val visionText = recognizer.process(image).await()
            Result.success(visionText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}