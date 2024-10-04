package org.lightsout.component;

import org.lightsout.model.Coordinate;

public class Puzzle implements Cloneable {

    private final int[][] cells;
    private final int dimension;

    public Puzzle(String string) { // 100,101,011
        String[] tokens = string.split(",");
        cells = new int[tokens.length][tokens.length];
        dimension = tokens.length;

        for (int i = 0; i < tokens.length; i++) {
            for (int j = 0; j < tokens.length; j++) {
                cells[i][j] = Integer.parseInt(String.valueOf(tokens[i].charAt(j)));
            }
        }
    }

    public Puzzle apply(Piece piece, Coordinate coordinate) {
        try {
            Puzzle puzzle = this.clone();
            int puzzleX = coordinate.x();
            int puzzleY = coordinate.y();
            int pieceX = 0;
            int pieceY = 0;

            return puzzle;
        } catch (ArrayIndexOutOfBoundsException e1) { // piece is outside the puzzle
            return null;
        } catch (CloneNotSupportedException e2) {
            // Handle the exception, though this should not happen since the class implements Cloneable
            throw new AssertionError("This should never happen since we implement Cloneable");
        }
    }

    int getDimension() {
        return dimension;
    }

    int getCellValue(Coordinate coordinate) {
        return cells[coordinate.x()][coordinate.y()];
    }

    @Override
    protected Puzzle clone() throws CloneNotSupportedException {
        return (Puzzle) super.clone();
    }

}
