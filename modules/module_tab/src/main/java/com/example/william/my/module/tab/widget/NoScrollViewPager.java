package com.example.william.my.module.tab.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

/**
 * 禁止手动滑动的 ViewPager。
 * <p>
 * 通过拦截触摸事件，阻止用户左右滑动切换页面，
 * 但仍可通过 {@code setCurrentItem()} 编程切换。
 */
public class NoScrollViewPager extends ViewPager {

    // 是否允许手动翻页，设为 true 可恢复滑动功能
    private final boolean isPagingScroll = false;

    public NoScrollViewPager(Context context) {
        super(context);
    }

    public NoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // 不处理触摸事件，放弃滑动响应
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return this.isPagingScroll && super.onTouchEvent(ev);
    }

    // 不拦截子 View 的触摸事件，保证子 View 正常响应点击
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return this.isPagingScroll && super.onInterceptTouchEvent(ev);
    }
}
