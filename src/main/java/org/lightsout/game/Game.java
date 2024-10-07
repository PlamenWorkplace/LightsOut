package org.lightsout.game;

import org.lightsout.component.Piece;
import org.lightsout.component.Puzzle;
import org.lightsout.model.Coordinate;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Game {

    private static final Logger LOGGER = Logger.getLogger(Game.class.getName());

    private final Puzzle puzzle;
    private final Map<Integer, Piece> pieces;
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(3);

    public Game(String line1, String line2, String line3) {
        this.puzzle = new Puzzle(line1, line2);
        Map<Integer, Piece> pieces = new HashMap<>();
        String[] piecesStr = line3.split(" ");

        for (int i = 0; i < piecesStr.length; i++) {
            Piece piece = new Piece(piecesStr[i]);
            pieces.put(i, piece);
        }

        this.pieces = pieces.entrySet()
                .stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().getSize(), e1.getValue().getSize())) // Sort by size, desc
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new // Use LinkedHashMap to preserve the order
                ));
    }

    public void solve() {
        Map.Entry<Integer, Set<Coordinate>> tuple = findSuitablePiece();
        int pieceIndex = tuple.getKey();
        Piece piece = this.pieces.remove(pieceIndex);

        try {
            for (Coordinate coordinate : tuple.getValue()) {
                Puzzle newPuzzle = puzzle.apply(piece, coordinate);

                if (newPuzzle.isSolvable(this.pieces.values())) {
                    Coordinate[] newCoordinates = new Coordinate[pieces.size() + 1];
                    newCoordinates[pieceIndex] = coordinate;
                    Map<Integer, Piece> clonedPieces = new LinkedHashMap<>(this.pieces);
                    Worker worker = new Worker(newPuzzle, clonedPieces, newCoordinates);
                    EXECUTOR_SERVICE.submit(worker);
                }
            }
        } finally {
            // Shutdown the executor after submitting tasks
            EXECUTOR_SERVICE.shutdown();
            LOGGER.info("Shutdown called");
            try {
                // Wait for all tasks to finish (optional)
                if (!EXECUTOR_SERVICE.awaitTermination(60, TimeUnit.SECONDS)) {
                    LOGGER.info("ShutdownNow called");
                    EXECUTOR_SERVICE.shutdownNow(); // Force shutdown if timeout occurs
                }
            } catch (InterruptedException ex) {
                LOGGER.severe("Interrupted");
                EXECUTOR_SERVICE.shutdownNow(); // Force shutdown in case of interruption
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
        }

    }

    private Map.Entry<Integer, Set<Coordinate>> findSuitablePiece() {
        return findPieceInRange(8, 12)
                .or(() -> findPieceInRange(4, 8))
                .or(() -> findPieceInRange(0, 4))
                .orElseThrow(() -> new AssertionError("No suitable piece found"));
    }

    private Optional<Map.Entry<Integer, Set<Coordinate>>> findPieceInRange(int minSize, int maxSize) {
        for (Map.Entry<Integer, Piece> entry : this.pieces.entrySet()) {
            Set<Coordinate> legalCoordinates = puzzle.getLegalCoordinates(entry.getValue());
            if (legalCoordinates.size() >= minSize && legalCoordinates.size() < maxSize) {
                return Optional.of(Map.entry(entry.getKey(), legalCoordinates));
            }
        }
        return Optional.empty();
    }

}
