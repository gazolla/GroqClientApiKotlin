package com.groq.api.utils

import com.groq.api.config.GroqApiConfig
import com.groq.api.exceptions.GroqApiException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.net.URL
import java.util.Base64

/**
 * Utility class for working with images in the Groq API.
 */
object ImageUtils {
    
    private val VISION_MODELS = setOf(
        GroqApiConfig.VISION_MODEL_90B,
        GroqApiConfig.VISION_MODEL_11B
    )
    
    /**
     * Converts an image file to a Base64-encoded string.
     *
     * @param imagePath The path to the image file.
     * @return A Base64-encoded string representing the image.
     * @throws NoSuchFileException If the image file does not exist.
     */
    fun convertImageToBase64(imagePath: String): String {
        val file = File(imagePath)
        if (!file.exists()) {
            throw NoSuchFileException(file, null, "Image file not found: $imagePath")
        }

        return Base64.getEncoder().encodeToString(file.readBytes())
    }
    
    /**
     * Validates that the model in the request is a valid vision model.
     *
     * @param request The JSON request object.
     * @throws IllegalArgumentException If the model is invalid.
     */
    fun validateVisionModel(request: JsonObject) {
        val model = request["model"]?.jsonPrimitive?.content
        if (model.isNullOrEmpty() || !VISION_MODELS.contains(model)) {
            throw GroqApiException(
                statusCode = 400,
                message = "Invalid vision model. Must be one of: ${VISION_MODELS.joinToString(", ")}"
            )
        }
    }
    
    /**
     * Validates that a Base64-encoded image does not exceed the maximum size.
     *
     * @param base64String The Base64-encoded image.
     * @param maxSizeMB The maximum size in megabytes.
     * @throws IllegalArgumentException If the image exceeds the maximum size.
     */
    fun validateBase64Size(base64String: String, maxSizeMB: Int = GroqApiConfig.MAX_BASE64_SIZE_MB) {
        val sizeInMB = (base64String.length * 3.0 / 4.0) / (1024 * 1024)
        if (sizeInMB > maxSizeMB) {
            throw GroqApiException(
                statusCode = 400,
                message = "Base64 image exceeds the maximum size of $maxSizeMB MB"
            )
        }
    }
    
    /**
     * Validates that an image URL is in a valid format.
     *
     * @param url The image URL to validate.
     * @throws IllegalArgumentException If the URL is invalid.
     */
    fun validateImageUrl(url: String) {
        if (url.isBlank()) {
            throw GroqApiException(
                statusCode = 400,
                message = "Image URL cannot be empty"
            )
        }

        try {
            URL(url)
        } catch (e: Exception) {
            throw GroqApiException(
                statusCode = 400,
                message = "Invalid image URL format",
                cause = e
            )
        }
    }
}