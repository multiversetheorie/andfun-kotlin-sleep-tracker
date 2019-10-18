/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SleepNight::class], version = 1, exportSchema = false)
abstract class SleepDatabase : RoomDatabase() {

    /**
     * We 'inform' the database about the Dao associated with the entity (SleepNight),
     * so that we can interact with the database
     */
    abstract val sleepDatabaseDao: SleepDatabaseDao

    /**
     * The companion object allows clients to access the methods for creating
     * or getting the database, without instantiating the class.
     * We only need on instance of the DB, so it can be a singleton.
     * (There is no need to instantiate it.)
     */
    companion object {

        /**
         * INSTANCE keeps a refence to the database once we have one.
         * This helps us to avoid repeatedly opening connenctions to the database.
         * (Doing this would be 'expensive'.)
         *
         * @Volatile means, that
         * (1) The value of INSTANCE is always up-to-date and the same to all execution threads.
         * (2) The value is never cached, all writes and read are done to and from the main memory.
         * (3) Changes made by one thread are visible to all other threads immediately.
         */
        @Volatile
        private var INSTANCE: SleepDatabase? = null

        /**
         * getInstance returns a reference to the database.
         * We pass in the context, because the database builder (Room.databaseBuilder) requires it.
         *
         * Multiple threads can ask for an instance of the DB at the same time,
         * which would leave us with two instances instead of the required one.
         * Because of this, we wrap the code in a synchronized block:
         * only on thread can access the block of code inside synchronized. This makes
         * sure, that the DB only gets initialized once.
         */
        fun getInstance(context: Context): SleepDatabase {
            synchronized(this) {
                // We copy the current value of INSTANCE into a local variable.
                // Smart cast is only available to local variables, not to class variables.
                var instance = INSTANCE

                // If the instance does not already exist, we create it
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            SleepDatabase::class.java,
                            "sleep_history_database"
                    )
                            // We wipe and rebuild the database if the schema changes.
                            // You DO NOT do this, if you want to save existing data after migration.
                            .fallbackToDestructiveMigration()

                            .build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}