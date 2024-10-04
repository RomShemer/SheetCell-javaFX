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

public class Equal extends BinaryExpression implements Funcion {
    public Equal(Expression left, Expression right) {
        super(OperationName.EQUAL, left, right);
    }

    public Equal(){
        super(OperationName.EQUAL);
    }

    @Override
    protected EffectiveValue evaluate(EffectiveValue evaluate1, EffectiveValue evaluate2) {
        boolean result = true;

        if (evaluate1 == null && evaluate2 == null) {
            result = true;
        } else  if (evaluate1 == null || evaluate2 == null) {
            result = false;
        } else {
            try {
                result = evaluate1.equals(evaluate2);
            } catch (Exception e) {
                return new EffectiveValueImpl(CellType.NEGATIVE, NegativeFunctionType.UNKNOWN);
            }
        }

        return new EffectiveValueImpl(CellType.BOOLEAN, result);
    }

    @Override
    public String getOperationName() {
        return OperationName.EQUAL.toString();
    }

    @Override
    public OperationName getOperationType() {
        return OperationName.EQUAL;
    }

    @Override
    public Class<?> getExpressionType() {
        return CellType.BOOLEAN.getType();
    }

    @Override
    //todo
    public List<Class> getRequiredExpressionTypesList() {
        List<Class> result = new ArrayList<>();
        result.add(CellType.class);
        result.add(CellType.class);
        return result;
    }
}
