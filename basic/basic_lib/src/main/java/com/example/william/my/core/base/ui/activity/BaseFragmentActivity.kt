package com.example.william.my.core.base.ui.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.william.my.core.base.R

/**
 * Fragment 宿主 Activity 基类
 *
 * 加载通用 Fragment 容器布局，由子类通过 [setFragment] 提供要展示的 Fragment。
 */
abstract class BaseFragmentActivity : BaseActivity() {

    override fun initViewBinding() {
        super.initViewBinding()
        setContentView(R.layout.base_activity_fragment)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initFragment()
    }

    private fun initFragment() {
        val manager = supportFragmentManager
        val fragmentTransaction = manager.beginTransaction()
        val fragment = setFragment()
        fragmentTransaction.replace(R.id.fragment, fragment)
        fragmentTransaction.commitAllowingStateLoss()
    }

    protected abstract fun setFragment(): Fragment
}
