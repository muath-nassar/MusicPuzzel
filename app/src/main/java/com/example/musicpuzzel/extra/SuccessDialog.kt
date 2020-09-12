package com.example.musicpuzzel.extra

import android.app.Dialog
import android.content.Context
import android.view.View
import com.example.musicpuzzel.R
import kotlinx.android.synthetic.main.success_dialog.*

class SuccessDialog(context: Context): Dialog(context) {
    private val delegate = context as OnSuccessDialogInterface
   private val dialog = Dialog(context)
fun showDialog(){
    dialog.setContentView(R.layout.success_dialog)
    dialog.setCancelable(false)
    dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
    dialog.show()
    dialog.btnNextRound.setOnClickListener {
        dialog.dismiss()
        delegate.onSuccessDialog()
    }
}
    fun setBtnNextRoundVisibilityOn(){
        dialog.btnNextRound.visibility = View.VISIBLE
    }
    fun disableProgressBar(){
        dialog.progress_circular.visibility = View.GONE
    }
    fun cancelDialog(){
        dialog.cancel()
    }
}