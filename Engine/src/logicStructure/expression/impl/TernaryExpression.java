package logicStructure.expression.impl;

import logicStructure.expression.api.Expression;
import logicStructure.expression.impl.operation.OperationName;
import logicStructure.expression.parser.Validator;
import logicStructure.sheet.cell.api.CellType;
import logicStructure.sheet.cell.api.EffectiveValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TernaryExpression implements Expression, Cloneable {

    private List<Expression> args;
    protected final OperationName operation;
    protected final Integer numOfArgs = 3;


    public TernaryExpression(OperationName operation, Expression expression1, Expression expression2, Expression expression3) {
        this.args = new ArrayList<Expression>();
        this.args.add(expression1);
        this.args.add(expression2);
        this.args.add(expression3);
        this.operation = operation;
        this.validateParser(args);
    }

    public TernaryExpression(OperationName operation){
        this.operation = operation;
    }

    public EffectiveValue evaluate() throws Exception {
        return evaluate(args.get(0).evaluate(), args.get(1).evaluate(), args.get(2).evaluate());
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
        String operationTypes = requiredTypes.stream()
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", "));
        String argumentTypes = Validator.compareReceivedTypesToRequired(args, this.getRequiredExpressionTypesList());

        if(operation.equals(OperationName.IF)){
            boolean isEnumMember = Arrays.stream(CellType.values())
                    .map(CellType::getType) // קבלת סוג הנתונים מה-CellType
                    .toList()
                    .contains(args.get(1).getExpressionType());

            if(isEnumMember && !args.get(1).getExpressionType().equals(args.get(2).getExpressionType())){
                String message = String.format("Error: Mismatch between 'then' and 'else' return types. The 'then' part returns a value of type %s, while the 'else' part returns a value of type %s", args.get(0).getExpressionType().getSimpleName(), args.get(1).getExpressionType().getSimpleName());
                throw new IllegalArgumentException(message);
            } else if(!isEnumMember){
                String message = String.format("Error: '%s' is not a valid cell type", args.get(1).getExpressionType().getSimpleName());
                throw new IllegalArgumentException(message);
            }
        } else if (!Validator.isAllReceivedTypeMatch(args, requiredTypes)) {
            throw new IllegalArgumentException(String.format("Invalid argument types for %s function. Expected types to be: %s, but got %s",
                    operation.name(), operationTypes, argumentTypes));
        }

        return true;
    }


    @Override
    public TernaryExpression clone() {
        try {
            TernaryExpression cloned = (TernaryExpression) super.clone();

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


    abstract protected EffectiveValue evaluate(EffectiveValue evaluate1, EffectiveValue evaluate2, EffectiveValue evaluate3);
    abstract public String getOperationName();
    abstract public OperationName getOperationType();
    abstract public  Class<?> getExpressionType();
    abstract public  List<Class> getRequiredExpressionTypesList();
}
