package com.example.william.my.module.widget_custom.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.example.william.my.basic.basic_shared.adapter.ViewPagerFragmentAdapter
import com.example.william.my.basic.basic_shared.fragment.PrimaryDarkFragment
import com.example.william.my.basic.basic_shared.fragment.PrimaryFragment
import com.example.william.my.core.widget.bottomsheet.ViewPagerBottomSheetDialogFragment
import com.example.william.my.module.widget_custom.R
import com.example.william.my.module.widget_custom.databinding.WidgetDialogBottomSheetBinding

/**
 * 底部弹窗
 *
 * 基于 ViewPagerBottomSheetDialogFragment 的底部弹窗。
 */
class MyBottomSheetDialog : ViewPagerBottomSheetDialogFragment() {

    private lateinit var binding: WidgetDialogBottomSheetBinding

    private val fragments: ArrayList<Fragment> = arrayListOf(
        PrimaryFragment(),
        PrimaryDarkFragment(),
        PrimaryFragment(),
        PrimaryDarkFragment(),
    )

    override fun getLayout(): Int = R.layout.widget_dialog_bottom_sheet

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = WidgetDialogBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewPager()
    }

    private fun initViewPager() {
        binding.widgetViewPager.offscreenPageLimit = fragments.size
        binding.widgetViewPager.adapter =
            ViewPagerFragmentAdapter(childFragmentManager, fragments)
        binding.widgetViewPager.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                onPageChange(binding.widgetViewPager)
            }
        })
    }
}
