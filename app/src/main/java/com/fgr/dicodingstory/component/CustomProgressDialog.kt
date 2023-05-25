package com.fgr.dicodingstory.component

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.fgr.dicodingstory.R

@SuppressLint("InflateParams")
class CustomProgressDialog(context: Context) {

    private var dialog: CustomDialog
    private var cpTitle: TextView
    private var cpCardView: CardView
    private var progressBar: ProgressBar

    fun start(title: String = "") {
        cpTitle.text = title
        dialog.show()
    }

    fun stop() {
        dialog.dismiss()
    }

    init {
        val inflater = (context as Activity).layoutInflater
        val view = inflater.inflate(R.layout.progress_dialog_view, null)

        cpTitle = view.findViewById(R.id.cp_title)
        cpCardView = view.findViewById(R.id.cp_cardview)
        progressBar = view.findViewById(R.id.cp_pbar)

        // Card Color
        cpCardView.setCardBackgroundColor(Color.parseColor("#70000000"))

        // Text Color
        cpTitle.setTextColor(Color.WHITE)

        // Custom Dialog initialization
        dialog = CustomDialog(context)
        dialog.setContentView(view)
    }

    class CustomDialog(context: Context) : Dialog(context, R.style.CustomDialogTheme) {
        init {
            window?.decorView?.setOnApplyWindowInsetsListener { _, insets ->
                insets.consumeSystemWindowInsets()
            }
        }
    }
}