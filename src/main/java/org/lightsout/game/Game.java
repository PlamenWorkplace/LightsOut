package org.lightsout.game;

import org.lightsout.component.Piece;
import org.lightsout.component.Puzzle;
import org.lightsout.model.Coordinate;
import org.lightsout.model.PieceInfo;
import org.lightsout.model.Solvability;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Game extends GameConfig {

    private static final int THREAD_POOL_SIZE = 4;
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    private final Puzzle puzzle;

    public Game(String line1, String line2, String line3) {
        this.puzzle = new Puzzle(line1, line2);
        pieces = parsePieces(line3);
        piecesSize = pieces.size();
    }

    /**
     * First finds a suitable piece that has a good number of legal coordinates,
     * Then parallel threads start applying it to different coordinates.
     */
    public void solve() {
        if (puzzle.isSolved() && piecesSize == 0) return;

        if (piecesSize == 1) {
            solveSinglePiecePuzzle();
            return;
        }
        // Then at least 2 pieces are present
        Map.Entry<Integer, Set<Coordinate>> indexCoordinatesMap = findSuitablePiece();
        int pieceArrayIndex = indexCoordinatesMap.getKey();
        Set<Coordinate> coordinates = indexCoordinatesMap.getValue();

        PieceInfo pieceInfo = pieces.remove(pieceArrayIndex);
        piecesSize--;

        Piece piece = pieceInfo.piece();
        int pieceCoordinateOutputIndex = pieceInfo.coordinateOutputIndex();

        try {
            for (Coordinate coordinate : coordinates) {
                Puzzle newPuzzle = puzzle.apply(piece, coordinate);

                Solvability solvability = newPuzzle.isSolvable(pieces);
                if (solvability.isMathPossible() && solvability.sufficientXs()) {
                    Coordinate[] newCoordinateIndexes = new Coordinate[piecesSize + 1]; // to account for the removed piece
                    newCoordinateIndexes[pieceCoordinateOutputIndex] = coordinate;
                    Worker worker = new Worker(newPuzzle, newCoordinateIndexes, 0);
                    EXECUTOR_SERVICE.submit(worker);
                } else if (!solvability.sufficientXs()) {
                    // next coordinates won't have sufficient Xs either
                    break;
                }
            }
        } finally {
            EXECUTOR_SERVICE.shutdown();

            try {
                if (!EXECUTOR_SERVICE.awaitTermination(5, TimeUnit.SECONDS)) {
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

    private void solveSinglePiecePuzzle() {
        Piece piece = pieces.getFirst().piece();
        Set<Coordinate> legalCoordinates = puzzle.getLegalCoordinates(piece);

        for (Coordinate coordinate : legalCoordinates) {
            Puzzle newPuzzle = this.puzzle.apply(piece, coordinate);

            if (newPuzzle.isSolved()) {
                System.out.println(coordinate);
                return;
            }
        }
    }

    private Map.Entry<Integer, Set<Coordinate>> findSuitablePiece() {
        Optional<Map.Entry<Integer, Set<Coordinate>>> indexCoordinatesMap =
                findPieceInRange(2 * THREAD_POOL_SIZE, 3 * THREAD_POOL_SIZE)
                .or(() -> findPieceInRange(THREAD_POOL_SIZE, 2 * THREAD_POOL_SIZE))
                .or(() -> findPieceInRange(0, THREAD_POOL_SIZE));

        if (indexCoordinatesMap.isPresent()) {
            return indexCoordinatesMap.get();
        } else {
            // get the biggest piece
            int pieceArrayIndex = 0;
            PieceInfo pieceInfo = pieces.get(pieceArrayIndex);
            Set<Coordinate> coordinates = puzzle.getLegalCoordinates(pieceInfo.piece());
            return Map.entry(pieceArrayIndex, coordinates);
        }
    }

    /**
     * Finds a piece depending on how many coordinates it can be applied to.
     *
     * @param minSize the minimum bound for coordinate amount
     * @param maxSize the maximum bound for coordinate amount
     * @return the piece that satisfied the above-mentioned constraints
     */
    private Optional<Map.Entry<Integer, Set<Coordinate>>> findPieceInRange(int minSize, int maxSize) {
        for (int i = 0; i < piecesSize; i++) {
            PieceInfo pieceInfo = pieces.get(i);
            Set<Coordinate> legalCoordinates = puzzle.getLegalCoordinates(pieceInfo.piece());
            int coordinatesSize = legalCoordinates.size();

            if (coordinatesSize >= minSize && coordinatesSize < maxSize) {
                return Optional.of(Map.entry(i, legalCoordinates));
            }
        }

        return Optional.empty();
    }

    private List<PieceInfo> parsePieces(String line3) {
        String[] piecesStr = line3.split(" ");
        List<PieceInfo> pieces = new ArrayList<>();

        for (int i = 0; i < piecesStr.length; i++) {
            pieces.add(new PieceInfo(new Piece(piecesStr[i]), i));
        }

        // Sort by amountOfXs() in descending order
        pieces.sort((p1, p2) -> Integer.compare(p2.piece().getAmountOfXs(), p1.piece().getAmountOfXs()));

        return pieces;
    }

}
