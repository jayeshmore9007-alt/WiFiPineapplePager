package com.example.wifipineapplepager.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wifipineapplepager.domain.model.DeviceState
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceStateDao {
    @Query("SELECT * FROM device_state WHERE id = :id")
    fun getState(id: String): Flow<DeviceState?>

@Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateState(state: DeviceState)

@Query("DELETE FROM device_state")
    suspend fun clearAll()
}

@Dao
interface LogEntryDao {
    @Query("SELECT * FROM logs ORDER BY timestamp DESC LIMIT :limit")
    fun getLatestLogs(limit: Int = 100): Flow<List<LogEntry>>

@Insert
    suspend fun insertLog(log: LogEntry)

@Query("DELETE FROM logs WHERE timestamp < :olderThanMs")
    suspend fun deleteLogs(olderThanMs: Long)

@Query("SELECT COUNT(*) FROM logs")
    suspend fun getLogCount(): Int
}
