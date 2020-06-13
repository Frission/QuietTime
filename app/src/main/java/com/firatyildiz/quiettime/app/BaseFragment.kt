package com.firatyildiz.quiettime.app

import android.content.Context
import androidx.fragment.app.Fragment

/**
 * @author Fırat Yıldız
 *
 * Created on 12/06/2020
 */
abstract class BaseFragment : Fragment() {
    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (activity !is BaseActivity)
            throw ClassCastException("All activity classes should inherit from the BaseActivity class")
    }

}