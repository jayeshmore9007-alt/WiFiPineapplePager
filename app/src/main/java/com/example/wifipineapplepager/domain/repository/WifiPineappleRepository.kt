package com.example.wifipineapplepager.domain.repository

import com.example.wifipineapplepager.data.local.DeviceStateDao
import com.example.wifipineapplepager.data.local.LogEntry
import com.example.wifipineapplepager.data.local.LogEntryDao
import com.example.wifipineapplepager.data.remote.WifiPineappleClient
import com.example.wifipineapplepager.domain.model.DeviceState
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber
import javax.inject.Inject

class WifiPineappleRepository @Inject constructor(
    private val client: WifiPineappleClient,
    private val deviceStateDao: DeviceStateDao,
    private val logEntryDao: LogEntryDao
) {
    fun getDeviceState(): Flow<DeviceState?> = deviceStateDao.getState("pineapple_1")

fun getLatestLogs(limit: Int = 100): Flow<List<LogEntry>> = 
        logEntryDao.getLatestLogs(limit)

suspend fun fetchAndUpdateStatus(): Result<DeviceState> {
        return try {
            val statusResult = client.getStatus()
            
            if (statusResult.isSuccess) {
                val statusJson = statusResult.getOrNull() ?: ""
                val parsed = parseStatusResponse(statusJson)
                deviceStateDao.updateState(parsed)
                logEntry("Device status updated", "INFO")
                Result.success(parsed)
            } else {
                val error = statusResult.exceptionOrNull()?.message ?: "Unknown error"
                val failedState = DeviceState(
                    isOnline = false,
                    error = error
                )
                deviceStateDao.updateState(failedState)
                logEntry("Failed to fetch status: \$error", "ERROR")
                Result.failure(statusResult.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching status")
            logEntry("Error: \${e.message}", "ERROR")
            Result.failure(e)
        }
    }

suspend fun startScan(durationSeconds: Int = 60): Result<String> {
        return try {
            val result = client.scan(durationSeconds)
            if (result.isSuccess) {
                logEntry("Scan started for \$durationSeconds seconds", "INFO")
            } else {
                logEntry("Scan failed: \${result.exceptionOrNull()?.message}", "ERROR")
            }
            result
        } catch (e: Exception) {
            logEntry("Scan error: \${e.message}", "ERROR")
            Result.failure(e)
        }
    }

suspend fun rebootDevice(delaySeconds: Int = 0): Result<String> {
        return try {
            val result = client.reboot(delaySeconds)
            if (result.isSuccess) {
                logEntry("Reboot initiated (delay: \$delaySeconds s)", "INFO")
            } else {
                logEntry("Reboot failed: \${result.exceptionOrNull()?.message}", "ERROR")
            }
            result
        } catch (e: Exception) {
            logEntry("Reboot error: \${e.message}", "ERROR")
            Result.failure(e)
        }
    }

private fun parseStatusResponse(json: String): DeviceState {
        return try {
            val element = Json.parseToJsonElement(json)
            val obj = element.jsonObject
            
            DeviceState(
                isOnline = true,
                status = obj["status"]?.jsonPrimitive?.content ?: "UNKNOWN",
                temperature = obj["temperature"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 0f,
                memoryFree = obj["memory_free"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L,
                memoryTotal = obj["memory_total"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L,
                cpuUsage = obj["cpu_usage"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 0f,
                networksSeen = obj["networks_seen"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                lastUpdateTime = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse status response")
            DeviceState(
                isOnline = false,
                error = "Parse error: \${e.message}",
                lastUpdateTime = System.currentTimeMillis()
            )
        }
    }

private suspend fun logEntry(message: String, level: String = "INFO") {
        try {
            logEntryDao.insertLog(
                LogEntry(
                    timestamp = System.currentTimeMillis(),
