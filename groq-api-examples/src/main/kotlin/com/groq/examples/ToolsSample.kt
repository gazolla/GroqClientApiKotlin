package com.groq.examples

import com.groq.api.client.GroqClientFactory
import com.groq.api.models.Function
import com.groq.api.models.Tool
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Example application demonstrating the use of tools/functions with the Groq API.
 */
fun main() = runBlocking {
    println("Groq API Tools/Functions Example")
    println("================================")

    // Get API key from environment variable or use a default for testing
    val apiKey = System.getenv("GROQ_API_KEY") ?: "your_api_key_here"

    if (apiKey == "your_api_key_here") {
        println("⚠️ Please set the GROQ_API_KEY environment variable")
        println("You can get an API key from https://console.groq.com/keys")
        return@runBlocking
    }

    // Create the Groq API client
    GroqClientFactory.createClient(apiKey).use { client ->
        // Define tools (functions) that the model can use
        val tools = listOf(
            Tool(
                type = "function",
                function = Function(
                    name = "get_current_time",
                    description = "Get the current date and time",
                    parameters = buildJsonObject {
                        put("type", "object")
                        putJsonArray("required") { }
                        putJsonObject("properties") { }
                    },
                    execute = {
                        val now = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        """{"datetime": "${now.format(formatter)}", "timezone": "System default"}"""
                    }
                )
            ),
            Tool(
                type = "function",
                function = Function(
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
                                put("description", "The city and country, e.g., 'London, UK'")
                            }
                        }
                    },
                    execute = { args ->
                        // Parse the arguments
                        val jsonArgs = try {
                            Json.parseToJsonElement(args).jsonObject
                        } catch (e: Exception) {
                            buildJsonObject {}
                        }

                        val location = jsonArgs["location"]?.toString()?.trim('"') ?: "Unknown"

                        // Mock weather data (in a real app, you would call a weather API)
                        """
                        {
                            "location": "$location",
                            "temperature": 22,
                            "unit": "celsius",
                            "condition": "Partly cloudy",
                            "humidity": 65,
                            "wind_speed": 10,
                            "wind_direction": "NE"
                        }
                        """.trimIndent()
                    }
                )
            ),
            Tool(
                type = "function",
                function = Function(
                    name = "calculate",
                    description = "Perform a mathematical calculation",
                    parameters = buildJsonObject {
                        put("type", "object")
                        putJsonArray("required") {
                            add(JsonPrimitive("expression"))
                        }
                        putJsonObject("properties") {
                            putJsonObject("expression") {
                                put("type", "string")
                                put("description", "The mathematical expression to evaluate, e.g., '2 + 2'")
                            }
                        }
                    },
                    execute = { args ->
                        // Parse the arguments
                        val jsonArgs = try {
                            Json.parseToJsonElement(args).jsonObject
                        } catch (e: Exception) {
                            buildJsonObject {}
                        }

                        val expression = jsonArgs["expression"]?.toString()?.trim('"') ?: ""

                        try {
                            // Very simple expression evaluator
                            // In a real application, use a proper expression parser with security measures
                            val result = when {
                                "+" in expression -> {
                                    val (a, b) = expression.split("+").map { it.trim().toDouble() }
                                    a + b
                                }
                                "-" in expression -> {
                                    val (a, b) = expression.split("-").map { it.trim().toDouble() }
                                    a - b
                                }
                                "*" in expression -> {
                                    val (a, b) = expression.split("*").map { it.trim().toDouble() }
                                    a * b
                                }
                                "/" in expression -> {
                                    val (a, b) = expression.split("/").map { it.trim().toDouble() }
                                    if (b == 0.0) throw ArithmeticException("Division by zero")
                                    a / b
                                }
                                else -> throw IllegalArgumentException("Unsupported operation")
                            }

                            """
                            {
                                "expression": "$expression",
                                "result": $result,
                                "status": "success"
                            }
                            """.trimIndent()
                        } catch (e: Exception) {
                            """
                            {
                                "expression": "$expression",
                                "error": "${e.message}",
                                "status": "error"
                            }
                            """.trimIndent()
                        }
                    }
                )
            )
        )

        // User prompt that might require tools
        val systemMessage = "You are a helpful assistant that can use tools when needed to provide accurate information."
        val userPrompt = "What time is it now? Also, what's the weather like in Paris? And can you calculate 1234 * 5678?"

        println("\nSystem: $systemMessage")
        println("User: $userPrompt")
        println("\nProcessing... (this may take a moment)")

        try {
            // Run the conversation with tools
            val response = client.runConversationWithTools(
                userPrompt = userPrompt,
                tools = tools,
                model = "llama-3.3-70b-versatile",
                systemMessage = systemMessage
            )

            println("\nAssistant: $response")

        } catch (e: Exception) {
            println("Error: ${e.message}")
            e.printStackTrace()
        }
    }

    println("\n✅ Tools example completed")
}