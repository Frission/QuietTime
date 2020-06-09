package com.firatyildiz.quiettime.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.firatyildiz.quiettime.helpers.QuietTimeDiffCallback
import com.firatyildiz.quiettime.model.entities.QuietTime

class QuietTimeRecyclerAdapter :
    RecyclerView.Adapter<QuietTimeRecyclerAdapter.QuietTimeViewHolder>() {

    var allQuietTimes: List<QuietTime>? = null

    fun setQuietTimes(quietTimes: List<QuietTime>) {
        allQuietTimes = quietTimes
        notifyDataSetChanged()
    }

    fun updateQuietTimes(quietTimes: List<QuietTime>) {
        if (allQuietTimes == null) {
            setQuietTimes(quietTimes)
            return
        }

        val differenceResult =
            DiffUtil.calculateDiff(QuietTimeDiffCallback(allQuietTimes!!, quietTimes))
        differenceResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuietTimeViewHolder {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        return allQuietTimes?.size ?: 0
    }

    override fun onBindViewHolder(holder: QuietTimeViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    class QuietTimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}