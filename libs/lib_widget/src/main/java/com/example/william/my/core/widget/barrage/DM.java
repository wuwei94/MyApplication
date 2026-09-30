package com.example.william.my.core.widget.barrage;

import com.example.william.my.core.widget.barrage.control.Controller;

/**
 * 弹幕容器能力接口：暴露内部 [Controller] 供外部添加弹幕、暂停/恢复。
 */
public interface DM {
    Controller getController();
}
