package com.groq.examples

import com.groq.api.client.GroqClientFactory
import com.groq.api.extensions.chatText
import com.groq.api.extensions.chatTextStream
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Example application demonstrating the use of the Groq API client library.
 */
fun main() = runBlocking {
    println("Groq API Client Examples")
    println("========================")

    // Get API key from environment variable or use a default for testing
    val apiKey = System.getenv("GROQ_API_KEY") ?: "your_api_key_here"

    if (apiKey == "your_api_key_here") {
        println("⚠️ Please set the GROQ_API_KEY environment variable")
        println("You can get an API key from https://console.groq.com/keys")
        return@runBlocking
    }

    // Create the Groq API client
    GroqClientFactory.createClient(apiKey).use { client ->
        // Example 1: Basic text completion
        println("\n📝 Example 1: Basic Text Completion")
        println("---------------------------------")
        basicTextCompletionExample(client)

        // Example 2: Streaming text completion
        println("\n🌊 Example 2: Streaming Text Completion")
        println("-------------------------------------")
        streamingTextCompletionExample(client)

        // Example 3: Structured output
        println("\n🧩 Example 3: Structured Output (JSON)")
        println("-------------------------------------")
        structuredOutputExample(client)

        // Example 4: List available models
        println("\n📋 Example 4: List Available Models")
        println("--------------------------------")
        listModelsExample(client)
    }

    println("\n✅ All examples completed successfully")
}

/**
 * Example of streaming text completion with the Groq API.
 */
suspend fun streamingTextCompletionExample(client: com.groq.api.client.GroqApiClient) {
    val model = "llama-3.3-70b-versatile"
    val systemMessage = "You are a helpful and informative assistant."
    val userMessage = "Explain briefly what machine learning is and give two examples of applications."

    println("System: $systemMessage")
    println("User: $userMessage")
    println("\nAssistant: ")

    try {
        // Using the extension function for text streaming
        client.chatTextStream(
            model = model,
            userMessage = userMessage,
            systemMessage = systemMessage
        )
            .onEach { textFragment ->
                // Print each text fragment as it arrives
                print(textFragment)
            }
            .collect()

        println("\n")

    } catch (e: Exception) {
        println("\nError: ${e.message}")
    }
}

/**
 * Example of requesting structured JSON output from the Groq API.
 */
suspend fun structuredOutputExample(client: com.groq.api.client.GroqApiClient) {
    val model = "llama-3.3-70b-versatile"
    val systemMessage = """
        You are an assistant that responds only in JSON format.
        Your responses should always follow the format:
        {
            "answer": "Your answer here",
            "references": ["ref1", "ref2", ...],
            "confidence": 0.XX (value from 0 to 1)
        }
    """.trimIndent()

    val userMessage = "What are the main frontend frameworks and their advantages?"

    println("System: $systemMessage")
    println("User: $userMessage")

    try {
        // Using the extension function for simple text response
        val jsonResponse = client.chatText(
            model = model,
            userMessage = userMessage,
            systemMessage = systemMessage
        )

        println("\nResponse JSON:")

        // Format the JSON for better readability
        try {
            val jsonElement = Json.parseToJsonElement(jsonResponse)
            val prettyJson = Json { prettyPrint = true }.encodeToString(jsonElement)
            println(prettyJson)
        } catch (e: Exception) {
            // If not valid JSON, display the raw response
            println(jsonResponse)
        }

    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}

/**
 * Example of listing available models from the Groq API.
 */
suspend fun listModelsExample(client: com.groq.api.client.GroqApiClient) {
    try {
        val response = client.listModels()

        if (response != null) {
            val data = response["data"]

            if (data != null) {
                println("Available models:")
                data.toString().split("id").drop(1).forEach { modelInfo ->
                    val modelId = modelInfo.substringAfter(":\"").substringBefore("\"")
                    if (modelId.isNotEmpty()) {
                        println(" - $modelId")
                    }
                }
            } else {
                println("No models data available")
            }
        } else {
            println("Failed to retrieve models")
        }

    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}

/**
 * Example of basic text completion with the Groq API.
 */
suspend fun basicTextCompletionExample(client: com.groq.api.client.GroqApiClient) {
    val model = "llama-3.3-70b-versatile"
    val systemMessage = "You are a concise and direct assistant."
    val userMessage = "What are the three main programming paradigms?"

    println("System: $systemMessage")
    println("User: $userMessage")

    try {
        // Using the extension function for simple text response
        val response = client.chatText(
            model = model,
            userMessage = userMessage,
            systemMessage = systemMessage
        )

        println("\nAssistant: $response")

    } catch (e: Exception) {
        println("Error: ${e.message}")
    }

}