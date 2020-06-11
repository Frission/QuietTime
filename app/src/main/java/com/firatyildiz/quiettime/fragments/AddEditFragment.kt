package com.firatyildiz.quiettime.fragments


import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.firatyildiz.quiettime.R
import com.firatyildiz.quiettime.model.entities.QuietTime
import com.firatyildiz.quiettime.model.viewmodel.QuietTimeViewModel
import timber.log.Timber


/**
 * @author Fırat Yıldız
 *
 * Created on 09/06/2020
 */
class AddEditFragment : Fragment(), View.OnClickListener, TextView.OnEditorActionListener {

    companion object {
        const val ARG_QUIET_TIME = "QUIET_TIME"

        /**
         * Puts the Quiet Time object as an argument if it is supplied
         */
        @JvmStatic
        fun newInstance(quietTime: QuietTime?) = AddEditFragment().apply {
            quietTime?.let {
                arguments = Bundle().apply { putSerializable(ARG_QUIET_TIME, it) }
            }
        }
    }

    private var quietTime: QuietTime? = null
    private lateinit var viewModel: QuietTimeViewModel

    private lateinit var startTimeLabel: TextView
    private lateinit var endTimeLabel: TextView
    private lateinit var titleEditText: EditText
    private lateinit var timePicker: TimePicker
    private var dayChoices: List<CheckBox> = emptyList()

    private var startTime = 12 * 60
    private var endTime = 12 * 60
    private var days = 0

    /**
     * If this is true, the user is using the time picker to set the start time
     * otherwise the time picker value will be assigned to the end time
     */
    private var editingStartTime = true

    /**
     * This value is set true if the user is trying to edit an existing quiet time
     */
    private var isInEditingMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        viewModel = ViewModelProvider(this).get(QuietTimeViewModel::class.java)
        // get parameters if they exist
        arguments?.let {
            quietTime = it.getSerializable(ARG_QUIET_TIME) as? QuietTime

            // if a quiet time was passed through the arguments,
            // it measn the user's intent is to edit an existing quiet time,
            // not create a new one
            if (quietTime != null)
                isInEditingMode = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleEditText = view.findViewById(R.id.edit_quiet_time_title)
        startTimeLabel = view.findViewById(R.id.start_time_label)
        endTimeLabel = view.findViewById(R.id.end_time_label)
        timePicker = view.findViewById(R.id.edit_time_picker)
        dayChoices = listOf(
            view.findViewById(R.id.edit_day1),
            view.findViewById(R.id.edit_day2),
            view.findViewById(R.id.edit_day3),
            view.findViewById(R.id.edit_day4),
            view.findViewById(R.id.edit_day5),
            view.findViewById(R.id.edit_day6),
            view.findViewById(R.id.edit_day7)
        )

        for (dayChoice in dayChoices)
            dayChoice.setOnClickListener(this)

        titleEditText.setOnEditorActionListener(this)
        startTimeLabel.setOnClickListener(this)
        endTimeLabel.setOnClickListener(this)
        timePicker.setIs24HourView(true)

        resetTimePicker(12 * 60)

        setInitialAppearanceForTimeLabels()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.add_edit_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_addedit_done) {
            // TODO insert quiet time here, should also display a dialog if it collides with other quiet times
            //val quietTime = QuietTime(titleEditText.text.toString(), days, startTime, endTime)
            val collidingQuietTimes = viewModel.getCollidingQuietTimes(days, startTime, endTime)

            if (collidingQuietTimes.isEmpty()) {

            } else {
                // TODO Display a dialog warning the user that there are colliding dialogs
            }

            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * There is only one time picker, and the user can edit both the start time and the end time with it.
     * Thus, I've provided two TextView that can be clicked. The user knows whether they're editing the start time
     * or the end time by it's bacground color
     * Here the swap between the two is animated to make it nice
     */
    override fun onClick(view: View?) {
        view?.let {
            when (view.id) {
                R.id.start_time_label -> {
                    if (!editingStartTime) {
                        var transitionDrawable = startTimeLabel.background as TransitionDrawable
                        transitionDrawable.startTransition(150)

                        transitionDrawable = endTimeLabel.background as TransitionDrawable
                        transitionDrawable.reverseTransition(150)

                        editingStartTime = true
                        startTime = timePicker.currentHour * 60 + timePicker.currentMinute
                        resetTimePicker(endTime)
                    }
                }

                R.id.end_time_label -> {
                    if (editingStartTime) {
                        var transitionDrawable = startTimeLabel.background as TransitionDrawable
                        transitionDrawable.reverseTransition(150)

                        transitionDrawable = endTimeLabel.background as TransitionDrawable
                        transitionDrawable.startTransition(150)

                        editingStartTime = false
                        endTime = timePicker.currentHour * 60 + timePicker.currentMinute
                        resetTimePicker(startTime)
                    }
                }

                R.id.edit_day1 -> setDayChoice(0)
                R.id.edit_day2 -> setDayChoice(1)
                R.id.edit_day3 -> setDayChoice(2)
                R.id.edit_day4 -> setDayChoice(3)
                R.id.edit_day5 -> setDayChoice(4)
                R.id.edit_day6 -> setDayChoice(5)
                R.id.edit_day7 -> setDayChoice(6)
            }
        }
    }

    private fun setDayChoice(index: Int) {
        // set the bit where this day is to !
        // for example if wednesday was chosen
        // this integers bits will look like 0010000...
        // that also equals to 8
        val orValue = if (index == 0) 1 else 1 shl index

        if (dayChoices[index].isChecked)
            days = days or orValue
        else
            days = days and 127 - orValue
        Timber.d("Set days are ${days.toString(2).padStart(7, '0').padEnd(7, '0').reversed()}")
    }

    /**
     * Marks the Start Time Label as selected by making it's background lightup
     */
    private fun setInitialAppearanceForTimeLabels() {
        val transitionDrawable = startTimeLabel.background as TransitionDrawable
        transitionDrawable.startTransition(0)
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEND) {
            Timber.d("clearing focus from text")
            titleEditText.clearFocus()
        }
        return false
    }

    private fun resetTimePicker(timeInMinutes: Int) {
        // this works even if it is deprecated
        timePicker.currentHour = timeInMinutes / 60
        timePicker.currentMinute = timeInMinutes % 60
    }
}