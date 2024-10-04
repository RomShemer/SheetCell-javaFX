package logicStructure.sheet.api;

import logicStructure.sheet.DTO.RangeDTO;
import logicStructure.sheet.DTO.SheetDTO;
import logicStructure.sheet.cell.api.Cell;
import logicStructure.sheet.coordinate.Coordinate;
import logicStructure.sheet.impl.Edge;
import logicStructure.sheet.impl.SheetImpl;
import logicStructure.sheet.range.Range;
import logicStructure.sheet.version.SheetVersionInfo;

import java.util.List;

public interface SheetUpdateActions {
    SheetVersionInfo setCellOriginalValueByCoordinate(String cellID, String newValue) throws Exception;
    void addNewCell(String cellID, String originalValue, int version, int rowSize, int colSize) throws Exception;
    void addNewCell(Cell cell) throws Exception;
    void addEdge(Edge edge);
    void removeEdge(Edge edge);
    void updateCells() throws Exception;
    void increaseVersion();
    void deleteRange(String name);
    void addRange(String name, String from, String to) throws Exception;
    void addRange(Range range);

    //?
    SheetDTO sortRows(RangeDTO range, List<String> columnSortOrder) throws Exception;
    SheetImpl applyFilter(RangeDTO range, char selectedColumn, List<String> selectedValues) throws Exception;
}
