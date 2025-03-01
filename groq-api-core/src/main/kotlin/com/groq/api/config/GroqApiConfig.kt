package com.groq.api.config

/**
 * Configuration class for the Groq API client.
 *
 * @property apiKey The API key used for authentication with Groq API.
 * @property baseUrl The base URL for the Groq API. Defaults to the official API endpoint.
 */
data class GroqApiConfig(
    val apiKey: String,
    val baseUrl: String = DEFAULT_BASE_URL,
    val maxBase64SizeMB: Int = MAX_BASE64_SIZE_MB
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://api.groq.com/openai/v1"
        const val CHAT_COMPLETIONS_ENDPOINT = "/chat/completions"
        const val TRANSCRIPTIONS_ENDPOINT = "/audio/transcriptions"
        const val TRANSLATIONS_ENDPOINT = "/audio/translations"
        const val MODELS_ENDPOINT = "/models"
        
        // Vision models
        const val VISION_MODEL_90B = "llama-3.2-90b-vision-preview"
        const val VISION_MODEL_11B = "llama-3.2-11b-vision-preview"
        
        // Size limits
        const val MAX_IMAGE_SIZE_MB = 20
        const val MAX_BASE64_SIZE_MB = 4
    }
    
    /**
     * Returns the full URL for the specified endpoint.
     *
     * @param endpoint The API endpoint to append to the base URL.
     * @return The complete URL for the specified endpoint.
     */
    fun getFullUrl(endpoint: String): String = "$baseUrl$endpoint"
}