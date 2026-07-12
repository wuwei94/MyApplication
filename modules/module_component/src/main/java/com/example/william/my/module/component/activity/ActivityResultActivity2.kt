package com.example.william.my.module.component.activity

import android.content.Intent
import android.os.Bundle
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity

/**
 * ActivityResultContracts 目标页
 *
 * 接收来自上游 Activity 的数据，点击按钮回传结果。
 */
class ActivityResultActivity2 : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showResponse("收到数据：${intent?.getStringExtra("input")}")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf("返回数据给上游")
    }

    override fun onRecyclerClick(position: Int, string: String) {
        val result = Intent().apply {
            putExtra("result", intent?.getStringExtra("input").orEmpty())
        }
        setResult(RESULT_OK, result)
        finish()
    }
}
