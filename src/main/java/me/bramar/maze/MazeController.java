package me.bramar.maze;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.*;

public class MazeController {
    @FXML
    private GridPane mazeGridPane;

    @FXML
    private Label rowSliderLabel, colSliderLabel;
    @FXML
    private Slider rowSlider, colSlider, speedSlider;
    @FXML
    private StackPane controlClose, controlMinimize, controlMaximize;
    @FXML
    private ImageView iconMaximize, iconRestore;

    @FXML
    private Button toggleBfsBtn;

    private Pane[][] gridCells;
    private int[][] predecessor;
    private Queue<int[]> bfsQueue;
    private int pathSize;

    private final Timeline bfsLoop = new Timeline(new KeyFrame(Duration.millis(100), evt -> stepBfs()));
    private final Timeline visualizePathLoop = new Timeline(new KeyFrame(Duration.millis(50), evt -> stepVisualizePath()));
    private final List<int[]> path = new ArrayList<>();

    private DragHandler dragHandler;

    private final AudioClip beep = new AudioClip(MazeApplication.class.getResource("/sfx/beep.mp3").toExternalForm());

    @FXML
    public void initialize() {
        bfsLoop.setCycleCount(Animation.INDEFINITE);
        visualizePathLoop.setCycleCount(Animation.INDEFINITE);

        for(Pair<Slider, Label> pair : Arrays.asList(
                new Pair<>(rowSlider, rowSliderLabel),
                new Pair<>(colSlider, colSliderLabel)
        )) {
            Slider slider = pair.getKey();
            Label label = pair.getValue();
            label.setText(String.valueOf(((Double) slider.getValue()).intValue()));
            slider.valueProperty().addListener((observableValue, oldVal, newVal) -> {
                slider.setValue(newVal.intValue());
                label.setText(String.valueOf(newVal.intValue()));
            });
        }

        generateRandomGrid();
    }

    public void initInScene() {
        dragHandler = new DragHandler(speedSlider.getScene());
        Stage stage = (Stage) speedSlider.getScene().getWindow();
        stage.maximizedProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal) {
                iconMaximize.setVisible(false);
                iconRestore.setVisible(true);
            }else {
                iconMaximize.setVisible(true);
                iconRestore.setVisible(false);
            }
        });
    }

    @FXML
    public void generateRandomGrid() {
        mazeGridPane.getChildren().clear();

        int rows = ((Double) rowSlider.getValue()).intValue();
        int cols = ((Double) colSlider.getValue()).intValue();

        gridCells = new Pane[rows][cols];
        predecessor = new int[rows][cols];
        bfsQueue = new ArrayDeque<>();

        bfsLoop.stop();
        visualizePathLoop.stop();
        path.clear();

        toggleBfsBtn.setText("Start");


        int[][] grid = MazeGenerator.initial(rows, cols);
        MazeGenerator.randomize(grid, 100000);

        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                boolean[] reachable = new boolean[4];
                if(grid[i][j] != Direction.ORIGIN) reachable[grid[i][j]] = true;
                for(int dirIdx = 0; dirIdx < 4; dirIdx++) {
                    int adjRow = i + Direction.DELTA[dirIdx][0];
                    int adjCol = j + Direction.DELTA[dirIdx][1];
                    if(0 <= adjRow && adjRow < rows && 0 <= adjCol && adjCol < cols && grid[adjRow][adjCol] == (dirIdx + 2) % 4) {
                        reachable[dirIdx] = true;
                    }
                }
                if(i == 0 && j == 0) reachable[0] = true;
                if(i == rows-1 && j == cols-1) reachable[1] = true;
                BorderWidths borderWidths = new BorderWidths(
                        reachable[0] ? 0 : 2,
                        reachable[1] ? 0 : 2,
                        reachable[2] ? 0 : 2,
                        reachable[3] ? 0 : 2
                );

                Pane pane = new Pane();
                pane.setMinHeight(3);
                pane.setMinWidth(3);
                GridPane.setHgrow(pane, Priority.ALWAYS);
                GridPane.setVgrow(pane, Priority.ALWAYS);
                pane.setMaxWidth(Double.POSITIVE_INFINITY);
                pane.setMaxHeight(Double.POSITIVE_INFINITY);
                pane.setBorder(new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, borderWidths)));

                gridCells[i][j] = pane;
                mazeGridPane.add(pane, j, i);
            }
        }

        bfsQueue.offer(new int[] {0, 0});
        gridCells[0][0].setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, new Insets(1))));
    }

    @FXML
    public void toggleBfs() {
        if(bfsLoop.getStatus() != Animation.Status.RUNNING && !bfsQueue.isEmpty()) {
            double duration = Math.max((1 - speedSlider.valueProperty().doubleValue()) * 500, 1);
            bfsLoop.getKeyFrames().set(0, new KeyFrame(Duration.millis(duration), evt -> stepBfs()));
            bfsLoop.play();

            toggleBfsBtn.setText("Stop");
        }else {
            bfsLoop.stop();

            toggleBfsBtn.setText("Start");
        }
    }

    @FXML
    public void stepBfs() {
        int size = bfsQueue.size();
        int rows = gridCells.length;
        int cols = gridCells[0].length;
        boolean finished = false;
        for(; size > 0; size--) {
            int[] cell = bfsQueue.poll();
            int row = cell[0], col = cell[1];
            gridCells[row][col].setBackground(new Background(new BackgroundFill(Color.GREEN, null, null)));

            if(row == rows - 1 && col == cols - 1) {
                finished = true;
            }

            for(int dirIdx = 0; dirIdx < 4; dirIdx++) {
                int nrow = row + Direction.DELTA[dirIdx][0];
                int ncol = col + Direction.DELTA[dirIdx][1];

                if(0 <= nrow && nrow < rows && 0 <= ncol && ncol < cols
                        && Direction.getBorderWidth(gridCells[row][col].getBorder().getStrokes().getFirst().getWidths(), dirIdx) == 0
                        && gridCells[nrow][ncol].getBackground() == null) {
                    gridCells[nrow][ncol].setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
                    predecessor[nrow][ncol] = (dirIdx + 2) % 4;
                    bfsQueue.offer(new int[] {nrow, ncol});
                }
            }
        }
        if(finished) {
            startVisualizePath();
            bfsQueue.clear();
            bfsLoop.stop();
        }
        beep.play(1, 0, 1.0f + 5.0f * (double) bfsQueue.size() / rows, 0, 0);
    }

    public void startVisualizePath() {
        int rows = gridCells.length;
        int cols = gridCells[0].length;
        int row = rows - 1, col = cols - 1;
        path.add(new int[] {row, col});
        while(row != 0 || col != 0) {
            int dirIdx = predecessor[row][col];
            row += Direction.DELTA[dirIdx][0];
            col += Direction.DELTA[dirIdx][1];
            path.add(new int[] {row, col});
        }
        double duration = Math.min(50, 5000.0 / path.size());
        pathSize = path.size();
        visualizePathLoop.getKeyFrames().set(0, new KeyFrame(Duration.millis(duration), (evt) -> stepVisualizePath()));
        visualizePathLoop.play();
    }

    public void stepVisualizePath() {
        if(!path.isEmpty()) {
            int[] cell = path.getLast();
            path.removeLast();

            int row = cell[0];
            int col = cell[1];

            gridCells[row][col].setBackground(new Background(new BackgroundFill(Color.YELLOW, null, null)));

            beep.play(1, 0, 1.0f + 3.0f * (double) (pathSize - path.size()) / pathSize, 0, 0);
        }
    }

    @FXML
    public void onTitleBarClick(MouseEvent event) {
        if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            maximize();
        }
    }

    @FXML
    public void onTitleBarDrag(MouseEvent event) {
        dragHandler.onTitleBarDrag(event);
    }

    @FXML
    public void onMouseMoved(MouseEvent event) {
        dragHandler.onMouseMoved(event);
    }

    @FXML
    public void onMouseDragged(MouseEvent event) {
        dragHandler.onMouseDragged(event);
    }

    @FXML
    public void hoverMinimize() {
        controlMinimize.setBackground(new Background(new BackgroundFill(Color.valueOf("#383a3d"), null, null)));
    }

    @FXML
    public void unhoverMinimize() {
        controlMinimize.setBackground(null);
    }

    @FXML
    public void hoverMaximize() {
        controlMaximize.setBackground(new Background(new BackgroundFill(Color.valueOf("#383a3d"), null, null)));
    }

    @FXML
    public void unhoverMaximize() {
        controlMaximize.setBackground(null);
    }

    @FXML
    public void hoverClose() {
        controlClose.setBackground(new Background(new BackgroundFill(Color.valueOf("#c42b1c"), null, null)));
    }

    @FXML
    public void unhoverClose() {
        controlClose.setBackground(null);
    }

    @FXML
    public void minimize() {
        if(dragHandler.onResizingPosition()) return;
        Stage stage = (Stage) speedSlider.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    public void maximize() {
        if(dragHandler.onResizingPosition()) return;
        Stage stage = (Stage) speedSlider.getScene().getWindow();
        if(stage.isMaximized()) {
            stage.setMaximized(false);

            stage.setMinWidth(0);
            stage.setMinHeight(0);
            stage.setMaxWidth(Double.POSITIVE_INFINITY);
            stage.setMaxHeight(Double.POSITIVE_INFINITY);
        }else {
            stage.setMaximized(true);

            Rectangle2D primaryScreen = Screen.getPrimary().getVisualBounds();
            stage.setX(primaryScreen.getMinX());
            stage.setY(primaryScreen.getMinY());

            stage.setMaxWidth(primaryScreen.getWidth());
            stage.setMinWidth(primaryScreen.getWidth());

            stage.setMaxHeight(primaryScreen.getHeight());
            stage.setMinHeight(primaryScreen.getHeight());
        }
    }

    @FXML
    public void close() {
        if(dragHandler.onResizingPosition()) return;
        Stage stage = (Stage) speedSlider.getScene().getWindow();
        stage.close();
    }
}
