package logicStructure.expression.parser;

import logicStructure.sheet.impl.SheetImpl;
import logicStructure.expression.api.Expression;
import logicStructure.expression.impl.operation.*;
import logicStructure.sheet.coordinate.Coordinate;
import logicStructure.sheet.coordinate.CoordinateFactory;
import logicStructure.sheet.range.Range;
import logicStructure.sheet.range.RangeFactory;

import java.util.List;
import java.util.ArrayList;

public class Convertor {

    public static Coordinate convertFromCellIdToCoordinate(String cellId, SheetImpl sheet) {
        int rowIndex = 0;
        int colIndex = 0;
        int i = 0;

        if(cellId.isEmpty()){
            throw new NullPointerException("Cell ID can't be empty: Cell ID must contain a valid letter for the column followed by a valid number for the row");
        }

        cellId = cellId.toUpperCase();

        while (i < cellId.length() && Character.isLetter(cellId.charAt(i))) {
            colIndex = colIndex * 26 + (cellId.charAt(i) - 'A' + 1);
            i++;
        }

        if(colIndex == 0){
            throw new NumberFormatException("Invalid Cell ID: Cell ID must contain a valid letter for the column");
        }

        try {
            rowIndex = Integer.parseInt(cellId.substring(i)) - 1;
        } catch (NumberFormatException e){
            colIndex = -1;
            //throw new NumberFormatException("Invalid Cell ID: Cell ID must contain a valid integer for the row");
        }

        if (rowIndex < 0 || rowIndex > sheet.getNumOfRows() ||
                colIndex < 0 || colIndex > sheet.getNumOfCols()) {
            String minCoord = sheet.getMinCoordinateInSheet().createCellCoordinateString();
            String maxCoord = sheet.getMaxCoordinateInSheet().createCellCoordinateString();
            throw new IndexOutOfBoundsException(String.format("Invalid Cell ID: Coordinate %s is out of bounds, should be between %s - %s", cellId, minCoord, maxCoord));
        }

        return CoordinateFactory.createCoordinate(rowIndex, colIndex);
    }

    public static Range convertRange(String fromCellID, String toCellID, String name, SheetImpl sheet) throws Exception {
        try {
            Coordinate fromCoordinate = convertFromCellIdToCoordinate(fromCellID, sheet);
            Coordinate toCoordinate = convertFromCellIdToCoordinate(toCellID, sheet);

            Validator.validateRange(fromCoordinate, toCoordinate, sheet);

            return RangeFactory.createCoordinate(name.toUpperCase(), fromCoordinate, toCoordinate);

        } catch (Exception e) {
            String message = String.format("Invalid \"%s\" Range:", name) + e.getMessage();
            throw new Exception(message, e);
        }
    }

    public static Expression stringToExpression(String input, SheetImpl sheet, Coordinate currentCoordinate) throws Exception {

        if (input.startsWith("{") && input.endsWith("}")) {
            String[] trimedInput = stringTrimer(input);
            List<Expression> argsList = createArgsList(trimedInput, sheet, currentCoordinate);

            return createExpression(trimedInput, argsList, sheet, currentCoordinate);

        } else {
            try {
                try {
                    int number = Integer.parseInt(input);
                    return new LeafExpression(number);
                }
                catch  (NumberFormatException e){
                    double number = Double.parseDouble(input);
                    return new LeafExpression(number);
                }
            } catch (NumberFormatException e) {
                return new LeafExpression(input);
            } catch (NullPointerException e){
                return new LeafExpression((String) null);
            }
        }
    }

    public static List<Expression> createArgsList(String[] expression, SheetImpl sheet, Coordinate currentCoordinate) throws Exception {

        List<Expression> args = new ArrayList<>();
        Expression argExpression;

        for (int i = 1; i < expression.length; i++) {
            argExpression = stringToExpression(expression[i], sheet, currentCoordinate);
            args.add(argExpression);
        }

        return args;
    }

    public static Expression createExpression(String[] expression, List<Expression> args, SheetImpl sheet, Coordinate currentCoordinate) throws Exception {
        String operator = expression[0].toUpperCase();
        Expression res;

        switch (operator) {
            case "PLUS" -> res = new Plus(args.get(0), args.get(1));
            case "MINUS" -> res = new Minus(args.get(0), args.get(1));
            case "TIMES" -> res = new Times(args.get(0), args.get(1));
            case "DIVIDE" -> res = new Divide(args.get(0), args.get(1));
            case "MOD" -> res = new Mod(args.get(0), args.get(1));
            case "POW" -> res = new Pow(args.get(0), args.get(1));
            case "ABS" -> res = new Abs(args.get(0));
            case "CONCAT" -> res = new Concat(args.get(0), args.get(1));
            case "SUB" -> res = new Sub(args.get(0), args.get(1), args.get(2));
            case "REF" -> {
                try {
                    Coordinate refCoordinate = convertFromCellIdToCoordinate(args.get(0).evaluate().getValue().toString(), sheet);
                    res = new Ref(refCoordinate, sheet, currentCoordinate);
                } catch (Exception e){
                    String message = "Failed to create REF function -> " + e.getMessage();
                    throw new Exception(message,e);
                }

            }
            case "EQUALS" -> res = new Equal(args.get(0), args.get(1));
            case "NOT" -> res = new Not(args.get(0));
            case "AND" -> res = new And(args.get(0), args.get(1));
            case "OR" -> res = new Or(args.get(0), args.get(1));
            case "BIGGER" -> res = new Bigger(args.get(0), args.get(1));
            case "LESS" -> res = new Less(args.get(0), args.get(1));
            case "IF" -> res = new If(args.get(0), args.get(1), args.get(2));
            case "SUM" -> res = new Sum(args.get(0),currentCoordinate, sheet);
            case "AVERAGE" -> res = new Average(args.get(0),currentCoordinate, sheet);
            case "PERCENT" -> res = new Percent(args.get(0), args.get(1));
            default -> throw new IllegalArgumentException("Unknown operator: " + operator);
        };

        return res;
    }

    public static String[] stringTrimer(String input) {

        if (input.startsWith("{") && input.endsWith("}")) {
            input = input.substring(1, input.length() - 1).trim();
        }

        List<String> result =  new ArrayList<>();
        StringBuilder currentElement = new StringBuilder();
        boolean insideBraces = false;
        int openBrackets = 0;

        // Parse the input to separate the operator and arguments
        for (char c : input.toCharArray()) {
            if (c == '{') {
                insideBraces = true;
                openBrackets++;
            } else if (c == '}') {
                openBrackets--;
                if (openBrackets == 0) {
                    insideBraces = false;
                }
            }

            if (c == ',' && !insideBraces) {
                result.add(currentElement.toString().trim());
                currentElement.setLength(0); // Clear the current element
            } else {
                currentElement.append(c);
            }
        }
        result.add(currentElement.toString().trim()); // Add the last element

        String operator = result.removeFirst();

        // Convert to array
        String[] operatorAndArgs = new String[result.size() + 1];
        operatorAndArgs[0] = operator;
        for (int i = 0; i < result.size(); i++) {
            operatorAndArgs[i + 1] = result.get(i);
        }

        return operatorAndArgs;
    }

    public static List<Class> getRequiredArgsClassForOperator(String operator) {

        switch (operator) {
            case "PLUS" -> {
                return (new Plus()).getRequiredExpressionTypesList();
            }
            case "MINUS" ->{
                return (new Minus()).getRequiredExpressionTypesList();
            }
            case "TIMES" -> {
                return (new Times()).getRequiredExpressionTypesList();
            }
            case "DIVIDE" -> {
                return (new Divide()).getRequiredExpressionTypesList();
            }
            case "MOD" -> {
                return (new Mod()).getRequiredExpressionTypesList();
            }
            case "POW" -> {
                return (new Pow()).getRequiredExpressionTypesList();
            }
            case "ABS" -> {
                return (new Abs()).getRequiredExpressionTypesList();
            }
            case "CONCAT" -> {
                return (new Concat()).getRequiredExpressionTypesList();
            }
            case "SUB" -> {
                return (new Sub()).getRequiredExpressionTypesList();
            }
            case "REF" -> {
                return (new Ref(null)).getRequiredExpressionTypesList();
//                try {
//                    Coordinate refCoordinate = convertFromCellIdToCoordinate(args.get(0).evaluate().getValue().toString(), sheet);
//                    res = new Ref(refCoordinate, sheet, currentCoordinate);
//                } catch (Exception e){
//                    String message = "Failed to create REF function -> " + e.getMessage();
//                    throw new Exception(message,e);
//                }
            }
            case "EQUALS" -> {
                return (new Equal()).getRequiredExpressionTypesList();
            }
            case "NOT" -> {
                return (new Not()).getRequiredExpressionTypesList();
            }
            case "AND" -> {
                return (new And()).getRequiredExpressionTypesList();
            }
            case "OR" -> {
                return (new Or()).getRequiredExpressionTypesList();
            }
            case "BIGGER" ->{
                return (new Bigger()).getRequiredExpressionTypesList();
            }
            case "LESS" -> {
                return (new Less()).getRequiredExpressionTypesList();
            }
            case "IF" -> {
                return (new If()).getRequiredExpressionTypesList();
            }
            case "SUM" -> {
                return (new Sum()).getRequiredExpressionTypesList();
            }
            case "AVERAGE" -> {
                return (new Average()).getRequiredExpressionTypesList();
            }
            case "PERCENT" -> {
                return (new Percent()).getRequiredExpressionTypesList();
            }
            default -> throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }
}
