package com.example.william.my.core.widget.barrage.control;

import android.graphics.Canvas;

/**
 * 弹幕绘制线程：固定帧间隔锁定 Surface Canvas 并回调绘制。
 *
 * <p>默认帧间隔 10ms；未在绘制态时休眠等待，避免空转耗电。
 * {@link #setDraw(boolean)} 只暂停绘制，{@link #setRun(false)} 才退出线程。
 */
public class DrawThread extends Thread {
    private static final int FRAME_INTERVAL = 10;

    private volatile boolean isRun = false;
    private volatile boolean isDraw = false;
    private OnDrawListener drawListener;
    private OnFrameListener frameListener;
    private final SurfaceProxy surfaceProxy;

    public DrawThread(SurfaceProxy surfaceProxy) {
        this.surfaceProxy = surfaceProxy;
    }

    @Override
    public synchronized void start() {
        isRun = true;
        isDraw = true;
        super.start();
    }

    @Override
    public void run() {
        while (isRun) {
            draw();
        }
    }

    private void draw() {
        if (isDraw && surfaceProxy != null) {
            long startTime = System.currentTimeMillis();
            Canvas canvas = surfaceProxy.lockCanvas();
            try {
                synchronized (surfaceProxy) {
                    if (this.drawListener != null) {
                        this.drawListener.onDraw(canvas);
                    }
                    long endTime = System.currentTimeMillis();
                    int diffTime = (int) (endTime - startTime);
                    if (diffTime < FRAME_INTERVAL) {
                        try {
                            Thread.sleep(FRAME_INTERVAL - diffTime);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    if (frameListener != null) {
                        frameListener.onFrameRate(System.currentTimeMillis() - startTime);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    surfaceProxy.unlockCanvasAndPost(canvas);
                }
            }
        } else {
            try {
                Thread.sleep(FRAME_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (frameListener != null) {
                frameListener.onFrameRate(1000L);
            }
        }
    }

    public void setOnDrawListener(OnDrawListener drawListener) {
        this.drawListener = drawListener;
    }

    public void setOnFrameListener(OnFrameListener frameListener) {
        this.frameListener = frameListener;
    }

    public void setRun(boolean run) {
        isRun = run;
    }

    public void setDraw(boolean draw) {
        isDraw = draw;
    }

    public boolean isRun() {
        return isRun;
    }

    public boolean isDraw() {
        return isDraw;
    }

    public interface OnFrameListener {
        void onFrameRate(long time);
    }

    public interface OnDrawListener {
        void onDraw(Canvas canvas);
    }
}
