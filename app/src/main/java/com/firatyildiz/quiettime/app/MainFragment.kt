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
import com.firatyildiz.quiettime.R
import com.firatyildiz.quiettime.adapters.QuietTimeRecyclerAdapter
import com.firatyildiz.quiettime.fragments.AddEditFragment
import com.firatyildiz.quiettime.model.viewmodel.QuietTimeViewModel
import timber.log.Timber

/**
 * @author Fırat Yıldız
 */
class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: QuietTimeViewModel
    private lateinit var dayNames: List<String>
    private lateinit var adapter: QuietTimeRecyclerAdapter
    private lateinit var noQuietTimesText: TextView

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

        adapter = QuietTimeRecyclerAdapter(requireContext(), dayNames)
        recyclerView.adapter = adapter

        noQuietTimesText = view.findViewById(R.id.no_quiet_times_text)

        viewModel.allQuietTimes.observe(viewLifecycleOwner, Observer {
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
}