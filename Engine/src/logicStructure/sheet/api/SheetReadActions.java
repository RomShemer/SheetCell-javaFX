package logicStructure.sheet.api;

import logicStructure.sheet.cell.api.EffectiveValue;
import logicStructure.sheet.cell.api.Cell;
import logicStructure.sheet.coordinate.Coordinate;
import logicStructure.sheet.range.Range;

import java.util.Map;

public interface SheetReadActions {
    String getSheetName();
    int getVersion();
    int getNumOfCols();
    int getNumOfRows();
    Coordinate getMinCoordinateInSheet();
    Coordinate getMaxCoordinateInSheet();
    Cell getCellByCoordinate(Coordinate coordinate);
    EffectiveValue getCellValueByCoordinate(Coordinate cellCoordinate);
    Map<Coordinate,Cell> getCells();
    int getRowSize();
    int getColumnSize();
    Map<String, Range> getRanges();
}
