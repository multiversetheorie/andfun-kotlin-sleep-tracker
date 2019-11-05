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

package com.example.android.trackmysleepquality.sleepquality

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.databinding.FragmentSleepQualityBinding

/**
 * Fragment that displays a list of clickable icons,
 * each representing a sleep quality rating.
 * Once the user taps an icon, the quality is set in the current sleepNight
 * and the database is updated.
 */
class SleepQualityFragment : Fragment() {

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentSleepQualityBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_sleep_quality, container, false)

        // Reference to the application this fragment is attached to
        // The application is passed in to the ViewModelProvider.Factory (SleepQualityViewModelFactory)
        // (in this case: SleepQualityViewModelFactory)
        // requireNotNull throws and IllegalArgumentException if the value is null
        val application = requireNotNull(this.activity).application

        // Getting the arguments that came with the navigation
        val arguments = SleepQualityFragmentArgs.fromBundle(arguments!!)

        // Creates reference our datasource (ie. SleepDatabase) via a reference to a Dao (ie. SleepDatabaseDao)
        val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao

        // Creates an instance of the ViewModelFactory (here: SleepQualityViewModelFactory)
        // We pass in the sleepNightKey (from Bundle arguments) and the dataSource
        val viewModelFactory = SleepQualityViewModelFactory(arguments.sleepNightKey, dataSource)

        // "Asking" for an instance of the SleepQualityViewModel from the ViewModelProvider,
        // using the previously declared instance of the SleepQualityViewModelFactory
        val sleepQualityViewModel = ViewModelProviders.of(this, viewModelFactory).get(SleepQualityViewModel::class.java)

        // Set up observer for navigateToSleepTracker variable
        // If navigateToSleepTracker is true, navigate.
        // Call doneNavigating, to reset variable
        sleepQualityViewModel.navigateToSleepTracker.observe(this, Observer {
            if (it == true) {
                this.findNavController().navigate(
                        SleepQualityFragmentDirections.actionSleepQualityFragmentToSleepTrackerFragment()
                )
                sleepQualityViewModel.doneNavigating()
            }
        })

        // Pass the viewModel into binding ('Let the binding know about our viewModel')
        binding.sleepQualityViewModel = sleepQualityViewModel

        return binding.root
    }
}
