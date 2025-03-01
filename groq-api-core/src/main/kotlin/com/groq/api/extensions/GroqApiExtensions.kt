package com.groq.api.extensions

import com.groq.api.client.GroqApiClient
import com.groq.api.utils.JsonUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonObject

/**
 * Extension functions to simplify common operations with the Groq API client.
 */

/**
 * Creates a chat completion and returns the request object.
 *
 * @param model The model to use.
 * @param userMessage The user's message.
 * @param systemMessage Optional system message.
 * @param temperature The temperature parameter for generation.
 * @return The raw JSON response from the API.
 */
suspend fun GroqApiClient.chat(
    model: String,
    userMessage: String,
    systemMessage: String? = null,
    temperature: Float = 0.7f
): JsonObject? {
    val request = JsonUtils.createSimpleChatRequest(
        model = model,
        userMessage = userMessage,
        systemMessage = systemMessage,
        temperature = temperature
    )

    return createChatCompletion(request)
}

/**
 * Creates a chat completion and returns just the text content.
 *
 * @param model The model to use.
 * @param userMessage The user's message.
 * @param systemMessage Optional system message.
 * @param temperature The temperature parameter for generation.
 * @return The text content of the response.
 */
suspend fun GroqApiClient.chatText(
    model: String,
    userMessage: String,
    systemMessage: String? = null,
    temperature: Float = 0.7f
): String {
    val response = chat(model, userMessage, systemMessage, temperature)
    return JsonUtils.extractContentFromCompletion(response) ?: ""
}

/**
 * Creates a streaming chat completion.
 *
 * @param model The model to use.
 * @param userMessage The user's message.
 * @param systemMessage Optional system message.
 * @param temperature The temperature parameter for generation.
 * @return A flow of JSON objects from the streaming response.
 */
suspend fun GroqApiClient.chatStream(
    model: String,
    userMessage: String,
    systemMessage: String? = null,
    temperature: Float = 0.7f
): Flow<JsonObject?> {
    val request = JsonUtils.createSimpleChatRequest(
        model = model,
        userMessage = userMessage,
        systemMessage = systemMessage,
        temperature = temperature
    )

    return createChatCompletionStream(request)
}

/**
 * Creates a streaming chat completion and returns just the text content.
 *
 * @param model The model to use.
 * @param userMessage The user's message.
 * @param systemMessage Optional system message.
 * @param temperature The temperature parameter for generation.
 * @return A flow of text fragments from the streaming response.
 */
suspend fun GroqApiClient.chatTextStream(
    model: String,
    userMessage: String,
    systemMessage: String? = null,
    temperature: Float = 0.7f
): Flow<String> {
    return chatStream(model, userMessage, systemMessage, temperature)
        .map { chunk ->
            JsonUtils.extractContentFromChunk(chunk) ?: ""
        }
}