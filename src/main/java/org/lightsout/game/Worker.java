package org.lightsout.game;

import org.lightsout.component.Piece;
import org.lightsout.component.Puzzle;
import org.lightsout.model.Coordinate;
import org.lightsout.model.PieceInfo;
import org.lightsout.model.Solvability;

import java.util.*;

public class Worker extends GameConfig implements Runnable {

    private static volatile boolean FINISHED = false;

    private final Coordinate[] coordinates;
    private final List<Puzzle> puzzles; // used as a stack
    private int index; // indicates what piece it should take

    Worker(Puzzle puzzle, Coordinate[] coordinates, int index) {
        this.coordinates = coordinates;
        this.index = index;
        this.puzzles = new ArrayList<>();
        this.puzzles.add(puzzle);
    }

    public void run() {
        if (Thread.currentThread().isInterrupted() || FINISHED)
            return;

        Puzzle puzzle = puzzles.getLast();

        if (index < piecesSize) {
            PieceInfo pieceInfo = pieces.get(index);
            Piece piece = pieceInfo.piece();
            int pieceIndex = pieceInfo.coordinateOutputIndex();
            Set<Coordinate> legalCoordinates = puzzle.getLegalCoordinatesSorted(piece);

            for (Coordinate coordinate : legalCoordinates) {
                Puzzle newPuzzle = puzzle.apply(piece, coordinate);

                LOGGER.info("Thread id " + Thread.currentThread().threadId() + ": " +
                        "\nOld puzzle: \n" + puzzle +
                        "\nPiece applied: \n" + piece +
                        "\nNew puzzle: \n" + newPuzzle +
                        "\nCoordinate: \n" + coordinate +
                        "\nPieces left: " + (piecesSize - index));

                Solvability solvability = newPuzzle.isSolvable(pieces.subList(index + 1, piecesSize));
                if (solvability.isMathPossible() && solvability.sufficientXs()) {
                    this.coordinates[pieceIndex] = coordinate;
                    puzzles.add(newPuzzle);
                    index++;
                    run();
                    puzzles.removeLast(); // equivalent to puzzles.remove(newPuzzle);
                    index--;
                } else if (!solvability.sufficientXs()) {
                    // next coordinates won't have sufficient Xs either
                    break;
                }
            }
        } else if (index == piecesSize) {
            if (puzzle.isSolved()) {
                synchronized (Worker.this) {
                    if (!FINISHED) {
                        FINISHED = true;
                        for (Coordinate coordinate : coordinates)
                            System.out.print(coordinate + " ");
                    }
                }
            }
        } else {
            LOGGER.severe("Something went wrong! It should always hold that currentIndex <= piecesSize");
        }
    }

}
