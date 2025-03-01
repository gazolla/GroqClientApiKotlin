package com.groq.api.models

import kotlinx.serialization.json.JsonObject

data class Function(
    val name: String,
    val description: String,
    val parameters: JsonObject,
    val execute: suspend (String) -> String
)