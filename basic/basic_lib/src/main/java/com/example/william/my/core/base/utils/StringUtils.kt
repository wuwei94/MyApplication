package com.example.william.my.core.base.utils

import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View

object StringUtils {

    fun List<String>?.toString(separator: String): String = this?.joinToString(separator) ?: ""

    fun SpannableString.setTextColor(
        text: String,
        highlight: String,
        color: Int,
        onClick: () -> Unit,
    ): SpannableString {
        val startIndex = text.indexOf(highlight)
        if (startIndex >= 0) {
            setSpan(
                object : ClickableSpan() {
                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = color
                        ds.isUnderlineText = false
                    }

                    override fun onClick(widget: View) {
                        onClick()
                    }
                },
                startIndex,
                startIndex + highlight.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
        return this
    }
}
