/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.william.my.basic.basic_repo.database

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.basic.basic_repo.database.dao.ArticleDao

/**
 * 包含 Article 表的 Room 数据库
 *
 * 注意：生产环境数据库应将 exportSchema 设为 true。
 */
@Database(
    entities = [ArticleDetailData::class],
    version = 1,
    exportSchema = false,
)
abstract class ArticleDatabase : RoomDatabase() {

    companion object {

        private const val DB_NAME = "Articles.db"

        @Volatile
        private var instance: ArticleDatabase? = null

        fun getInstance(context: Context): ArticleDatabase = instance ?: synchronized(this) {
            instance ?: createDataBase(context).also {
                instance = it
            }
        }

        /**
         * 创建并返回一个内存数据库实例，专供单元测试使用（支持主线程查询，随进程结束销毁）。
         */
        @VisibleForTesting
        fun createInMemoryDatabase(context: Context): ArticleDatabase = createDataBase(context, inMemory = true)

        /**
         * 清空并关闭数据库实例，供单元测试或重置时使用。
         */
        @VisibleForTesting
        fun resetDatabase() {
            synchronized(this) {
                instance?.apply {
                    clearAllTables()
                    close()
                }
                instance = null
            }
        }

        private fun createDataBase(
            context: Context,
            inMemory: Boolean = false,
        ): ArticleDatabase {
            val result = if (inMemory) {
                // 使用更快的内存中数据库进行测试
                Room.inMemoryDatabaseBuilder(
                    context.applicationContext,
                    ArticleDatabase::class.java,
                )
                    .allowMainThreadQueries()
                    .build()
            } else {
                // 使用 SQLite 的真实数据库
                Room.databaseBuilder(
                    context.applicationContext,
                    ArticleDatabase::class.java,
                    DB_NAME,
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
            }
            return result
        }
    }

    abstract fun articleDao(): ArticleDao
}
