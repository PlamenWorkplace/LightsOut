package org.lightsout.component;

import org.lightsout.model.Coordinate;
import org.lightsout.model.PieceInfo;

import java.util.*;

public class Puzzle implements Cloneable {

    private int[][] cells;
    private final int width;
    private final int height;
    private final int depth;

    public Puzzle(String line1, String line2) { // 100,101,011
        String[] tokens = line2.split(",");
        this.height = tokens.length;
        this.width = tokens[0].length();
        this.cells = new int[this.height][this.width];
        this.depth = Integer.parseInt(line1);

        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                this.cells[i][j] = Integer.parseInt(String.valueOf(tokens[i].charAt(j)));
            }
        }
    }

    int[][] getCells() {
        return this.cells;
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
     * @param piecesInfo the set of pieces
     * @return if the puzzle is solvable given this set of pieces
     */
    public boolean isSolvable(List<PieceInfo> piecesInfo) {
        int piecesXCount = 0;

        for (PieceInfo pieceInfo : piecesInfo)
            piecesXCount += pieceInfo.piece().getAmountOfXs();

        int puzzleTotalRemainderTo0 = getTotalRemainderTo0();

        boolean sufficientXs = piecesXCount - puzzleTotalRemainderTo0 >= 0;
        boolean isMathPossible = (piecesXCount - puzzleTotalRemainderTo0) % this.depth == 0;

        return sufficientXs && isMathPossible;
    }

    public boolean isSolved() {
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                if (cells[i][j] != 0)
                    return false;
            }
        }

        return true;
    }

    /**
     * Gives all possible coordinates to place the piece in the puzzle.
     *
     * @param piece the piece to place
     * @return a list of legal coordinates to place the piece
     */
    public List<Coordinate> getLegalCoordinates(Piece piece) {
        List<Coordinate> legalCoordinates = new ArrayList<>(); // <coordinate, No affected cells>

        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                Coordinate coordinate = new Coordinate(i, j);

                if (doesPieceFit(piece.getWidth(), piece.getHeight(), coordinate)) {
                    legalCoordinates.add(coordinate);
                }
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

    /**
     * Applies the piece to the board if it doesn't encounter any zeroes.
     * It is robust.
     *
     * @param piece the piece to place on the board
     * @param coordinate the coordinate to place the piece on
     * @return null if the piece cannot be placed on the board, or it falls on 0,
     *         or the new puzzle produced by applying the piece
     */
    public Puzzle applyIfNo0(Piece piece, Coordinate coordinate) {
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
                    if (puzzle.cells[currX][currY] == 0)
                        return null;

                    puzzle.cells[currX][currY]++;
                    if (puzzle.cells[currX][currY] == depth)
                        puzzle.cells[currX][currY] = 0;
                }
                currY++;
            }
        }

        return puzzle;
    }

    public int getTotalRemainderTo0() {
        int totalRemainderTo0 = 0;

        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                int cellValue = this.cells[i][j];

                if (cellValue != 0) {
                    totalRemainderTo0 += this.depth - cellValue;
                }
            }
        }

        return totalRemainderTo0;
    }

    @Override
    protected Puzzle clone() throws CloneNotSupportedException {
        Puzzle clonedPuzzle = (Puzzle) super.clone();
        clonedPuzzle.cells = new int[this.height][this.width];

        for (int i = 0; i < this.height; i++) {
            clonedPuzzle.cells[i] = this.cells[i].clone();
        }

        return clonedPuzzle;
    }

    public Puzzle cloneP() {
        try {
            return clone();
        } catch (CloneNotSupportedException e) {
            // Handle the exception, though this should not happen since the class implements Cloneable
            throw new AssertionError("This should never happen since we implement Cloneable");
        }
    }

    boolean doesPieceFit(int pieceWidth, int pieceHeight, Coordinate coordinate) {
        boolean fitsWidth = coordinate.y() + pieceWidth <= this.width;
        boolean fitsHeight = coordinate.x() + pieceHeight <= this.height;
        return fitsWidth && fitsHeight;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                builder.append(cells[i][j]).append(" ");
            }
            builder.append("\n");
        }

        return builder.toString();
    }

}
