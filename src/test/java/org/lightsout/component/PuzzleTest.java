package org.lightsout.component;

import org.junit.jupiter.api.Test;
import org.lightsout.model.Coordinate;
import org.lightsout.model.PieceInfo;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PuzzleTest {

    @Test
    void testLoadPuzzle3x3() {
        Puzzle puzzle = new Puzzle("2", "100,101,011");
        assertEquals(puzzle.getCellValue(Coordinate.of(0, 0)), 1);
        assertEquals(puzzle.getCellValue(Coordinate.of(0, 1)), 0);
        assertEquals(puzzle.getCellValue(Coordinate.of(0, 2)), 0);
        assertEquals(puzzle.getCellValue(Coordinate.of(1, 0)), 1);
        assertEquals(puzzle.getCellValue(Coordinate.of(1, 1)), 0);
        assertEquals(puzzle.getCellValue(Coordinate.of(1, 2)), 1);
        assertEquals(puzzle.getCellValue(Coordinate.of(2, 0)), 0);
        assertEquals(puzzle.getCellValue(Coordinate.of(2, 1)), 1);
        assertEquals(puzzle.getCellValue(Coordinate.of(2, 2)), 1);
    }

    @Test
    void testPuzzleIsCloned() throws CloneNotSupportedException {
        Puzzle puzzle = new Puzzle("2", "100,101,011");
        Puzzle clonedPuzzle = puzzle.clone();

        assertNotEquals(puzzle.getCells()[0], clonedPuzzle.getCells()[0]);
        assertNotEquals(puzzle.getCells()[1], clonedPuzzle.getCells()[1]);
        assertNotEquals(puzzle.getCells()[2], clonedPuzzle.getCells()[2]);
    }

    @Test
    void testPuzzleApplyPiece() {
        Puzzle puzzle = new Puzzle("2", "100,101,011");
        Piece piece = new Piece("X,X,X");

        Puzzle puzzle1 = puzzle.apply(piece, Coordinate.of(0, 0));
        assertEquals(puzzle1.getCellValue(Coordinate.of(0, 0)), 0);
        assertEquals(puzzle1.getCellValue(Coordinate.of(1, 0)), 0);
        assertEquals(puzzle1.getCellValue(Coordinate.of(2, 0)), 1);

        // Piece is out of the border
        Puzzle puzzle2 = puzzle.apply(piece, Coordinate.of(1, 0));
        assertNull(puzzle2);

        Puzzle puzzle3 = puzzle.apply(piece, Coordinate.of(100, 100));
        assertNull(puzzle3);
    }

    @Test
    void testPuzzleGetLegalCoordinates() {
        Puzzle puzzle = new Puzzle("2", "100,101,011");
        Piece piece = new Piece("X,X,X");

        List<Coordinate> legalCoordinates = puzzle.getLegalCoordinates(piece);

        assertEquals(legalCoordinates.size(), 3);
        assertTrue(legalCoordinates.contains(Coordinate.of(0, 0)));
        assertTrue(legalCoordinates.contains(Coordinate.of(0, 1)));
        assertTrue(legalCoordinates.contains(Coordinate.of(0, 2)));
    }

    @Test
    void testPuzzleIsSolvable() {
        Puzzle puzzle = new Puzzle("2", "011,000,100");
        List<PieceInfo> pieces = new ArrayList<>() {};
        Piece piece1 = new Piece("X,X,X");
        pieces.add(new PieceInfo(piece1, 1, null));
        Piece piece2 = new Piece(".X,XX");
        pieces.add(new PieceInfo(piece2, 2, null));
        Piece piece3 = new Piece("XX,X.");
        pieces.add(new PieceInfo(piece3, 3, null));
        Piece piece4 = new Piece("XX");
        pieces.add(new PieceInfo(piece4, 4, null));
        Piece piece5 = new Piece(".XX,XX.");
        pieces.add(new PieceInfo(piece5, 5, null));

        assertTrue(puzzle.isSolvable(pieces));

        pieces.add(new PieceInfo(new Piece("X"), 6, null));
        assertFalse(puzzle.isSolvable(pieces));
    }

    @Test
    void testPuzzleIsSolved() {
        Puzzle puzzle1 = new Puzzle("2", "000,000,000");
        assertTrue(puzzle1.isSolved());

        Puzzle puzzle2 = new Puzzle("2", "001,000,000");
        assertFalse(puzzle2.isSolved());
    }

    @Test
    void testGetTotalRemainderTo0() {
        Puzzle puzzle1 = new Puzzle("2", "011,000,100");
        int remainder1 = puzzle1.getTotalRemainderTo0();
        assertEquals(remainder1, 3);

        Puzzle puzzle2 = new Puzzle("3", "011,000,100");
        int remainder2 = puzzle2.getTotalRemainderTo0();
        assertEquals(remainder2, 6);
    }

}
