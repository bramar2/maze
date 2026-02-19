package me.bramar.maze;

import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;

import java.util.Arrays;
import java.util.List;

public class DragHandler {
    private static final int WIDTH = -1;
    private static final int HEIGHT = -2;
    private static final int IGNORE = -3;

    private static final List<Pair<int[], Cursor>> BOUNDS = Arrays.asList(
            new Pair<>(new int[] {0, 0},            Cursor.NW_RESIZE),
            new Pair<>(new int[] {0, HEIGHT},       Cursor.SW_RESIZE),
            new Pair<>(new int[] {WIDTH, 0},        Cursor.NE_RESIZE),
            new Pair<>(new int[] {WIDTH, HEIGHT},   Cursor.SE_RESIZE),
            new Pair<>(new int[] {IGNORE, 0},       Cursor.N_RESIZE),
            new Pair<>(new int[] {IGNORE, HEIGHT},  Cursor.S_RESIZE),
            new Pair<>(new int[] {0, IGNORE},       Cursor.W_RESIZE),
            new Pair<>(new int[] {WIDTH, IGNORE},   Cursor.E_RESIZE)
    );

    // { resize_width?, shift_x?, resize_height?, shift_y? }
    private static final boolean[][] RESIZE = {
            {true, true, true, true},
            {true, true, true, false},
            {true, false, true, true},
            {true, false, true, false},
            {false, false, true, true},
            {false, false, true, false},
            {true, true, false, false},
            {true, false, false, false}
    };

    private final Stage stage;
    private final Scene scene;

    private int resizingState = -1;
    private double[] lastMousePos;

    public DragHandler(Scene scene) {
        this.stage = (Stage) scene.getWindow();
        this.scene = scene;
    }

    public boolean onResizingPosition() {
        return resizingState != -1;
    }

    public void onMouseMoved(MouseEvent event) {
        if(stage.isMaximized()) {
            resizingState = -1;
            return;
        }
        double x = event.getSceneX();
        double y = event.getSceneY();
        for(int i = 0; i < BOUNDS.size(); i++) {
            Pair<int[], Cursor> bound = BOUNDS.get(i);
            double cx = bound.getKey()[0];
            double cy = bound.getKey()[1];
            if(cx == WIDTH) cx = scene.getWidth();
            if(cy == HEIGHT) cy = scene.getHeight();

            double dx = (cx == IGNORE) ? 0 : (cx - x);
            double dy = (cy == IGNORE) ? 0 : (cy - y);

            double dist = Math.sqrt(dx * dx + dy * dy);
            boolean isCorner = cx != IGNORE && cy != IGNORE;
            if(dist <= 5 || (isCorner && dist <= 10)) {
                resizingState = i;
                scene.setCursor(bound.getValue());
                return;
            }
        }
        resizingState = -1;
        lastMousePos = null;
        scene.setCursor(Cursor.DEFAULT);
    }

    public void onMouseDragged(MouseEvent event) {
        if(resizingState != -1) {
            System.out.println("RESIZE");
            double x = event.getScreenX();
            double y = event.getScreenY();
            if(lastMousePos != null) {
                double dx = x - lastMousePos[0];
                double dy = y - lastMousePos[1];

                boolean resize_width = RESIZE[resizingState][0];
                boolean shift_x = RESIZE[resizingState][1];
                boolean resize_height = RESIZE[resizingState][2];
                boolean shift_y = RESIZE[resizingState][3];

                if(resize_width) {
                    if(shift_x) {
                        stage.setWidth(stage.getWidth() - dx);
                        stage.setX(stage.getX() + dx);
                    }else {
                        stage.setWidth(stage.getWidth() + dx);
                    }
                }

                if(resize_height) {
                    if(shift_y) {
                        stage.setHeight(stage.getHeight() - dy);
                        stage.setY(stage.getY() + dy);
                    }else {
                        stage.setHeight(stage.getHeight() + dy);
                    }
                }
            }
            lastMousePos = new double[] {x, y};
        }
    }

    public void onTitleBarDrag(MouseEvent event) {
        if(resizingState != -1) return;
        if(lastMousePos != null) {
            double dx = event.getScreenX() - lastMousePos[0];
            double dy = event.getScreenY() - lastMousePos[1];

            stage.setX(stage.getX() + dx);
            stage.setY(stage.getY() + dy);
        }
        lastMousePos = new double[] {event.getScreenX(), event.getScreenY()};
    }
}
