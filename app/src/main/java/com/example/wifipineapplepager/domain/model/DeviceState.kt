package com.example.wifipineapplepager.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "device_state")
data class DeviceState(
    @PrimaryKey
    val id: String = "pineapple_1",
    val isOnline: Boolean = false,
    val status: String = "UNKNOWN",
    val temperature: Float = 0f,
    val memoryFree: Long = 0L,
    val memoryTotal: Long = 0L,
    val cpuUsage: Float = 0f,
    val networksSeen: Int = 0,
    val lastUpdateTime: Long = 0L,
    val error: String? = null
)

@Serializable
data class ScanResult(
    val ssid: String,
    val bssid: String,
    val signal: Int,
    val channel: Int,
    val encryption: String
)

@Serializable
data class ApiResponse<T>(
    val status: String,
    val data: T? = null,
    val error: String? = null
)
