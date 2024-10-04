package logicStructure.expression.impl;

import logicStructure.expression.api.Expression;
import logicStructure.expression.impl.operation.*;
import logicStructure.expression.parser.Validator;
import logicStructure.sheet.cell.api.EffectiveValue;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class UnaryExpression implements Expression, Cloneable {
    private List<Expression> args;
    protected final OperationName operation;
    protected final Integer numOfArgs = 1;

    public UnaryExpression(OperationName Operation, Expression expression1) {
        this.args = new ArrayList<Expression>();
        args.add(expression1);
        this.operation = Operation;
        this.validateParser(args);
    }

    public UnaryExpression(OperationName operation){
        this.operation = operation;
    }

    public EffectiveValue evaluate() throws Exception {
        if(this instanceof Abs){
          Abs abs = (Abs)this;
          return abs.evaluate(args.get(0).evaluate());
        } else if (this instanceof Not){
            Not not = (Not)this;
            return not.evaluate();
        } else if (this instanceof Average){
            Average average = (Average)this;
            return average.evaluate(args.get(0).evaluate());
        } else if (this instanceof Sum){
            Sum sum = (Sum)this;
            return sum.evaluate(args.get(0).evaluate());
        }

        return args.get(0).evaluate();
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

        if(operation == OperationName.REF && (args.size() != 1 )){
            throw new IllegalArgumentException(String.format("Invalid argument for %s function. Expected 1 argument but got %d: ",
                    operation.name(), args.size()));
        } else if (!Validator.isAllReceivedTypeMatch(args, requiredTypes) && operation != OperationName.REF) {
            throw new IllegalArgumentException(String.format("Invalid argument types for %s function. Expected types to be: %s, but got %s",
                    operation.name(), operationTypes, argumentTypes));
        }

        return true;
    }

    @Override
    public UnaryExpression clone() {
        try {
            UnaryExpression cloned = (UnaryExpression) super.clone();

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

    abstract protected EffectiveValue evaluate(EffectiveValue evaluate1) throws Exception;
    abstract public String getOperationName();
    abstract public OperationName getOperationType();
    abstract public  Class<?> getExpressionType();
    abstract public  List<Class> getRequiredExpressionTypesList();

}
