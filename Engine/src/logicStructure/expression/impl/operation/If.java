package logicStructure.expression.impl.operation;

import logicStructure.expression.api.Expression;
import logicStructure.expression.api.Funcion;
import logicStructure.expression.impl.TernaryExpression;
import logicStructure.sheet.cell.api.CellType;
import logicStructure.sheet.cell.api.EffectiveValue;
import logicStructure.sheet.cell.impl.EffectiveValueImpl;

import java.util.ArrayList;
import java.util.List;

public class If extends TernaryExpression implements Funcion {

    public If(Expression condition, Expression thenExpression, Expression elseExpression) {
        super(OperationName.IF, condition, thenExpression, elseExpression);
    }

    public If(){
        super(OperationName.IF);
    }

    @Override
    public EffectiveValue evaluate(EffectiveValue conditionEvaluate, EffectiveValue ThenEvaluate, EffectiveValue ElseEvaluate) {

        if (!conditionEvaluate.getCellType().isAssignableFrom(Boolean.class)) {
            throw new IllegalArgumentException("Unsupported operation: Condition must be a boolean");
        }

        if(!ThenEvaluate.getCellType().equals(ElseEvaluate.getCellType())){
            String message = String.format("Error: Mismatch between 'then' and 'else' return types. The 'then' part returns a value of type %s, while the 'else' part returns a value of type %s. Both must return values of the same type.", ThenEvaluate.getCellType().getType().getSimpleName(), ElseEvaluate.getCellType().getType().getSimpleName());
            throw new IllegalArgumentException(message);
        }

        boolean condition  = conditionEvaluate.extractValueWithExpectation(Boolean.class);

        if(condition){
            return new EffectiveValueImpl(ThenEvaluate.getCellType(), ThenEvaluate.getValue());
        } else {
            return new EffectiveValueImpl(ElseEvaluate.getCellType(), ElseEvaluate.getValue());
        }
    }


    @Override
    public String getOperationName(){
        return OperationName.IF.toString();
    }

    @Override
    public OperationName getOperationType(){
        return OperationName.IF;
    }

    @Override
    public Class<?> getExpressionType(){
        Expression thenExpression = getArgsList().get(1);
        Expression elseExpression = getArgsList().get(2);

        if (thenExpression.getExpressionType() == elseExpression.getExpressionType()) {
            return thenExpression.getExpressionType();
        } else {
            String message = String.format("Error: Mismatch between 'then' and 'else' return types. The 'then' part returns a value of type %s, while the 'else' part returns a value of type %s", thenExpression.getExpressionType().getSimpleName(), elseExpression.getExpressionType().getSimpleName());
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    //todo
    public List<Class> getRequiredExpressionTypesList(){
        //Class thenAndElseType = getArgsList().get(1).getExpressionType();
        List<Class> result = new ArrayList<>();
        result.add(Boolean.class);
        result.add(CellType.class);
        result.add(CellType.class);
        return result;
    }
}
