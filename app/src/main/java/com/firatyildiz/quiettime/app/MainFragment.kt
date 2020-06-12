package com.firatyildiz.quiettime.app

import android.app.AlertDialog
import android.os.Bundle
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.view.*
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.firatyildiz.quiettime.R
import com.firatyildiz.quiettime.adapters.QuietTimeRecyclerAdapter
import com.firatyildiz.quiettime.fragments.AddEditFragment
import com.firatyildiz.quiettime.model.entities.QuietTime
import com.firatyildiz.quiettime.model.viewmodel.QuietTimeViewModel
import timber.log.Timber

/**
 * @author Fırat Yıldız
 */
class MainFragment : BaseFragment(), QuietTimeRecyclerAdapter.QuietTimeItemViewClickListener {

    companion object {
        const val DIALOG_ID_COLLISION = 1
        const val DIALOG_ID_DELETE = 2
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: QuietTimeViewModel
    private lateinit var dayNames: List<String>
    private lateinit var adapter: QuietTimeRecyclerAdapter
    private lateinit var noQuietTimesText: TextView
    private lateinit var recyclerView: RecyclerView

    /**
     * The curent quiet time that is being edited by the user, if this is not null, it means that the quiet view item is expanded
     */
    private var currentQuietTime: QuietTime? = null
    private var currentQuietTimeViewHolder: QuietTimeRecyclerAdapter.QuietTimeViewHolder? = null
    private var tempDays: Int = 0

    private var currentDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(requireActivity()).get(QuietTimeViewModel::class.java)
        dayNames = listOf<String>(
            getString(R.string.monday_short),
            getString(R.string.tuesday_short),
            getString(R.string.wednesday_short),
            getString(R.string.thursday_short),
            getString(R.string.friday_short),
            getString(R.string.saturday_short),
            getString(R.string.sunday_short)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById<RecyclerView>(R.id.quiet_time_list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = QuietTimeRecyclerAdapter(requireContext(), dayNames, this)

        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        noQuietTimesText = view.findViewById(R.id.no_quiet_times_text)

        viewModel.allQuietTimes.observe(viewLifecycleOwner, Observer {
            Timber.d("live data callback")
            adapter.updateQuietTimes(it)

            if (it.isEmpty())
                noQuietTimesText.visibility = View.VISIBLE
            else
                noQuietTimesText.visibility = View.GONE
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        Timber.d("Creating main fragment options menu")
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.main_menu_add_quiet_time) {
            val addEditFragment = AddEditFragment.newInstance(null)
            (activity as BaseActivity).navigateToFragment(addEditFragment)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        currentQuietTime = null
        currentQuietTimeViewHolder = null
        currentDialog?.dismiss()
        currentDialog = null
        Timber.d("main fragment is stopped")
    }

    //region Quiet Time Recycler View Item Listener functions

    override fun onEditButtonClicked(
        position: Int,
        quietTime: QuietTime,
        holder: QuietTimeRecyclerAdapter.QuietTimeViewHolder
    ) {
        Timber.d("edit/close button clicked on quiet time: ${quietTime.title}")

        tempDays = quietTime.days

        // prepare the transition for the edit layout visibility changes
        val transition = TransitionInflater.from(requireContext())
            .inflateTransition(
                if (currentQuietTime == null)
                    R.transition.layout_to_visible
                else
                    R.transition.layout_to_gone
            )

        if (currentQuietTime == null) {
            Timber.d("expanding the recycler view")
            currentQuietTime = quietTime
            currentQuietTimeViewHolder = holder

            holder.isExpanded = true
        } else if (quietTime == currentQuietTime) {
            holder.isExpanded = false
            currentQuietTime = null
        } else {
            currentQuietTimeViewHolder!!.isExpanded = false
            holder.isExpanded = true

            currentQuietTime = quietTime
            currentQuietTimeViewHolder = holder
        }

        // animate the visibility changes inside the recycler view item
        // it took me several hours to find this solution
        // why is it so obscure :(
        TransitionManager.beginDelayedTransition(recyclerView, transition)
        adapter.notifyDataSetChanged()
    }

    override fun onDaySelected(indexOfDay: Int) {
        tempDays = tempDays xor (1 shl indexOfDay)
    }

    override fun onEditAllButtonClicked() {
        val addEditFragment = AddEditFragment.newInstance(currentQuietTime)
        (activity as BaseActivity).navigateToFragment(addEditFragment)
    }

    override fun onDeleteButtonClicked() {
        // building the dialog here without using the AppDialog
        // because I want it to disappear if the orientation is changed
        val builder = AlertDialog.Builder(requireContext())

        currentDialog =
            builder.setMessage(getString(R.string.deletion_dialog, currentQuietTime!!.title))
                .setPositiveButton(R.string.yes) { _, _ ->
                    currentDialog = null
                    onPositiveDialogResult(DIALOG_ID_DELETE)
                }
                .setNegativeButton(R.string.no) { _, _ -> currentDialog = null }
                .setOnDismissListener { currentDialog = null }
                .create()

        currentDialog!!.show()
    }

    override fun onSaveButtonClicked() {
        Timber.d("save button clicked on item")

        // TODO fire a dialog if the new settings collide with any other

        saveQuietTimeDetails()
    }

    //endregion

    private fun saveQuietTimeDetails() {
        currentQuietTime!!.days = tempDays
        viewModel.updateQuietTime(currentQuietTime!!)

        val transition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.layout_to_gone)

        currentQuietTimeViewHolder!!.isExpanded = false
        currentQuietTime = null

        TransitionManager.beginDelayedTransition(recyclerView, transition)
        adapter.notifyDataSetChanged()
    }

    //region Dialog events

    private fun onPositiveDialogResult(dialogId: Int) {
        when (dialogId) {
            DIALOG_ID_DELETE -> {
                Timber.d("deleting ${currentQuietTime!!.title}")

                viewModel.deleteQuietTime(currentQuietTime!!)
                currentQuietTime = null
                currentQuietTimeViewHolder = null
            }
        }
    }

    //endregion
}