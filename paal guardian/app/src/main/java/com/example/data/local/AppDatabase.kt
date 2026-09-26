package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        CanEntity::class,
        SensorReadingEntity::class,
        AlertEntity::class,
        ThermalRecordEntity::class,
        MlPredictionEntity::class,
        SyncQueueEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun canDao(): CanDao
    abstract fun sensorReadingDao(): SensorReadingDao
    abstract fun alertDao(): AlertDao
    abstract fun thermalRecordDao(): ThermalRecordDao
    abstract fun mlPredictionDao(): MlPredictionDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "paal_guardian.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
