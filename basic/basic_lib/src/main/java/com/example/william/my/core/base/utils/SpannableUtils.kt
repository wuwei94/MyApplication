package com.example.william.my.core.base.utils

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View

object SpannableUtils {

    fun SpannableStringBuilder.setSpan(
        highlight: String,
        color: Int,
        isUnderlineText: Boolean = false,
        onClick: () -> Unit = {}
    ): SpannableStringBuilder {
        val startIndex = this.indexOf(highlight)
        if (startIndex >= 0) {
            setSpan(
                object : ClickableSpan() {
                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = color
                        ds.isUnderlineText = isUnderlineText
                    }

                    override fun onClick(widget: View) {
                        onClick()
                    }
                },
                startIndex,
                startIndex + highlight.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return this
    }
}