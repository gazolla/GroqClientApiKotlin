package com.groq.api.client

import kotlinx.serialization.json.JsonObject
import java.io.InputStream

interface AudioService {
    suspend fun createTranscription(
        audioFile: InputStream,
        fileName: String,
        model: String,
        prompt: String? = null,
        responseFormat: String = "json",
        language: String? = null,
        temperature: Float? = null
    ): JsonObject?
    
    suspend fun createTranslation(
        audioFile: InputStream,
        fileName: String,
        model: String,
        prompt: String? = null,
        responseFormat: String = "json",
        temperature: Float? = null
    ): JsonObject?
}