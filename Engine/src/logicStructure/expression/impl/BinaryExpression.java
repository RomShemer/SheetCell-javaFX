package logicStructure.expression.impl;

import logicStructure.expression.impl.operation.OperationName;
import logicStructure.expression.parser.Validator;
import logicStructure.sheet.cell.api.CellType;
import logicStructure.sheet.cell.api.EffectiveValue;
import logicStructure.expression.api.Expression;
import logicStructure.sheet.cell.api.NegativeFunctionType;
import logicStructure.sheet.cell.impl.EffectiveValueImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BinaryExpression implements Expression, Cloneable {

    private List<Expression> args;
    protected final OperationName operation;
    protected final Integer numOfArgs = 2;

    public BinaryExpression(OperationName operation, Expression expression1, Expression expression2) {
        this.args = new ArrayList<Expression>();
        args.add(expression1);
        args.add(expression2);
        this.operation = operation;
        this.validateParser(args);

    }

    public BinaryExpression(OperationName operation){
        this.operation = operation;
    }

    public EffectiveValue evaluate() throws Exception {
        return evaluate(args.get(0).evaluate(), args.get(1).evaluate());
    }

    public List<Expression> getArgsList() {
        return args;
    }

    public Integer getNumOfArgs() {
        return numOfArgs;
    }

    @Override
    public Boolean validateParser() {
        return validateParser(args);
    }

    public Boolean validateParser(List<Expression> args) {
        if (!Validator.isValidNumOfArgs(args.size(), numOfArgs)) {
            throw new IllegalArgumentException(String.format("Invalid number of arguments for %s function. Expected %d, but got %d",
                    operation.name(), numOfArgs, args.size()));
        }

        List<Class> requiredTypes = this.getRequiredExpressionTypesList();
        String operationTypes = this.getRequiredExpressionTypesList().stream()
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", "));
        String argumentTypes = Validator.compareReceivedTypesToRequired(args, this.getRequiredExpressionTypesList());

        if (!Validator.isAllReceivedTypeMatch(args, requiredTypes)) {
            throw new IllegalArgumentException(String.format("Invalid argument types for %s function. Expected types to be: %s, but got %s",
                    operation.name(), operationTypes, argumentTypes));
        }

        return true;
    }

    @Override
    public BinaryExpression clone() {
        try {
            BinaryExpression cloned = (BinaryExpression) super.clone();

            // Deep copy of args
            cloned.args = new ArrayList<>();
            for (Expression expr : this.args) {
                cloned.args.add(expr.clone());
            }

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported", e);
        }
    }

    abstract protected EffectiveValue evaluate(EffectiveValue evaluate1, EffectiveValue evaluate2);
    abstract public String getOperationName();
    abstract public OperationName getOperationType();
    abstract public  Class<?> getExpressionType();
    abstract public  List<Class> getRequiredExpressionTypesList();
}
