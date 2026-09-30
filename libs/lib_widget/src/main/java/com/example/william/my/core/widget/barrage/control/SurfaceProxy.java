package com.example.william.my.core.widget.barrage.control;

import android.graphics.Canvas;
import android.view.Surface;
import android.view.SurfaceHolder;

/**
 * Surface / SurfaceHolder 统一锁画布代理。
 *
 * <p>DMTextureView 走 {@link Surface}，DMSurfaceView 走 {@link SurfaceHolder}，
 * 二者 lockCanvas 线程模型不同，代理层抹平差异供 DrawThread 使用。
 */
public class SurfaceProxy {
    private final Surface surface;
    private final SurfaceHolder surfaceHolder;

    public SurfaceProxy(Surface surface) {
        this.surface = surface;
        this.surfaceHolder = null;
    }

    public SurfaceProxy(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        this.surface = null;
    }

    public Canvas lockCanvas() {
        if (surfaceHolder != null) {
            return surfaceHolder.lockCanvas();
        }
        return surface.lockCanvas(null);
    }

    public void unlockCanvasAndPost(Canvas canvas) {
        if (surfaceHolder != null) {
            surfaceHolder.unlockCanvasAndPost(canvas);
        } else {
            surface.unlockCanvasAndPost(canvas);
        }
    }
}
