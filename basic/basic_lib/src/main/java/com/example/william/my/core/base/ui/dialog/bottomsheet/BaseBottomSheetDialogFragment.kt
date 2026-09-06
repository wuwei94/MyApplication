package com.example.william.my.core.base.ui.dialog.bottomsheet

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import com.alibaba.android.arouter.launcher.ARouter
import com.example.william.my.core.base.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * 通用底部弹窗（BottomSheet）DialogFragment 基类
 *
 * 集成 ARouter 依赖注入与 BottomSheet 属性配置。生命周期顺序：
 * onAttach -> onCreateDialog -> onCreateView -> onViewCreated -> onStart
 */
abstract class BaseBottomSheetDialogFragment(
    val layout: Int = 0,
    private val windowAnimationsRes: Int = 0,
) : BottomSheetDialogFragment() {

    protected var behavior: BottomSheetBehavior<FrameLayout>? = null

    /**
     * 在Fragment中，IProvider会在Fragment的生命周期方法onCreateView中被初始化。
     * 这是因为Fragment的onCreateView方法是在Fragment被创建并添加到视图层次结构中时被调用的。
     * 因此，当ARouter导航到一个Fragment时，IProvider会被初始化并提供页面所需的数据。
     * 然而，在DialogFragment中，IProvider并不会在DialogFragment的生命周期方法onCreateView中被初始化。
     * 这是因为DialogFragment的onCreateView方法是在DialogFragment被创建并显示出来时被调用的。
     * 因此，当ARouter导航到一个DialogFragment时，IProvider并不会被初始化，因为DialogFragment还没有被显示出来。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.base_CustomBottomSheetDialogTheme)

        ARouter.getInstance().inject(this)
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
            behavior = (dialog as BottomSheetDialog).behavior
            dialog.dismissWithAnimation = true

            dialog.window?.let { window ->
                setWindowAttributes(window.attributes)
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

    protected fun setWindowAttributes(params: WindowManager.LayoutParams) {
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.gravity = Gravity.BOTTOM
        params.dimAmount = 0f
    }
}
