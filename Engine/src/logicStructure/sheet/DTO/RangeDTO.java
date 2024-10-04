package logicStructure.sheet.DTO;

import logicStructure.sheet.coordinate.Coordinate;
import logicStructure.sheet.coordinate.CoordinateFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RangeDTO {
    private String name;
    private CoordinateDTO topLeft;
    private CoordinateDTO bottomRight;
    private List<CoordinateDTO> cellsInRange;
    private Boolean isUsedInFunction = false;

    public RangeDTO(String name, CoordinateDTO topLeft, CoordinateDTO bottomRight) {
        this.name = name.toUpperCase();
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        this.cellsInRange = calculateCellsInRange(topLeft, bottomRight);
    }

    public String getName() {
        return name.toUpperCase();
    }

    public CoordinateDTO getStartCoordinate() {
        return topLeft;
    }

    public CoordinateDTO getEndCoordinate() {
        return bottomRight;
    }

    public List<CoordinateDTO> getCellsInRange() {
        return cellsInRange;
    }

    public Boolean getIsUsedInFunction(){
        return this.isUsedInFunction;
    }

    public void setIsUsedInFunction(boolean isUsedInFunction){
        this.isUsedInFunction = isUsedInFunction;
    }

    // Calculate all cells within the range
    public List<CoordinateDTO> calculateCellsInRange(CoordinateDTO topLeft, CoordinateDTO bottomRight) {
        List<CoordinateDTO> cells = new ArrayList<>();
        for (int row = topLeft.getRow(); row <= bottomRight.getRow(); row++) {
            for (int col = topLeft.getColumn(); col <= bottomRight.getColumn(); col++) {
                cells.add(SheetMapper.toCoordinateDTO(row, col));
            }
        }

        return cells;
    }

    public List<String> getColumnNamesListInRange(){
        List<String> columnNames = new ArrayList<>();

        int startColumn = topLeft.getColumn();
        int endColumn = bottomRight.getColumn();

        for (int col = startColumn; col <= endColumn; col++) {
            columnNames.add(String.valueOf((char) ('A' + (col-1))));
        }

        Collections.sort(columnNames);

        return columnNames;
    }
}
