package com.groq.examples

import com.groq.api.client.GroqClientFactory
import com.groq.api.models.Function
import com.groq.api.models.Tool
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.JsonPrimitive

fun main() = runBlocking {
    val apiKey = System.getenv("GROQ_API_KEY") ?: "your_api_key_here"

    // Define a function to get weather data
    val weatherFunction = Function(
        name = "get_weather",
        description = "Get the current weather for a location",
        parameters = buildJsonObject {
            put("type", "object")
            putJsonArray("required") {
                add(JsonPrimitive("location"))
            }
            putJsonObject("properties") {
                putJsonObject("location") {
                    put("type", "string")
                    put("description", "The city and state, e.g. San Francisco, CA")
                }
            }
        },
        execute = { args ->
            // In a real app, you would call a weather API
            // This is a mock implementation
            """
            {
                "location": "San Francisco, CA",
                "temperature": 22,
                "unit": "celsius",
                "condition": "sunny"
            }
            """.trimIndent()
        }
    )

    // Create a list of tools
    val tools = listOf(Tool("function", weatherFunction))

    GroqClientFactory.createClient(apiKey).use { client ->
        val response = client.runConversationWithTools(
            userPrompt = "What's the weather like in San Francisco?",
            tools = tools,
            model = "llama-3.3-70b-versatile",
            systemMessage = "You are a helpful assistant."
        )

        println("Response: $response")
    }
}