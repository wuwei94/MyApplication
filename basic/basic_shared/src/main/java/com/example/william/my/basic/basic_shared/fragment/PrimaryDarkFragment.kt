package com.example.william.my.basic.basic_shared.fragment

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.fragment.BaseFragment

/**
 * 深色主色占位 Fragment（用于 ViewPager 演示）
 */
@Route(path = RouterPath.Fragment.FragmentPrimaryDark)
class PrimaryDarkFragment : BaseFragment(R.layout.shared_fragment_primary_dark)
