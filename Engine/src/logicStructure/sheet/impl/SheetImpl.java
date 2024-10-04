package logicStructure.sheet.impl;

import javafx.scene.Parent;
import logicStructure.expression.api.Expression;
import logicStructure.expression.parser.Convertor;
import logicStructure.sheet.DTO.RangeDTO;
import logicStructure.sheet.DTO.SheetDTO;
import logicStructure.sheet.DTO.SheetMapper;
import logicStructure.sheet.cell.api.CellType;
import logicStructure.sheet.cell.api.NegativeFunctionType;
import logicStructure.sheet.coordinate.CoordinateFactory;
import logicStructure.sheet.range.Range;
import logicStructure.sheet.range.RangeFactory;
import logicStructure.sheet.version.SheetVersionInfo;
import logicStructure.specialException.CircularDependencyException;
import logicStructure.sheet.api.SheetUpdateActions;
import logicStructure.sheet.cell.api.EffectiveValue;
import logicStructure.sheet.cell.api.Cell;
import logicStructure.sheet.cell.impl.CellImpl;
import logicStructure.sheet.coordinate.Coordinate;
import logicStructure.sheet.api.SheetReadActions;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class SheetImpl implements SheetUpdateActions, SheetReadActions, Cloneable, Serializable {

    private Map<Coordinate, Cell> activeCells;
    private List<Edge> edges = new ArrayList<>();
    private final String name;
    private final int numOfRows;
    private final int numOfCols;
    private int version;
    private int rowSize;
    private int colSize;
    private Map<String, Range> ranges= new HashMap<>();

    public SheetImpl(String name, int numOfRows, int numOfCols, int rowSize, int colSize) {
        this.name = name;
        this.activeCells = new HashMap<>();
        this.numOfRows = numOfRows;
        this.numOfCols = numOfCols;
        this.version = 1;
        this.rowSize = rowSize;
        this.colSize = colSize;
    }

    @Override
    public String getSheetName(){
        return name;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public int getNumOfRows(){
        return numOfRows;
    }

    @Override
    public int getNumOfCols(){
        return numOfCols;
    }

    @Override
    public int getRowSize() {
        return rowSize;
    }

    @Override
    public int getColumnSize() {
        return colSize;
    }

    @Override
    public Coordinate getMinCoordinateInSheet(){
        return CoordinateFactory.createCoordinate(0,1);
    }

    @Override
    public Coordinate getMaxCoordinateInSheet(){
        return CoordinateFactory.createCoordinate(numOfRows-1, numOfCols);
    }

    @Override
    public EffectiveValue getCellValueByCoordinate(Coordinate cellCoordinate) {
        return activeCells.get(cellCoordinate).getEffectiveValue();
    }

    @Override
    public Cell getCellByCoordinate(Coordinate coordinate) {
        return activeCells.get(coordinate);
    }

    @Override
    public Map<Coordinate,Cell> getCells(){
        return activeCells;
    }

    @Override
    public void addNewCell(String cellID, String originalValue, int version, int rowSize, int colSize) throws Exception {
        Coordinate newCoordinate = Convertor.convertFromCellIdToCoordinate(cellID, this);
        Cell cell = new CellImpl(newCoordinate, originalValue, version, rowSize, colSize);
        cell.calculateEffectiveValue(this);
        activeCells.put(newCoordinate, cell);
    }

    @Override
    public void addNewCell(Cell cell) throws Exception {
        cell = new CellImpl(cell.getCoordinate(), cell.getOriginalValue(), version, rowSize, colSize);
        cell.calculateEffectiveValue(this);
        activeCells.put(cell.getCoordinate(), cell);
    }

    @Override
    public void addEdge(Edge edge) {
        if(!edges.contains(edge)) {
            edges.add(edge);
        }

        Set<Coordinate> visited = new HashSet<>();
        Set<Coordinate> recStack = new HashSet<>();
        List<Coordinate> cycle = new ArrayList<>();

        for (Coordinate coord : activeCells.keySet()) {
            if (dfsDetectCycle(coord, visited, recStack, cycle)) {
                edges.remove(edge);
                String fromCoordinateID = edge.getFrom().createCellCoordinateString();
                String toCoordinateID = edge.getTo().createCellCoordinateString();
                String message = String.format("Cell %s is already depended on cell %s%n", toCoordinateID, fromCoordinateID);
                throw new CircularDependencyException(message + "Circular dependency detected in cells: ", cycle);
            }
        }
    }

    private boolean dfsDetectCycle(Coordinate v, Set<Coordinate> visited, Set<Coordinate> recStack, List<Coordinate> cycle) {
        if (recStack.contains(v)) {
            cycle.add(v);
            return true;
        }

        if (visited.contains(v)) {
            return false;
        }

        visited.add(v);
        recStack.add(v);

        for (Edge edge : edges) {
            if (edge.getFrom().equals(v)) {
                Coordinate w = edge.getTo();
                if (dfsDetectCycle(w, visited, recStack, cycle)) {
                    cycle.add(v);
                    return true;
                }
            }
        }

        recStack.remove(v);
        return false;
    }

    @Override
    public void removeEdge(Edge edge){
        edges.remove(edge);
    }

    @Override
    public void increaseVersion(){
        version++;
    }

    @Override
    public SheetVersionInfo setCellOriginalValueByCoordinate(String cellID, String newValue) throws Exception {
        Map<Coordinate, Cell> originalCells = new HashMap<>();
        for (Map.Entry<Coordinate, Cell> entry : this.activeCells.entrySet()) {
            originalCells.put(entry.getKey(), ((CellImpl) entry.getValue()).clone());
        }

        Coordinate newCoordinate = Convertor.convertFromCellIdToCoordinate(cellID, this);
        try{
            activeCells.get(newCoordinate).setOriginalValue(newValue, this);
        } catch (NullPointerException e){
            addNewCell(cellID.toUpperCase(), newValue, version, rowSize, colSize);
        } catch (IllegalArgumentException | IndexOutOfBoundsException e){
            if(originalCells.containsKey(newCoordinate)){
                activeCells.replace(newCoordinate, originalCells.get(newCoordinate));
            } else {
                activeCells.remove(newCoordinate);
            }
            throw e;
        }

        Integer numOfChangedCells = countAndUpdateChangedCellsVersion(originalCells);
        return new SheetVersionInfo(this, numOfChangedCells, version +1);
    }

    private Integer countAndUpdateChangedCellsVersion(Map<Coordinate, Cell> originalCells) {
        AtomicReference<Integer> countChangedCells = new AtomicReference<>(0);

        activeCells.forEach((coordinate, updatedCell) -> {
            Cell originalCell = originalCells.get(coordinate);

            if (originalCell != null) {
                EffectiveValue originalValue = originalCell.getEffectiveValue();
                EffectiveValue updatedValue = updatedCell.getEffectiveValue();

                if (valuesAreDifferent(originalValue, updatedValue)) {
                    updatedCell.setVersion(version + 1);
                    countChangedCells.updateAndGet(v -> v + 1);
                }
            } else if (updatedCell.getOriginalValue() != null && !updatedCell.getOriginalValue().isEmpty()) {
                updatedCell.setVersion(version + 1);
                countChangedCells.updateAndGet(v -> v + 1);
            }
        });

        return countChangedCells.get();
    }

    private boolean valuesAreDifferent(EffectiveValue originalValue, EffectiveValue updatedValue) {
        if (originalValue == null && updatedValue == null) {
            return false;
        }
        if (originalValue == null || updatedValue == null) {
            return true;
        }
        return !originalValue.equals(updatedValue);
    }

    @Override
    public void updateCells() throws Exception {
        List<Cell> sortedCells = topologicalSort();
        for (Cell cell : sortedCells) {
            cell.calculateEffectiveValue(this);
        }
    }

    private List<Cell> topologicalSort() {
        Map<Coordinate, Integer> inDegree = new HashMap<>();
        Queue<Cell> queue = new LinkedList<>();
        List<Cell> sortedCells = new ArrayList<>();
        Set<Coordinate> visited = new HashSet<>();
        List<Coordinate> cycle = new ArrayList<>();

        for (Cell cell : activeCells.values()) {
            inDegree.put(cell.getCoordinate(), 0);
        }

        for (Edge edge : edges) {
            inDegree.put(edge.getTo(), inDegree.get(edge.getTo()) + 1);
        }

        for (Cell cell : activeCells.values()) {
            if (inDegree.get(cell.getCoordinate()) == 0) {
                queue.add(cell);
            }
        }

        while (!queue.isEmpty()) {
            Cell cell = queue.poll();
            sortedCells.add(cell);
            visited.add(cell.getCoordinate());

            for (Edge edge : edges) {
                if (edge.getFrom().equals(cell.getCoordinate())) {
                    Coordinate dependentCoord = edge.getTo();
                    inDegree.put(dependentCoord, inDegree.get(dependentCoord) - 1);
                    if (inDegree.get(dependentCoord) == 0) {
                        queue.add(getCellByCoordinate(dependentCoord));
                    } else if (visited.contains(dependentCoord)) {
                        cycle.add(dependentCoord);
                        cycle.add(cell.getCoordinate());
                        throw new CircularDependencyException("Circular dependency detected", cycle);
                    }
                }
            }
        }

        if (sortedCells.size() != activeCells.size()) {
            throw new CircularDependencyException("Circular dependency detected in the cells", cycle);
        }

        return sortedCells;
    }

    @Override
    public SheetImpl clone() {
        try {
            SheetImpl clonedSheet = (SheetImpl) super.clone();

            clonedSheet.activeCells = new HashMap<>();
            for (Map.Entry<Coordinate, Cell> entry : this.activeCells.entrySet()) {
                clonedSheet.activeCells.put(entry.getKey(), ((CellImpl) entry.getValue()).clone());
            }

            clonedSheet.edges = new ArrayList<>();
            for (Edge edge : this.edges) {
                clonedSheet.edges.add(new Edge(edge.getFrom(), edge.getTo()));
            }

            return clonedSheet;

        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported", e);
        }
    }

    @Override
    public void addRange(Range range) {
        if (RangeFactory.isRangeExist(range.getName())) {
            throw new IllegalArgumentException("Range name already exists");
        }

        ranges.put(range.getName(), range);
    }

    @Override
    public void addRange(String name, String from, String to) throws Exception {
        if (RangeFactory.isRangeExist(name)) {
            throw new IllegalArgumentException("Range name already exists");
        }

        Range range = Convertor.convertRange(from, to, name, this);
        ranges.put(name, range);

        //remove
        System.out.println(String.format("Range %s: %s -> %s added successfully\n", name, from, to));
    }

    @Override
    public void deleteRange(String name) {
        if (RangeFactory.getRange(name).getIsUsedInFunction()) {
            throw new IllegalStateException("Cannot delete range!\nIt is used in functions");
        }

        RangeFactory.removeRange(name);
        ranges.remove(name);
    }

    @Override
    public Map<String, Range> getRanges() {
        return ranges;
    }

    @Override
    public SheetDTO sortRows(RangeDTO range, List<String> columnSortOrder) throws Exception {
        List<List<Cell>> rowsToSort = getRowsInRange(range);

        rowsToSort.sort((row1, row2) -> {
            for (String columnId : columnSortOrder) {
                int columnIndex = (columnId.toUpperCase().charAt(0) - 'A') + 1;
                int compareResult = 0;

                Cell cell1 = row1.get(columnIndex - range.getStartCoordinate().getColumn());
                Cell cell2 = row2.get(columnIndex - range.getStartCoordinate().getColumn());

                if(cell1 != null && cell2 != null) {
                    compareResult = cell1.compareCells(cell2);
                } else if(cell1 != null && cell2 == null) {
                     compareResult = -1;//cell1.compareCells(cell2);
                } else if(cell1 == null && cell2 != null) {
                    compareResult = 1;//cell2.compareCells(cell1);
                }

                if (compareResult != 0) {
                    return compareResult;
                }
            }
            return 0;
        });

        //SheetImpl copySheet = this.clone();
        // עדכון הגיליון עם השורות הממוינות
        return updateSheetWithSortedRows(rowsToSort, range);

    }

    private List<List<Cell>> getRowsInRange(RangeDTO range) {
        List<List<Cell>> rowsInRange = new ArrayList<>();

        for (int row = range.getStartCoordinate().getRow(); row <= range.getEndCoordinate().getRow(); row++) {
            List<Cell> rowCells = new ArrayList<>();
            for (int col = range.getStartCoordinate().getColumn(); col <= range.getEndCoordinate().getColumn(); col++) {
                rowCells.add(activeCells.get(CoordinateFactory.createCoordinate(row, col)));
            }
            rowsInRange.add(rowCells);
        }

        return rowsInRange;
    }

    private SheetDTO updateSheetWithSortedRows(List<List<Cell>> sortedRows, RangeDTO range) throws Exception {
        Map<Coordinate, Cell> sheetValuesInString = new HashMap<>();

        for (int rowIndex = 0 ; rowIndex < numOfRows; rowIndex++) {
            for (int colIndex = 1; colIndex <= numOfCols; colIndex++) {
                Coordinate coordinate = CoordinateFactory.createCoordinate(rowIndex,colIndex);
                Cell cell = this.activeCells.get(coordinate);

                String value = " ";
                if(cell != null && cell.getEffectiveValue() != null){
                    value = String.valueOf(cell.getEffectiveValue().getValue());
                }
                Cell newCell = new CellImpl(coordinate, value, version ,rowSize, colSize);
                newCell.setEffectiveValueAsOriginal();
                sheetValuesInString.put(coordinate,newCell);
            }
        }

        for (int rowIndex = range.getStartCoordinate().getRow(); rowIndex <= range.getEndCoordinate().getRow(); rowIndex++) {
            List<Cell> sortedRow = sortedRows.get(rowIndex - range.getStartCoordinate().getRow());
            for (int colIndex = 0; colIndex < sortedRow.size(); colIndex++) {
                Coordinate coordinate = CoordinateFactory.createCoordinate(rowIndex, range.getStartCoordinate().getColumn() + colIndex);
                Cell cell = sortedRow.get(colIndex);
                String value = "";
                if(cell != null && cell.getEffectiveValue() != null){
                    value = String.valueOf(cell.getEffectiveValue().getValue());
                }
                Cell newCell = new CellImpl(coordinate, value, version ,rowSize, colSize);
                newCell.setEffectiveValueAsOriginal();
                sheetValuesInString.put(coordinate,newCell);
            }
        }

        return SheetMapper.toSheetDTO(sheetValuesInString, this);
    }

    @Override
    public SheetImpl applyFilter(RangeDTO range, char selectedColumn, List<String> selectedValues) throws Exception {

        Coordinate startCell = CoordinateFactory.createCoordinate(range.getStartCoordinate().getRow(), range.getStartCoordinate().getColumn());
        Coordinate endCell = CoordinateFactory.createCoordinate(range.getEndCoordinate().getRow(), range.getEndCoordinate().getColumn());

        SheetImpl filteredSheet = new SheetImpl(name + " - Filtered", numOfRows, numOfCols, rowSize, colSize);
        int columnIndex = (selectedColumn - 'A') + 1;
        Map<Coordinate, Cell> filterCells = filteredSheet.getCells();

        for (int row = startCell.getRow(); row <= endCell.getRow(); row++) {
            Coordinate currentCoordinate = CoordinateFactory.createCoordinate(row, columnIndex);
            Cell cell = activeCells.get(currentCoordinate);
            if (cell != null) {
                String cellValue = (cell.getEffectiveValue() != null ?
                        (cell.getEffectiveValue().getValue() != null ?
                                getEffectiveValueAsString(cell.getEffectiveValue()) : "") : "");

                if (selectedValues.contains(cellValue)) {
                    addCellToFilteredSheet(filterCells, row, startCell.getColumn(), endCell.getColumn());
                }
            } else {
                if (selectedValues.contains(" ") || selectedValues.contains("") || selectedValues.contains("empty") || selectedValues.contains("Empty")) {
                    addCellToFilteredSheet(filterCells, row, startCell.getColumn(), endCell.getColumn());
                }
            }
        }

        for(int row = 0; row<startCell.getRow(); row++){
            Coordinate currentCoordinate = CoordinateFactory.createCoordinate(row, columnIndex);
            Cell cell = activeCells.get(currentCoordinate);
            if (cell != null) {
                addCellToFilteredSheet(filterCells, row, startCell.getColumn(), endCell.getColumn());
            }
        }

        for(int row = endCell.getRow()+1; row<numOfRows; row++){
            Coordinate currentCoordinate = CoordinateFactory.createCoordinate(row, columnIndex);
            Cell cell = activeCells.get(currentCoordinate);
            if (cell != null) {
                addCellToFilteredSheet(filterCells, row, startCell.getColumn(), endCell.getColumn());
            }
        }

            return filteredSheet;
    }

    private String getEffectiveValueAsString(EffectiveValue effectiveValue) {
        if(effectiveValue != null) {
            CellType type = effectiveValue.getCellType();
            switch (type) {
                case STRING -> {
                    return effectiveValue.extractValueWithExpectation(String.class);
                }
                case BOOLEAN -> {
                    Boolean value = effectiveValue.extractValueWithExpectation(Boolean.class);
                    return String.valueOf(value).toUpperCase();
                }
                case DOUBLE -> {
                    Double value = effectiveValue.extractValueWithExpectation(Double.class);
                    if (value % 1 == 0) {
                        return String.valueOf(value.intValue());
                    } else {
                        DecimalFormat df = new DecimalFormat("#.00");
                        return df.format(value);
                    }
                }
                case INTEGER -> {
                    Integer value = effectiveValue.extractValueWithExpectation(Integer.class);
                    return String.valueOf(value);
                }
                default -> {
                    return String.valueOf(effectiveValue.extractValueWithExpectation(effectiveValue.getCellType().getType()));
                }
            }
        }
        return null;
    }

    private void addCellToFilteredSheet(Map<Coordinate,Cell> filterCells, int row, int startCol, int endCol) {
//        for (int col = startCol; col <= endCol; col++) {
//            Coordinate newCoordinate = CoordinateFactory.createCoordinate(row, col);
//            Cell originalCell = activeCells.get(newCoordinate);
//            if (originalCell != null) {
//                filterCells.put(newCoordinate, originalCell.clone());
//            }
//        }
        for (int col = 1; col <= numOfCols; col++) {
            Coordinate newCoordinate = CoordinateFactory.createCoordinate(row, col);
            Cell originalCell = activeCells.get(newCoordinate);
            if (originalCell != null) {
                filterCells.put(newCoordinate, originalCell.clone());
            }
        }
    }
}