package com.example.wifipineapplepager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.wifipineapplepager.domain.model.DeviceState

@Database(
    entities = [DeviceState::class, LogEntry::class],
    version = 1,
    exportSchema = false
)
abstract class WifiPineappleDatabase : RoomDatabase() {
    abstract fun deviceStateDao(): DeviceStateDao
    abstract fun logEntryDao(): LogEntryDao
}

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logs")
data class LogEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val level: String = "INFO", // INFO, WARN, ERROR
    val tag: String = "",
    val message: String = ""
)
