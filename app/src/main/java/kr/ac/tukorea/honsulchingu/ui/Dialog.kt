package kr.ac.tukorea.honsulchingu.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kr.ac.tukorea.honsulchingu.R

object DialogUtil {
    fun showHonsulDialog(
        context: Context,
        title: String,
        message: String,
        iconRes: Int? = null,
        positiveText: String = "확인",
        negativeText: String = "취소",
        onPositiveClick: (() -> Unit)? = null,
        onNegativeClick: (() -> Unit)? = null
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog, null)
        val dialog = MaterialAlertDialogBuilder(context).setView(view).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        val imgIcon = view.findViewById<ImageView>(R.id.imgIcon)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirm)

        tvTitle.text = title
        tvMessage.text = message
        btnCancel.text = negativeText
        btnConfirm.text = positiveText

        if (iconRes != null) {
            imgIcon.setImageResource(iconRes)
            imgIcon.visibility = View.VISIBLE
        } else {
            imgIcon.visibility = View.GONE
        }

        btnCancel.setOnClickListener {
            onNegativeClick?.invoke()
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            onPositiveClick?.invoke()
            dialog.dismiss()
        }

        dialog.show()
    }
}