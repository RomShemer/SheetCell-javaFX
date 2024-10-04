package xmlLoader;

import xmlLoader.jaxb.*;

import java.io.File;

public class XmlSheetValidator {

    public static void validateXmlPath(String filePath) {
        if (!filePath.endsWith(".xml")) {
            throw new IllegalArgumentException("The file is not an XML file.");
        }
        if (!isOnlyValidChars(filePath)) {
            throw new IllegalArgumentException("The file contains invalid characters.");
        }
        if (filePath.contains(" ")) {
            filePath = handleSpacesInPath(filePath);
        }
    }

    public static void isXmlFileExists(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("The file does not exist.");
        }
    }

    public static Boolean isOnlyValidChars(String path) {
        return path.matches("^[ a-zA-Z0-9_\\\\:\\-\\.]+$");
    }

    private static String handleSpacesInPath(String path) {
        return "\"" + path + "\"";
    }

    public static void validateSheetSize(STLSheet sheet) {
        STLLayout layout = sheet.getSTLLayout();
        int rows = layout.getRows();
        int columns = layout.getColumns();

        if(rows < 1 || rows > 50) {
            throw new IndexOutOfBoundsException("Invalid number of rows: " + rows + ". The number of rows must be between 1 and 50.");
        }
        if ( columns < 1 || columns > 20){
            throw new IndexOutOfBoundsException("Invalid number of columns: " + columns + ". The number of columns must be between 1 and 20.");
        }
    }

    public static void validateCellsWithinBounds(STLSheet sheet) {
        STLLayout layout = sheet.getSTLLayout();
        int maxRows = layout.getRows();
        int maxColumns = layout.getColumns();
        String maxCellID = ("A" + maxColumns) + String.valueOf(maxRows);

        for (STLCell cell : sheet.getSTLCells().getSTLCell()) {
            int row = cell.getRow();
            int column = columnToIndex(cell.getColumn());

            if (row < 1 || row > maxRows || column < 1 || column > maxColumns) {
                throw new IllegalArgumentException(String.format("%s cell is outside the sheet bounds. Cells bounds must be up to %s.",
                        createCellId(cell), maxCellID));
            }
        }
    }

    private static int columnToIndex(String column) {
        return column.charAt(0) - 'A' + 1;
    }

    public static String createCellId(STLCell cell){
        return cell.getColumn() + String.valueOf(cell.getRow());
    }

//    public static boolean isValidRange(STLRange range) {
//        STLBoundaries boundaries = range.getSTLBoundaries();
//        String toCoordinate = boundaries.getTo();
//        String fromCoordinate = boundaries.getFrom();
//
//
//    }
}
