package logicStructure.sheet.coordinate;

import java.io.Serializable;

public class CoordinateImpl implements Coordinate, Serializable {
    private final int row;
    private final int column;

    public CoordinateImpl(int row, int column) {
        this.row = row;
        this.column = column;
    }

    @Override
    public int getRow() {
        return row;
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return String.format("(row:%d, column:%d)", row, column);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || this.getClass() != object.getClass())
            return false;
        Coordinate current = (Coordinate) object;
        return row == current.getRow() && column == current.getColumn();
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + row;
        result = 31 * result + column;
        return result;
    }

    @Override
    public String createCellCoordinateString(){
        String rowString =  String.valueOf(row +1);
        String colString =  String.valueOf((char) ('A' + ((column -1 ) % 26)));
        return colString + rowString;
    }
}
