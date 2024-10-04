package logicStructure.specialException;

import logicStructure.sheet.coordinate.Coordinate;
import java.util.List;
import java.util.stream.Collectors;

public class CircularDependencyException extends RuntimeException  {
    public CircularDependencyException(String message) {
        super(message);
    }

    public CircularDependencyException(String message, List<Coordinate> cycle) {
        super(message + ": " + cycle.stream()
                .map(Coordinate::createCellCoordinateString)
                .collect(Collectors.joining(" -> ")));
    }
}
