package com.firatyildiz.quiettime.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.firatyildiz.quiettime.R
import com.firatyildiz.quiettime.helpers.DateTimeLocalizationHelper
import com.firatyildiz.quiettime.helpers.QuietTimeDiffCallback
import com.firatyildiz.quiettime.model.entities.QuietTime
import timber.log.Timber

/**
 * @author Fırat Yıldız
 */
class QuietTimeRecyclerAdapter(var context: Context, var dayNames: List<String>) :
    RecyclerView.Adapter<QuietTimeRecyclerAdapter.QuietTimeViewHolder>() {

    var allQuietTimes: List<QuietTime>? = null

    fun setQuietTimes(quietTimes: List<QuietTime>) {
        allQuietTimes = quietTimes
        notifyDataSetChanged()
    }

    fun updateQuietTimes(quietTimes: List<QuietTime>) {
        if (allQuietTimes == null) {
            Timber.d("setting quiet times")
            setQuietTimes(quietTimes)
            notifyDataSetChanged()
            return
        }

        Timber.d("updating quiet times")
        val differenceResult =
            DiffUtil.calculateDiff(QuietTimeDiffCallback(allQuietTimes!!, quietTimes))
        allQuietTimes = quietTimes
        differenceResult.dispatchUpdatesTo(this)
    }

    // region ViewHolder overridden functions

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuietTimeViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.quiet_time_item, parent, false)
        return QuietTimeViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return allQuietTimes?.size ?: 0
    }

    override fun onBindViewHolder(holder: QuietTimeViewHolder, position: Int) {
        allQuietTimes?.let {
            holder.titleText.text = it[position].title
            holder.daysText.text =
                DateTimeLocalizationHelper.getActiveWeekDaysAsString(dayNames, it[position].days)

            if (it[position].startTime == it[position].endTime)
                holder.timeRangeText.text = context.getString(R.string.all_day)
            else {
                holder.timeRangeText.text = DateTimeLocalizationHelper.getTimeRangeAsReadableString(
                    it[position].startTime,
                    it[position].endTime
                )
                if (it[position].startTime > it[position].endTime)
                    holder.timeRangeText.text = context.getString(
                        R.string.qt_item_time_next_day,
                        holder.timeRangeText.text.toString()
                    )
            }

            for (i in 0..6)
                holder.dayChoiceButtons[i].text = dayNames[i]

            holder.editTimesButton.setOnClickListener { onEditButtonClicked.onItemClicked(position) }
            holder.saveButton.setOnClickListener { onSaveButtonClicked.onItemClicked(position) }

            animateViewHolder(holder)
        }
    }

    //endregion

    val onEditButtonClicked = object : OnRecyclerViewItemClicked {
        override fun onItemClicked(position: Int) {
            Timber.d("Item number %d was clicked", position)
            // TODO Implement this
        }
    }

    private val onSaveButtonClicked = object : OnRecyclerViewItemClicked {
        override fun onItemClicked(position: Int) {
            Timber.d("Item number %d was clicked", position)
            // TODO Implement this
        }
    }

    private fun animateViewHolder(holder: QuietTimeViewHolder) {
        holder.container.animation = AnimationUtils.loadAnimation(context, R.anim.anim_pop_in)
    }

    //region View Holder

    class QuietTimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var container: View
        var titleText: TextView
        var daysText: TextView
        var timeRangeText: TextView
        var dayChoiceButtons: List<RadioButton>
        var editTimesButton: Button
        var saveButton: Button

        var expanded = false

        init {
            container = itemView.findViewById(R.id.quiet_time_card)
            titleText = itemView.findViewById(R.id.qt_item_title)
            daysText = itemView.findViewById(R.id.qt_item_days)
            timeRangeText = itemView.findViewById(R.id.qt_item_time)
            dayChoiceButtons = listOf(
                itemView.findViewById(R.id.qt_item_edit_day1),
                itemView.findViewById(R.id.qt_item_edit_day2),
                itemView.findViewById(R.id.qt_item_edit_day3),
                itemView.findViewById(R.id.qt_item_edit_day4),
                itemView.findViewById(R.id.qt_item_edit_day5),
                itemView.findViewById(R.id.qt_item_edit_day6),
                itemView.findViewById(R.id.qt_item_edit_day7)
            )

            editTimesButton = itemView.findViewById(R.id.qt_item_edit_button)
            saveButton = itemView.findViewById(R.id.qt_item_edit_button)
        }
    }

    //endregion

    interface OnRecyclerViewItemClicked {
        fun onItemClicked(position: Int)
    }
}