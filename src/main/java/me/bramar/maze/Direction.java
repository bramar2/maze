package me.bramar.maze;

import javafx.scene.layout.BorderWidths;

public class Direction {
    public static final int UP = 0;
    public static final int RIGHT = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;
    public static final int ORIGIN = 4;
    public static final int[][] DELTA = {
            {-1, 0},
            {0, 1},
            {1, 0},
            {0, -1}
    };

    public static int getBorderWidth(BorderWidths borderWidths, int direction) {
        return switch(direction) {
            case UP -> (int) borderWidths.getTop();
            case RIGHT -> (int) borderWidths.getRight();
            case DOWN -> (int) borderWidths.getBottom();
            case LEFT -> (int) borderWidths.getLeft();
            default -> throw new IllegalArgumentException();
        };
    }
}
