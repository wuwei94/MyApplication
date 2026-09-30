package com.example.william.my.core.widget.barrage.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import com.example.william.my.core.widget.R;
import com.example.william.my.core.widget.barrage.DM;
import com.example.william.my.core.widget.barrage.Direction;
import com.example.william.my.core.widget.barrage.Util;
import com.example.william.my.core.widget.barrage.control.Controller;
import com.example.william.my.core.widget.barrage.control.SurfaceProxy;

/**
 * 基于 TextureView 的弹幕容器。
 *
 * <p>核心机制与避坑点：
 * 1. setOpaque(false) 保证透明背景，可叠在视频/图片之上
 * 2. onSurfaceTextureDestroyed 会销毁 Controller，旋转/重建后需重新添加弹幕
 * 3. onWindowFocusChanged 联动 pause/resume，后台返回不空转绘制
 * 4. getController() 在 Surface 就绪前可能为 null，调用方需判空
 */
public class DMTextureView extends TextureView implements TextureView.SurfaceTextureListener, DM {

    private Surface surface;
    private Controller controller;
    private final Controller.Builder builder;
    private OnDMReadyListener readyListener;

    public DMTextureView(Context context) {
        this(context, null);
    }

    public DMTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DMTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSurfaceTextureListener(this);
        setOpaque(false);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DMTextureView, defStyleAttr, 0);
        final Direction direction =
                Direction.getType(a.getInt(R.styleable.DMTextureView_dm_direction, Direction.RIGHT_LEFT.value));
        final int span = a.getDimensionPixelOffset(R.styleable.DMTextureView_dm_span, Util.dp2px(context, 2));
        final int sleep = a.getInteger(R.styleable.DMTextureView_dm_sleep, 0);
        final int spanTime = a.getInteger(R.styleable.DMTextureView_dm_span_time, 0);
        final int vSpace = a.getDimensionPixelOffset(R.styleable.DMTextureView_dm_v_space, Util.dp2px(context, 10));
        final int hSpace = a.getDimensionPixelOffset(R.styleable.DMTextureView_dm_h_space, Util.dp2px(context, 10));
        a.recycle();

        builder = new Controller.Builder()
                .setDirection(direction)
                .setSpan(span)
                .setSleep(sleep)
                .setSpanTime(spanTime)
                .sethSpace(hSpace)
                .setvSpace(vSpace);
    }

    /** Surface 就绪回调：此时 getController() 非空，可安全添加弹幕。 */
    public interface OnDMReadyListener {
        void onReady();
    }

    public void setOnDMReadyListener(OnDMReadyListener listener) {
        this.readyListener = listener;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        surface = new Surface(surfaceTexture);
        controller = builder
                .setSurfaceProxy(new SurfaceProxy(surface))
                .setWidth(width)
                .setHeight(height)
                .build();
        controller.start();
        if (readyListener != null) {
            readyListener.onReady();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        if (controller != null) {
            controller.setSize(width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        if (surface != null) {
            surface.release();
        }
        surface = null;
        if (controller != null) {
            controller.destroy();
            controller = null;
        }
        return true;
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
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    @Override
    public Controller getController() {
        return controller;
    }
}
