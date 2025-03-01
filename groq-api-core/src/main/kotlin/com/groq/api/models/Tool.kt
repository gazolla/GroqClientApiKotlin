package com.groq.api.models

data class Tool(
    val type: String = "function",
    val function: Function
)