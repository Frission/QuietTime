package com.firatyildiz.quiettime.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.firatyildiz.quiettime.BuildConfig
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