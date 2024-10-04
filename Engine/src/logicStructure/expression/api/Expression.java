package logicStructure.expression.api;

import logicStructure.expression.impl.operation.OperationName;
import logicStructure.sheet.cell.api.EffectiveValue;

public interface Expression extends Cloneable{
    EffectiveValue evaluate() throws Exception;
    String getOperationName();
    OperationName getOperationType();
    Boolean validateParser();
    Class<?> getExpressionType();
    Expression clone();
}
