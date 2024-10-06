package org.lightsout.game;

import org.lightsout.component.Piece;
import org.lightsout.component.Puzzle;
import org.lightsout.model.Coordinate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Worker implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Worker.class.getName());
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(1);

    private final Puzzle solvingPuzzle;
    private final Map<Integer, Piece> pieces;
    private final int biggestPieceIndex;
    private final Coordinate[] currentCoordinates;

    Worker(Puzzle solvingPuzzle, Map<Integer, Piece> pieces, Coordinate[] currentCoordinates) {
        this.solvingPuzzle = solvingPuzzle;
        this.pieces = pieces;
        this.currentCoordinates = currentCoordinates;

        int biggestPieceSize = 0;
        int biggestPieceIndex = -1;
        for (Map.Entry<Integer, Piece> entry : pieces.entrySet()) {
            Piece piece = entry.getValue();
            int size = piece.getSize();

            if (size > biggestPieceSize) {
                biggestPieceSize = size;
                biggestPieceIndex = entry.getKey();
            }
        }
        this.biggestPieceIndex = biggestPieceIndex;
    }

    @Override
    public void run() {
//        LOGGER.info("Thread id: " + Thread.currentThread().threadId());
        if (!pieces.isEmpty()) {
            Piece biggestPiece = this.pieces.remove(biggestPieceIndex);

            Set<Coordinate> legalCoordinates = solvingPuzzle.getLegalCoordinates(biggestPiece);

            for (Coordinate coordinate : legalCoordinates) {
                Puzzle newPuzzle = solvingPuzzle.apply(biggestPiece, coordinate);

//                LOGGER.info("Thread id " + Thread.currentThread().threadId() + ": " +
//                        "\nOld puzzle: \n" + solvingPuzzle +
//                        "\nPiece applied: \n" + biggestPiece +
//                        "\nNew puzzle: \n" + newPuzzle +
//                        "\nPieces left: " + this.pieces.size());
                if (newPuzzle.isSolvable(this.pieces.values())) {
                    Coordinate[] newCoordinates = cloneCoordinatesArray();
                    newCoordinates[biggestPieceIndex] = coordinate;
                    Map<Integer, Piece> clonedPieces = new LinkedHashMap<>(this.pieces);
                    Worker worker = new Worker(newPuzzle, clonedPieces, newCoordinates);
                    EXECUTOR_SERVICE.submit(worker);
                }
            }
        } else {
            if (solvingPuzzle.isSolved()) {
                synchronized (Worker.class) {
                    EXECUTOR_SERVICE.shutdownNow();
                    LOGGER.info("Thread id: " + Thread.currentThread().threadId() + ": PUZZLE SOLVED!");

                    for (Coordinate coordinate : currentCoordinates)
                        System.out.print(coordinate + " ");
                }
            }
        }
    }

    Coordinate[] cloneCoordinatesArray() {
        Coordinate[] newCoordinates = new Coordinate[this.currentCoordinates.length];
        System.arraycopy(this.currentCoordinates, 0, newCoordinates, 0, this.currentCoordinates.length);
        return newCoordinates;
    }

}
