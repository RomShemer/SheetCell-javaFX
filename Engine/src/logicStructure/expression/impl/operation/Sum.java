package logicStructure.expression.impl.operation;

import logicStructure.expression.api.Expression;
import logicStructure.expression.api.Funcion;
import logicStructure.expression.api.RangeBasedExpression;
import logicStructure.expression.impl.UnaryExpression;
import logicStructure.sheet.cell.api.Cell;
import logicStructure.sheet.cell.api.CellType;
import logicStructure.sheet.cell.api.EffectiveValue;
import logicStructure.sheet.cell.impl.EffectiveValueImpl;
import logicStructure.sheet.coordinate.Coordinate;
import logicStructure.sheet.impl.Edge;
import logicStructure.sheet.impl.SheetImpl;
import logicStructure.sheet.range.Range;
import logicStructure.sheet.range.RangeFactory;

import java.util.ArrayList;
import java.util.List;

public class Sum extends UnaryExpression implements Funcion, RangeBasedExpression {

    private SheetImpl sheet = null;
    private Coordinate currentCoordinate;
    private Range range = null;

    public Sum(Expression expr, Coordinate currentCoordinate, SheetImpl sheet) {
        super(OperationName.SUM, expr);
        this.sheet = sheet;
        this.currentCoordinate = currentCoordinate;
    }

    public Sum(){
        super(OperationName.SUM);
    }

    @Override
    public EffectiveValue evaluate(EffectiveValue evaluate1) throws Exception {
        double result = 0;
        String rangeName = evaluate1.extractValueWithExpectation(String.class);

        if(RangeFactory.isRangeExist(rangeName)) {
            this.range = RangeFactory.getRange(rangeName);
            if(range != null) {
                for(Coordinate coordinate : range.getCellsInRange()){
                    Cell cell = sheet.getCellByCoordinate(coordinate);
                    if(cell == null || cell.getEffectiveValue() == null || cell.getEffectiveValue().getValue() == null){
                        continue;
                    }
                    if(cell.getEffectiveValue().getCellType().equals(CellType.INTEGER)) {
                        result += Double.valueOf(cell.getEffectiveValue().extractValueWithExpectation(Integer.class));
                    } else if (cell.getEffectiveValue().getCellType().equals(CellType.DOUBLE)) {
                        result += cell.getEffectiveValue().extractValueWithExpectation(Double.class);
                    } else {
                        continue;
                    }

                    sheet.addEdge(new Edge(coordinate, currentCoordinate));
                    updateInfluencingOn(sheet, currentCoordinate, coordinate);
                }
            }

            return new EffectiveValueImpl(CellType.DOUBLE, result);
        }
        else {
            throw new IllegalArgumentException(String.format("Unsupported operation: range %s does not exist.",rangeName));
        }
    }

    private void updateInfluencingOn(SheetImpl sheet, Coordinate currentCoordinate, Coordinate coordinateInRange) throws Exception {
        if (sheet.getCells().containsKey(coordinateInRange)){
            updateInfluencingOnAction(sheet, currentCoordinate, coordinateInRange);
        } else {
            try {
                sheet.setCellOriginalValueByCoordinate(coordinateInRange.createCellCoordinateString(),null);
                updateInfluencingOnAction(sheet, currentCoordinate, coordinateInRange);
            } catch (Exception e){
                String message = "Failed to update influencing cells on " + currentCoordinate.createCellCoordinateString() + "\n" + e.getMessage();
                throw new Exception(message,e);
            }
        }
    }

    private void updateInfluencingOnAction(SheetImpl sheet, Coordinate currentCoordinate, Coordinate coordinateInRange){
        List<Coordinate> refInfluencuingOnList = sheet.getCellByCoordinate(coordinateInRange).getInfluencingOn();
        if (!refInfluencuingOnList.contains(currentCoordinate)) {
            sheet.getCellByCoordinate(coordinateInRange).addToInfluencingOn(currentCoordinate);
            sheet.addEdge(new Edge(coordinateInRange, currentCoordinate));
        }
    }

    @Override
    public String getOperationName(){
        return OperationName.SUM.toString();
    }

    @Override
    public  Class<?> getExpressionType(){
        return CellType.DOUBLE.getType();
    }


    @Override
    public OperationName getOperationType(){
        return OperationName.SUM;
    }

    @Override
    //todo
    public List<Class> getRequiredExpressionTypesList(){
        List<Class> result = new ArrayList<>();
        result.add(Range.class);
        return result;
    }

    @Override
    public List<Coordinate> getRangeCellsCoordinates() {
        if(range == null) {
            return null;
        }

        return range.getCellsInRange();
    }

    @Override
    public String getRangeName(){
        if(range == null) {
            return null;
        }

        return range.getName();
    }
}
