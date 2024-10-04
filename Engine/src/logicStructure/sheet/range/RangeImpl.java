package logicStructure.sheet.range;

import javafx.fxml.FXML;
import logicStructure.sheet.DTO.CoordinateDTO;
import logicStructure.sheet.coordinate.Coordinate;
import logicStructure.sheet.coordinate.CoordinateFactory;

import java.util.ArrayList;
import java.util.List;

public class RangeImpl implements Range{
    private String name;
    private Coordinate topLeft;
    private Coordinate bottomRight;
    private List<Coordinate> cellsInRange;
    private Boolean isUsedInFunction = false;

    public RangeImpl(String name, Coordinate topLeft, Coordinate bottomRight) {
        this.name = name.toUpperCase();
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        this.cellsInRange = calculateCellsInRange(topLeft, bottomRight);
    }

    @Override
    public String getName() {
        return name.toUpperCase();
    }

    @Override
    public Coordinate getStartCoordinate() {
        return topLeft;
    }

    @Override
    public Coordinate getEndCoordinate() {
        return bottomRight;
    }

    @Override
    public List<Coordinate> getCellsInRange() {
        return cellsInRange;
    }

    @Override
    public Boolean getIsUsedInFunction(){
        return this.isUsedInFunction;
    }

    @Override
    public void setIsUsedInFunction(boolean isUsedInFunction){
        this.isUsedInFunction = isUsedInFunction;
    }

    // Calculate all cells within the range
    @Override
    public List<Coordinate> calculateCellsInRange(Coordinate topLeft, Coordinate bottomRight) {
        List<Coordinate> cells = new ArrayList<>();
        for (int row = topLeft.getRow(); row <= bottomRight.getRow(); row++) {
            for (int col = topLeft.getColumn(); col <= bottomRight.getColumn(); col++) {
                cells.add(CoordinateFactory.createCoordinate(row, col));
            }
        }

        return cells;
    }
}
