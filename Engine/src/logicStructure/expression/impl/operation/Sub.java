package logicStructure.expression.impl.operation;

import logicStructure.expression.api.Expression;
import logicStructure.expression.api.Funcion;
import logicStructure.expression.impl.TernaryExpression;
import logicStructure.sheet.cell.api.CellType;
import logicStructure.sheet.cell.api.EffectiveValue;
import logicStructure.sheet.cell.impl.EffectiveValueImpl;
import logicStructure.sheet.cell.api.NegativeFunctionType;
import java.util.ArrayList;
import java.util.List;

public class Sub extends TernaryExpression implements Funcion {

    public Sub(Expression source, Expression startIndex, Expression endIndex) {
        super(OperationName.SUB, source, startIndex, endIndex);
    }

    public Sub(){
        super(OperationName.SUB);
    }

    @Override
    public EffectiveValue evaluate(EffectiveValue evaluate1, EffectiveValue evaluate2, EffectiveValue evaluate3) {

        try {
            validateSubArgs(evaluate1, evaluate2, evaluate3);
        } catch (Exception e) {
            return new EffectiveValueImpl(CellType.NEGATIVE, NegativeFunctionType.UNDEFINED);
        }

        String source = evaluate1.extractValueWithExpectation(String.class);
        int startIndex = evaluate2.extractValueWithExpectation(Integer.class);
        int endIndex = evaluate3.extractValueWithExpectation(Integer.class);

        if (startIndex < 0 || startIndex > source.length() || endIndex < 0 || endIndex >= source.length() || endIndex < startIndex) {
            return new EffectiveValueImpl(CellType.NEGATIVE, NegativeFunctionType.UNDEFINED);
        }
        else {
            String result = source.substring(startIndex, endIndex + 1);
            return new EffectiveValueImpl(CellType.STRING, result);
        }
    }

    private void validateSubArgs(EffectiveValue evaluate1, EffectiveValue evaluate2, EffectiveValue evaluate3) {
        if (!evaluate1.getCellType().isAssignableFrom(String.class))
            throw new IllegalArgumentException("Unsupported operation: source argument must be a string.");
        else if (evaluate1.extractValueWithExpectation(String.class) == null) {
            throw new IllegalArgumentException("Invalid: null source string");
        }
        else if (! evaluate2.getCellType().isAssignableFrom(Integer.class)) {
            String message = String.format("Invalid substring indices: start-index should be non-negative integer smaller than %d",
                    evaluate1.extractValueWithExpectation(String.class).length() - 1);
            throw new IllegalArgumentException(message);
        } else if (!evaluate3.getCellType().isAssignableFrom(Integer.class)) {
            String message = String.format("Invalid substring indices: end-index should be non-negative integer in range [%d,%d]",
                    (evaluate2.extractValueWithExpectation(Integer.class)), (evaluate1.extractValueWithExpectation(String.class).length() - 1));
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public String getOperationName(){
        return OperationName.SUB.toString();
    }

    @Override
    public OperationName getOperationType(){
        return OperationName.SUB;
    }

    @Override
    public Class<?> getExpressionType(){
        return CellType.STRING.getType();
    }

    @Override
    public List<Class> getRequiredExpressionTypesList(){
        List<Class> result = new ArrayList<>();
        result.add(String.class);
        result.add(Integer.class);
        result.add(Integer.class);
        return result;
    }
}
