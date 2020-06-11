package com.firatyildiz.quiettime.app

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
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
class MainFragment : Fragment(), QuietTimeRecyclerAdapter.QuietTimeItemViewClickListener {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: QuietTimeViewModel
    private lateinit var dayNames: List<String>
    private lateinit var adapter: QuietTimeRecyclerAdapter
    private lateinit var noQuietTimesText: TextView

    /**
     * The curent quiet time that is being edited by the user, if this is not null, it means that the quiet view item is expanded
     */
    private var currentQuietTime: QuietTime? = null
    private var currentQTViewHolder: QuietTimeRecyclerAdapter.QuietTimeViewHolder? = null
    private var currentQTposition: Int = -1

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (activity !is BaseActivity)
            throw ClassCastException("All activity classes should inherit from the BaseActivity class")
    }

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

        val recyclerView = view.findViewById<RecyclerView>(R.id.quiet_time_list)
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
            (activity as BaseActivity).navigateToFragment(addEditFragment, true)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    //region Quiet Time Recycler View Item Listener functions
    override fun onEditButtonClicked(
        position: Int,
        quietTime: QuietTime,
        holder: QuietTimeRecyclerAdapter.QuietTimeViewHolder
    ) {
        Timber.d("edit/close button clicked on quiet time: ${quietTime.title}")

        if (currentQuietTime == null) {
            Timber.d("expanding the recycler view")
            currentQuietTime = quietTime
            currentQTViewHolder = holder
            currentQTposition = position

            holder.expanded = true
            adapter.notifyItemChanged(position)

        } else {

            currentQTViewHolder!!.expanded = false
            adapter.notifyItemChanged(currentQTposition)

            currentQuietTime = null
            currentQTViewHolder = null
            currentQTposition = -1
        }
    }

    override fun onDaySelected(indexOfDay: Int) {
        TODO("Not yet implemented")
    }

    override fun onEditTimesButtonClicked() {
        TODO("Not yet implemented")
    }

    override fun onDeleteButtonClicked() {
        TODO("Not yet implemented")
    }

    override fun onSaveButtonClicked() {
        TODO("Not yet implemented")
    }

    //endregion
}