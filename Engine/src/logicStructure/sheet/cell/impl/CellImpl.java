package logicStructure.sheet.cell.impl;

import logicStructure.expression.api.Funcion;
import logicStructure.expression.api.RangeBasedExpression;
import logicStructure.sheet.cell.api.CellType;
import logicStructure.sheet.cell.api.NegativeFunctionType;
import logicStructure.sheet.coordinate.CoordinateFactory;
import logicStructure.sheet.impl.Edge;
import logicStructure.sheet.impl.SheetImpl;
import logicStructure.expression.api.Expression;
import logicStructure.expression.impl.operation.Ref;
import logicStructure.expression.parser.Convertor;
import logicStructure.sheet.cell.api.Cell;
import logicStructure.sheet.cell.api.EffectiveValue;
import logicStructure.sheet.coordinate.Coordinate;
import logicStructure.sheet.range.Range;
import logicStructure.sheet.range.RangeFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CellImpl implements Cell, Cloneable, Serializable {

    private final Coordinate coordinate;
    private String originalValue;
    private EffectiveValue effectiveValue;
    private int version;
    private List<Coordinate> dependsOn;
    private List<Coordinate> influencingOn;
    private int rowSize;
    private int colSize;

    public CellImpl(Coordinate coordinate, String originalValue, int version, int rowSize, int colSize) {
        this.coordinate = coordinate;
        this.originalValue = originalValue;
        this.version = version;
        this.dependsOn = new ArrayList<Coordinate>();
        this.influencingOn = new ArrayList<Coordinate>();
        this.rowSize = rowSize;
        this.colSize = colSize;
    }

    @Override
    public Coordinate getCoordinate() {
        return coordinate;
    }

    @Override
    public String getOriginalValue() {
        return originalValue;
    }

    @Override
    public EffectiveValue getEffectiveValue() {
        return effectiveValue;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public List<Coordinate> getDependsOn() {
        return dependsOn;
    }

    @Override
    public List<Coordinate> getInfluencingOn() {
        return influencingOn;
    }

    @Override
    public void addToDependsOn(Coordinate coordinate) {
        dependsOn.add(coordinate);
    }

    @Override
    public void addToInfluencingOn(Coordinate coordinate) {
        influencingOn.add(coordinate);
    }

    @Override
    public void calculateEffectiveValue(SheetImpl sheet) throws Exception {
        if(originalValue == null){
            effectiveValue = null;
            return;
        }

        Expression expression = Convertor.stringToExpression(originalValue, sheet, coordinate);
        if (expression instanceof Funcion){
            this.originalValue = originalValue.toUpperCase();
        }
        try {
            EffectiveValue newEffectiveValue = expression.evaluate();
            this.effectiveValue = newEffectiveValue;
        } catch (Exception e) {
            if(effectiveValue != null){
                switch (effectiveValue.getCellType()){
                    case CellType.DOUBLE, CellType.INTEGER -> this.effectiveValue = new EffectiveValueImpl(CellType.NEGATIVE, NegativeFunctionType.NAN);
                    case CellType.BOOLEAN -> this.effectiveValue = new EffectiveValueImpl(CellType.NEGATIVE, NegativeFunctionType.UNKNOWN);
                    default -> this.effectiveValue = new EffectiveValueImpl(CellType.NEGATIVE, NegativeFunctionType.UNDEFINED);
                }
            }
        }

        if (expression instanceof Ref) {
            Coordinate refCoord = ((Ref) expression).getRefCoordinate();
            updateDependsOn(sheet, refCoord);
//            if (!dependsOn.contains(refCoord)) {
//                dependsOn.add(refCoord);
//            }
//            if(sheet.getCells().containsKey(refCoord)){
//                Cell refCell = sheet.getCellByCoordinate(refCoord);
//                if (!refCell.getInfluencingOn().contains(this.coordinate)) {
//                    refCell.addToInfluencingOn(this.coordinate);
//                }
//            }
        }
        if (expression instanceof RangeBasedExpression) {
            String rangeName = ((RangeBasedExpression) expression).getRangeName();
            RangeFactory.getRange(rangeName).setIsUsedInFunction(true);
            List<Coordinate> rangeCoordinateList = ((RangeBasedExpression) expression).getRangeCellsCoordinates();
            for(Coordinate rangeCoordinate : rangeCoordinateList){
                updateDependsOn(sheet, rangeCoordinate);
            }
        }
    }

    private void updateDependsOn(SheetImpl sheet, Coordinate coordinate) {
        if (!dependsOn.contains(coordinate)) {
            dependsOn.add(coordinate);
        }
        if(sheet.getCells().containsKey(coordinate)){
            Cell refCell = sheet.getCellByCoordinate(coordinate);
            if (!refCell.getInfluencingOn().contains(this.coordinate)) {
                refCell.addToInfluencingOn(this.coordinate);
            }
        }
    }

    @Override
    public void setOriginalValue(String newValue, SheetImpl sheet) throws Exception {
        this.originalValue = newValue;
        removeOldDependencies(sheet);
        sheet.updateCells();
    }

    @Override
    public void setEffectiveValueAsOriginal(){
        this.effectiveValue = new EffectiveValueImpl(CellType.STRING, originalValue);
    }

    @Override
    public void setVersion(Integer newVersion) {
       this.version = newVersion;
    }

    @Override
    public int compareCells(Cell other) {
        try {
            if (this.effectiveValue == null && other.getEffectiveValue() == null) {
                return 0;
            } else if (this.effectiveValue == null) {
                return 1;
            } else if (other.getEffectiveValue() == null) {
                return -1;
            }

            double value1 = this.effectiveValue.extractValueWithExpectation(Number.class).doubleValue();
            double value2 = other.getEffectiveValue().extractValueWithExpectation(Number.class).doubleValue();
            return Double.compare(value1, value2);
        } catch (NumberFormatException | NullPointerException e) {
            if (this.effectiveValue == null && other.getEffectiveValue() == null) {
                return 0;
            } else if (this.effectiveValue == null) {
                return 1;
            } else if (other.getEffectiveValue() == null) {
                return -1;
            }

            return this.effectiveValue.compareTo(other.getEffectiveValue());
        }
    }

    @Override
    public Cell clone() {
        Coordinate clonedCoordinate = CoordinateFactory.createCoordinate(coordinate.getRow(), coordinate.getColumn());
        String clonedOriginalValue = String.valueOf(originalValue);
        CellImpl clonedCell = new CellImpl(clonedCoordinate, clonedOriginalValue, version, rowSize, colSize);
        clonedCell.effectiveValue = effectiveValue == null ? null : effectiveValue.clone();

        clonedCell.dependsOn = new ArrayList<>();
        clonedCell.dependsOn.addAll(dependsOn);

        clonedCell.influencingOn = new ArrayList<>();
        clonedCell.influencingOn.addAll(influencingOn);

        return clonedCell;

    }

    private void removeOldDependencies(SheetImpl sheet) {
        for (Coordinate dependentCoord : dependsOn) {
            Cell dependentCell = sheet.getCellByCoordinate(dependentCoord);
            dependentCell.getInfluencingOn().remove(coordinate);
            sheet.removeEdge(new Edge(dependentCoord, coordinate));
        }
        dependsOn.clear();
    }
}
