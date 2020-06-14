package com.firatyildiz.quiettime.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.firatyildiz.quiettime.R
import com.firatyildiz.quiettime.helpers.DateTimeLocalizationHelper
import com.firatyildiz.quiettime.helpers.QuietTimeDiffCallback
import com.firatyildiz.quiettime.model.entities.QuietTime
import timber.log.Timber
import java.io.Serializable
import java.util.*

/**
 * @author Fırat Yıldız
 */
class QuietTimeRecyclerAdapter(
    var context: Context,
    var dayNames: List<String>,
    var currentLocale: Locale,
    var itemListener: QuietTimeItemViewClickListener
) :
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
            holder.daysText.text = if (it[position].days != 127)
                DateTimeLocalizationHelper.getActiveWeekDaysAsString(dayNames, it[position].days)
            else
                context.getString(R.string.every_day)

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

            // assign listeners here

            for (i in 0..6) {
                holder.dayChoiceButtons[i].text = dayNames[i]
                holder.dayChoiceButtons[i].setOnClickListener { itemListener.onDaySelected(i) }
            }

            holder.editOrCloseButton.setOnClickListener {
                itemListener.onEditButtonClicked(
                    position,
                    allQuietTimes!![position],
                    holder
                )
            }
            holder.saveButton.setOnClickListener { itemListener.onSaveButtonClicked() }
            holder.editAllButton.setOnClickListener { itemListener.onEditAllButtonClicked() }
            holder.deleteButton.setOnClickListener { itemListener.onDeleteButtonClicked() }

            if (holder.isExpanded) {
                holder.editLayout.visibility = View.VISIBLE

                // set the days for the checkboxes by comparing bits
                for (i in 0..6)
                    holder.dayChoiceButtons[i].isChecked = it[position].days and (1 shl i) != 0
            } else
                holder.editLayout.visibility = View.GONE

            // animate the appearing of this item if it is the first time it's appearing
            if (holder.isAppearingForFirstTime) {
                holder.container.animation = AnimationUtils.loadAnimation(context, R.anim.pop_in)
                holder.container.animation.startOffset = (100 * position).toLong()
                holder.isAppearingForFirstTime = false
            } else
                holder.container.clearAnimation()
        }
    }

    //endregion

    private fun animateViewHolder(holder: QuietTimeViewHolder) {
        holder.container.animation = AnimationUtils.loadAnimation(context, R.anim.pop_in)
    }

    //region View Holder

    class QuietTimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), Serializable {
        companion object {
            @JvmStatic
            private val serialVersionUID: Long = 12062020L
        }

        var container: View
        var titleText: TextView
        var daysText: TextView
        var timeRangeText: TextView
        var dayChoiceButtons: List<CheckBox>
        var editLayout: View
        var editAllButton: Button
        var editOrCloseButton: ImageButton
        var deleteButton: Button
        var saveButton: Button

        var isExpanded = false
        var isAppearingForFirstTime = true

        init {
            container = itemView.findViewById(R.id.quiet_time_card)
            titleText = itemView.findViewById(R.id.qt_item_title)
            daysText = itemView.findViewById(R.id.qt_item_days)
            editLayout = itemView.findViewById(R.id.qt_item_edit_layout)
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

            editAllButton = itemView.findViewById(R.id.qt_item_edit_all_button)
            saveButton = itemView.findViewById(R.id.qt_item_save_button)
            deleteButton = itemView.findViewById(R.id.qt_item_delete_button)
            editOrCloseButton = itemView.findViewById(R.id.qt_item_edit_or_close_button)
        }
    }

    //endregion

    /**
     * Events for the recycler view items to fire
     */
    interface QuietTimeItemViewClickListener {
        fun onEditButtonClicked(
            position: Int,
            quietTime: QuietTime,
            holder: QuietTimeViewHolder
        )

        fun onDaySelected(indexOfDay: Int)
        fun onEditAllButtonClicked()
        fun onDeleteButtonClicked()
        fun onSaveButtonClicked()
    }
}