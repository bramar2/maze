package me.bramar.maze;

import java.util.Random;

public class MazeGenerator {
    private static final Random rnd = new Random();

    static int[][] initial(int rows, int cols) {
        int[][] grid = new int[rows][cols];
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols - 1; j++) {
                grid[i][j] = Direction.RIGHT;
            }
        }
        for(int i = 0; i < rows - 1; i++) {
            grid[i][cols - 1] = Direction.DOWN;
        }
        grid[rows - 1][cols - 1] = Direction.ORIGIN;
        return grid;
    }

    static void randomize(int[][] grid, int iterations) {
        int rows = grid.length, cols = grid[0].length;
        int originRow = 0, originCol = 0;
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                if(grid[i][j] == Direction.ORIGIN) {
                    originRow = i;
                    originCol = j;
                }
            }
        }
        for(int it = 0; it < iterations; it++) {
            boolean[] possible = new boolean[4];
            int cnt = 0;
            for(int dirIdx = 0; dirIdx < 4; dirIdx++) {
                int deltaRow = Direction.DELTA[dirIdx][0], deltaCol = Direction.DELTA[dirIdx][1];
                int adjRow = originRow + deltaRow;
                int adjCol = originCol + deltaCol;
                if(0 <= adjRow && adjRow < rows && 0 <= adjCol && adjCol < cols) {
                    possible[dirIdx] = true;
                    cnt++;
                }
            }
            if(cnt == 0) throw new RuntimeException();
            int target = rnd.nextInt(cnt);
            for(int dirIdx = 0; dirIdx < 4; dirIdx++) {
                if(possible[dirIdx]) {
                    if(target == 0) {
                        int delta_row = Direction.DELTA[dirIdx][0], delta_col = Direction.DELTA[dirIdx][1];
                        int adj_row = originRow + delta_row;
                        int adj_col = originCol + delta_col;

                        grid[originRow][originCol] = dirIdx;
                        grid[adj_row][adj_col] = Direction.ORIGIN;

                        originRow = adj_row;
                        originCol = adj_col;
                        break;
                    }else {
                        target--;
                    }
                }
            }
        }
    }
}
