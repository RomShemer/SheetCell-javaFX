package logicStructure.sheet.DTO;

import logicStructure.sheet.cell.api.Cell;
import logicStructure.sheet.cell.api.EffectiveValue;
import logicStructure.sheet.coordinate.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import logicStructure.sheet.api.SheetReadActions;
import logicStructure.sheet.coordinate.CoordinateFactory;
import logicStructure.sheet.range.Range;

public class SheetMapper {

    public static CoordinateDTO toCoordinateDTO(Coordinate coordinate) {
        return new CoordinateDTO(coordinate.getRow(), coordinate.getColumn());
    }

    public static CoordinateDTO toCoordinateDTO(String coordinate, SheetDTO sheetDTO) {
        return new CoordinateDTO(coordinate, sheetDTO);
    }

    public static CoordinateDTO toCoordinateDTO(String coordinate) {
        int colIndex = (coordinate.charAt(0) - 'A') + 1;
        int rowIndex = Integer.parseInt(coordinate.substring(1)) - 1;
        return new CoordinateDTO(rowIndex, colIndex);
    }

    public static CoordinateDTO toCoordinateDTO(int row, int column) {
        return new CoordinateDTO(row, column);
    }

    public static CellDTO toCellDTO(Cell cell) {
        return new CellDTO(
                toCoordinateDTO(cell.getCoordinate()),
                cell.getOriginalValue(),
                createEffectiveValueString(cell.getEffectiveValue()),
                cell.getVersion(),
                cell.getDependsOn().stream().map(SheetMapper::toCoordinateDTO).collect(Collectors.toList()),
                cell.getInfluencingOn().stream().map(SheetMapper::toCoordinateDTO).collect(Collectors.toList())
        );
    }

    public static RangeDTO toRangeDTO(Range range) {
        return new RangeDTO(range.getName(),
                toCoordinateDTO(range.getStartCoordinate()),
                toCoordinateDTO(range.getEndCoordinate()));
    }

    public static RangeDTO toRangeDTO(String start, String end) {
//        List<CoordinateDTO> cells = new ArrayList<>();
//        for (int row = start.getRow(); row <= end.getRow(); row++) {
//            for (int col = start.getColumn(); col <= end.getColumn(); col++) {
//                cells.add(toCoordinateDTO(row, col));
//            }
//        }
        String name = start + ":" + end;
        return new RangeDTO(name,toCoordinateDTO(start), toCoordinateDTO(end));
    }

    private static String createEffectiveValueString(EffectiveValue effectiveValue){
        if (effectiveValue == null || effectiveValue.getValue() == null){
            return " ";
        }

        if (effectiveValue.getCellType().getType() == Boolean.class){
            return effectiveValue.getValue().toString().toUpperCase();
        }

        return effectiveValue.getValue().toString();
    }

    public static SheetDTO toSheetDTO(SheetReadActions sheet) {
        Map<CoordinateDTO, CellDTO> cellDTOs = sheet.getCells().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> toCoordinateDTO(entry.getKey()),
                        entry -> toCellDTO(entry.getValue())));

        Map<String, RangeDTO> rangesDTO = sheet.getRanges().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> toRangeDTO(entry.getValue())));

        return new SheetDTO(sheet.getSheetName(), sheet.getNumOfRows(), sheet.getNumOfCols(),
                sheet.getRowSize(), sheet.getColumnSize(),cellDTOs, sheet.getVersion(),rangesDTO);
    }

    public static SheetDTO toSheetDTO(Map<Coordinate, Cell> cells, SheetReadActions sheet) {
        Map<CoordinateDTO, CellDTO> cellDTOs = cells.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> toCoordinateDTO(entry.getKey()),
                        entry -> toCellDTO(entry.getValue())));

        Map<String, RangeDTO> rangesDTO = sheet.getRanges().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> toRangeDTO(entry.getValue())));

        return new SheetDTO(sheet.getSheetName(), sheet.getNumOfRows(), sheet.getNumOfCols(),
                sheet.getRowSize(), sheet.getColumnSize(),cellDTOs, sheet.getVersion(),rangesDTO);
    }
}
