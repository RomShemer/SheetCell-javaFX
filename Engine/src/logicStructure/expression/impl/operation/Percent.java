package logicStructure.expression.impl.operation;

import logicStructure.expression.api.Expression;
import logicStructure.expression.api.Funcion;
import logicStructure.expression.impl.BinaryExpression;
import logicStructure.sheet.cell.api.CellType;
import logicStructure.sheet.cell.api.EffectiveValue;
import logicStructure.sheet.cell.impl.EffectiveValueImpl;

import java.util.ArrayList;
import java.util.List;

public class Percent extends BinaryExpression implements Funcion {
    public Percent(Expression left, Expression right) {
        super(OperationName.PERCENT, left, right);
    }

    public Percent(){
        super(OperationName.PERCENT);
    }

    @Override
    public EffectiveValue evaluate(EffectiveValue evaluate1, EffectiveValue evaluate2)
    {
        double result;

        if(Number.class.isAssignableFrom(evaluate1.getCellType().getType()) && Number.class.isAssignableFrom(evaluate2.getCellType().getType())) {
            double part = ((Number) evaluate1.getValue()).doubleValue();
            double whole = ((Number) evaluate2.getValue()).doubleValue();
            result = (part * whole) / 100.0;

            return new EffectiveValueImpl(CellType.DOUBLE, result);
        }
        else {
            //if (evaluate1.getValue().toString().isEmpty() || evaluate2.getValue().toString().isEmpty()) {
                return new EffectiveValueImpl(CellType.DOUBLE, Double.NaN);
            //}

//            throw new IllegalArgumentException("Unsupported operation:  Both arguments must be numeric." );
        }
    }
    @Override
    public String getOperationName(){
        return OperationName.PERCENT.toString();
    }

    @Override
    public OperationName getOperationType(){
        return OperationName.PERCENT;
    }

    @Override
    public Class<?> getExpressionType(){
        return CellType.DOUBLE.getType();
    }

    @Override
    public List<Class> getRequiredExpressionTypesList(){
        List<Class> result = new ArrayList<>();
        result.add(Double.class);
        result.add(Double.class);
        return result;
    }
}
