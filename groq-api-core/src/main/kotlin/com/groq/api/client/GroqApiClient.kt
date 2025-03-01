package com.groq.api.client

import com.groq.api.config.GroqApiConfig
import com.groq.api.exceptions.GroqApiException
import com.groq.api.models.Tool
import com.groq.api.utils.ImageUtils
import com.groq.api.utils.JsonUtils
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.Closeable
import java.io.InputStream

/**
 * Main client for interacting with the Groq API.
 * Provides implementation for all Groq API services.
 *
 * @property config Configuration for the API client
 * @property httpClient HTTP client used for making requests
 */
class GroqApiClient(
    private val config: GroqApiConfig,
    private val httpClient: HttpClient
) : GroqApi, Closeable {

    /**
     * Secondary constructor that accepts just an API key
     */
    constructor(apiKey: String) : this(
        GroqApiConfig(apiKey),
        GroqClientFactory.createDefaultHttpClient()
    )

    /**
     * Create a chat completion with the provided request.
     * 
     * @param request JSON object containing the request parameters
     * @return JSON object containing the API response
     */
    override suspend fun createChatCompletion(request: JsonObject): JsonObject? {
        val response = httpClient.post(config.getFullUrl(GroqApiConfig.CHAT_COMPLETIONS_ENDPOINT)) {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${config.apiKey}")
            setBody(Json.encodeToString(JsonObject.serializer(), request))
        }

        val responseBody = response.bodyAsText()
        val responseJson = Json.parseToJsonElement(responseBody).jsonObject

        // Verifica se há um objeto de erro na resposta
        if (responseJson.contains("error")) {
            val errorObject = responseJson["error"]?.jsonObject
            val errorMessage = errorObject?.get("message")?.jsonPrimitive?.content
            val errorType = errorObject?.get("type")?.jsonPrimitive?.content
            val errorCode = errorObject?.get("code")?.jsonPrimitive?.content

            throw GroqApiException(
                statusCode = response.status.value,
                message = "API error: ${errorMessage ?: "Unknown error"}",
                errorType = errorType,
                errorCode = errorCode
            )
        }

        if (!response.status.isSuccess()) {
            throw GroqApiException(response.status.value, "API request failed: ${response.bodyAsText()}")
        }

        return responseJson
    }

    /**
     * Create a streaming chat completion.
     * 
     * @param request JSON object containing the request parameters
     * @return Flow of JSON objects representing the streamed response
     */
    override suspend fun createChatCompletionStream(request: JsonObject): Flow<JsonObject?> = flow {
        val mutableRequest = request.toMutableMap()
        mutableRequest["stream"] = JsonPrimitive(true)

        val response = httpClient.post(config.getFullUrl(GroqApiConfig.CHAT_COMPLETIONS_ENDPOINT)) {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${config.apiKey}")
            setBody(JsonObject(mutableRequest).toString())
        }

        if (!response.status.isSuccess()) {
            throw GroqApiException(response.status.value, "API request failed: ${response.bodyAsText()}")
        }

        val lines = response.bodyAsText().split("\n")

        for (line in lines) {
            if (line.startsWith("data: ")) {
                val data = line.substring("data: ".length)
                if (data != "[DONE]") {
                    emit(Json.parseToJsonElement(data).jsonObject)
                }
            }
        }
    }

    /**
     * Create an audio transcription.
     */
    override suspend fun createTranscription(
        audioFile: InputStream,
        fileName: String,
        model: String,
        prompt: String?,
        responseFormat: String,
        language: String?,
        temperature: Float?
    ): JsonObject? {
        val response = httpClient.submitFormWithBinaryData(
            url = config.getFullUrl(GroqApiConfig.TRANSCRIPTIONS_ENDPOINT),
            formData = formData {
                append("file", audioFile.readBytes(), Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                })
                append("model", model)
                prompt?.let { append("prompt", it) }
                append("response_format", responseFormat)
                language?.let { append("language", it) }
                temperature?.let { append("temperature", it.toString()) }
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer ${config.apiKey}")
        }

        if (!response.status.isSuccess()) {
            throw GroqApiException(response.status.value, "API request failed: ${response.bodyAsText()}")
        }

        return Json.parseToJsonElement(response.bodyAsText()).jsonObject
    }

    /**
     * Create an audio translation.
     */
    override suspend fun createTranslation(
        audioFile: InputStream,
        fileName: String,
        model: String,
        prompt: String?,
        responseFormat: String,
        temperature: Float?
    ): JsonObject? {
        val response = httpClient.submitFormWithBinaryData(
            url = config.getFullUrl(GroqApiConfig.TRANSLATIONS_ENDPOINT),
            formData = formData {
                append("file", audioFile.readBytes(), Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                })
                append("model", model)
                prompt?.let { append("prompt", it) }
                append("response_format", responseFormat)
                temperature?.let { append("temperature", it.toString()) }
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer ${config.apiKey}")
        }

        if (!response.status.isSuccess()) {
            throw GroqApiException(response.status.value, "API request failed: ${response.bodyAsText()}")
        }

        return Json.parseToJsonElement(response.bodyAsText()).jsonObject
    }

    /**
     * Create a vision completion.
     */
    override suspend fun createVisionCompletion(request: JsonObject): JsonObject? {
        ImageUtils.validateVisionModel(request)
        return createChatCompletion(request)
    }

    /**
     * Create a vision completion with an image URL.
     */
    override suspend fun createVisionCompletionWithImageUrl(
        imageUrl: String,
        prompt: String,
        model: String,
        temperature: Float?
    ): JsonObject? {
        ImageUtils.validateImageUrl(imageUrl)
        
        val request = JsonUtils.createVisionRequestWithUrl(
            imageUrl = imageUrl,
            prompt = prompt,
            model = model,
            temperature = temperature
        )
        
        return createVisionCompletion(request)
    }

    /**
     * Create a vision completion with a base64-encoded image.
     */
    override suspend fun createVisionCompletionWithBase64Image(
        imagePath: String,
        prompt: String,
        model: String,
        temperature: Float?
    ): JsonObject? {
        val base64Image = ImageUtils.convertImageToBase64(imagePath)
        ImageUtils.validateBase64Size(base64Image, config.maxBase64SizeMB)
        
        val request = JsonUtils.createVisionRequestWithBase64(
            base64Image = base64Image,
            prompt = prompt,
            model = model,
            temperature = temperature
        )
        
        return createVisionCompletion(request)
    }

    /**
     * Run a conversation with tools.
     */
    override suspend fun runConversationWithTools(
        userPrompt: String,
        tools: List<Tool>,
        model: String,
        systemMessage: String
    ): String {
        try {
            // Create initial messages
            val messages = mutableListOf(
                buildJsonObject {
                    put("role", "system")
                    put("content", systemMessage)
                },
                buildJsonObject {
                    put("role", "user")
                    put("content", userPrompt)
                }
            )

            // First request with tools
            val request = JsonUtils.createToolsRequest(model, messages, tools)
            val response = createChatCompletion(request)
            
            // Process tool calls if present
            val responseMessage = response?.get("choices")?.jsonArray?.get(0)?.jsonObject?.get("message")?.jsonObject
            val toolCalls = responseMessage?.get("tool_calls")?.jsonArray

            if (toolCalls != null && toolCalls.isNotEmpty()) {
                responseMessage?.let { messages.add(it) }

                // Process each tool call
                for (toolCall in toolCalls) {
                    val functionName = toolCall.jsonObject["function"]?.jsonObject?.get("name")?.jsonPrimitive?.content
                    val functionArgs = toolCall.jsonObject["function"]?.jsonObject?.get("arguments")?.jsonPrimitive?.content
                    val toolCallId = toolCall.jsonObject["id"]?.jsonPrimitive?.content

                    if (!functionName.isNullOrEmpty() && !functionArgs.isNullOrEmpty() && toolCallId != null) {
                        val tool = tools.find { it.function.name == functionName }
                        tool?.let {
                            val functionResponse = it.function.execute(functionArgs)
                            messages.add(JsonUtils.createToolResponseMessage(toolCallId, functionName, functionResponse))
                        }
                    }
                }

                // Second request with tool results
                val secondRequest = JsonUtils.createChatRequest(model, messages)
                val secondResponse = createChatCompletion(secondRequest)
                return JsonUtils.extractContentFromCompletion(secondResponse) ?: ""
            }

            return responseMessage?.get("content")?.jsonPrimitive?.content ?: ""
        } catch (e: Exception) {
            throw GroqApiException(
                statusCode = -1,
                message = "Error in tool conversation: ${e.message}",
                cause = e
            )
        }
    }

    /**
     * List available models.
     */
    override suspend fun listModels(): JsonObject? {
        val response = httpClient.get(config.getFullUrl(GroqApiConfig.MODELS_ENDPOINT)) {
            header(HttpHeaders.Authorization, "Bearer ${config.apiKey}")
        }

        if (!response.status.isSuccess()) {
            throw GroqApiException(
                statusCode = response.status.value,
                message = "API request failed: ${response.bodyAsText()}"
            )
        }

        return Json.parseToJsonElement(response.bodyAsText()).jsonObject
    }

    /**
     * Close the client and release resources.
     */
    override fun close() {
        httpClient.close()
    }
}