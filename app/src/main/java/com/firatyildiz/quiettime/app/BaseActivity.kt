package com.firatyildiz.quiettime.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.firatyildiz.quiettime.BuildConfig
import com.firatyildiz.quiettime.R
import com.firatyildiz.quiettime.model.QuietTimeConstants
import timber.log.Timber

/**
 * @author Fırat Yıldız
 *
 * Created on 10/06/2020
 */
abstract class BaseActivity : AppCompatActivity(), OnFragmentNavigationListener,
    AppDialog.DialogEvents {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG && savedInstanceState == null)
            Timber.plant(Timber.DebugTree())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificaltionChannel()
    }

    /**
     * Creates a notification channel for Android 8.0 and above
     */
    fun createNotificaltionChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val description = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(QuietTimeConstants.NOTIFICATION_CHANNEL_ID, name, importance)
            channel.description = description

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle?) {
        Timber.d("positive dialog result")
        for (fragment in supportFragmentManager.fragments) {
            if (fragment.isVisible && fragment is AppDialog.DialogEvents) {
                fragment.onPositiveDialogResult(dialogId, args)
            }
        }
    }

    override fun onNegativeDialogResult(dialogId: Int, args: Bundle?) {
        Timber.d("negative dialog result")
        for (fragment in supportFragmentManager.fragments) {
            if (fragment.isVisible && fragment is AppDialog.DialogEvents) {
                fragment.onNegativeDialogResult(dialogId, args)
            }
        }
    }

    override fun onDialogCancelled(dialogId: Int) {
        Timber.d("dialog cancelled")
        for (fragment in supportFragmentManager.fragments) {
            if (fragment.isVisible && fragment is AppDialog.DialogEvents) {
                fragment.onDialogCancelled(dialogId)
            }
        }
    }
}

interface OnFragmentNavigationListener {
    fun navigateToFragment(fragment: Fragment)
    fun navigateBack()
}