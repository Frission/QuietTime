package com.firatyildiz.quiettime.app

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.firatyildiz.quiettime.R
import timber.log.Timber

/**
 * @author Fırat Yıldız
 */
class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        Timber.d("creating main activity")

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        if (savedInstanceState == null)
            createMainFragment()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            Timber.d("home button selected")
            if (supportFragmentManager.backStackEntryCount >= 1) {
                if (supportFragmentManager.backStackEntryCount == 1)
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)

                supportFragmentManager.popBackStack()
                return true
            } else {
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun navigateToFragment(fragment: Fragment, addToBackStack: Boolean) {
        Timber.d("navigating to new fragment")
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fragment_open_enter,
                R.anim.fragment_close_exit,
                R.anim.fragment_open_enter,
                R.anim.fragment_close_exit
            )
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun createMainFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MainFragment.newInstance())
            .commitNow()
    }
}