package org.lightsout.model;

import org.lightsout.component.Piece;

import java.util.List;

public record PieceInfo(Piece piece, int coordinateOutputIndex, List<Coordinate> legalCoordinates) {
}
