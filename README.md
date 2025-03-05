# Groq API Kotlin Client

A modern Kotlin client library for the [Groq API](https://console.groq.com/docs).

## Features

- Complete support for Groq API endpoints
- Asynchronous API using Kotlin Coroutines and Flows
- Support for streaming responses
- Type-safe extensions for common operations
- Support for chat completions, audio transcription/translation, vision, and tools/functions
- Modular design for flexible usage

## Installation

- Clone the project
- Generate a JAR file for your Groq API Kotlin client library

For a basic JAR file containing just the compiled classes:
```bash
./gradlew :groq-api-core:jar
```

This will create a sources JAR in the same directory, named something like `groq-api-core-1.0.0-sources.jar`.

In your project just add this in your project:
### build.gradle.kt
```kotlin
implementation(files("libs/groq-api-core-0.1.0.jar"))

// groq-api-core dependency
api("io.ktor:ktor-client-core:$ktorVersion")
implementation("io.ktor:ktor-client-cio:$ktorVersion")
implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
```

## Quick Start

```kotlin
import com.groq.api.client.GroqClientFactory
import com.groq.api.extensions.chatText
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    // Initialize the client with your API key
    val apiKey = System.getenv("GROQ_API_KEY") ?: "your_api_key_here"
    
    GroqClientFactory.createClient(apiKey).use { client ->
        // Simple chat completion
        val response = client.chatText(
            model = "llama-3.3-70b-versatile",
            userMessage = "Hello, who are you?",
            systemMessage = "You are a helpful assistant."
        )
        
        println("Response: $response")
    }
}
```

## Examples

### Chat Completions

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

### Streaming Responses

```kotlin
import com.groq.api.client.GroqClientFactory
import com.groq.api.extensions.chatTextStream
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val apiKey = System.getenv("GROQ_API_KEY") ?: "your_api_key_here"
    
    GroqClientFactory.createClient(apiKey).use { client ->
        client.chatTextStream(
            model = "llama-3.3-70b-versatile",
            userMessage = "Write a short poem about programming.",
            systemMessage = "You are a creative writing assistant."
        )
        .onEach { textFragment ->
            // Print each fragment as it arrives
            print(textFragment)
        }
        .collect()
    }
}
```

### Using Tools/Functions

```kotlin
import com.groq.api.client.GroqClientFactory
import com.groq.api.models.Function
import com.groq.api.models.Tool
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

fun main() = runBlocking {
    val apiKey = System.getenv("GROQ_API_KEY") ?: "your_api_key_here"
    
    val weatherFunction = Function(
        name = "get_weather",
        description = "Get the current weather for a location",
        parameters = buildJsonObject {
            put("type", "object")
            putJsonArray("required") {
                add("location")
            }
            putJsonObject("properties") {
                putJsonObject("location") {
                    put("type", "string")
                    put("description", "The city name")
                }
            }
        },
        execute = { args ->
            // In a real app, you would call a weather API
            """{"temperature": 22, "condition": "sunny"}"""
        }
    )
    
    val tools = listOf(Tool("function", weatherFunction))
    
    GroqClientFactory.createClient(apiKey).use { client ->
        val response = client.runConversationWithTools(
            userPrompt = "What's the weather like in Paris?",
            tools = tools,
            model = "llama-3.3-70b-versatile",
            systemMessage = "You are a helpful assistant."
        )
        
        println("Response: $response")
    }
}
```

## Advanced Usage

### Custom Configuration

```kotlin
import com.groq.api.client.GroqClientFactory
import com.groq.api.config.GroqApiConfig
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val apiKey = System.getenv("GROQ_API_KEY") ?: "your_api_key_here"
    
    // Create a custom configuration
    val config = GroqApiConfig(
        apiKey = apiKey,
        baseUrl = "https://api.groq.com/openai/v1" // Default, can be changed for testing
    )
    
    // Create a client with custom configuration
    GroqClientFactory.createClient(config).use { client ->
        // Use the client...
    }
}
```

## Documentation

For complete documentation, see the [API documentation](HOWTOUSE.md).

## Building from Source

```bash
git clone https://github.com/yourusername/groq-api-kotlin.git
cd groq-api-kotlin
./gradlew build
```

## Running the Examples

```bash
export GROQ_API_KEY=your_api_key_here
./gradlew :groq-api-examples:run
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.