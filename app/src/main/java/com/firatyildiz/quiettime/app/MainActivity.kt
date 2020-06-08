package com.firatyildiz.quiettime.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.firatyildiz.quiettime.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        if (savedInstanceState == null)
            createMainFragment()
    }

    fun createMainFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, MainFragment.newInstance())
            .commitNow()
    }
}