package logicStructure.sheet.DTO;

import logicStructure.sheet.coordinate.CoordinateFactory;
import xmlLoader.jaxb.STLCell;

public class CoordinateDTO {
    private final int row;
    private final int column;

    public CoordinateDTO(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public CoordinateDTO(String cellId, SheetDTO sheet) {
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

//        if(colIndex == 0){
//            throw new NumberFormatException("Invalid Cell ID: Cell ID must contain a valid letter for the column");
//        }

        try {
            rowIndex = Integer.parseInt(cellId.substring(i)) - 1;
        } catch (NumberFormatException e){
           // throw new NumberFormatException("Invalid Cell ID: Cell ID must contain a valid integer for the row");
            colIndex = -1;
        }

        if (rowIndex < 0 || rowIndex > sheet.getRows() ||
                colIndex < 0 || colIndex > sheet.getColumns()) {
            String minCoord = sheet.getMinCoordinateInSheet().createCellCoordinateString();
            String maxCoord = sheet.getMaxCoordinateInSheet().createCellCoordinateString();
            //throw new IndexOutOfBoundsException(String.format("Invalid Cell ID: Coordinate %s is out of bounds, should be between %s - %s", cellId, minCoord, maxCoord));
        }

        row = rowIndex;
        column = colIndex;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public String createCellCoordinateString(){
        return CoordinateFactory.createCoordinate(row, column).createCellCoordinateString();
    }

    @Override
    public String toString() {
        return this.createCellCoordinateString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || this.getClass() != object.getClass())
            return false;
        CoordinateDTO current = (CoordinateDTO) object;
        return row == current.getRow() && column == current.getColumn();
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + row;
        result = 31 * result + column;
        return result;
    }
}
