package com.example.william.my.core.widget.barrage.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.example.william.my.core.widget.R;
import com.example.william.my.core.widget.barrage.DM;
import com.example.william.my.core.widget.barrage.Direction;
import com.example.william.my.core.widget.barrage.Util;
import com.example.william.my.core.widget.barrage.callback.OnDMAddListener;
import com.example.william.my.core.widget.barrage.control.Controller;
import com.example.william.my.core.widget.barrage.control.SurfaceProxy;

/**
 * 基于 SurfaceView 的弹幕容器。
 *
 * <p>核心机制与避坑点：
 * 1. setZOrderOnTop + TRANSPARENT 实现透明叠层，会遮挡同 Window 下的普通 View
 * 2. surfaceDestroyed 不 destroy Controller，避免后台返回后无法续播
 * 3. onDetachedFromWindow 才真正 destroy，防止 DrawThread 泄漏
 * 4. addBarrageView 仅在 isShown() 时入列，后台丢弃避免队列堆积
 */
public class DMSurfaceView extends SurfaceView implements SurfaceHolder.Callback, DM {

    private SurfaceHolder surfaceHolder;
    private Controller controller;
    private int width;
    private int height;
    private final Controller.Builder builder;

    public DMSurfaceView(Context context) {
        this(context, null);
    }

    public DMSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DMSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initHolder();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DMSurfaceView, defStyleAttr, 0);
        final Direction direction =
                Direction.getType(a.getInt(R.styleable.DMSurfaceView_dm_direction, Direction.RIGHT_LEFT.value));
        final int span = a.getDimensionPixelOffset(R.styleable.DMSurfaceView_dm_span, Util.dp2px(context, 2));
        final int sleep = a.getInteger(R.styleable.DMSurfaceView_dm_sleep, 0);
        final int spanTime = a.getInteger(R.styleable.DMSurfaceView_dm_span_time, 0);
        final int vSpace = a.getDimensionPixelOffset(R.styleable.DMSurfaceView_dm_v_space, Util.dp2px(context, 10));
        final int hSpace = a.getDimensionPixelOffset(R.styleable.DMSurfaceView_dm_h_space, Util.dp2px(context, 10));
        a.recycle();

        builder = new Controller.Builder()
                .setDirection(direction)
                .setSpan(span)
                .setSleep(sleep)
                .setSpanTime(spanTime)
                .sethSpace(hSpace)
                .setvSpace(vSpace);
    }

    private void initHolder() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        setZOrderOnTop(true);
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (width == w && height == h) {
            return;
        }
        if (controller != null) {
            controller.destroy();
        }
        this.width = w;
        this.height = h;
        controller = builder
                .setSurfaceProxy(new SurfaceProxy(surfaceHolder))
                .setWidth(width)
                .setHeight(height)
                .build();
        controller.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 禁止清理，否则程序从后台返回到前台时无法继续播放
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (controller == null) {
            return;
        }
        if (hasWindowFocus) {
            controller.resume();
        } else {
            controller.pause();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (controller == null) {
            return;
        }
        controller.pause();
        controller.destroy();
        controller = null;
    }

    public void setOnDMAddListener(OnDMAddListener l) {
        builder.setOnDMAddListener(l);
    }

    @Override
    public Controller getController() {
        return controller;
    }

    public void addBarrageView(final View view) {
        if (view == null || controller == null) {
            return;
        }
        if (isShown()) {
            controller.add(view);
        }
    }
}
