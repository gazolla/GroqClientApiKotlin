# Groq API Client - How to Use

This guide provides examples of how to use the `GroqApiClient` class to interact with the Groq API for various AI capabilities.

## Table of Contents

- [Getting Started](#getting-started)
- [Text Generation](#text-generation)
- [Streaming Responses](#streaming-responses)
- [Multi-turn Conversations](#multi-turn-conversations)
- [Structured Output](#structured-output)
- [Using Tools/Functions](#using-toolsfunctions)
- [Vision Capabilities](#vision-capabilities)
- [Audio Transcription](#audio-transcription)
- [Available Models](#available-models)

## Getting Started

First, you need to initialize the `GroqApiClient` with your API key:

```kotlin
import com.groq.api.client.GroqClientFactory
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    // Get API key from environment variable or configuration
    val apiKey = System.getenv("GROQ_API_KEY") ?: "your_api_key_here"
    
    // Create a client instance
    GroqClientFactory.createClient(apiKey).use { client ->
        // Use the client here
    }
}
```

## Text Generation

### Basic Text Completion

```kotlin
import com.groq.api.client.GroqClientFactory
import com.groq.api.extensions.chatText
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val apiKey = System.getenv("GROQ_API_KEY") ?: "your_api_key_here"
    
    GroqClientFactory.createClient(apiKey).use { client ->
        val response = client.chatText(
            model = "llama-3.3-70b-versatile",
            userMessage = "What are three interesting facts about space?",
            systemMessage = "You are a knowledgeable astronomy expert."
        )
        
        println("Response: $response")
    }
}
```

### Using the Raw API

```kotlin
import com.groq.api.client.GroqClientFactory
import com.groq.api.utils.JsonUtils
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val apiKey = System.getenv("GROQ_API_KEY") ?: "your_api_key_here"
    
    GroqClientFactory.createClient(apiKey).use { client ->
        val request = JsonUtils.createSimpleChatRequest(
            model = "llama-3.3-70b-versatile",
            userMessage = "Explain quantum computing in simple terms.",
            systemMessage = "You are a teacher who explains complex topics simply."
        )
        
        val response = client.createChatCompletion(request)
        val content = JsonUtils.extractContentFromCompletion(response)
        
        println("Response: $content")
    }
}
```

## Streaming Responses

For long responses, streaming is more efficient and provides a better user experience:

```kotlin
import com.groq.api.client.GroqClientFactory
import com.groq.api.extensions.chatTextStream
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val apiKey = System.getenv("GROQ_API_KEY") ?: "your_api_key_here"
    
    GroqClientFactory.createClient(apiKey).use { client ->
        println("Generating a story about a space adventure...")
        
        client.chatTextStream(
            model = "llama-3.3-70b-versatile",
            userMessage = "Write a short story about a space adventure.",
            systemMessage = "You are a creative sci-fi writer."
        )
        .onEach { textFragment ->
            // Print each fragment as it arrives
            print(textFragment)
        }
        .collect()
    }
}
```

## Multi-turn Conversations

You can simulate a conversation with multiple turns:

```kotlin
import com.groq.api.client.GroqClientFactory
import com.groq.api.utils.JsonUtils
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val apiKey = System.getenv("GROQ_API_KEY") ?: "your_api_key_here"
    
    GroqClientFactory.createClient(apiKey).use { client ->
        // Create a list to track the conversation
        val messages = mutableListOf<JsonObject>()
        
        // Add a system message
        messages.add(buildJsonObject {
            put("role", "system")
            put("content", "You are a helpful travel assistant.")
        })
        
        // First user message
        messages.add(buildJsonObject {
            put("role", "user")
            put("content", "I'm planning a trip to Japan.")
        })
        
        // Get response for the first message
        val request1 = JsonUtils.createChatRequest("llama-3.3-70b-versatile", messages)
        val response1 = client.createChatCompletion(request1)
        val assistantMessage1 = response1?.get("choices")
            ?.jsonArray?.get(0)?.jsonObject
            ?.get("message")?.jsonObject
        
        // Add assistant's response to the conversation
        if (assistantMessage1 != null) {
            messages.add(assistantMessage1)
            println("Assistant: ${JsonUtils.extractContentFromCompletion(response1)}")
        }
        
        // Add second user message
        messages.add(buildJsonObject {
            put("role", "user")
            put("content", "What's the best time to visit Kyoto?")
        })
        
        // Get response for the second message
        val request2 = JsonUtils.createChatRequest("llama-3.3-70b-versatile", messages)
        val response2 = client.createChatCompletion(request2)
        println("Assistant: ${JsonUtils.extractContentFromCompletion(response2)}")
    }
}
```

## Structured Output

You can request structured output in formats like JSON:

```kotlin
import com.groq.api.client.GroqClientFactory
import com.groq.api.extensions.chatText
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

fun main() = runBlocking {
    val apiKey = System.getenv("GROQ_API_KEY") ?: "your_api_key_here"
    
    GroqClientFactory.createClient(apiKey).use { client ->
        val systemMessage = """
            You are an assistant that responds only in JSON format.
            Your responses should always follow the format:
            {
                "answer": "Your answer here",
                "references": ["ref1", "ref2", ...],
                "confidence": 0.XX (value from 0 to 1)
            }
        """.trimIndent()
        
        val userMessage = "What are the main benefits of electric vehicles?"
        
        val jsonResponse = client.chatText(
            model = "llama-3.3-70b-versatile",
            userMessage = userMessage,
            systemMessage = systemMessage
        )
        
        // Parse and pretty-print the JSON
        try {
            val jsonElement = Json.parseToJsonElement(jsonResponse)
            val prettyJson = Json { prettyPrint = true }.encodeToString(
                kotlinx.serialization.json.JsonElement.serializer(), 
                jsonElement
            )
            println(prettyJson)
            
            // You can also access specific fields
            val answer = jsonElement.jsonObject["answer"]?.toString()
            println("Answer: $answer")
        } catch (e: Exception) {
            println("Failed to parse JSON: $jsonResponse")
        }
    }
}
```

## Using Tools/Functions

Groq's API supports function calling, allowing models to invoke custom functions:

```kotlin
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
```

## Vision Capabilities

Groq's vision models can analyze images:

```kotlin
import com.groq.api.client.GroqClientFactory
import com.groq.api.utils.JsonUtils
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val apiKey = System.getenv("GROQ_API_KEY") ?: "your_api_key_here"
    
    GroqClientFactory.createClient(apiKey).use { client ->
        // Using an image URL
        val imageUrl = "https://example.com/image.jpg"
        val response = client.createVisionCompletionWithImageUrl(
            imageUrl = imageUrl,
            prompt = "Describe what you see in this image.",
            model = "llama-3.2-90b-vision-preview"
        )
        
        val description = JsonUtils.extractContentFromCompletion(response)
        println("Image description: $description")
        
        // Or using a local image file
        val localImagePath = "/path/to/local/image.jpg"
        val localResponse = client.createVisionCompletionWithBase64Image(
            imagePath = localImagePath,
            prompt = "What objects do you see in this image?",
            model = "llama-3.2-90b-vision-preview"
        )
        
        val localDescription = JsonUtils.extractContentFromCompletion(localResponse)
        println("Local image description: $localDescription")
    }
}
```

## Audio Transcription

Transcribe audio files to text:

```kotlin
import com.groq.api.client.GroqClientFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

fun main() = runBlocking {
    val apiKey = System.getenv("GROQ_API_KEY") ?: "your_api_key_here"
    
    GroqClientFactory.createClient(apiKey).use { client ->
        // Open an audio file
        val audioFile = File("/path/to/audio.mp3").inputStream()
        
        // Transcribe the audio
        val response = client.createTranscription(
            audioFile = audioFile,
            fileName = "recording.mp3",
            model = "whisper-1",
            responseFormat = "json"
        )
        
        // Extract the transcription
        val text = response?.jsonObject?.get("text")?.jsonPrimitive?.content
        println("Transcription: $text")
        
        // Close the input stream
        audioFile.close()
    }
}
```

## Available Models

List all available models:

```kotlin
import com.groq.api.client.GroqClientFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun main() = runBlocking {
    val apiKey = System.getenv("GROQ_API_KEY") ?: "your_api_key_here"
    
    GroqClientFactory.createClient(apiKey).use { client ->
        val response = client.listModels()
        
        if (response != null) {
            val models = response["data"]?.jsonArray
            
            if (models != null) {
                println("Available Models:")
                for (model in models) {
                    val id = model.jsonObject["id"]?.jsonPrimitive?.content
                    println(" - $id")
                }
            } else {
                println("No models data available")
            }
        } else {
            println("Failed to retrieve models")
        }
    }
}
```

---

These examples demonstrate the core functionality of the Groq API Kotlin client. You can combine these patterns and adapt them to your specific use cases.

Remember to always properly handle error cases and close resources when you're done using them.