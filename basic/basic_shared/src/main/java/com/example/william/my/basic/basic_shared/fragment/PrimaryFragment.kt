package com.example.william.my.basic.basic_shared.fragment

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.lib.fragment.BaseFragment

@Route(path = RouterPath.Fragment.FragmentPrimary)
class PrimaryFragment : BaseFragment(R.layout.shared_fragment_primary)