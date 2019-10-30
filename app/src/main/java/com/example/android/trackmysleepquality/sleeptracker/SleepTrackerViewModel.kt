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

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    // 1. Creating a Job, that manages Coroutines started by this ViewModel
    // Allows us to cancel all Coroutines started by this ViewModel, when it is destroyed
    private var viewModelJob = Job()

    // Cancels all Coroutines when onCleared is called (ie. the ViewModel is destroyed)
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    // 2. Defining a scope for the coroutines to run in
    // We 'ask' for an instance of CoroutineScope, passing in a Dispatcher and a Job.
    // Coroutines launched in the uiScope will run on the Main thread (Dispatchers.Main).
    // This is OK for Coroutines started by the ViewModel,
    // because they will eventually resolve in an update of the UI.
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // 3. Define variable to hold the current night as LiveData
    // LiveData, because we want to Observe it. Mutable, so that we can change it.
    private var tonight = MutableLiveData<SleepNight?>()

    // Variable for holding all 'nights' (rows) from the database. Using a function defined in DAO.
    private val nights = database.getAllNights()

    // Transformation is executed every time 'nights' receives new data from the database
    val nightsString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources)
    }

    // Encapsulated navigation event LiveData
    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
    val navigateToSleepQuality: LiveData<SleepNight>
        get() = _navigateToSleepQuality

    // Initialize tonight, so that we can use it
    init {
        initializeTonight()
    }

    // Launching a coroutine to get the current 'night' from the database
    fun initializeTonight() {
        uiScope.launch {
            tonight.value = getTonightFromDatabase()
        }
    }

    // Function used to actually get the current night from database
    // Marked with 'suspend' means the function is available to coroutines
    // While the function is suspended (waiting for a result), it unblocks the thread it is running on
    // This means that other functions or coroutines can run
    private suspend fun getTonightFromDatabase() : SleepNight? {
        // Return the result from another coroutine that runs in the Dispatchers.IO context
        return withContext(Dispatchers.IO) {
            var night = database.getTonight()
            // If the start end end times are not the same (meaning the night has already been completed), return null
            if (night?.endTimeMilli != night?.startTimeMilli) {
                night = null
            }
            night
        }
    }

    // Function to call when pressing Start button
    fun onStartTracking() {
        // Launch another coroutine on the main thread, because we need this result to continue and update the UI
        uiScope.launch {
            // Create a new SleepNight, which captures the current time as the start time (by class definition)
            val newNight = SleepNight()
            // Insert newNight into the database (this is also a suspend function)
            insert(newNight)
            // Set tonight by calling getTonightFromDatabase
            // (We set tonight to be the last row in the database - ie. the one we inserted in the previous line.)
            tonight.value = getTonightFromDatabase()
        }
    }

    // Function that inserts night into the database (via a coroutine)
    private suspend fun insert(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.insert(night)
        }
    }

    // Function to call when pressing Stop button
    fun onStopTracking() {
        uiScope.launch {
            // Capture the value of the current night in oldNight. If the value is null, return to the launch call
            val oldNight = tonight.value ?: return@launch
            // Set current time as end time of the night
            oldNight.endTimeMilli = System.currentTimeMillis()
            // Update the database with oldNight
            update(oldNight)
            // Trigger navigation (If oldNight is null, we can't set the variable, so we don't navigate)
            _navigateToSleepQuality.value = oldNight
        }
    }
    // Function that updates the database (via a coroutine)
    private suspend fun update(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.update(night)
        }
    }

    // Function to call when pressing Clear button
    fun onClear() {
        uiScope.launch {
            clear()
            tonight.value = null
        }
    }

    // Function that clears the database (via a coroutine)
    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }

    // Resets navigation event
    fun doneNavigating() {
        _navigateToSleepQuality.value = null
    }
}

