package com.example.william.my.module.widget_thirdparty.activity.loadsir

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseFragmentActivity
import com.example.william.my.module.widget_thirdparty.fragment.LoadSirFragment

/**
 * LoadSir — Fragment 多状态页面示例
 *
 * 通过 [BaseFragmentActivity] 承载 [LoadSirFragment]，演示 LoadSir
 * 在 Fragment 场景下的无侵入状态管理。
 *
 * 核心机制与避坑点：
 * 1. Fragment 场景：与 Activity 场景共用同一套状态回调
 * 2. 无侵入包裹：不修改原有 Fragment 业务布局
 * 3. 状态切换：支持加载中 / 错误 / 成功等状态切换
 *
 * https://github.com/kingja/LoadSir
 */
@Route(path = RouterPath.WidgetThirdparty.LoadSirFragment)
class LoadSirFragmentActivity : BaseFragmentActivity() {

    override fun setFragment(): Fragment = LoadSirFragment()
}
