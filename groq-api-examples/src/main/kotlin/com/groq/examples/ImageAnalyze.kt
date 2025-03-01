package com.groq.examples

import com.groq.api.client.GroqClientFactory
import com.groq.api.utils.JsonUtils
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val apiKey = System.getenv("GROQ_API_KEY") ?: "your_api_key_here"

    GroqClientFactory.createClient(apiKey).use { client ->
        // Using an image URL
        val imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4d/Cat_November_2010-1a.jpg/800px-Cat_November_2010-1a.jpg"
        val response = client.createVisionCompletionWithImageUrl(
            imageUrl = imageUrl,
            prompt = "Describe what you see in this image.",
            model = "llama-3.2-90b-vision-preview"
        )

        val description = JsonUtils.extractContentFromCompletion(response)
        println("Image description: $description")


    }
}