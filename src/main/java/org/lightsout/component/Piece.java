package org.lightsout.component;

import org.lightsout.model.Coordinate;

public class Piece {

    private final char[][] cells;
    private final int height;
    private final int width;

    public Piece(String string) { // .XX,XX.
        String[] tokens = string.split(",");
        height = tokens.length;
        width = tokens[0].length();
        cells = new char[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                cells[i][j] = tokens[i].charAt(j);
            }
        }
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    char getCellValue(Coordinate coordinate) {
        return cells[coordinate.x()][coordinate.y()];
    }

}
