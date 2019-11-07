package com.example.android.trackmysleepquality.sleeptracker

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.TextItemViewHolder
import com.example.android.trackmysleepquality.database.SleepNight

class SleepNightAdapter: RecyclerView.Adapter<TextItemViewHolder>() {

    // List of SleepNights that the RecyclerView can display
    // RecyclerView does not use this directly
    // SleepNightAdapter >>adapts<< it for the RecyclerView
    var data = listOf<SleepNight>()

    // Counts the number of items we want to display in the RecyclerView
    // Documentation: "Returns the total number of items in the data set held by the adapter."
    override fun getItemCount() = data.size

    // Describes how to actually draw an item
    // holder: The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
    // position: The position of the item within the adapter's data set.
    override fun onBindViewHolder(holder: TextItemViewHolder, position: Int) {
        val item = data[position]
        holder.textView.text = item.sleepQuality.toString()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextItemViewHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}