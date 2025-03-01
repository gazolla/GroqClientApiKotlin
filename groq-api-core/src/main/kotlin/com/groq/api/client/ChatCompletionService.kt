package com.groq.api.client

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject

interface ChatCompletionService {
    suspend fun createChatCompletion(request: JsonObject): JsonObject?
    suspend fun createChatCompletionStream(request: JsonObject): Flow<JsonObject?>
}
