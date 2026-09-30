package com.example.william.my.core.widget.barrage.callback;

import com.example.william.my.core.widget.barrage.entity.BaseDmEntity;

/**
 * 弹幕入列回调：单条上屏、队列耗尽时在主线程回调。
 */
public interface OnDMAddListener {
    void added(BaseDmEntity dmEntity);

    void addedAll();
}
