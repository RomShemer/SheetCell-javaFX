package logicStructure.expression.api;

import logicStructure.sheet.coordinate.Coordinate;

import java.util.List;

public interface RangeBasedExpression {
    List<Coordinate> getRangeCellsCoordinates();
    String getRangeName();
}
