package logicStructure.expression.parser;

import logicStructure.expression.api.Expression;
import logicStructure.expression.impl.operation.OperationName;
import logicStructure.sheet.cell.api.CellType;
import logicStructure.sheet.cell.api.NegativeFunctionType;
import logicStructure.sheet.coordinate.Coordinate;
import logicStructure.sheet.impl.SheetImpl;
import logicStructure.sheet.range.Range;
import logicStructure.sheet.range.RangeFactory;
import logicStructure.specialException.InvalidRangeException;

import java.util.List;
import java.util.stream.IntStream;

public abstract class Validator {

    public static Boolean isNumeric(Expression expression) {
        Class<?> expressionType = expression.getExpressionType();
        return expressionType == Double.class || Double.class.isAssignableFrom(expressionType) ||
                Number.class.isAssignableFrom(expressionType);
    }

    public static Boolean isString(Expression expression) {
        return expression.getExpressionType() == CellType.STRING.getType();
    }

    public static Boolean isBoolean(Expression expression) {
        return expression.getExpressionType() == CellType.BOOLEAN.getType();
    }

    public static Boolean isValidNumOfArgs(Integer numArgs, Integer requiredNumArgs) {
        return numArgs == requiredNumArgs;
    }

    public static String compareReceivedTypesToRequired(List<Expression> received, List<Class> required) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < received.size(); i++) {
            Class<?> receivedType = received.get(i).getExpressionType();
            Class<?> requiredType = required.get(i);

            if (received.get(i).getOperationType() == OperationName.REF) {
                result.append(requiredType.getSimpleName());
            } else if (requiredType.isAssignableFrom(receivedType)) {
                result.append(requiredType.getSimpleName());
            } else if (requiredType == Double.class && Number.class.isAssignableFrom(receivedType)) {
                result.append(requiredType.getSimpleName());
            } else {
                result.append(receivedType.getSimpleName());
            }
            if (i < received.size() - 1) {
                result.append(", ");
            }
        }
        return result.toString();
    }


    public static Boolean isAllReceivedTypeMatch(List<Expression> received, List<Class> required) {
        return IntStream.range(0, received.size())
                .allMatch(i -> {
                    Expression expr = received.get(i);
                    if (expr.getOperationType() == OperationName.REF) {
                        return true;
                    }
                    if(expr.getOperationType() == OperationName.RANGE_LEAF){
                        return true;
                        //is it ok? if range doesnt exist should ?
                    }
                    if(expr.getOperationType() == OperationName.STRING_LEAF && required.get(i) == Range.class){
                        return true;
                    }
                    Object exprValue = null;
                    try {
                        exprValue = expr.evaluate().getValue();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    if (required.get(i) == Coordinate.class && exprValue == null) {
                        return true;
                    }
                    if (exprValue.equals(Double.NaN) || exprValue.equals(NegativeFunctionType.UNDEFINED) || exprValue.equals(NegativeFunctionType.UNKNOWN)) {
                        return true;
                    }
                    if (required.get(i) == Double.class && Number.class.isAssignableFrom(exprValue.getClass())) {
                        return true;
                    }

                    return required.get(i).isAssignableFrom(exprValue.getClass());
                });
    }

    // Method to check if the range is valid or throw an exception with specific messages
    public static void validateRange(Coordinate fromCoordinate, Coordinate toCoordinate, SheetImpl sheet) {
        validateRangeFormat(fromCoordinate, toCoordinate, sheet.getMaxCoordinateInSheet());

        //need?
        if (!isOrderedCorrectly(fromCoordinate, toCoordinate)) {
            throw new InvalidRangeException("Invalid range order: The top-left cell must be above and/or to the left of the bottom-right cell.");
        }
    }

    private static void validateRangeFormat(Coordinate fromCoordinate, Coordinate toCoordinate, Coordinate maxCoordinateInSheet) {

        // Validate range single column
        if (fromCoordinate.getColumn() == toCoordinate.getColumn() &&
                     fromCoordinate.getRow() > toCoordinate.getRow()) {
            String message = String.format("Invalid range format: Range of a single column must form a contiguous block from row %d up till row %d.", fromCoordinate.getRow(), maxCoordinateInSheet.getRow());
            throw new InvalidRangeException(message);
        }

        // Validate range single row
        if (fromCoordinate.getRow() == toCoordinate.getRow() &&
                fromCoordinate.getColumn() > toCoordinate.getColumn()) {
            String message = String.format("Invalid range format: Range of a single row must form a contiguous block from column %d up till column %d.", fromCoordinate.getColumn(), maxCoordinateInSheet.getColumn());
            throw new InvalidRangeException(message);
        }

        if (!(fromCoordinate.getRow() <= toCoordinate.getRow()) || !(fromCoordinate.getColumn() <= toCoordinate.getColumn())) {
            String message = String.format("Invalid range format: A 2D range must form a rectangular block from column %d to column %d and row %d to row %d.",
                    fromCoordinate.getColumn(), maxCoordinateInSheet.getColumn(), fromCoordinate.getRow(), maxCoordinateInSheet.getRow());
            throw new InvalidRangeException(message);
        }
    }

    // Helper method to check if the range is ordered correctly
    private static boolean isOrderedCorrectly(Coordinate fromCoordinate, Coordinate toCoordinate) {
        return fromCoordinate.getRow() <= toCoordinate.getRow() && fromCoordinate.getColumn() <= toCoordinate.getColumn();
    }
}
