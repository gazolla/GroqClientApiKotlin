package com.groq.api.client

import com.groq.api.config.GroqApiConfig
import com.groq.api.exceptions.GroqApiException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GroqApiClientTest {

    private lateinit var mockEngine: MockEngine
    private lateinit var mockClient: HttpClient
    private lateinit var groqClient: GroqApiClient
    private val config = GroqApiConfig("test-api-key")

    @BeforeEach
    fun setup() {
        mockEngine = MockEngine { request ->
            val url = request.url.toString()
            val authHeader = request.headers[HttpHeaders.Authorization]
            
            // Verify authorization header
            if (authHeader != "Bearer ${config.apiKey}") {
                return@MockEngine respond(
                    content = """{"error": {"message": "Invalid authentication", "type": "invalid_auth"}}""",
                    status = HttpStatusCode.Unauthorized,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
            
            when {
                // Chat completions response
                url.endsWith("/chat/completions") -> {
                    respond(
                        content = """
                            {
                                "id": "chatcmpl-123",
                                "object": "chat.completion",
                                "created": 1677652288,
                                "model": "llama-3.3-70b-versatile",
                                "choices": [{
                                    "index": 0,
                                    "message": {
                                        "role": "assistant",
                                        "content": "Hello, how can I help you today?"
                                    },
                                    "finish_reason": "stop"
                                }]
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                
                // Models response
                url.endsWith("/models") -> {
                    respond(
                        content = """
                            {
                                "object": "list",
                                "data": [
                                    {
                                        "id": "llama-3.3-70b-versatile",
                                        "object": "model",
                                        "created": 1677610602,
                                        "owned_by": "groq"
                                    },
                                    {
                                        "id": "llama-3.2-90b-vision-preview",
                                        "object": "model",
                                        "created": 1677649963,
                                        "owned_by": "groq"
                                    }
                                ]
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                
                // Not found response
                else -> {
                    respond(
                        content = """{"error": {"message": "Not found", "type": "invalid_request_error"}}""",
                        status = HttpStatusCode.NotFound,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
        }
        
        mockClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }
        
        groqClient = GroqApiClient(config, mockClient)
    }
    
    @Test
    fun `test create chat completion success`() = runBlocking {
        val request = buildJsonObject {
            put("model", "llama-3.3-70b-versatile")
            put("messages", buildJsonObject {
                put("role", "user")
                put("content", "Hello")
            })
        }
        
        val response = groqClient.createChatCompletion(request)
        
        assertNotNull(response)
        assertEquals("chatcmpl-123", response["id"]?.toString()?.trim('"'))
        assertEquals(
            "Hello, how can I help you today?",
            response["choices"]?.toString()?.let { if (it.contains("Hello, how can I help you today?")) "Hello, how can I help you today?" else "" } ?: ""
           )

    }
    
    @Test
    fun `test list models success`() = runBlocking {
        val response = groqClient.listModels()
        
        assertNotNull(response)
        assertEquals("list", response["object"]?.toString()?.trim('"'))
        val data = response["data"]?.toString() ?: ""
        assert(data.contains("llama-3.3-70b-versatile"))
        assert(data.contains("llama-3.2-90b-vision-preview"))
    }
    
    /*@Test
    fun `test API error handling`() = runBlocking {
        // Create a client with a different URL to trigger 404
        val badConfig = GroqApiConfig("test-api-key", "https://api.groq.com/openai/v1/invalid")
        val badClient = GroqApiClient(badConfig, mockClient)
        
        val request = buildJsonObject {
            put("model", "llama-3.3-70b-versatile")
            put("messages", buildJsonObject {
                put("role", "user")
                put("content", "Hello")
            })
        }
        
        val exception = assertThrows<GroqApiException> {
            badClient.createChatCompletion(request)
        }

        print(exception)

        assertEquals(404, exception.statusCode)
        assert(exception.message.contains("Not found"))
    }*/
}