package logicStructure.expression.impl.operation;

import logicStructure.expression.api.Expression;
import logicStructure.expression.api.Funcion;
import logicStructure.expression.impl.UnaryExpression;
import logicStructure.sheet.cell.api.CellType;
import logicStructure.sheet.cell.api.EffectiveValue;
import logicStructure.sheet.cell.impl.EffectiveValueImpl;
import java.util.ArrayList;
import java.util.List;

public class Abs extends UnaryExpression implements Funcion {

    public Abs(Expression exp) {
        super(OperationName.ABS, exp);
    }

    public Abs() {
        super(OperationName.ABS);
    }

    @Override
    public EffectiveValue evaluate(EffectiveValue evaluate1)
    {
        if(Number.class.isAssignableFrom(evaluate1.getCellType().getType())) {
            double result = Math.abs(evaluate1.extractValueWithExpectation(Number.class).doubleValue());
            return new EffectiveValueImpl(CellType.DOUBLE, result);
        }
        else {
//            if (evaluate1.getValue().toString().isEmpty()) {
//                return new EffectiveValueImpl(CellType.DOUBLE, Double.NaN);
//            }
            return new EffectiveValueImpl(CellType.DOUBLE, Double.NaN);
            //throw new IllegalArgumentException("Unsupported operation: argument must be numeric." );
        }
    }

    @Override
    public String getOperationName(){
        return OperationName.ABS.toString();
    }

    @Override
    public  Class<?> getExpressionType(){
        return CellType.DOUBLE.getType();
    }


    @Override
    public OperationName getOperationType(){
        return OperationName.ABS;
    }

    @Override
    public List<Class> getRequiredExpressionTypesList(){
        List<Class> result = new ArrayList<>();
        result.add(Double.class);
        return result;
    }
}
