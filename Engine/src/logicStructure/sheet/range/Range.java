package logicStructure.sheet.range;

import logicStructure.sheet.coordinate.Coordinate;
import java.util.List;

public interface Range {
    String getName();

    Coordinate getStartCoordinate();

    Coordinate getEndCoordinate();
    
    List<Coordinate> getCellsInRange();

    Boolean getIsUsedInFunction();

    void setIsUsedInFunction(boolean isUsedInFunction);

    List<Coordinate> calculateCellsInRange(Coordinate topLeft, Coordinate bottomRight);
}
