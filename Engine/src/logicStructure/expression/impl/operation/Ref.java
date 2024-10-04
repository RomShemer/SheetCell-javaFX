package logicStructure.expression.impl.operation;

import logicStructure.expression.api.Funcion;
import logicStructure.sheet.impl.SheetImpl;
import logicStructure.expression.api.Expression;
import logicStructure.expression.impl.UnaryExpression;
import logicStructure.sheet.cell.api.CellType;
import logicStructure.sheet.impl.Edge;
import logicStructure.sheet.cell.api.EffectiveValue;
import logicStructure.sheet.cell.api.Cell;
import logicStructure.sheet.coordinate.Coordinate;
import java.util.ArrayList;
import java.util.List;

public class Ref extends UnaryExpression implements Funcion {

    private final SheetImpl sheet;
    private Coordinate refCoordinate;
    private Edge edge;

    public Ref(Coordinate refCoordinate, SheetImpl sheet, Coordinate currentCoordinate) throws Exception {
        super(OperationName.REF,createRefExpression(refCoordinate, sheet));
        this.sheet = sheet;
        this.refCoordinate = refCoordinate;
        this.edge = new Edge(refCoordinate, currentCoordinate);
        sheet.addEdge(edge);
        updateInfluencingOn(sheet, currentCoordinate, refCoordinate);
    }

    public Ref(SheetImpl sheet){
        super(OperationName.REF);
        this.sheet = sheet;
    }

    private static void updateInfluencingOn(SheetImpl sheet, Coordinate currentCoordinate, Coordinate refCoordinate) throws Exception {
        if (sheet.getCells().containsKey(refCoordinate)){
            updateInfluencingOnAction(sheet, currentCoordinate, refCoordinate);
        } else {
            try {
                sheet.setCellOriginalValueByCoordinate(refCoordinate.createCellCoordinateString(),null);
                updateInfluencingOnAction(sheet, currentCoordinate, refCoordinate);
            } catch (Exception e){
                String message = "Failed to update influencing cells on " + currentCoordinate.createCellCoordinateString() + "\n" + e.getMessage();
                throw new Exception(message,e);
            }
        }
    }

    private static void updateInfluencingOnAction(SheetImpl sheet, Coordinate currentCoordinate, Coordinate refCoordinate){
        List<Coordinate> refInfluencuingOnList = sheet.getCellByCoordinate(refCoordinate).getInfluencingOn();
        if (!refInfluencuingOnList.contains(currentCoordinate)) {
            sheet.getCellByCoordinate(refCoordinate).addToInfluencingOn(currentCoordinate);
            sheet.addEdge(new Edge(refCoordinate, currentCoordinate));
        }
    }

    private static String getRefOriginalValue(Coordinate refCoordinate, SheetImpl sheet) {
        return sheet.getCellByCoordinate(refCoordinate).getOriginalValue();
    }

    private static Expression createRefExpression(Coordinate refCoordinate, SheetImpl sheet) {
        try {
            EffectiveValue coordinateEffectiveValue = sheet.getCellValueByCoordinate(refCoordinate);
            CellType cellType = coordinateEffectiveValue.getCellType();

            switch (cellType) {
                case STRING -> {
                    return new LeafExpression(coordinateEffectiveValue.extractValueWithExpectation(String.class));
                }
                case DOUBLE -> {
                    return new LeafExpression(coordinateEffectiveValue.extractValueWithExpectation(Double.class));
                }
                case INTEGER -> {
                    return new LeafExpression(coordinateEffectiveValue.extractValueWithExpectation(Integer.class));
                }
                case BOOLEAN -> {
                    return new LeafExpression(coordinateEffectiveValue.extractValueWithExpectation(Boolean.class));
                }
                default -> throw new IllegalStateException("Unexpected cell type: " + cellType);
            }
        }
        catch (NullPointerException e) {
            return new LeafExpression((String) null);
        }
    }

    @Override
    public EffectiveValue evaluate(EffectiveValue evaluate1)
    {
        return sheet.getCellValueByCoordinate(evaluate1.extractValueWithExpectation(Coordinate.class));
    }

    @Override
    public String getOperationName(){
        return OperationName.REF.toString();
    }

    @Override
    public OperationName getOperationType(){
        return OperationName.REF;
    }

    @Override
    public Class<?> getExpressionType(){
        return CellType.CELL_COORDINATE.getType();
    }

    @Override
    public List<Class> getRequiredExpressionTypesList(){
        List<Class> result = new ArrayList<>();
        result.add(Coordinate.class);
        return result;
    }

    public Coordinate getRefCoordinate() {
        return refCoordinate;
    }

    public Cell getRefCell(){
        return sheet.getCellByCoordinate(refCoordinate);
    }

    public Edge getEdge() {
        return edge;
    }
}
