package org.lightsout.component;

import org.lightsout.model.Coordinate;

public class Piece {

    private final char[][] cells;
    private final int height;
    private final int width;

    public Piece(String string) { // .XX,XX.
        String[] tokens = string.split(",");
        this.height = tokens.length;
        this.width = tokens[0].length();
        this.cells = new char[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                this.cells[i][j] = tokens[i].charAt(j);
            }
        }
    }

    int getHeight() {
        return this.height;
    }

    int getWidth() {
        return this.width;
    }

    int getAmountOfXs() {
        int amountOfXs = 0;

        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                if (this.cells[i][j] == 'X')
                    amountOfXs++;
            }
        }

        return amountOfXs;
    }

    char getCellValue(Coordinate coordinate) {
        return this.cells[coordinate.x()][coordinate.y()];
    }

}
