package org.lightsout.game;

import org.lightsout.component.Piece;
import org.lightsout.component.Puzzle;
import org.lightsout.model.Coordinate;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class Worker implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Worker.class.getName());
    private static boolean FINISHED = false;

    private final Puzzle solvingPuzzle;
    private final Map<Integer, Piece> pieces;
    private final Coordinate[] currentCoordinates;

    Worker(Puzzle solvingPuzzle, Map<Integer, Piece> pieces, Coordinate[] currentCoordinates) {
        this.solvingPuzzle = solvingPuzzle;
        this.pieces = pieces;
        this.currentCoordinates = currentCoordinates;
    }

    public void run() {
        if (Thread.currentThread().isInterrupted() || FINISHED)
            return;

//        LOGGER.info("Thread id: " + Thread.currentThread().threadId());
        Iterator<Map.Entry<Integer, Piece>> iterator = this.pieces.entrySet().iterator();

        if (iterator.hasNext()) {
            Map.Entry<Integer, Piece> nextEntry = iterator.next();
            int biggestPieceIndex = nextEntry.getKey();
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
                    worker.run();
                }
            }
        } else {
            if (solvingPuzzle.isSolved()) {
                synchronized (Worker.this) {
                    if (!FINISHED) {
                        FINISHED = true;
                        for (Coordinate coordinate : currentCoordinates)
                            System.out.print(coordinate + " ");
                    }
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
