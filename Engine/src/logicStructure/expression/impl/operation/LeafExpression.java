package logicStructure.expression.impl.operation;

import logicStructure.expression.api.Expression;
import logicStructure.sheet.cell.api.CellType;
import logicStructure.sheet.cell.api.EffectiveValue;
import logicStructure.sheet.cell.impl.EffectiveValueImpl;
import logicStructure.sheet.range.Range;
import logicStructure.sheet.range.RangeFactory;

public class LeafExpression implements Expression, Cloneable {
    private EffectiveValue value;
    private OperationName operation;
    private Class<?> expressionType;

    public LeafExpression(String value) {
        this.value = new EffectiveValueImpl(CellType.STRING, value);
        operation = value == null ? OperationName.EMPTY : getOperation(value);
        expressionType = operation.equals(OperationName.RANGE_LEAF) ? Range.class : String.class;
    }

    private OperationName getOperation(String value){
        if(RangeFactory.isRangeExist(value)){
            return OperationName.RANGE_LEAF;
        } else {
            return OperationName.STRING_LEAF;
        }
    }

    public LeafExpression(double value) {
        this.value = new EffectiveValueImpl(CellType.DOUBLE, value);
        operation = OperationName.DOUBLE_LEAF;
        expressionType = Double.class;
    }

    public LeafExpression(int value) {
        this.value = new EffectiveValueImpl(CellType.INTEGER, value);
        operation = OperationName.INTEGER_LEAF;
        expressionType = Integer.class;
    }

    public LeafExpression(Boolean value) {
        this.value = new EffectiveValueImpl(CellType.BOOLEAN, value);
        operation = OperationName.BOOLEAN_LEAF;
        expressionType = Boolean.class;
    }

    @Override
    public EffectiveValue evaluate() {
        return value;
    }

    @Override
    public String getOperationName()
    {
        return operation.toString();
    }

    @Override
    public OperationName getOperationType() {
        return operation;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public Class<?> getExpressionType(){
        return expressionType;
    }

    @Override
    public Boolean validateParser() {
        return  operation.getOperationType() == value.getCellType();
    }

    @Override
    public LeafExpression clone() {
        try {
            LeafExpression cloned = (LeafExpression) super.clone();
            cloned.value = new EffectiveValueImpl(this.value.getCellType(), this.value.getValue());
            cloned.operation = this.operation;
            cloned.expressionType = this.expressionType;
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported", e);
        }
    }
}
