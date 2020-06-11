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
abstract class BaseActivity : AppCompatActivity(), OnFragmentNavigationListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
    }
}

interface OnFragmentNavigationListener {
    fun navigateToFragment(fragment: Fragment, addToBackStack: Boolean)
    fun navigateBack()
}