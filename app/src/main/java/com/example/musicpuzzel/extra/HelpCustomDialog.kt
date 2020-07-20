package com.example.musicpuzzel.extra

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.musicpuzzel.R
import com.example.musicpuzzel.extra.OnHelpDialogInterface
import kotlinx.android.synthetic.main.popup_help_dialog.*
import kotlinx.android.synthetic.main.popup_help_dialog.view.*

class HelpCustomDialog(context: Context) : Dialog(context) {
    private val delegate = context as OnHelpDialogInterface
    private val dialog = Dialog(context)
     fun showDialog() {

        dialog.setContentView(R.layout.popup_help_dialog)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        dialog.btnShowType.setOnClickListener {
            delegate.showType()
            dialog.dismiss()
        }
        dialog.btnRevealLetter.setOnClickListener {
            delegate.revealLetter()
            dialog.dismiss()
        }
        dialog.btnShowDescription.setOnClickListener {
            delegate.showDescription()
            dialog.dismiss()
        }

        dialog.btnHelpClose.setOnClickListener {
            dialog.dismiss()
        }

    }
}