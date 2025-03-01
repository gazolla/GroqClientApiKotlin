package com.groq.api.client

import com.groq.api.models.Tool

interface ToolsService {
    suspend fun runConversationWithTools(
        userPrompt: String,
        tools: List<Tool>,
        model: String,
        systemMessage: String
    ): String
}