package com.example.william.my.module.widget_thirdparty.activity.loadsir

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseFragmentActivity
import com.example.william.my.module.widget_thirdparty.fragment.LoadSirFragment

/**
 * LoadSir — Fragment 多状态页面示例
 *
 * 通过 [BaseFragmentActivity] 承载 [LoadSirFragment]，演示 LoadSir
 * 在 Fragment 场景下的无侵入状态管理。
 */
@Route(path = RouterPath.WidgetThirdparty.LoadSirFragment)
class LoadSirFragmentActivity : BaseFragmentActivity() {

    override fun setFragment(): Fragment {
        return LoadSirFragment()
    }
}
