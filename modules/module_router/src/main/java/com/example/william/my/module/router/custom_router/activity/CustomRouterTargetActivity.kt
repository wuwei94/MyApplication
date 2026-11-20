package com.example.william.my.module.router.custom_router.activity

import android.os.Bundle
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity

/**
 * 自研无插件路由目标接收页，直接通过标准 Intent 读取参数。
 */
class CustomRouterTargetActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("自研路由目标接收页\n直接读取 Intent Extras，无需 @Autowired 字节码注入。")

        val from = intent.getStringExtra("from")
        val code = intent.getIntExtra("code", 0)
        val traceId = intent.getStringExtra("traceId")
        appendLog("✓ 收到 Intent 参数: from=$from, code=$code, traceId=$traceId")
    }

    override fun buildList(): ArrayList<String> = arrayListOf("关闭并返回")

    override fun onRecyclerClick(position: Int, string: String) {
        if (position == 0) {
            finish()
        }
    }
}
