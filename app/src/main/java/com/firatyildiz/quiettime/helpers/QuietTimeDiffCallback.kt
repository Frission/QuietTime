package com.firatyildiz.quiettime.helpers

import androidx.recyclerview.widget.DiffUtil
import com.firatyildiz.quiettime.model.entities.QuietTime

class QuietTimeDiffCallback(
    var oldQuietTimes: List<QuietTime>,
    var newQuietTimes: List<QuietTime>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldQuietTimes[oldItemPosition].id == newQuietTimes[newItemPosition].id
    }

    override fun getOldListSize(): Int {
        return oldQuietTimes.size
    }

    override fun getNewListSize(): Int {
        return newQuietTimes.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldQuietTimes[oldItemPosition] == newQuietTimes[newItemPosition]
    }
}