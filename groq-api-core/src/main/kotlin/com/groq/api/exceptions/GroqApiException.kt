package com.groq.api.exceptions

/**
 * Exception thrown when an error occurs during API operations.
 *
 * @property statusCode The HTTP status code or error code.
 * @property message A descriptive error message.
 * @property cause The underlying cause of the exception.
 */
class GroqApiException(
    val statusCode: Int,
    override val message: String,
    val errorType: String? = null,
    val errorCode: String? = null,
    override val cause: Throwable? = null
) : Exception(message, cause)