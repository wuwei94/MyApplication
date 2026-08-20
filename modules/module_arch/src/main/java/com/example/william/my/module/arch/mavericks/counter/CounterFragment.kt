package com.example.william.my.module.arch.mavericks.counter

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.example.william.my.basic.basic_shared.databinding.SharedLayoutResponseBinding
import com.example.william.my.module.arch.R
import com.example.william.my.module.arch.mavericks.counter.viewmodel.CounterViewModel
import com.example.william.my.module.arch.mavericks.utils.viewBinding

/**
 * 计数器页面
 *
 * 演示 Mavericks 中基础 State 的绑定与更新。
 */
class CounterFragment : Fragment(R.layout.shared_layout_response), MavericksView {

    private val binding: SharedLayoutResponseBinding by viewBinding()
    private val viewModel: CounterViewModel by fragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.basicsResponse.setOnClickListener {
            viewModel.incrementCount()
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.basicsResponse.text = "Count: ${state.count}"
    }
}