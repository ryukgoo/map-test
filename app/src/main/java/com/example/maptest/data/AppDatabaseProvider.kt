package com.example.maptest.data

import android.content.Context
import androidx.room.Room
import com.example.maptest.data.dao.LocationDao
import com.example.maptest.data.database.AppDatabase

object AppDatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "location_database"
            )
                // ✅ 새로운 API 사용 (모든 테이블을 삭제 후 재생성 허용 여부 지정)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
            INSTANCE = instance
            instance
        }
    }

    fun getLocationDao(context: Context): LocationDao =
        getDatabase(context).locationDao()
}