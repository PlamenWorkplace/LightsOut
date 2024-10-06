package org.lightsout.game;

import org.lightsout.component.Piece;
import org.lightsout.component.Puzzle;
import org.lightsout.model.Coordinate;

import java.util.LinkedHashMap;
import java.util.Map;

public class Game {

    private final Puzzle puzzle;
    private final Map<Integer, Piece> pieces;

    public Game(String line1, String line2, String line3) {
        this.puzzle = new Puzzle(line1, line2);
        Map<Integer, Piece> pieces = new LinkedHashMap<>();
        String[] piecesStr = line3.split(" ");

        for (int i = 0; i < piecesStr.length; i++) {
            Piece piece = new Piece(piecesStr[i]);
            pieces.put(i, piece);
        }
        this.pieces = pieces;
    }

    public void solve() {
        Coordinate[] coordinates = new Coordinate[pieces.size()];
        Worker worker = new Worker(this.puzzle, pieces, coordinates);
        worker.run();
    }

}
