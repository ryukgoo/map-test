package com.example.maptest.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.maptest.data.LocationEntity

@Database(entities = [LocationEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}