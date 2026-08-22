package com.example.william.my.module.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * exportSchema = false 或者
 * arg("room.schemaLocation", "$projectDir/schemas")
 */
@Database(
    entities = [OAuth::class],
    version = 1,
    exportSchema = false,
)
abstract class OAuthDataBase : RoomDatabase() {

    companion object {
        private const val DB_NAME = "personal-db"

        private var instance: OAuthDataBase? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: createDataBase(context).also {
                    instance = it
                }
            }

        private fun createDataBase(context: Context): OAuthDataBase {
            return Room.databaseBuilder(
                context.applicationContext,
                OAuthDataBase::class.java,
                DB_NAME
            )
                .fallbackToDestructiveMigration(true)
                .build()
        }

        fun exit() {
            instance?.close()
            instance = null
        }
    }

    abstract fun getOAuthDao(): OAuthDao

}
