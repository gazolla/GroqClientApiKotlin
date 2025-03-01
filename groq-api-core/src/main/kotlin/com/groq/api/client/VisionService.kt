package com.groq.api.client

import kotlinx.serialization.json.JsonObject

interface VisionService {
    suspend fun createVisionCompletion(request: JsonObject): JsonObject?
    suspend fun createVisionCompletionWithImageUrl(
        imageUrl: String,
        prompt: String,
        model: String = "llama-3.2-90b-vision-preview",
        temperature: Float? = null
    ): JsonObject?
    
    suspend fun createVisionCompletionWithBase64Image(
        imagePath: String,
        prompt: String,
        model: String = "llama-3.2-90b-vision-preview",
        temperature: Float? = null
    ): JsonObject?
}