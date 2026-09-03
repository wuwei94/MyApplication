package com.example.william.my.core.base.fragment

import com.example.william.my.core.base.R
import com.example.william.my.core.base.databinding.BaseFragmentWebViewBinding

class BaseWebViewFragment : BaseVBFragment<BaseFragmentWebViewBinding>(R.layout.base_fragment_web_view) {
    override fun getViewBinding(): BaseFragmentWebViewBinding = BaseFragmentWebViewBinding.inflate(layoutInflater)
}
