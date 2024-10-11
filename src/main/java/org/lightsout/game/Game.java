package org.lightsout.game;

import org.lightsout.component.Piece;
import org.lightsout.component.Puzzle;
import org.lightsout.model.Coordinate;
import org.lightsout.model.PieceInfo;

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
        pieces = parsePieces(line3, puzzle);
        piecesSize = pieces.size();
    }

    /**
     * First finds a suitable piece that has a good number of legal coordinates,
     * Then parallel threads start applying it to different coordinates.
     */
    public void solve() {
        int count = 0;
        for (PieceInfo pieceInfo : pieces) {
            count += pieceInfo.piece().getAmountOfXs();
        }
        LOGGER.info("Pieces size: " + piecesSize);
        LOGGER.info("Puzzle remainder to 0: " + puzzle.getTotalRemainderTo0());
        LOGGER.info("Total Xs: " + count);
        if (puzzle.isSolved() && piecesSize == 0) return;

        if (piecesSize == 1) {
            solveSinglePiecePuzzle();
            return;
        }

        // Then at least 2 pieces are present
        PieceInfo pieceInfo = findSuitablePieceIndex();
//        PieceInfo pieceInfo = pieces.get(10);
        pieces.remove(pieceInfo);
        piecesSize--;

        Piece piece = pieceInfo.piece();
        int pieceCoordinateOutputIndex = pieceInfo.coordinateOutputIndex();
        List<Coordinate> coordinates = pieceInfo.legalCoordinates();

        LOGGER.info("Coordinates size: " + coordinates.size());
        try {
            for (Coordinate coordinate : coordinates) {
                Puzzle newPuzzle = puzzle.apply(piece, coordinate);

                boolean isSolvable = newPuzzle.isSolvable(pieces);
                if (isSolvable) {
                    Coordinate[] coordinateIndexes = new Coordinate[piecesSize + 1];
                    coordinateIndexes[pieceCoordinateOutputIndex] = coordinate;
                    Worker worker = new Worker(newPuzzle, coordinateIndexes, 0);
                    EXECUTOR_SERVICE.submit(worker);
                }
            }
        } finally {
            long ms = System.currentTimeMillis();
            EXECUTOR_SERVICE.shutdown();

            try {
                if (!EXECUTOR_SERVICE.awaitTermination(30, TimeUnit.SECONDS)) {
                    LOGGER.warning("Executor service did not terminate in time, forcing shutdown");
                    EXECUTOR_SERVICE.shutdownNow();
                }
            } catch (InterruptedException ex) {
                LOGGER.severe("Executor service interrupted, forcing shutdown");
                EXECUTOR_SERVICE.shutdownNow();
                Thread.currentThread().interrupt();
            }
            LOGGER.info("Solving took " + (System.currentTimeMillis() - ms) / 1000 + " seconds");
        }
    }

    private void solveSinglePiecePuzzle() {
        Piece piece = pieces.getFirst().piece();
        List<Coordinate> legalCoordinates = puzzle.getLegalCoordinates(piece);

        for (Coordinate coordinate : legalCoordinates) {
            Puzzle newPuzzle = this.puzzle.apply(piece, coordinate);

            if (newPuzzle.isSolved()) {
                System.out.println(coordinate);
                return;
            }
        }
    }

    /**
     * Finds a suitable piece to start the puzzle with.
     * It will be applied in different threads.
     *
     * @return a suitable piece to start the puzzle with
     */
    private PieceInfo findSuitablePieceIndex() {
        for (int i = THREAD_POOL_SIZE; i > 0; i--) {
            for (PieceInfo pieceInfo : pieces) {
                if (pieceInfo.legalCoordinates().size() == i) {
                    return pieceInfo;
                }
            }
        }

        // then get the piece with the least number of legal coordinates
        return pieces.stream()
                .min(Comparator.comparingInt(pieceInfo -> pieceInfo.legalCoordinates().size()))
                .orElseThrow(() -> new NoSuchElementException("No pieces available"));
    }

    private List<PieceInfo> parsePieces(String line3, Puzzle puzzle) {
        String[] piecesStr = line3.split(" ");
        List<PieceInfo> pieces = new ArrayList<>();

        for (int i = 0; i < piecesStr.length; i++) {
            Piece piece = new Piece(piecesStr[i]);
            List<Coordinate> legalCoordinates = puzzle.getLegalCoordinates(piece);
            pieces.add(new PieceInfo(piece, i, legalCoordinates));
        }

        // Sort by amountOfXs() in descending order
        pieces.sort((p1, p2) -> Integer.compare(p2.piece().getAmountOfXs(), p1.piece().getAmountOfXs()));

        return pieces;
    }

}
