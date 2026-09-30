package com.example.william.my.core.widget.barrage.control;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.SparseArray;
import android.view.View;

import com.example.william.my.core.widget.barrage.Direction;
import com.example.william.my.core.widget.barrage.callback.OnDMAddListener;
import com.example.william.my.core.widget.barrage.callback.ViewCreator;
import com.example.william.my.core.widget.barrage.entity.BaseDmEntity;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 弹幕轨迹控制器：负责入列、赛道排布、位移绘制与生命周期。
 *
 * <p>核心机制与避坑点：
 * 1. 轨道排布：按 vSpace/hSpace 分轨道，新弹幕插入首个不重叠轨道，避免同道追尾
 * 2. 速度模型：spanTime > 0 时按时间差推算 offset（毫秒级），否则按固定 span 步进
 * 3. 线程边界：add() 在后台线程转 Bitmap；绘制在 DrawThread；回调切主线程
 * 4. 资源释放：destroy() 必须调用，否则 DrawThread 泄漏；pause() 只停绘制不停线程
 */
public class Controller {

    private Direction direction = Direction.RIGHT_LEFT;
    private final Queue<BaseDmEntity> newDmQueue = new ConcurrentLinkedQueue<>();
    private final Queue<BaseDmEntity> addedDmList = new ConcurrentLinkedQueue<>();
    private int width;
    private int height;
    private float offset;
    private int hSpace = 20;
    private int vSpace = 20;
    private float span = 5F;
    private int spanTime = 0;
    private float speed = 0F;
    private boolean isH;
    private final ExecutorService exec = Executors.newCachedThreadPool();
    private OnDMAddListener onDMAddListener;
    private Handler mainHandler;
    private DrawThread drawThread;

    private Controller() {
    }

    public void setSurfaceProxy(SurfaceProxy surfaceProxy) {
        drawThread = new DrawThread(surfaceProxy);
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        initOffset();
    }

    public void initOffset() {
        switch (direction) {
            case RIGHT_LEFT:
                offset = width;
                if (span > 0) {
                    span = -span;
                }
                break;
            case LEFT_RIGHT:
            case UP_DOWN:
                offset = 0;
                if (span < 0) {
                    span = -span;
                }
                break;
            case DOWN_UP:
                offset = height;
                if (span > 0) {
                    span = -span;
                }
                break;
        }
        updateSpeed();
    }

    public void start() {
        if (drawThread == null || drawThread.isRun()) {
            return;
        }
        drawThread.setOnDrawListener(new DrawThread.OnDrawListener() {
            @Override
            public void onDraw(Canvas canvas) {
                runTask(canvas);
            }
        });
        drawThread.start();
    }

    private Handler getMainHandler() {
        if (mainHandler == null) {
            mainHandler = new Handler(Looper.getMainLooper());
        }
        return mainHandler;
    }

    private long lastTime = 0L;

    private void runTask(Canvas canvas) {
        if (spanTime > 0) {
            final long nowTime = SystemClock.uptimeMillis();
            final long disTime = nowTime - lastTime;
            // 首次或 pause 后时间差过大，跳过以免瞬移
            if (lastTime != 0L && disTime < 100) {
                offset += speed * disTime;
            }
            lastTime = nowTime;
        } else {
            offset += span;
        }

        drawDm(canvas, offset);
        if (!addDmInQueue() && addedDmList.isEmpty()) {
            drawThread.setDraw(false);
            if (onDMAddListener != null) {
                getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        onDMAddListener.addedAll();
                    }
                });
            }
        }
    }

    private void drawDm(Canvas canvas, float value) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.save();
        if (isH) {
            canvas.translate(value, 0);
        } else {
            canvas.translate(0, value);
        }

        Iterator<BaseDmEntity> iterator = addedDmList.iterator();
        while (iterator.hasNext()) {
            BaseDmEntity entity = iterator.next();
            boolean removeThisEntity;
            switch (direction) {
                case RIGHT_LEFT:
                    removeThisEntity = offset < -entity.rect.right;
                    break;
                case LEFT_RIGHT:
                    removeThisEntity = offset > width + entity.rect.right;
                    break;
                case DOWN_UP:
                    removeThisEntity = offset < -entity.rect.bottom;
                    break;
                case UP_DOWN:
                    removeThisEntity = offset > height + entity.rect.bottom;
                    break;
                default:
                    removeThisEntity = false;
                    break;
            }
            if (removeThisEntity) {
                iterator.remove();
                continue;
            }

            switch (direction) {
                case RIGHT_LEFT:
                case DOWN_UP:
                    canvas.drawBitmap(entity.bitmap, entity.rect.left, entity.rect.top, null);
                    break;
                case LEFT_RIGHT:
                    canvas.drawBitmap(entity.bitmap, -entity.rect.left - entity.rect.width(), entity.rect.top, null);
                    break;
                case UP_DOWN:
                    canvas.drawBitmap(entity.bitmap, entity.rect.left, -entity.rect.top - entity.rect.height(), null);
                    break;
            }
        }
        canvas.restore();
    }

    public void add(final ViewCreator viewCreator) {
        exec.execute(new Runnable() {
            @Override
            public void run() {
                BaseDmEntity entity = new BaseDmEntity(viewCreator.build());
                addToQueue(entity);
            }
        });
    }

    public void add(final View templateView) {
        exec.execute(new Runnable() {
            @Override
            public void run() {
                BaseDmEntity entity = new BaseDmEntity(templateView);
                addToQueue(entity);
            }
        });
    }

    public void addToQueue(BaseDmEntity entity) {
        if (entity == null) {
            throw new RuntimeException("entity cannot null");
        }
        newDmQueue.add(entity);
        if (drawThread != null && !drawThread.isDraw()) {
            initOffset();
            drawThread.setDraw(true);
        }
    }

    private final SparseArray<LinkedList<BaseDmEntity>> hierarchy = new SparseArray<>();

    private boolean addDmInQueue() {
        BaseDmEntity entity = newDmQueue.peek();
        if (entity == null) {
            return false;
        }
        final float minLimit;
        final float maxLimit;
        switch (direction) {
            case RIGHT_LEFT:
                minLimit = width - offset;
                maxLimit = minLimit + width;
                break;
            case LEFT_RIGHT:
                minLimit = offset;
                maxLimit = minLimit + width;
                break;
            case DOWN_UP:
                minLimit = height - offset;
                maxLimit = minLimit + height;
                break;
            case UP_DOWN:
                minLimit = offset;
                maxLimit = minLimit + height;
                break;
            default:
                minLimit = 0;
                maxLimit = 0;
                break;
        }
        if (addedDmList.isEmpty()) {
            addToDisplay(entity);
            return true;
        }

        hierarchy.clear();
        if (isH) {
            for (BaseDmEntity addedDM : addedDmList) {
                int key = (int) addedDM.rect.top;
                if (hierarchy.get(key) == null) {
                    hierarchy.put(key, new LinkedList<BaseDmEntity>());
                }
                hierarchy.get(key).addFirst(addedDM);
            }
        } else {
            for (BaseDmEntity addedDM : addedDmList) {
                int key = (int) addedDM.rect.left;
                if (hierarchy.get(key) == null) {
                    hierarchy.put(key, new LinkedList<BaseDmEntity>());
                }
                hierarchy.get(key).addFirst(addedDM);
            }
        }

        BaseDmEntity lastDm = null;
        for (int i = 0; i < hierarchy.size(); i++) {
            LinkedList<BaseDmEntity> linkedList = hierarchy.get(hierarchy.keyAt(i));
            if (linkedList == null || linkedList.isEmpty()) {
                continue;
            }
            BaseDmEntity lastEntity = linkedList.getFirst();

            if (isH) {
                if (lastDm == null && lastEntity.rect.top >= lastEntity.rect.height() + vSpace) {
                    entity.rect.offsetTo(minLimit, 0);
                    addToDisplay(entity);
                    return true;
                }
            } else {
                if (lastDm == null && lastEntity.rect.left >= lastEntity.rect.width() + hSpace) {
                    entity.rect.offsetTo(0, minLimit);
                    addToDisplay(entity);
                    return true;
                }
            }

            if (isH) {
                if (lastDm != null && lastDm.rect.bottom + lastEntity.rect.height() < lastEntity.rect.top) {
                    entity.rect.offsetTo(minLimit, lastDm.rect.bottom + vSpace);
                    addToDisplay(entity);
                    return true;
                }
            } else {
                if (lastDm != null && lastDm.rect.right + lastEntity.rect.width() < lastEntity.rect.left) {
                    entity.rect.offsetTo(lastDm.rect.right + hSpace, minLimit);
                    addToDisplay(entity);
                    return true;
                }
            }

            lastDm = lastEntity;

            if (isH) {
                if (lastEntity.rect.right < maxLimit) {
                    entity.rect.offsetTo((Math.max(lastEntity.rect.right, minLimit)) + hSpace, lastEntity.rect.top);
                    addToDisplay(entity);
                    return true;
                }
            } else {
                if (lastEntity.rect.bottom < maxLimit) {
                    entity.rect.offsetTo(lastEntity.rect.left, (Math.max(lastEntity.rect.bottom, minLimit)) + vSpace);
                    addToDisplay(entity);
                    return true;
                }
            }
        }

        if (lastDm == null) {
            return false;
        }

        if (isH) {
            if (lastDm.rect.bottom < height - lastDm.rect.height()) {
                entity.rect.offsetTo(minLimit, lastDm.rect.bottom + vSpace);
                addToDisplay(entity);
                return true;
            }
        } else {
            if (lastDm.rect.right < width - lastDm.rect.width()) {
                entity.rect.offsetTo(lastDm.rect.right + hSpace, minLimit);
                addToDisplay(entity);
                return true;
            }
        }
        return false;
    }

    private synchronized void addToDisplay(final BaseDmEntity entity) {
        if (entity == null) {
            return;
        }
        newDmQueue.remove(entity);
        addedDmList.add(entity);
        hierarchy.clear();
        if (onDMAddListener != null) {
            getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    onDMAddListener.added(entity);
                }
            });
        }
    }

    public void setOnDMAddListener(OnDMAddListener l) {
        this.onDMAddListener = l;
    }

    public void resume() {
        if (drawThread != null) {
            drawThread.setDraw(true);
        }
    }

    public void pause() {
        if (drawThread != null) {
            drawThread.setDraw(false);
        }
    }

    public void clean() {
        newDmQueue.clear();
        addedDmList.clear();
        if (drawThread != null) {
            drawThread.setDraw(false);
        }
        initOffset();
    }

    public void destroy() {
        clean();
        mainHandler = null;
        onDMAddListener = null;
        if (drawThread != null) {
            drawThread.setRun(false);
            drawThread.interrupt();
            drawThread = null;
        }
        exec.shutdownNow();
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
        this.isH = direction == Direction.LEFT_RIGHT || direction == Direction.RIGHT_LEFT;
    }

    public void sethSpace(int hSpace) {
        this.hSpace = hSpace;
    }

    public void setvSpace(int vSpace) {
        this.vSpace = vSpace;
    }

    public void setSpan(int span) {
        if (span == 0) {
            span = 2;
        }
        this.span = this.span < 0 ? -span : span;
        updateSpeed();
    }

    public void setSpanTime(int spanTime) {
        this.spanTime = spanTime;
        updateSpeed();
    }

    private void updateSpeed() {
        if (spanTime > 0L && span != 0) {
            speed = span / spanTime;
        }
    }

    /** 弹幕控制器构建器：Surface 尺寸与方向需在 start 前配置完成。 */
    public static class Builder {
        private SurfaceProxy surfaceProxy;
        private Direction direction = Direction.RIGHT_LEFT;
        private int span = 2;
        private int sleep;
        private int spanTime;
        private int vSpace = 20;
        private int hSpace = 20;
        private int width;
        private int height;
        private OnDMAddListener onDMAddListener;

        public Builder setSurfaceProxy(SurfaceProxy surfaceProxy) {
            this.surfaceProxy = surfaceProxy;
            return this;
        }

        public Builder setDirection(Direction direction) {
            this.direction = direction;
            return this;
        }

        public Builder setSpan(int span) {
            this.span = span;
            return this;
        }

        public Builder setSleep(int sleep) {
            this.sleep = sleep;
            return this;
        }

        public Builder setSpanTime(int spanTime) {
            this.spanTime = spanTime;
            return this;
        }

        public Builder setvSpace(int vSpace) {
            this.vSpace = vSpace;
            return this;
        }

        public Builder sethSpace(int hSpace) {
            this.hSpace = hSpace;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setOnDMAddListener(OnDMAddListener l) {
            this.onDMAddListener = l;
            return this;
        }

        public Controller build() {
            Controller controller = new Controller();
            controller.setSurfaceProxy(surfaceProxy);
            controller.setDirection(direction);
            controller.setSpan(span);
            controller.setSpanTime(spanTime == 0 ? sleep : spanTime);
            controller.setvSpace(vSpace);
            controller.sethSpace(hSpace);
            controller.setSize(width, height);
            controller.setOnDMAddListener(onDMAddListener);
            return controller;
        }
    }
}
