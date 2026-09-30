package com.example.william.my.core.widget.barrage.callback;

import android.view.View;

/**
 * 弹幕 View 工厂：在后台线程构建弹幕 View，再转 Bitmap 上屏。
 */
public interface ViewCreator {
    View build();
}
