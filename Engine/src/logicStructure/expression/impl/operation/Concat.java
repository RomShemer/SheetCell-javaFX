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

public class Concat extends BinaryExpression implements Funcion {

    public Concat(Expression exp1, Expression exp2) {
        super(OperationName.CONCAT, exp1, exp2);
    }

    public Concat(){
        super(OperationName.CONCAT);
    }

    @Override
    public EffectiveValue evaluate(EffectiveValue evaluate1, EffectiveValue evaluate2)
    {
        if(evaluate1.getCellType().isAssignableFrom(String.class) && evaluate2.getCellType().isAssignableFrom(String.class)) {
            String result = evaluate1.extractValueWithExpectation(String.class) + evaluate2.extractValueWithExpectation(String.class);
            return new EffectiveValueImpl(CellType.STRING, result);
        }
        else {
            return new EffectiveValueImpl(CellType.STRING, NegativeFunctionType.UNDEFINED);
            //throw new IllegalArgumentException("Unsupported operation:  Both arguments must be string." );
        }
    }

    @Override
    public String getOperationName(){
        return OperationName.CONCAT.toString();
    }

    @Override
    public OperationName getOperationType(){
        return OperationName.CONCAT;
    }

    @Override
    public Class<?> getExpressionType(){
        return CellType.STRING.getType();
    }

    @Override
    public List<Class> getRequiredExpressionTypesList(){
        List<Class> result = new ArrayList<>();
        result.add(String.class);
        result.add(String.class);
        return result;
    }
}
