package com.example.william.my.core.base.ui.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

/**
 * 通用 DialogFragment 基类
 *
 * 集成 Dialog 属性配置。生命周期顺序：
 * onAttach -> onCreateDialog -> onCreateView -> onViewCreated -> onStart
 */
abstract class BaseDialogFragment(
    val layout: Int = 0,
    private val windowAnimationsRes: Int = 0,
) : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initDialog()

        initView(view, savedInstanceState)

        initViewModel()
        observeViewModel()
    }

    private fun initDialog() {
        dialog?.let { dialog ->
            dialog.window?.let { window ->
                setAttributes(window.attributes)
                window.attributes = window.attributes
                if (windowAnimationsRes > 0) {
                    window.setWindowAnimations(windowAnimationsRes)
                }
                // Android 5.0以上自定义Dialog时发现无法横向铺满屏幕
                window.decorView.setPadding(0, 0, 0, 0)
                window.setBackgroundDrawableResource(android.R.color.transparent)
            }
        }
    }

    /**
     * 在此方法内初始化控件
     */
    open fun initView(view: View?, state: Bundle?) {
    }

    /**
     * 在此方法内初始化ViewModel
     */
    open fun initViewModel() {
    }

    /**
     * 在此方法内监听ViewModel
     */
    open fun observeViewModel() {
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            val transaction = manager.beginTransaction()
            // 在每个add事务前增加一个remove事务，防止连续的add
            transaction.remove(this)
            // commit()方法换成了commitAllowingStateLoss()
            // 解决Can not perform this action after onSaveInstanceState with DialogFragment
            transaction.add(this, tag)
            transaction.commitAllowingStateLoss()
            // 解决java.lang.IllegalStateException: Fragment already added
            manager.executePendingTransactions()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected open fun setAttributes(params: WindowManager.LayoutParams) {
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.gravity = Gravity.CENTER
        params.dimAmount = 0.0f
    }
}
