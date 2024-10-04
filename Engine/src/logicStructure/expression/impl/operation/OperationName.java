package logicStructure.expression.impl.operation;

import logicStructure.sheet.cell.api.CellType;
import logicStructure.sheet.cell.api.NegativeFunctionType;
import logicStructure.sheet.range.Range;

import java.util.ArrayList;
import java.util.List;

public enum OperationName {
    ABS(CellType.DOUBLE),
    CONCAT(CellType.STRING),
    DIVIDE(CellType.DOUBLE),
    MINUS(CellType.DOUBLE),
    MOD(CellType.DOUBLE),
    PLUS(CellType.DOUBLE),
    POW(CellType.DOUBLE),
    REF(CellType.CELL_COORDINATE),
    SUB(CellType.STRING),
    TIMES(CellType.DOUBLE),
    EQUAL(CellType.BOOLEAN),
    NOT(CellType.BOOLEAN),
    BIGGER(CellType.BOOLEAN),
    LESS(CellType.BOOLEAN),
    OR(CellType.BOOLEAN),
    AND(CellType.BOOLEAN),
    IF(CellType.UNKNOWN_CELL_TYPE),
    SUM(CellType.DOUBLE),
    AVERAGE(CellType.DOUBLE),
    PERCENT(CellType.DOUBLE),
    INTEGER_LEAF(CellType.INTEGER),
    DOUBLE_LEAF(CellType.DOUBLE),
    STRING_LEAF(CellType.STRING),
    BOOLEAN_LEAF(CellType.BOOLEAN),
    RANGE_LEAF(CellType.RANGE),
    EMPTY(CellType.EMPTY);

    private CellType type;

    OperationName(CellType type) {
        this.type = type;
    }

    public CellType getOperationType() {
        return this.type;
    }

    public static List<String> getOperationNamesList() {
        List<String> list = new ArrayList<String>();
        for (OperationName op : OperationName.values()) {
            if (!op.name().equals(INTEGER_LEAF.name()) && !op.name().equals(DOUBLE_LEAF.name()) &&
                    !op.name().equals(STRING_LEAF.name()) && !op.name().equals(BOOLEAN_LEAF.name()) && !op.name().equals(EMPTY.name())) {
                list.add(op.name());
            }
        }

        return list;
    }
}
