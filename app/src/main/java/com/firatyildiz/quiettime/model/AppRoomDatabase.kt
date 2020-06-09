package com.firatyildiz.quiettime.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.firatyildiz.quiettime.model.daos.QuietTimeDao
import com.firatyildiz.quiettime.model.entities.QuietTime

@Database(entities = [QuietTime::class], version = 1)
abstract class AppRoomDatabase : RoomDatabase() {

    abstract fun quietTimeDao(): QuietTimeDao

    companion object {
        @Volatile
        private var INSTANCE: AppRoomDatabase? = null

        fun getDatabase(context: Context): AppRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null)
                return tempInstance

            synchronized(this) {
                // allowing main thread queries here because
                // our database is super small, and there is hardly any data
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppRoomDatabase::class.java,
                    QuietTimeConstants.DATABASE_NAME
                )
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}