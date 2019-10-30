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
import com.example.android.trackmysleepquality.databinding.FragmentSleepTrackerBinding

/**
 * A fragment with buttons to record start and end times for sleep, which are saved in
 * a database. Cumulative data is displayed in a simple scrollable TextView.
 * (Because we have not learned about RecyclerView yet.)
 */
class SleepTrackerFragment : Fragment() {

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentSleepTrackerBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_sleep_tracker, container, false)

        // Reference to the application this fragment is attached to
        // The application is passed in to the ViewModelProver.Factory (SleepTrackerViewModelFactory)
        // (in this case: SleepTrackerViewModelFactory)
        // requireNotNull throws and IllegalArgumentException if the value is null
        val application = requireNotNull(this.activity).application

        // Creates reference our datasource (ie. SleepDatabase) via a reference to a Dao (ie. SleepDatabaseDao)
        val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao

        // Creates an instance of the ViewModelFactory (here: SleepTrackerViewModelFactory)
        // We pass in the dataSource and the application
        val viewModelFactory = SleepTrackerViewModelFactory(dataSource, application)

        // We 'ask' the ViewModelProver for an instance of SleepTrackerViewModel, using our ViewModelFactory
        val sleepTrackerViewModel =
                ViewModelProviders.of(this, viewModelFactory).get(SleepTrackerViewModel::class.java)

        // Set up observer for navigateToSleepQuality variable
        // Navigate and pass along the ID of the current night
        // Call doneNavigating, to reset variable
        sleepTrackerViewModel.navigateToSleepQuality.observe(this, Observer { night ->
            night?.let {
                this.findNavController().navigate(
                        SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepQualityFragment(night.nightId)
                )
                sleepTrackerViewModel.doneNavigating()
            }
        })

        // Pass the viewModel into binding ('Let the binding know about our viewModel')
        binding.sleepTrackerViewModel = sleepTrackerViewModel

        // Specifies the current activity/fragment as the LifecycleOwner of the DataBinding
        // This is necessary for the binding to observe LiveData updates
        binding.setLifecycleOwner(this)

        return binding.root
    }
}
