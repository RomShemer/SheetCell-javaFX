package logicStructure.expression.impl.operation;

import logicStructure.expression.api.Expression;
import logicStructure.expression.api.Funcion;
import logicStructure.expression.impl.BinaryExpression;
import logicStructure.sheet.cell.api.CellType;
import logicStructure.sheet.cell.api.EffectiveValue;
import logicStructure.sheet.cell.api.NegativeFunctionType;
import logicStructure.sheet.cell.impl.EffectiveValueImpl;

import java.util.ArrayList;
import java.util.List;

public class Or extends BinaryExpression implements Funcion {
    public Or(Expression left, Expression right) {
        super(OperationName.OR, left, right);
    }

    public Or(){
        super(OperationName.OR);
    }

    @Override
    protected EffectiveValue evaluate(EffectiveValue evaluate1, EffectiveValue evaluate2) {
        boolean result;

        if(Boolean.class.isAssignableFrom(evaluate1.getCellType().getType()) && Boolean.class.isAssignableFrom(evaluate2.getCellType().getType())) {
            result = evaluate1.extractValueWithExpectation(Boolean.class) || evaluate2.extractValueWithExpectation(Boolean.class);
            return new EffectiveValueImpl(CellType.BOOLEAN, result);
        }
        else {
//            if (evaluate1.getValue().toString().isEmpty() || evaluate2.getValue().toString().isEmpty()) {
//                throw new IllegalArgumentException("Unsupported operation:  Both arguments must be boolean." );
//            } //need?

            return new EffectiveValueImpl(CellType.BOOLEAN, NegativeFunctionType.UNKNOWN);
        }
    }

    @Override
    public String getOperationName() {
        return OperationName.OR.toString();
    }

    @Override
    public OperationName getOperationType() {
        return OperationName.OR;
    }

    @Override
    public Class<?> getExpressionType() {
        return CellType.BOOLEAN.getType();
    }

    @Override
    public List<Class> getRequiredExpressionTypesList() {
        List<Class> result = new ArrayList<>();
        result.add(Boolean.class);
        result.add(Boolean.class);
        return result;
    }
}
