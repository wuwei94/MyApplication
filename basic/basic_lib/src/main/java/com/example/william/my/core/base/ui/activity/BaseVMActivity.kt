package com.example.william.my.core.base.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.example.william.my.core.base.arch.mvvm.BaseViewModel
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType

/**
 * ViewBinding + ViewModel Activity 基类
 *
 * 结合 ViewBinding 与 [BaseViewModel]，通过反射自动注入 ViewModel 并监听其 [BaseViewModel.error] 错误状态。
 */
abstract class BaseVMActivity<VB : ViewBinding?, VM : BaseViewModel> : BaseVBActivity<VB>() {

    private var _viewModel: VM? = null
    protected val mViewModel get() = _viewModel!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initViewModel() {
        _viewModel = viewModel()
        lifecycle.addObserver(mViewModel)
    }

    private fun viewModel(): VM? {
        try {
            val aClass =
                (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<VM>
            return ViewModelProvider(this)[aClass]
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return null
    }

    override fun observeViewModel() {
        super.observeViewModel()

        _viewModel!!.error.observe(this) { throwable ->
            onError(throwable)
        }
    }

    fun onError(throwable: Throwable) {
        Log.e(TAG, throwable.message.toString())
    }

    override fun onDestroy() {
        _viewModel = null
        super.onDestroy()
    }
}
