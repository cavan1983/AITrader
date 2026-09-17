package com.example.aitrader

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missed_syncs")
data class MissedSyncEntity(
    @PrimaryKey val timestamp: Long
)
