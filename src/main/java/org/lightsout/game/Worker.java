package org.lightsout.game;

import org.lightsout.component.Piece;
import org.lightsout.component.Puzzle;
import org.lightsout.model.Coordinate;
import org.lightsout.model.PieceInfo;

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

        if (index == 0) {
            LOGGER.info("Thread id " + Thread.currentThread().threadId() + ": my first iteration");
        }

        Puzzle currPuzzle = puzzles.getLast();

        if (index < piecesSize) {
            PieceInfo pieceInfo = pieces.get(index);
            Piece piece = pieceInfo.piece();
            int pieceIndex = pieceInfo.coordinateOutputIndex();
            List<Coordinate> legalCoordinates = pieceInfo.legalCoordinates();

            index++;
            for (Coordinate coordinate : legalCoordinates) {
                Puzzle newPuzzle = currPuzzle.apply(piece, coordinate);
//                LOGGER.info("Thread id " + Thread.currentThread().threadId() + ": " +
//                        "\nOld puzzle: \n" + currPuzzle +
//                        "\nPiece applied: \n" + piece +
//                        "\nNew puzzle: \n" + newPuzzle +
//                        "\nCoordinate: \n" + coordinate +
//                        "\nPieces left: " + (piecesSize - index));

                boolean isSolvable = newPuzzle.isSolvable(pieces.subList(index, piecesSize));

                if (isSolvable) {
                    this.coordinates[pieceIndex] = coordinate;
                    puzzles.add(newPuzzle);
                    run();
                    puzzles.removeLast(); // equivalent to puzzles.remove(newPuzzle);
                }
            }
            index--;
        } else if (index == piecesSize) {
            if (currPuzzle.isSolved()) {
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
