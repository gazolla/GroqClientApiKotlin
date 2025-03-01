package com.groq.api.utils

import com.groq.api.models.Tool
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * Utility class for working with JSON in the Groq API.
 */
object JsonUtils {

    /**
     * Extracts the content of text from a chat completion response.
     *
     * @param response The JSON response from the chat completion API.
     * @return The extracted content as a string, or null if not found.
     */
    fun extractContentFromCompletion(response: JsonObject?): String? {
        return response
            ?.get("choices")
            ?.jsonArray
            ?.getOrNull(0)
            ?.jsonObject
            ?.get("message")
            ?.jsonObject
            ?.get("content")
            ?.jsonPrimitive
            ?.content
    }

    /**
     * Extracts the content of text from a streaming chunk.
     *
     * @param chunk The JSON chunk from a streaming response.
     * @return The extracted content as a string, or null if not found.
     */
    fun extractContentFromChunk(chunk: JsonObject?): String? {
        return chunk
            ?.get("choices")
            ?.jsonArray
            ?.getOrNull(0)
            ?.jsonObject
            ?.get("delta")
            ?.jsonObject
            ?.get("content")
            ?.jsonPrimitive
            ?.content
    }

    /**
     * Creates a chat request with multiple messages.
     *
     * @param model The model to use for the request.
     * @param messages The list of messages as role-content pairs.
     * @param temperature The temperature parameter for generation.
     * @return A JSON object representing the request.
     */
    fun createChatRequest(
        model: String,
        messages: List<JsonObject>,
        temperature: Float = 0.7f
    ): JsonObject {
        return buildJsonObject {
            put("model", model)
            putJsonArray("messages") {
                messages.forEach { add(it) }
            }
            put("temperature", temperature)
        }
    }

    /**
     * Creates a simple chat request with a single user message.
     *
     * @param model The model to use for the request.
     * @param userMessage The user's message.
     * @param systemMessage Optional system message.
     * @param temperature The temperature parameter for generation.
     * @return A JSON object representing the request.
     */
    fun createSimpleChatRequest(
        model: String,
        userMessage: String,
        systemMessage: String? = null,
        temperature: Float = 0.7f
    ): JsonObject {
        val messages = mutableListOf<JsonObject>()

        systemMessage?.let {
            messages.add(
                buildJsonObject {
                    put("role", "system")
                    put("content", it)
                }
            )
        }

        messages.add(
            buildJsonObject {
                put("role", "user")
                put("content", userMessage)
            }
        )

        return createChatRequest(model, messages, temperature)
    }

    /**
     * Creates a vision request with an image URL.
     *
     * @param imageUrl The URL of the image to analyze.
     * @param prompt The text prompt for the model.
     * @param model The vision model to use.
     * @param temperature The temperature parameter for generation.
     * @return A JSON object representing the request.
     */
    fun createVisionRequestWithUrl(
        imageUrl: String,
        prompt: String,
        model: String,
        temperature: Float? = null
    ): JsonObject {
        return buildJsonObject {
            put("model", model)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "user")
                    putJsonArray("content") {
                        addJsonObject {
                            put("type", "text")
                            put("text", prompt)
                        }
                        addJsonObject {
                            put("type", "image_url")
                            putJsonObject("image_url") {
                                put("url", imageUrl)
                            }
                        }
                    }
                }
            }
            temperature?.let { put("temperature", it) }
        }
    }

    /**
     * Creates a vision request with a base64-encoded image.
     *
     * @param base64Image The base64-encoded image data.
     * @param prompt The text prompt for the model.
     * @param model The vision model to use.
     * @param temperature The temperature parameter for generation.
     * @return A JSON object representing the request.
     */
    fun createVisionRequestWithBase64(
        base64Image: String,
        prompt: String,
        model: String,
        temperature: Float? = null
    ): JsonObject {
        return buildJsonObject {
            put("model", model)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "user")
                    putJsonArray("content") {
                        addJsonObject {
                            put("type", "text")
                            put("text", prompt)
                        }
                        addJsonObject {
                            put("type", "image_url")
                            putJsonObject("image_url") {
                                put("url", "data:image/jpeg;base64,$base64Image")
                            }
                        }
                    }
                }
            }
            temperature?.let { put("temperature", it) }
        }
    }

    /**
     * Creates a request for using tools.
     *
     * @param model The model to use.
     * @param messages The conversation messages.
     * @param tools The list of tools to make available.
     * @param temperature The temperature parameter for generation.
     * @return A JSON object representing the request.
     */
    fun createToolsRequest(
        model: String,
        messages: List<JsonObject>,
        tools: List<Tool>,
        temperature: Float = 0.7f
    ): JsonObject {
        val toolsArray = buildJsonArray {
            tools.forEach { tool ->
                addJsonObject {
                    put("type", tool.type)
                    putJsonObject("function") {
                        put("name", tool.function.name)
                        put("description", tool.function.description)
                        put("parameters", tool.function.parameters)
                    }
                }
            }
        }

        return buildJsonObject {
            put("model", model)
            putJsonArray("messages") {
                messages.forEach { add(it) }
            }
            put("tools", toolsArray)
            put("tool_choice", "auto")
            put("temperature", temperature)
        }
    }

    /**
     * Creates a message representing a tool's response.
     *
     * @param toolCallId The ID of the tool call.
     * @param functionName The name of the function that was called.
     * @param functionResponse The response from the function.
     * @return A JSON object representing the tool's response message.
     */
    fun createToolResponseMessage(
        toolCallId: String,
        functionName: String,
        functionResponse: String
    ): JsonObject {
        return buildJsonObject {
            put("tool_call_id", toolCallId)
            put("role", "tool")
            put("name", functionName)
            put("content", functionResponse)
        }
    }
}