package com.example.william.my.core.base.ui.activity

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import com.example.william.my.core.base.arch.mvp.IBasePresenter
import com.example.william.my.core.base.arch.mvp.IBaseView
import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle
import com.trello.rxlifecycle4.LifecycleProvider
import java.lang.reflect.InvocationTargetException

/**
 * MVP 架构 Activity 基类
 *
 * 通过反射实例化 Presenter 并在 [onCreate] 时初始化，在 [onDestroy] 时释放。
 */
abstract class BaseMvpActivity<T : IBasePresenter?, V : IBaseView<T>?> : BaseActivity() {

    protected var mPresenter: T? = null

    private lateinit var provider: LifecycleProvider<Lifecycle.Event>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        provider = AndroidLifecycle.createLifecycleProvider(this)
        initPresenter()
    }

    /**
     * 返回逻辑处理的具体类型.
     */
    protected abstract val presenterClass: Class<T>

    /**
     * 返回View层的接口类.
     */
    protected abstract val viewClass: Class<V>?

    /**
     * 初始化Presenter
     */
    private fun initPresenter() {
        try {
            val constructor = presenterClass.getConstructor(viewClass)
            mPresenter = constructor.newInstance(this)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        clearPresenter()
        super.onDestroy()
    }

    private fun clearPresenter() {
        mPresenter?.clear()
        mPresenter = null
    }
}
