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

public class Less extends BinaryExpression implements Funcion {

    public Less(Expression left, Expression right) {
        super(OperationName.LESS, left, right);
    }

    public Less(){
        super(OperationName.LESS);
    }

    @Override
    protected EffectiveValue evaluate(EffectiveValue evaluate1, EffectiveValue evaluate2) {
        boolean result;

        if(Number.class.isAssignableFrom(evaluate1.getCellType().getType()) && Number.class.isAssignableFrom(evaluate2.getCellType().getType())) {
            result = evaluate1.extractValueWithExpectation(Number.class).doubleValue() <= evaluate2.extractValueWithExpectation(Number.class).doubleValue();
            return new EffectiveValueImpl(CellType.BOOLEAN, result);
        }
        else {
//            if (evaluate1.getValue().toString().isEmpty() || evaluate2.getValue().toString().isEmpty()) {
//                throw new IllegalArgumentException("Unsupported operation:  Both arguments must be numeric." );
//            } //need?

            return new EffectiveValueImpl(CellType.BOOLEAN, NegativeFunctionType.UNKNOWN);
        }
    }

    @Override
    public String getOperationName() {
        return OperationName.LESS.toString();
    }

    @Override
    public OperationName getOperationType() {
        return OperationName.LESS;
    }

    @Override
    public Class<?> getExpressionType() {
        return CellType.BOOLEAN.getType();
    }

    @Override
    public List<Class> getRequiredExpressionTypesList() {
        List<Class> result = new ArrayList<>();
        result.add(Double.class);
        result.add(Double.class);
        return result;
    }
}
