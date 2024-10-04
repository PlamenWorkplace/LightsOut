package org.lightsout.component;

import org.junit.jupiter.api.Test;
import org.lightsout.model.Coordinate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class PuzzleTest {

    @Test
    void testLoadPuzzle3x3() throws CloneNotSupportedException {
        Puzzle puzzle = new Puzzle("100,101,011");
        assertEquals(puzzle.getCellValue(Coordinate.of(0, 0)), 1);
        assertEquals(puzzle.getCellValue(Coordinate.of(0, 1)), 0);
        assertEquals(puzzle.getCellValue(Coordinate.of(0, 2)), 0);
        assertEquals(puzzle.getCellValue(Coordinate.of(1, 0)), 1);
        assertEquals(puzzle.getCellValue(Coordinate.of(1, 1)), 0);
        assertEquals(puzzle.getCellValue(Coordinate.of(1, 2)), 1);
        assertEquals(puzzle.getCellValue(Coordinate.of(2, 0)), 0);
        assertEquals(puzzle.getCellValue(Coordinate.of(2, 1)), 1);
        assertEquals(puzzle.getCellValue(Coordinate.of(2, 2)), 1);
        assertEquals(puzzle.getDimension(), 3);
        assertNotEquals(puzzle, puzzle.clone());
    }

}
