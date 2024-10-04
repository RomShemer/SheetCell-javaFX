package logicStructure.sheet.DTO;

import logicStructure.sheet.coordinate.Coordinate;
import logicStructure.sheet.coordinate.CoordinateFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SheetDTO {
    private final String name;
    private final int rows;
    private final int columns;
    private final int rowSize;
    private final int columnSize;
    private final Map<CoordinateDTO, CellDTO> cells;
    private final Map<String, RangeDTO> ranges;
    private final int version;

    public SheetDTO(String name, int rows, int columns,int rowSize, int columnSize, Map<CoordinateDTO, CellDTO> cells, int version, Map<String, RangeDTO> ranges) {
        this.name = name;
        this.rows = rows;
        this.columns = columns;
        this.cells = cells;
        this.rowSize = rowSize;
        this.columnSize = columnSize;
        this.version = version;
        this.ranges = ranges;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getRowSize() {
        return rowSize;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public int getVersion(){return version;}

    public String getName() {
        return name;
    }

    public Map<CoordinateDTO, CellDTO> getCells() {
        return cells;
    }

    public Map<String, RangeDTO> getRanges() {
        return ranges;
    }

    public CoordinateDTO getMinCoordinateInSheet(){
        return SheetMapper.toCoordinateDTO(CoordinateFactory.createCoordinate(0,1));
    }

    public CoordinateDTO getMaxCoordinateInSheet(){
        return SheetMapper.toCoordinateDTO(CoordinateFactory.createCoordinate(rows-1, columns));
    }


    @Override
    public String toString() {
        StringBuilder outputString = new StringBuilder();
        outputString.append("=".repeat(columnSize*(columns+1)));
        outputString.append("\nName: " + name + "\n");
        outputString.append("Showing version: " + version + "\n\n");
        outputString.append("   | ");

        for (int col = 0; col < columns; col++) {
            String colHeader = String.format("%-" + columnSize + "s", (char) ('A' + col));
            outputString.append(colHeader + "| ");
        }
        outputString.append("\n");
        outputString.append(" ".repeat(3)); //להוריד
        outputString.append("-".repeat((columnSize + 2) * (columns))); //להוריד
        outputString.append("\n");

        for (int row = 0; row < rows; row++) {
            String rowHeader = String.format("%02d | ", row + 1);
            outputString.append(rowHeader);

            for (int col = 0; col < columns; col++) {
                CoordinateDTO cellCoordinate = new CoordinateDTO(row, col + 1);
                String cellValue = cells.containsKey(cellCoordinate) ?
                        formatAsDouble(cells.get(cellCoordinate).getEffectiveValue()) : " ";

                String formattedCellValue = centerText(cellValue, columnSize);
                outputString.append(formattedCellValue + "| ");
            }

            for (int i = 1; i < rowSize; i++) {
                outputString.append("\n");
                outputString.append("   | ");  // השארת מקום עבור כותרת השורה

                for (int col = 0; col < columns; col++) {
                    outputString.append(String.format("%-" + columnSize + "s", "") + "| ");
                }
            }

            outputString.append("\n");
        }

        outputString.append("=".repeat(columnSize*(columns+1) )+ "\n");

        return outputString.toString();
    }

    private static String centerText(String text, int width) {
        if (text == null) {
            text = "";
        }
        if (width <= text.length()) {
            return text.substring(0, width);
        }

        int padding = (width - text.length()) / 2;
        String leftPadding = " ".repeat(padding);
        String rightPadding = " ".repeat(width - text.length() - padding);

        return leftPadding + text + rightPadding;
    }

    public static String formatAsDouble(String value) {
        try {
            double doubleValue = Double.parseDouble(value);

            if (doubleValue == (long) doubleValue) {
                return String.format("%d", (long) doubleValue);
            } else {
                return String.format("%.2f", doubleValue);
            }
        } catch (NumberFormatException e) {
            return value;
        }
    }

    public List<String> getColumnNamesList(){
        List<String> columnNames = new ArrayList<>();

        for (int col = 1; col <= columns; col++) {
            columnNames.add(String.valueOf((char) ('A' + (col-1))));
        }

        Collections.sort(columnNames);

        return columnNames;
    }

}
