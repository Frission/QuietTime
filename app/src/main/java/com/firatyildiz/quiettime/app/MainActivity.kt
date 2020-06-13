package com.firatyildiz.quiettime.app

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.firatyildiz.quiettime.R
import com.firatyildiz.quiettime.model.viewmodel.QuietTimeViewModel
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

        // make the viewmodel last through both fragment lifecycles
        ViewModelProvider(this).get(QuietTimeViewModel::class.java)

        if (savedInstanceState == null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            createMainFragment()
        } else {
            Timber.d("activity is being recreated")
            supportActionBar?.setDisplayHomeAsUpEnabled(supportFragmentManager.backStackEntryCount != 0)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            Timber.d("home button selected")
            navigateBack()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (supportFragmentManager.backStackEntryCount == 0)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun navigateToFragment(fragment: Fragment) {
        Timber.d("navigating to new fragment")
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fragment_fade_in,
                R.anim.fragment_fade_out,
                R.anim.fragment_fade_in,
                R.anim.fragment_fade_out
            )
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun navigateBack() {
        if (supportFragmentManager.backStackEntryCount != 0) {
            supportFragmentManager.popBackStack()
            supportActionBar?.setDisplayHomeAsUpEnabled(false)

            if (supportFragmentManager.backStackEntryCount == 1)
                title = getString(R.string.app_name)

            Timber.d("${supportFragmentManager.backStackEntryCount}")
        } else {
            Timber.w("Back stack was empty, this should not happen.")
            supportActionBar?.setHomeButtonEnabled(false)
        }

    }

    private fun createMainFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MainFragment.newInstance())
            .setCustomAnimations(
                R.anim.fragment_fade_in,
                R.anim.fragment_fade_out,
                R.anim.fragment_fade_in,
                R.anim.fragment_fade_out
            )
            .commitNow()
    }
}