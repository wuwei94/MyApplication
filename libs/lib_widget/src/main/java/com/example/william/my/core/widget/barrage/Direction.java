package com.example.william.my.core.widget.barrage;

/**
 * 弹幕运动方向。
 *
 * <p>源自 xujiaji/Barrage（Apache-2.0），迁移时保留方向枚举语义与取值映射。
 */
public enum Direction {
    /** 弹幕从右至左运动（默认）。 */
    RIGHT_LEFT(1),
    /** 弹幕从左至右运动。 */
    LEFT_RIGHT(2),
    /** 弹幕从上至下运动。 */
    UP_DOWN(3),
    /** 弹幕从下至上运动。 */
    DOWN_UP(4);

    public final int value;

    Direction(int v) {
        value = v;
    }

    public static Direction getType(int value) {
        switch (value) {
            case 2:
                return Direction.LEFT_RIGHT;
            case 3:
                return Direction.UP_DOWN;
            case 4:
                return Direction.DOWN_UP;
            default:
                return Direction.RIGHT_LEFT;
        }
    }
}
