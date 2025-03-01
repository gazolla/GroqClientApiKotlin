package com.groq.examples

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