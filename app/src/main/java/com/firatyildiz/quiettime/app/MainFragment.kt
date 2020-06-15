package com.firatyildiz.quiettime.app

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.view.*
import android.widget.TextView
import androidx.core.os.ConfigurationCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.firatyildiz.quiettime.R
import com.firatyildiz.quiettime.adapters.QuietTimeRecyclerAdapter
import com.firatyildiz.quiettime.fragments.AddEditFragment
import com.firatyildiz.quiettime.helpers.DateTimeLocalizationHelper
import com.firatyildiz.quiettime.model.entities.QuietTime
import com.firatyildiz.quiettime.model.viewmodel.QuietTimeViewModel
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.util.*

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
    private lateinit var currentLocale: Locale

    /**
     * The current quiet time that is being edited by the user, if this is not null, it means that the quiet view item is expanded
     */
    private var currentQuietTime: QuietTime? = null
    private var currentQuietTimeViewHolder: QuietTimeRecyclerAdapter.QuietTimeViewHolder? = null
    private var tempDays: Int = 0

    private var currentDialog: AlertDialog? = null
    private var aboutdialog: AlertDialog? = null
    private var hasDoNotDisturbPermission = true
    private lateinit var mainView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(requireActivity()).get(QuietTimeViewModel::class.java)
        currentLocale = ConfigurationCompat.getLocales(resources.configuration)[0]

        val weekDayStringIds = DateTimeLocalizationHelper.getWeekStringIds(currentLocale)
        dayNames = listOf<String>(
            getString(weekDayStringIds[0]),
            getString(weekDayStringIds[1]),
            getString(weekDayStringIds[2]),
            getString(weekDayStringIds[3]),
            getString(weekDayStringIds[4]),
            getString(weekDayStringIds[5]),
            getString(weekDayStringIds[6])
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

        mainView = view

        hasDoNotDisturbPermission = true
        // check if we have permission
        checkDoNotDisturbPermission(view)

        recyclerView = view.findViewById<RecyclerView>(R.id.quiet_time_list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = QuietTimeRecyclerAdapter(requireContext(), dayNames, currentLocale, this)

        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        noQuietTimesText = view.findViewById(R.id.no_quiet_times_text)

        viewModel.allQuietTimes.observe(viewLifecycleOwner, Observer {
            Timber.d("live data callback")
            adapter.updateQuietTimes(it)
            viewModel.updateAlarms(it, currentLocale, lifecycleScope)

            if (it.isEmpty())
                noQuietTimesText.visibility = View.VISIBLE
            else
                noQuietTimesText.visibility = View.GONE
        })

        if (!hasDoNotDisturbPermission) {
            recyclerView.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()

        checkDoNotDisturbPermission(mainView)

        if (hasDoNotDisturbPermission) {
            recyclerView.visibility = View.VISIBLE
            requireActivity().invalidateOptionsMenu()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        Timber.d("Creating main fragment options menu")
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        menu.getItem(0).isEnabled = hasDoNotDisturbPermission
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.main_menu_add_quiet_time) {
            val addEditFragment = AddEditFragment.newInstance(null)
            (activity as BaseActivity).navigateToFragment(addEditFragment)
            return true
        } else if (item.itemId == R.id.main_menu_about) {
            showAboutDialog()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        currentQuietTime = null
        currentQuietTimeViewHolder = null

        currentDialog?.dismiss()
        currentDialog = null
        aboutdialog?.dismiss()
        aboutdialog = null
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
            holder.isExpanded = true

            // animate the button
            val transitionDrawable = holder.editOrCloseButton.drawable as TransitionDrawable
            transitionDrawable.isCrossFadeEnabled = true
            transitionDrawable.startTransition(200)

            currentQuietTime = quietTime
            currentQuietTimeViewHolder = holder
        } else if (quietTime == currentQuietTime) {
            holder.isExpanded = false

            // animate the button
            val transitionDrawable = holder.editOrCloseButton.drawable as TransitionDrawable
            transitionDrawable.isCrossFadeEnabled = true
            transitionDrawable.reverseTransition(200)

            currentQuietTime = null
            currentQuietTimeViewHolder = null
        } else {
            currentQuietTimeViewHolder!!.isExpanded = false
            holder.isExpanded = true

            // animate the button
            var transitionDrawable =
                currentQuietTimeViewHolder!!.editOrCloseButton.drawable as TransitionDrawable
            transitionDrawable.isCrossFadeEnabled = true
            transitionDrawable.reverseTransition(200)

            // animate the button
            transitionDrawable = holder.editOrCloseButton.drawable as TransitionDrawable
            transitionDrawable.isCrossFadeEnabled = true
            transitionDrawable.startTransition(200)

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

        currentQuietTimeViewHolder!!.saveButton.isEnabled = tempDays != 0
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
                .setPositiveButton(R.string.yes) { _, _ -> onPositiveDialogResult(DIALOG_ID_DELETE) }
                .setNegativeButton(R.string.no) { _, _ -> currentDialog = null }
                .setOnDismissListener { currentDialog = null; Timber.d("dialog dismissed") }
                .create()

        currentDialog!!.show()
    }

    override fun onSaveButtonClicked() {
        Timber.d("save button clicked on item")

        // TODO fire a dialog if the new settings collide with any other

        val collidingTimes = viewModel.getCollidingQuietTimes(
            currentQuietTime!!.id,
            tempDays,
            currentQuietTime!!.startTime,
            currentQuietTime!!.endTime
        )

        // this quiet time collides with some other quiet times, the app will not behave as expected
        // not sure what they're expecting when the times collide anyways
        if (collidingTimes.isNotEmpty()) {
            // this should absolutely be null here but, i can't help myself
            if (currentDialog != null)
                currentDialog?.dismiss()

            val builder = AlertDialog.Builder(requireContext())
            val message = StringBuilder()
            message.append(getString(R.string.collision_dialog_start) + "\n\n")

            for (collidingTime in collidingTimes)
                message.append(collidingTime.title + "\n")

            message.append("\n" + getString(R.string.collision_dialog_end))

            currentDialog = builder.setMessage(message.toString())
                .setPositiveButton(R.string.yes) { _, _ ->
                    onPositiveDialogResult(
                        DIALOG_ID_COLLISION
                    )
                }
                .setNegativeButton(R.string.no) { _, _ -> currentDialog = null }
                .setOnDismissListener { currentDialog = null; Timber.d("dialog dismissed") }
                .create()

            currentDialog!!.show()
        } else {
            saveQuietTimeDetails()
        }
    }

    //endregion

    private fun saveQuietTimeDetails() {
        currentQuietTime!!.days = tempDays
        viewModel.updateQuietTime(currentQuietTime!!, currentLocale)

        val transition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.layout_to_gone)

        currentQuietTimeViewHolder!!.isExpanded = false

        // animate the button back
        val transitionDrawable =
            currentQuietTimeViewHolder!!.editOrCloseButton.drawable as TransitionDrawable
        transitionDrawable.isCrossFadeEnabled = true
        transitionDrawable.reverseTransition(200)

        currentQuietTime = null
        currentQuietTimeViewHolder = null

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
            DIALOG_ID_COLLISION -> {
                // we warned the user , but they still want the times to collide, okay..
                Timber.d("updating ${currentQuietTime!!.title}")

                saveQuietTimeDetails()
            }
        }
    }

    private fun checkDoNotDisturbPermission(view: View) {
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {
            Snackbar.make(
                view,
                getString(R.string.notification_permission),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.grant_permission) {
                    startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                }
                .show()

            hasDoNotDisturbPermission = false
            requireActivity().invalidateOptionsMenu()
        } else {
            hasDoNotDisturbPermission = true
        }
    }

    private fun showAboutDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val title = getString(R.string.about_title)
        val message = HtmlCompat.fromHtml(getString(R.string.about_description), 0)

        aboutdialog = builder.setMessage(message)
            .setPositiveButton(R.string.ok) { _, _ -> aboutdialog = null }
            .setOnDismissListener { aboutdialog = null }
            .setTitle(title)
            .setIcon(R.drawable.ic_about_icon)
            .create()

        aboutdialog!!.show()
    }

    //endregion
}