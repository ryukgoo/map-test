package com.example.maptest.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val latitude: Double = 37.5665,
    val longitude: Double = 126.9780,
    val timestamp: Long = System.currentTimeMillis()
)
