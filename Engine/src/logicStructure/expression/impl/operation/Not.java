package logicStructure.expression.impl.operation;

import logicStructure.expression.api.Expression;
import logicStructure.expression.api.Funcion;
import logicStructure.expression.impl.UnaryExpression;
import logicStructure.sheet.cell.api.CellType;
import logicStructure.sheet.cell.api.EffectiveValue;
import logicStructure.sheet.cell.api.NegativeFunctionType;
import logicStructure.sheet.cell.impl.EffectiveValueImpl;

import java.util.ArrayList;
import java.util.List;

public class Not extends UnaryExpression implements Funcion {
    public Not(Expression expr) {
        super(OperationName.NOT, expr);
    }

    public Not() {
        super(OperationName.NOT);
    }

    @Override
    protected EffectiveValue evaluate(EffectiveValue evaluate1) {
        if(Boolean.class.isAssignableFrom(evaluate1.getCellType().getType())) {
            try {
                boolean result = !evaluate1.extractValueWithExpectation(Boolean.class);
                return new EffectiveValueImpl(CellType.BOOLEAN, result);
            } catch (Exception e){
                return new EffectiveValueImpl(CellType.BOOLEAN, NegativeFunctionType.UNKNOWN);
            }
        } else {
            return new EffectiveValueImpl(CellType.BOOLEAN, NegativeFunctionType.UNKNOWN);
        }
    }

    @Override
    public String getOperationName() {
        return OperationName.NOT.toString();
    }

    @Override
    public OperationName getOperationType() {
        return OperationName.NOT;
    }

    @Override
    public Class<?> getExpressionType() {
        return CellType.BOOLEAN.getType();
    }

    @Override
    public List<Class> getRequiredExpressionTypesList() {
        List<Class> result = new ArrayList<>();
        result.add(Boolean.class);
        return result;
    }
}
