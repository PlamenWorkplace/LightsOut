package org.lightsout.game;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lightsout.component.Piece;
import org.lightsout.component.Puzzle;
import org.lightsout.model.Coordinate;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WorkerTest {

    private static Worker worker;
    private static Map<Integer, Piece> pieces;
    private static Coordinate[] coordinates;

    @BeforeAll
    static void setUp() {
        Puzzle puzzle = new Puzzle("2", "100,101,011");

        pieces = new LinkedHashMap<>();
        String[] piecesStr = "..X,XXX,X.. X,X,X .X,XX XX.,.X.,.XX XX,X. XX .XX,XX.".split(" ");

        for (int i = 0; i < piecesStr.length; i++) {
            Piece piece = new Piece(piecesStr[i]);
            pieces.put(i, piece);
        }

        coordinates = new Coordinate[pieces.size()];
        worker = new Worker(puzzle, pieces, coordinates);
    }

    @Test
    void cloneCoordinatesArray() {
        Coordinate[] newCoordinates = worker.cloneCoordinatesArray();
//        newCoordinates.remove(0);
        assertNotEquals(newCoordinates, coordinates);
    }
}