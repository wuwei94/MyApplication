package com.example.william.my.module.widget_thirdparty.callback

import android.content.Context
import android.view.View
import android.widget.TextView
import com.example.william.my.basic.basic_shared.R
import com.kingja.loadsir.callback.Callback

class DefaultCallback : Callback() {

    override fun onCreateView(): Int {
        return R.layout.shared_layout_response
    }

    override fun onReloadEvent(context: Context, view: View): Boolean {
        val response: TextView = view.findViewById(R.id.basics_response)
        response.text = "DefaultCallback"
        return true
    }
}
