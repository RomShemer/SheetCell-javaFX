package logicStructure.sheet.cell.api;

import logicStructure.sheet.coordinate.Coordinate;
import logicStructure.sheet.range.Range;

public enum CellType {
    DOUBLE(Double.class) ,
    STRING(String.class) ,
    BOOLEAN(Boolean.class),
    INTEGER(Integer.class),
    CELL_COORDINATE(Coordinate.class),
    RANGE(Range.class),
    NEGATIVE(NegativeFunctionType.class),
    EMPTY(String.class),
    UNKNOWN_CELL_TYPE(CellType.class);

    private Class<?> type;

    CellType(Class<?> type) {
        this.type = type;
    }

    public boolean isAssignableFrom(Class<?> aType) {
        return type.isAssignableFrom(aType);
    }

    public Class<?> getType(){
        return type;
    }
}
