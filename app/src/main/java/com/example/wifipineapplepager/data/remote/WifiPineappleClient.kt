package com.example.wifipineapplepager.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.IOException
import kotlin.math.pow

data class WifiPineappleConfig(
    val ipAddress: String = "192.168.1.1",
    val port: Int = 8080,
    val timeout: Long = 5000,
    val maxRetries: Int = 3
)

class WifiPineappleClient(
    private val config: WifiPineappleConfig
) {
    private val baseUrl = "http://${config.ipAddress}:${config.port}"

private val httpClient = HttpClient(Android) {
        install(Logging)
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        engine {
            connectTimeout = config.timeout.toInt()
        }
    }

suspend fun <T> get(
        endpoint: String,
        parser: (String) -> T? = { null }
    ): Result<T> {
        return executeWithRetry { attempt ->
            try {
                val response = httpClient.get("$baseUrl$endpoint")
                val body = response.bodyAsText()
                
                if (parser != null) {
                    val parsed = parser(body)
                    if (parsed != null) {
                        Result.success(parsed)
                    } else {
                        Result.failure(Exception("Failed to parse response"))
                    }
                } else {
                    @Suppress("UNCHECKED_CAST")
                    Result.success(body as T)
                }
            } catch (e: IOException) {
                if (attempt == config.maxRetries - 1) {
                    Result.failure(e)
                } else {
                    throw e
                }
            }
        }
    }

suspend fun <T> post(
        endpoint: String,
        body: String,
        parser: (String) -> T? = { null }
    ): Result<T> {
        return executeWithRetry { attempt ->
            try {
                val response = httpClient.post("$baseUrl$endpoint") {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
                val responseBody = response.bodyAsText()
                
                if (parser != null) {
                    val parsed = parser(responseBody)
                    if (parsed != null) {
                        Result.success(parsed)
                    } else {
                        Result.failure(Exception("Failed to parse response"))
                    }
                } else {
                    @Suppress("UNCHECKED_CAST")
                    Result.success(responseBody as T)
                }
            } catch (e: IOException) {
                if (attempt == config.maxRetries - 1) {
                    Result.failure(e)
                } else {
                    throw e
                }
            }
        }
    }

private suspend fun <T> executeWithRetry(block: suspend (Int) -> Result<T>): Result<T> {
        var lastError: Exception? = null
        
        repeat(config.maxRetries) { attempt ->
            try {
                val result = block(attempt)
                if (result.isSuccess) {
                    return result
                } else {
                    lastError = result.exceptionOrNull()
                    if (attempt < config.maxRetries - 1) {
                        val delayMs = (2.0.pow(attempt) * 1000).toLong()
                        Timber.w("Retry attempt ${attempt + 1} after ${delayMs}ms")
                        kotlinx.coroutines.delay(delayMs)
                    }
                }
            } catch (e: IOException) {
                lastError = e
                if (attempt < config.maxRetries - 1) {
                    val delayMs = (2.0.pow(attempt) * 1000).toLong()
                    Timber.w("Network error, retrying after ${delayMs}ms: ${e.message}")
                    kotlinx.coroutines.delay(delayMs)
                }
            }
        }
        
        return Result.failure(lastError ?: Exception("Unknown error after retries"))
    }

suspend fun getStatus(): Result<String> {
        return get("/api/status") { it }
    }

suspend fun scan(durationSeconds: Int = 60): Result<String> {
        return post("/api/scan", """{"duration":$durationSeconds}""") { it }
    }

suspend fun reboot(delaySeconds: Int = 0): Result<String> {
        return post("/api/reboot", """{"delay_seconds":$delaySeconds}""") { it }
    }

fun close() {
        httpClient.close()
    }
}
