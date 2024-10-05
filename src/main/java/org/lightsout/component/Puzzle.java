package org.lightsout.component;

import org.lightsout.model.Coordinate;

import java.util.HashSet;
import java.util.Set;

public class Puzzle implements Cloneable {

    private int[][] cells;
    private final int dimension;
    private final int depth;

    public Puzzle(String line1, String line2) { // 100,101,011
        String[] tokens = line2.split(",");
        this.cells = new int[tokens.length][tokens.length];
        this.dimension = tokens.length;
        this.depth = Integer.parseInt(line1);

        for (int i = 0; i < tokens.length; i++) {
            for (int j = 0; j < tokens.length; j++) {
                this.cells[i][j] = Integer.parseInt(String.valueOf(tokens[i].charAt(j)));
            }
        }
    }

    int[][] getCells() {
        return this.cells;
    }

    int getDimension() {
        return this.dimension;
    }

    int getDepth() {
        return this.depth;
    }

    int getCellValue(Coordinate coordinate) {
        return this.cells[coordinate.x()][coordinate.y()];
    }

    /**
     * For a cell to reset to 0, it must be updated a certain number of times.
     * The number of updates needed can be expressed as x + n * depth,
     * where 'x' is the remaining updates to reach 0, and 'n' is a natural number (>=0).
     * <p>
     * Given this, the method checks how many 'X's are present in all the pieces combined.
     * If the total number of 'X's is enough to reset all cells in the puzzle to 0, the puzzle is considered solvable.
     * Otherwise, the puzzle is unsolvable.
     * <p>
     * Overall, this method helps avoid unnecessary attempts to solve the puzzle when it is
     * mathematically impossible based on the pieces provided.
     *
     * @param pieces the set of pieces
     * @return if the puzzle is solvable given this set of pieces
     */
    public boolean isSolvable(Set<Piece> pieces) {
        int xCount = 0;
        for (Piece piece : pieces)
            xCount += piece.getAmountOfXs();

        int totalRemainderTo0 = 0;
        for (int i = 0; i < this.dimension; i++) {
            for (int j = 0; j < this.dimension; j++) {
                totalRemainderTo0 += this.depth - this.cells[i][j];
            }
        }

        return (xCount - totalRemainderTo0) % this.depth == 0;
    }

    /**
     * Gives all possible coordinates to place the piece in the puzzle.
     *
     * @param pieceWidth the width of the piece
     * @param pieceHeight the height of the piece
     * @return a set of legal coordinates to place the piece
     */
    public Set<Coordinate> getLegalCoordinates(int pieceWidth, int pieceHeight) {
        Set<Coordinate> legalCoordinates = new HashSet<>();

        for (int i = 0; i < this.dimension; i++) {
            for (int j = 0; j < this.dimension; j++) {
                Coordinate coordinate = new Coordinate(i, j);

                if (doesPieceFit(pieceWidth, pieceHeight, coordinate))
                    legalCoordinates.add(coordinate);
            }
        }

        return legalCoordinates;
    }

    /**
     * Applies the piece to the board. It is robust.
     *
     * @param piece the piece to place on the board
     * @param coordinate the coordinate to place the piece on
     * @return null if the piece cannot be placed on the board,
     *         or the new puzzle produced by applying the piece
     */
    public Puzzle apply(Piece piece, Coordinate coordinate) {
        if (!doesPieceFit(piece.getWidth(), piece.getHeight(), coordinate))
            return null;

        Puzzle puzzle = cloneP();
        int x = coordinate.x();
        int y = coordinate.y();

        for (int i = 0; i < piece.getHeight(); i++) {
            int currX = x + i;
            int currY = y;

            for (int j = 0; j < piece.getWidth(); j++) {
                if (piece.getCellValue(Coordinate.of(i, j)) == 'X') {
                    puzzle.cells[currX][currY]++;
                    if (puzzle.cells[currX][currY] == depth)
                        puzzle.cells[currX][currY] = 0;
                }
                currY++;
            }
        }

        return puzzle;
    }

    @Override
    protected Puzzle clone() throws CloneNotSupportedException {
        Puzzle clonedPuzzle = (Puzzle) super.clone();
        clonedPuzzle.cells = new int[this.dimension][this.dimension];

        for (int i = 0; i < this.dimension; i++) {
            clonedPuzzle.cells[i] = this.cells[i].clone();
        }

        return clonedPuzzle;
    }

    private Puzzle cloneP() {
        try {
            return clone();
        } catch (CloneNotSupportedException e) {
            // Handle the exception, though this should not happen since the class implements Cloneable
            throw new AssertionError("This should never happen since we implement Cloneable");
        }
    }

    private boolean doesPieceFit(int pieceWidth, int pieceHeight, Coordinate coordinate) {
        boolean fitsWidth = coordinate.y() + pieceWidth <= this.dimension;
        boolean fitsHeight = coordinate.x() + pieceHeight <= this.dimension;
        return fitsWidth && fitsHeight;
    }

}
