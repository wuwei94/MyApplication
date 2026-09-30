package com.example.william.my.core.widget.barrage.entity;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.view.View;

import com.example.william.my.core.widget.barrage.Direction;
import com.example.william.my.core.widget.barrage.Util;

/**
 * 单条弹幕实体：持有 View 绘制出的 Bitmap 与运动轨迹矩形。
 *
 * <p>Bitmap 由 {@link Util#convertViewToBitmap(View)} 在后台线程生成；
 * 弹幕移出屏幕后由 Controller 从队列移除，调用方可在 {@code addedAll} 后统一回收。
 */
public class BaseDmEntity {
    public final Bitmap bitmap;
    public final RectF rect = new RectF();
    public final int priority;

    public BaseDmEntity(View itemView) {
        this(itemView, 0);
    }

    public BaseDmEntity(View itemView, int priority) {
        bitmap = Util.convertViewToBitmap(itemView);
        this.priority = priority;
        this.rect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    /**
     * 是否需要绘制（尚未完全进入展示区时跳过）。
     *
     * @param direction  弹幕运动方向
     * @param displayDis 展示区域长度（沿运动方向）
     */
    public boolean isNeedDraw(Direction direction, int displayDis) {
        switch (direction) {
            case RIGHT_LEFT:
            case LEFT_RIGHT:
                return rect.left < displayDis;
            case DOWN_UP:
            case UP_DOWN:
                return rect.top < displayDis;
        }
        throw new RuntimeException("not direction " + direction.name() + " in 'isNeedDraw()'");
    }
}
