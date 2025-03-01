package com.groq.api.client

import kotlinx.serialization.json.JsonObject

interface ModelsService {
    suspend fun listModels(): JsonObject?
}