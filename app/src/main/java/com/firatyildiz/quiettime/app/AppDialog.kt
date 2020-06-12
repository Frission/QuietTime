package com.firatyildiz.quiettime.app

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.firatyildiz.quiettime.R
import timber.log.Timber

/**
 * @author Fırat Yıldız
 *
 * Created on 12/06/2020
 */
class AppDialog : DialogFragment() {
    companion object {
        const val DIALOG_ID = "id"
        const val DIALOG_MESSAGE = "message"
        const val DIALOG_POSITIVE_RID = "positive_rid"
        const val DIALOG_NEGATIVE_RID = "negative_rid"
    }

    interface DialogEvents {
        fun onPositiveDialogResult(dialogId: Int, args: Bundle?)
        fun onNegativeDialogResult(dialogId: Int, args: Bundle?)
        fun onDialogCancelled(dialogId: Int)
    }

    private var dialogEvents: DialogEvents? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Timber.d("${AppDialog::class.simpleName} has been attached to ${context.applicationInfo.name} Context")

        if (context !is DialogEvents)
            throw ClassCastException("$context must implement AppDialog.DialogEvents interface")

        dialogEvents = context
    }

    override fun onDetach() {
        Timber.d("${AppDialog::class.simpleName} has been detached from Activity")
        super.onDetach()

        dialogEvents = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Timber.d("Creating a new dialog...")
        val builder = AlertDialog.Builder(requireActivity())

        val arguments: Bundle? = arguments
        val dialogId: Int
        val message: String?
        var positiveStringId: Int
        var negativeStringId: Int

        if (arguments != null) {
            with(arguments) {
                dialogId = getInt(DIALOG_ID)
                message = getString(DIALOG_MESSAGE)

                if (dialogId == 0 || message == null)
                    throw java.lang.IllegalArgumentException("DIALOG_ID and/or DIALOG_MESSAGE not present in the bundle")

                positiveStringId = getInt(DIALOG_POSITIVE_RID, 0)
                if (positiveStringId == 0)
                    positiveStringId = R.string.yes

                negativeStringId = getInt(DIALOG_NEGATIVE_RID, 0)
                if (negativeStringId == 0)
                    negativeStringId = R.string.no
            }
        } else {
            throw IllegalArgumentException("Must pass DIALOG_ID and DIALOG_MESSAGE in the bundle")
        }


        with(builder) {
            setMessage(message)

            setPositiveButton(positiveStringId, DialogInterface.OnClickListener { _, which ->
                dialogEvents?.onPositiveDialogResult(dialogId, arguments)
            })

            setNegativeButton(negativeStringId, DialogInterface.OnClickListener { _, which ->
                dialogEvents?.onNegativeDialogResult(dialogId, arguments)
            })
        }

        return builder.create()
    }

    override fun onCancel(dialog: DialogInterface) {
        Timber.d("Dialog has been cancelled")
        if (dialogEvents != null) {
            val dialogId = arguments?.getInt(DIALOG_ID)!!
            dialogEvents?.onDialogCancelled(dialogId)
        }

        super.onCancel(dialog)
    }

//    override fun onDismiss(dialog: DialogInterface) {
//        Log.d(TAG, "onDismiss: Dialog has been dismissed")
//        super.onDismiss(dialog)
//    }
}