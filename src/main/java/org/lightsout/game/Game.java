package org.lightsout.game;

import org.lightsout.component.Piece;
import org.lightsout.component.Puzzle;
import org.lightsout.model.Coordinate;
import org.lightsout.model.Solvability;

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
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(4);

    public Game(String line1, String line2, String line3) {
        this.puzzle = new Puzzle(line1, line2);
        this.pieces = parsePieces(line3);
    }

    /**
     * First finds a suitable piece that has a good number of legal coordinates,
     * Then different threads start applying it to different coordinates.
     */
    public void solve() {
        Map.Entry<Integer, Set<Coordinate>> suitablePiece = findSuitablePiece();

        int pieceIndex;
        Set<Coordinate> coordinates;

        if (suitablePiece == null) {
            Map.Entry<Integer, Piece> entry = pieces.entrySet().iterator().next();
            pieceIndex = entry.getKey();
            coordinates = puzzle.getLegalCoordinates(entry.getValue());
        } else {
            pieceIndex = suitablePiece.getKey();
            coordinates = suitablePiece.getValue();
        }

        Piece piece = this.pieces.remove(pieceIndex);

        try {
            for (Coordinate coordinate : coordinates) {
                Puzzle newPuzzle = puzzle.apply(piece, coordinate);

                Solvability solvability = newPuzzle.isSolvable(this.pieces.values());
                if (solvability.isMathPossible() && solvability.sufficientXs()) {
                    Coordinate[] newCoordinates = new Coordinate[pieces.size() + 1];
                    newCoordinates[pieceIndex] = coordinate;
                    Map<Integer, Piece> clonedPieces = new LinkedHashMap<>(this.pieces);
                    Worker worker = new Worker(newPuzzle, clonedPieces, newCoordinates);
                    EXECUTOR_SERVICE.submit(worker);
                } else if (!solvability.sufficientXs()) {
                    // next coordinates won't have sufficient Xs either
                    break;
                }
            }
        } finally {
            EXECUTOR_SERVICE.shutdown();

            try {
                if (!EXECUTOR_SERVICE.awaitTermination(20, TimeUnit.SECONDS)) {
                    LOGGER.warning("Executor service did not terminate in time, forcing shutdown");
                    EXECUTOR_SERVICE.shutdownNow();
                }
            } catch (InterruptedException ex) {
                LOGGER.severe("Executor service interrupted, forcing shutdown");
                EXECUTOR_SERVICE.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private Map.Entry<Integer, Set<Coordinate>> findSuitablePiece() {
        return findPieceInRange(8, 12)
                .or(() -> findPieceInRange(4, 8))
                .or(() -> findPieceInRange(0, 4))
                .orElse(null);
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

    private Map<Integer, Piece> parsePieces(String line3) {
        String[] piecesStr = line3.split(" ");
        Map<Integer, Piece> pieces = new HashMap<>();

        for (int i = 0; i < piecesStr.length; i++) {
            pieces.put(i, new Piece(piecesStr[i]));
        }

        return pieces.entrySet()
                .stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().getAmountOfXs(), e1.getValue().getAmountOfXs())) // Sort by Xs count, desc
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new // Preserve order
                ));
    }

}
