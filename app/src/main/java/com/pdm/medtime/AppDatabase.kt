package com.pdm.medtime

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.pdm.medtime.dao.RemedioDAO
import com.pdm.medtime.dao.RemedioTomadoDAO
import com.pdm.medtime.entities.Remedio
import com.pdm.medtime.entities.RemedioTomado

@Database(entities = [Remedio::class, RemedioTomado::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun remedioDao(): RemedioDAO
    abstract fun remedioTomadoDao(): RemedioTomadoDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance

            }
        }
    }
}