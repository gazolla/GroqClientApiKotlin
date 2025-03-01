package com.groq.api.client

import com.groq.api.config.GroqApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Factory for creating Groq API clients and related components.
 */
object GroqClientFactory {
    /**
     * Creates a default HTTP client for the Groq API.
     * 
     * @return A configured HttpClient instance.
     */
    fun createDefaultHttpClient(): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = false
            })
        }
    }
    
    /**
     * Creates a new Groq API client with the provided API key.
     * 
     * @param apiKey The API key to use for authentication.
     * @return A configured GroqApiClient instance.
     */
    fun createClient(apiKey: String): GroqApiClient {
        val config = GroqApiConfig(apiKey)
        return createClient(config)
    }
    
    /**
     * Creates a new Groq API client with the provided configuration.
     * 
     * @param config The configuration for the client.
     * @param httpClient Optional custom HTTP client. If not provided, a default one will be created.
     * @return A configured GroqApiClient instance.
     */
    fun createClient(
        config: GroqApiConfig,
        httpClient: HttpClient = createDefaultHttpClient()
    ): GroqApiClient {
        return GroqApiClient(config, httpClient)
    }
}