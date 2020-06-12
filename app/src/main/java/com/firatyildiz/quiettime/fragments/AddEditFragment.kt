package com.firatyildiz.quiettime.fragments


import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import androidx.lifecycle.ViewModelProvider
import com.firatyildiz.quiettime.R
import com.firatyildiz.quiettime.app.BaseFragment
import com.firatyildiz.quiettime.app.OnFragmentNavigationListener
import com.firatyildiz.quiettime.model.entities.QuietTime
import com.firatyildiz.quiettime.model.viewmodel.QuietTimeViewModel
import timber.log.Timber


/**
 * @author Fırat Yıldız
 *
 * Created on 09/06/2020
 */
class AddEditFragment : BaseFragment(), View.OnClickListener, TextView.OnEditorActionListener,
    TimePicker.OnTimeChangedListener {

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

    private var titleEdited = false
    private var daysEdited = false

    // since the time picker start with the start time, i'll consider the start time edited
    //private var startTimeEdited = true
    private var endTimeEdited = false

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

        viewModel = ViewModelProvider(requireActivity()).get(QuietTimeViewModel::class.java)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("startTime", startTime)
        outState.putInt("endTime", endTime)
        outState.putInt("days", days)
        outState.putBoolean("titleEdited", titleEdited)
        outState.putBoolean("daysEdited", daysEdited)
        outState.putBoolean("endTimeEdited", endTimeEdited)
        outState.putBoolean("editingStartTime", editingStartTime)
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
        timePicker.setOnTimeChangedListener(this)
        timePicker.setIs24HourView(true)

        if (savedInstanceState != null) {
            startTime = savedInstanceState.getInt("startTime", 12 * 60)
            endTime = savedInstanceState.getInt("endTime", 12 * 60)
            days = savedInstanceState.getInt("days", 0)
            titleEdited = savedInstanceState.getBoolean("titleEdited", false)
            daysEdited = savedInstanceState.getBoolean("daysEdited", false)
            endTimeEdited = savedInstanceState.getBoolean("endTimeEdited", false)
            editingStartTime = savedInstanceState.getBoolean("editingStartTime", false)

            setTimePicker(if (editingStartTime) startTime else endTime)
        } else if (isInEditingMode) {
            days = quietTime!!.days
            startTime = quietTime!!.startTime
            endTime = quietTime!!.endTime

            titleEditText.setText(quietTime!!.title)
            setTimePicker(quietTime!!.startTime)

            for (i in 0..6)
                dayChoices[i].isChecked = days and (1 shl i) != 0

        } else {
            setTimePicker(12 * 60)
        }

        setInitialAppearanceForTimeLabels()
        setTitle()
    }

    /**
     * Marks the time label as selected by making it's background light up
     */
    private fun setInitialAppearanceForTimeLabels() {
        if (editingStartTime) {
            val transitionDrawable = startTimeLabel.background as TransitionDrawable
            transitionDrawable.startTransition(0)
        } else {
            val transitionDrawable = endTimeLabel.background as TransitionDrawable
            transitionDrawable.startTransition(0)
        }
    }

    private fun setTitle() {
        if (isInEditingMode)
            requireActivity().title = getString(R.string.editing_quiet_time, quietTime!!.title)
        else
            requireActivity().title = getString(R.string.new_quiet_time)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.add_edit_fragment_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.getItem(0).isEnabled = (titleEdited && daysEdited && endTimeEdited) || isInEditingMode
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_addedit_done) {

            if (titleEditText.text.isNullOrEmpty()) {
                titleEdited = false
                requireActivity().invalidateOptionsMenu()
                return super.onOptionsItemSelected(item)
            }

            val collidingQuietTimes = if (isInEditingMode)
                viewModel.getCollidingQuietTimes(quietTime!!.id, days, startTime, endTime)
            else
                viewModel.getCollidingQuietTimes(days, startTime, endTime)

            if (collidingQuietTimes.isEmpty()) {
                saveQuietTimeAndReturn()
            } else {
                // TODO Display a dialog warning the user that there are colliding dialogs
            }

            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveQuietTimeAndReturn() {
        if (isInEditingMode) {
            Timber.d("updating quiet time ${titleEditText.text}")
            quietTime!!.title = titleEditText.text.toString()
            quietTime!!.days = days
            quietTime!!.startTime = startTime
            quietTime!!.endTime = endTime

            viewModel.updateQuietTime(quietTime!!)
        } else {
            Timber.d("inserting quiet time ${titleEditText.text}")

            val quietTime = QuietTime(titleEditText.text.toString(), days, startTime, endTime)
            viewModel.insertQuietTime(quietTime)
        }
        // finally navigate back after updating or inserting the quiet time
        (activity as OnFragmentNavigationListener).navigateBack()
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
                        setTimePicker(startTime)

                        requireActivity().invalidateOptionsMenu()
                    }
                }

                R.id.end_time_label -> {
                    if (editingStartTime) {
                        var transitionDrawable = startTimeLabel.background as TransitionDrawable
                        transitionDrawable.reverseTransition(150)

                        transitionDrawable = endTimeLabel.background as TransitionDrawable
                        transitionDrawable.startTransition(150)

                        editingStartTime = false
                        setTimePicker(endTime)

                        endTimeEdited = true
                        requireActivity().invalidateOptionsMenu()
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

        daysEdited = days != 0
        requireActivity().invalidateOptionsMenu()

        Timber.d("Set days are ${days.toString(2).padStart(7, '0').padEnd(7, '0').reversed()}")
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEND) {
            Timber.d("clearing focus from text")
            titleEditText.clearFocus()

            titleEdited = titleEditText.text.isNotEmpty()

            requireActivity().invalidateOptionsMenu()
        }
        return false
    }

    private fun setTimePicker(timeInMinutes: Int) {
        // this works even if it is deprecated
        timePicker.currentHour = timeInMinutes / 60
        timePicker.currentMinute = timeInMinutes % 60
    }

    override fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int) {
        if (editingStartTime)
            startTime = hourOfDay * 60 + minute
        else
            endTime = hourOfDay * 60 + minute
    }
}