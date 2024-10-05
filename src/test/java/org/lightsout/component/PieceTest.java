package org.lightsout.component;

import org.junit.jupiter.api.Test;
import org.lightsout.model.Coordinate;

import static org.junit.jupiter.api.Assertions.*;

class PieceTest {

    @Test
    void testLoadPiece() {
        Piece piece = new Piece(".XX,XX.");
        assertEquals(piece.getCellValue(Coordinate.of(0, 0)), '.');
        assertEquals(piece.getCellValue(Coordinate.of(0, 1)), 'X');
        assertEquals(piece.getCellValue(Coordinate.of(0, 2)), 'X');
        assertEquals(piece.getCellValue(Coordinate.of(1, 0)), 'X');
        assertEquals(piece.getCellValue(Coordinate.of(1, 1)), 'X');
        assertEquals(piece.getCellValue(Coordinate.of(1, 2)), '.');
        assertEquals(piece.getWidth(), 3);
        assertEquals(piece.getHeight(), 2);
    }

    @Test
    void testAmountOfXs() {
        Piece piece1 = new Piece(".XX,XX.");
        assertEquals(piece1.getAmountOfXs(), 4);

        Piece piece2 = new Piece(".X,XX,.X,X.");
        assertEquals(piece2.getAmountOfXs(), 5);
    }

}