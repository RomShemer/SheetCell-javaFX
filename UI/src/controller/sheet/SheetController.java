package controller.sheet;

import controller.*;
import controller.api.CellSelectionListener;
import controller.api.CellUnselectionListener;
import controller.commands.CommandController;
import controller.sheet.api.SheetControllerFilterAction;
import controller.sheet.api.SheetControllerSortActions;
import controller.sheet.styling.StylingPerCellLabel;
import controller.sheet.styling.StylingPerColumn;
import controller.skinStyle.SkinChangeListener;
import controller.skinStyle.SkinManager;
import controller.skinStyle.SkinOption;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import logicStructure.sheet.DTO.*;
import javafx.scene.control.Label;

import java.util.*;

public class SheetController implements CellUnselectionListener, SheetControllerFilterAction, SheetControllerSortActions, SkinChangeListener {
    private GridPane gridPane;
    private String tabID;
    private String tabName;
    private ObjectProperty<GridPane> gridPaneProperty = new SimpleObjectProperty<>(new GridPane());
    private AppController mainController;
    private Map<String, Label> cellLabelMap = new HashMap<>(); // מפה לשמירת תאים לפי ID
    private Map<String, Label> cellLabelReadOnlyMap = new HashMap<>(); // מפה לשמירת תאים לפי ID
    private Map<String, StylingPerColumn> stylingPerColumnMap = new HashMap<>();
    private Map<String, StylingPerCellLabel> stylingPerCellsMap = new HashMap<>();
    private Integer rowsHeightUnits;
    private Integer columnWidthUnits;

    private ObjectProperty<SheetDTO> sheetDTOObjectProperty = new SimpleObjectProperty<>();
    private IntegerProperty selectedColumnIndex = new SimpleIntegerProperty(-1);// אינדקס העמודה שנבחרה
    private IntegerProperty selectedRowIndex = new SimpleIntegerProperty(-1);
    private ObjectProperty<CoordinateDTO> selectedCellCoordinate = new SimpleObjectProperty<>(new CoordinateDTO(-1, -1));

    private ColumnConstraints colConst = new ColumnConstraints();
    private double columnConstraintsInitValue;
    private RowConstraints rowConst = new RowConstraints();
    private double rowConstraintsInitValue;
    private boolean isReadonly = false;

    private ObjectProperty<StylingPerColumn> selectedColumnStylingProperty = new SimpleObjectProperty<>(new StylingPerColumn(this));
    private ObjectProperty<StylingPerCellLabel> selectedCellCoordinateStylingProperty = new SimpleObjectProperty<>(new StylingPerCellLabel(this));

    private CommandController commandController;
    private List<CellSelectionListener> cellSelectionListeners = new ArrayList<>();
    private boolean isDragging = false;
    private ArrayList<Label> draggedLabels = new ArrayList<>();
    private boolean isResizing = false;
    final double[] startXResize = new double[1];
    final double[] startYResize = new double[1];

    public SheetController(){
        gridPane = gridPaneProperty.get();
        gridPaneProperty.addListener((observable, oldValue, newValue) -> {
            gridPane.getChildren().clear();
            gridPane = newValue;
        });
    }

    @Override
    public void onSkinChange(SkinOption newSkin) {

        gridPane.getStylesheets().removeAll(SkinOption.DEFAULT.getSheet(), SkinOption.DARK_MODE.getSheet(),
                SkinOption.MONOCHROME.getSheet(), SkinOption.VIBRANT.getSheet());

        gridPane.getStylesheets().add(newSkin.getSheet());
    }

    public void setSheetDTO(SheetDTO sheetDTO) {
        this.sheetDTOObjectProperty.set(sheetDTO);

        // קביעת מגבלות גובה ורוחב דינאמיים לפי אחוזים
        initColAndRowSize();
    }

    @Override
    public void setTabID(String tabID) {
        this.tabID = tabID;
    }

    @Override
    public void setTabName(String name){
        this.tabName = name;
    }

    @Override
    public String getTabID() {
        return this.tabID;
    }

    @Override
    public String getTabName(){
        return this.tabName;
    }

    @Override
    public void setGridPane(GridPane gridPane) {
        this.gridPane = gridPane;
    }

    public void setCommandController(CommandController commandController) {
        this.commandController = commandController;

        if(commandController == null){
            return;
        }

        commandController.setAllTitlePanesDisable();

        selectedColumnIndex.addListener((observable, oldValue, newValue) -> {
            if(newValue.intValue() != -1){
                if (!draggedLabels.isEmpty()){
                    resetRangeHighlights(draggedLabels.getFirst(), draggedLabels.getLast());
                    draggedLabels.clear(); // Clear the list after dragging ends

                }
                if(commandController!=null){
                    commandController.resetColumnSliderPos(newValue.intValue());
                }

                String colID = StylingPerColumn.convertColumnIDFromIndex(newValue.intValue());
                Pos alignmentPos;
                String overflow;
                if(stylingPerColumnMap.containsKey(colID)){
                    alignmentPos = stylingPerColumnMap.get(colID).getTextAlignment();
                    overflow = stylingPerColumnMap.get(colID).getOverflowOption().toString();
                } else {
                    alignmentPos = StylingPerColumn.TextAlignmentOptions.getDefaultAlignmentPosition();
                    overflow = StylingPerColumn.OverflowOptions.getDefaultOverflow().toString();
                }

                String alignment = StylingPerColumn.TextAlignmentOptions.extractValueFromPosToString(alignmentPos);
                if(commandController!=null){
                    commandController.setColumnAlignmentComboBoxShowValue(alignment);
                    commandController.setColumnOverflowComboBoxShowValue(overflow);
                    commandController.handleOpeningColumnStylePane();
                }
            }
        });
        selectedRowIndex.addListener((observable, oldValue, newValue) -> {
            if(newValue.intValue() != -1){
                if (!draggedLabels.isEmpty()){
                    resetRangeHighlights(draggedLabels.getFirst(), draggedLabels.getLast());
                    draggedLabels.clear(); // Clear the list after dragging ends
                }

                if(commandController!=null){
                    commandController.handleOpeningRowStylePane();
                    commandController.resetRowSliderPos(newValue.intValue());
                }
            }
        });
        selectedCellCoordinate.addListener((observable, oldValue, newValue) -> {
            if(newValue == null){
                Map<String, Label> cellMap = isReadonly ? cellLabelReadOnlyMap : cellLabelMap;
                cellMap.values().forEach(label -> label.getStyleClass().removeAll("highlight", "highlightInfluentedCells", "highlightDependedCells"));
                if(commandController!=null){
                    commandController.setAllPaneDisable();
                    //mainController.
                }
                if (!draggedLabels.isEmpty()){
                    resetRangeHighlights(draggedLabels.getFirst(), draggedLabels.getLast());
                    draggedLabels.clear(); // Clear the list after dragging ends
                }
            }
        });
        colConst.percentWidthProperty().addListener((observable, oldValue, newValue) -> {
            if (commandController != null) {
                commandController.setWidthSlider(newValue.doubleValue());
            }
        });
        rowConst.percentHeightProperty().addListener((observable, oldValue, newValue) -> {
            if(commandController!=null){
                commandController.setWidthSlider(newValue.doubleValue());
            }
        });
        selectedColumnStylingProperty.addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                String colID = newValue.getColumnID();
                if(stylingPerColumnMap.containsKey(colID)){
                    stylingPerColumnMap.replace(colID, newValue);
                } else {
                    stylingPerColumnMap.put(colID, newValue);
                }
                handleSelectedCloumnStyling(newValue);
            }
        });
        selectedCellCoordinateStylingProperty.addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                String selectedCellID = newValue.getCellID();
                if(stylingPerCellsMap.containsKey(selectedCellID)){
                    stylingPerCellsMap.replace(selectedCellID, newValue);
                } else {
                    stylingPerCellsMap.put(selectedCellID, newValue);
                }
                handleSelectedCellStyling(newValue);
            }
        });
    }

    // Initialize the dragging and selection functionality
    private void initializeDragAndDrop() {
        gridPane.addEventHandler(MouseEvent.DRAG_DETECTED, event -> {
            Node node = event.getPickResult().getIntersectedNode();
            Label cell = getLabelFromNode(node);
            if (cell != null) {
                CoordinateDTO startCoordinate = getCellCoordinate(cell);

                if (startCoordinate != null) {
                    if(startCoordinate.getRow() == 0 || startCoordinate.getColumn() == 0){
                        isResizing = true;
                        draggedLabels.remove(startCoordinate);
                        resetRangeHighlights(cell, cell);
                    } else {
                        if(!draggedLabels.isEmpty()){
                            resetRangeHighlights(draggedLabels.getFirst(), draggedLabels.getLast());
                            draggedLabels.clear();
                        }

                        selectedCellCoordinate.set(null);

                        isDragging = true;
                        draggedLabels.clear(); // Clear previous dragged cells
                        draggedLabels.add(cell); // Add the starting cell
                        highlightSelectedRangeCells(cell); // Highlight the first selected cell
                        gridPane.setCursor(Cursor.MOVE);
                        gridPane.startFullDrag(); // Start the full drag operation on the grid
                    }
                }
            }
        });

        gridPane.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (isDragging && !isResizing) {
                Node node = event.getPickResult().getIntersectedNode();
                Label cell = getLabelFromNode(node);

                if (cell != null && !draggedLabels.contains(cell)) {
                    draggedLabels.add(cell); // Add cell to dragged list if not already there
                    highlightRange(draggedLabels.get(0), cell); // Highlight the range
                }
            }
        });

        gridPane.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (isDragging && !isResizing) {
                isDragging = false;
                Label lastCell = draggedLabels.get(draggedLabels.size() - 1);
                highlightRange(draggedLabels.get(0), lastCell); // Highlight the range

                // Notify the listener about the selection range
                CoordinateDTO start = getCellCoordinate(draggedLabels.get(0));
                CoordinateDTO end = getCellCoordinate(lastCell);
                if (start != null && end != null && !cellSelectionListeners.isEmpty()) {
                    cellSelectionListeners.forEach(listener -> listener.onCellSelection(
                            start.createCellCoordinateString(),
                            end.createCellCoordinateString(), this));
                }
                gridPane.setCursor(Cursor.DEFAULT);
            }
        });

        gridPane.addEventHandler(MouseEvent.MOUSE_PRESSED,event ->{
            double cellHeight = gridPane.getHeight() / gridPane.getRowConstraints().size();
            double cellWidth = gridPane.getWidth() / gridPane.getColumnConstraints().size();
            int rowIndex = (int) (event.getY() / cellHeight);
            int columnIndex = (int) (event.getX() / cellWidth);

            if(rowIndex == 0 && columnIndex == 0){
                if(!draggedLabels.isEmpty()){
                    resetRangeHighlights(draggedLabels.getFirst(), draggedLabels.getLast());
                    draggedLabels.clear();
                }
                selectedCellCoordinate.set(null);
            }
        });
    }

    @Override
    public void onCellUnselection(String startCell, String endCell){
        resetRangeHighlights(cellLabelMap.get(startCell), cellLabelMap.get(endCell));
        draggedLabels.clear();
    }

    private Label getLabelFromNode(Node node) {
        // If the node is a Label, return it directly
        if (node instanceof Label) {
            return (Label) node;
        }

        // If the node is LabeledText or other part of a Label, return its parent Label
        if (node.getParent() instanceof Label) {
            return (Label) node.getParent();
        }

        // Otherwise, return null if no valid Label is found
        return null;
    }

    private CoordinateDTO getCellCoordinate(Node cell) {
        Integer row = GridPane.getRowIndex(cell);
        Integer col = GridPane.getColumnIndex(cell);
        if (row == null || col == null) {
            return null;
        }
        return SheetMapper.toCoordinateDTO(row - 1, col); // Convert to CoordinateDTO
    }

    public void highlightSelectedRangeCells(List<CoordinateDTO> rangeCells){
        if(!draggedLabels.isEmpty()){
            resetRangeHighlights(draggedLabels.getFirst(), draggedLabels.getLast());
        }

        for (CoordinateDTO coordinate : rangeCells) {
            Label label = getLableAcoordingToCoordinate(coordinate);
            label.getStyleClass().add("highlight-Range-selected");
        }
    }

    private void highlightSelectedRangeCells(Label cell) {
        cell.getStyleClass().remove("highlight-Range-selected");
        cell.getStyleClass().add("highlight-Range");
    }

    public void removeHighlightRangeCells(List<CoordinateDTO> rangeCells){
        if(!draggedLabels.isEmpty()){
            highlightRange(draggedLabels.getFirst(), draggedLabels.getLast());
        }

        for (CoordinateDTO coordinate : rangeCells) {
            Label label = getLableAcoordingToCoordinate(coordinate);
            if(label!=null){
                label.getStyleClass().remove("highlight-Range-selected");
            }
        }
    }

    private void highlightRange(Node start, Node current) {
        resetRangeHighlights(start,current); // Reset all highlights before applying new ones
        int rowStart = GridPane.getRowIndex(start);
        int rowEnd = GridPane.getRowIndex(current);
        int colStart = GridPane.getColumnIndex(start);
        int colEnd = GridPane.getColumnIndex(current);

        if(rowStart == 0 || colStart==0){
            return;
        }

        for (int row = Math.min(rowStart, rowEnd); row <= Math.max(rowStart, rowEnd); row++) {
            for (int col = Math.min(colStart, colEnd); col <= Math.max(colStart, colEnd); col++) {
                CoordinateDTO coordinate = SheetMapper.toCoordinateDTO(row - 1, col);
                Label cell = cellLabelMap.get(coordinate.createCellCoordinateString());
                if (cell != null) {
                    highlightSelectedRangeCells(cell);
                }
            }
        }
    }

    // Helper method to reset all highlights
    private void resetRangeHighlights(Node startCell, Node endCell) {
        int rowStart = GridPane.getRowIndex(startCell);
        int rowEnd = GridPane.getRowIndex(endCell);
        int colStart = GridPane.getColumnIndex(startCell);
        int colEnd = GridPane.getColumnIndex(endCell);

        for (int row = Math.min(rowStart, rowEnd); row <= Math.max(rowStart, rowEnd); row++) {
            for (int col = Math.min(colStart, colEnd); col <= Math.max(colStart, colEnd); col++) {
                CoordinateDTO coordinate = SheetMapper.toCoordinateDTO(row - 1, col);
                Label cell = cellLabelMap.get(coordinate.createCellCoordinateString());
                if (cell != null) {
                    cell.getStyleClass().remove("highlight-Range");
                }
            }
        }
    }

    public void setOnCellSelectionListener(CellSelectionListener listenerToAdd) {
        this.cellSelectionListeners.add(listenerToAdd);
        if(!draggedLabels.isEmpty()){
            CoordinateDTO start = getCellCoordinate(draggedLabels.getFirst());
            CoordinateDTO end = getCellCoordinate(draggedLabels.getLast());
            this.cellSelectionListeners.forEach(listener -> listener.onCellSelection(start.createCellCoordinateString(),
                    end.createCellCoordinateString(), this));
        }
    }

    @Override
    public void removeFromOnCellSelectionListener(CellSelectionListener listenerToRemove) {
        if(cellSelectionListeners.contains(listenerToRemove)){
            this.cellSelectionListeners.remove(listenerToRemove);
        }
    }

    private void handleSelectedCloumnStyling(StylingPerColumn stylingPerColumn) {
        for(int row = 1; row < gridPane.getRowCount(); row++){
            int finalRow = row;
            Label label = (Label) gridPane.getChildren().stream()
                    .filter(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) == finalRow &&
                            GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) == selectedColumnIndex.get() &&
                            node instanceof Label)
                    .findFirst()
                    .orElse(null);
            if (label != null) {
                CoordinateDTO coordinate = new CoordinateDTO(row - 1, selectedColumnIndex.get());
                label.getStyleClass().removeAll("center", "right", "left");
                label.getStyleClass().removeAll("wrap", "clip");
                label.setAlignment(stylingPerColumn.getTextAlignment());
                label.getStyleClass().add(stylingPerColumn.getOverflowCss());
                label.setPrefWidth(stylingPerColumn.getColumnWidth());
                cellLabelMap.put(coordinate.createCellCoordinateString(),label);
            }
        }
    }

    private void handleSelectedCellStyling(StylingPerCellLabel stylingPerCellLabel) {
        Label label = cellLabelMap.get(stylingPerCellLabel.getCellID());

        if (label != null) {
            //label.setStyle(stylingPerCellLabel.getBackground());
            label.setBackground(stylingPerCellLabel.getBackground());
            label.setTextFill(stylingPerCellLabel.getTextColor());
            if(commandController!=null){
                commandController.handleOpeningCellStylePaneColSelected();
            }
        }

    }

    public double getInitColumnConstraints(Optional<Integer> columnIndex){
        if (!columnIndex.isPresent()){
            return columnConstraintsInitValue;
        }

        if(stylingPerColumnMap.containsKey(columnIndex)){
            double value = Math.max(gridPane.getColumnConstraints().get(columnIndex.get()).getPercentWidth(),
                    stylingPerColumnMap.get(columnIndex).getColumnWidth());
            return value;
        }

        return columnConstraintsInitValue;
    }

    public double getInitRowConstraints(){
        return rowConstraintsInitValue;
    }

    public void initColAndRowSize(){
        if(rowsHeightUnits == null){
            rowConstraintsInitValue = 100.0 / (sheetDTOObjectProperty.getValue().getRows() + 1);
        } else {
            rowConstraintsInitValue = rowsHeightUnits;
        }

        if(rowsHeightUnits == null){
            columnConstraintsInitValue =100.0 / (sheetDTOObjectProperty.getValue().getColumns() + 1);
        } else {
            columnConstraintsInitValue = columnWidthUnits;
        }

        for (int i = 0; i <= sheetDTOObjectProperty.getValue().getColumns(); i++) {
            colConst.setPercentWidth(columnConstraintsInitValue); // חלוקה שווה
            gridPane.getColumnConstraints().add(colConst);
        }

        for (int i = 0; i <= sheetDTOObjectProperty.getValue().getRows(); i++) {
            rowConst.setPercentHeight(rowConstraintsInitValue); // חלוקה שווה
            gridPane.getRowConstraints().add(rowConst);
        }
    }

    public ObjectProperty<SheetDTO> sheetDTOProperty() {
        return sheetDTOObjectProperty;
    }

    public ObjectProperty<GridPane> getGridPaneProperty() {
        return gridPaneProperty;
    }

    public IntegerProperty getColumnIndexProperty() {
        return selectedColumnIndex;
    }

    public IntegerProperty getRowIndexProperty() {
        return selectedRowIndex;
    }

    public ObjectProperty<CoordinateDTO> getSelectedCellCoordinate() {
        return selectedCellCoordinate;
    }

    public ObjectProperty<StylingPerCellLabel> getSelectedCellStylingProperty() {
        return selectedCellCoordinateStylingProperty;
    }

    public ObjectProperty<StylingPerColumn> getSelectedColumnStylingProperty() {
        return selectedColumnStylingProperty;
    }

    public StylingPerColumn getStylingPerColumn(Integer columnIndex) {
        String colID = StylingPerColumn.convertColumnIDFromIndex(columnIndex);
        if(stylingPerColumnMap.containsKey(colID)){
            return stylingPerColumnMap.get(colID);
        } else {
            StylingPerColumn stylingPerColumn = new StylingPerColumn(this, columnIndex);
            stylingPerColumnMap.put(colID, stylingPerColumn);
            return stylingPerColumn;
        }
    }

    public StylingPerCellLabel getStylingPerCellLabel(String selectedCellID) {
        if(stylingPerCellsMap.containsKey(selectedCellID)){
            return stylingPerCellsMap.get(selectedCellID);
        } else {
            StylingPerCellLabel stylingPerCellLabel = new StylingPerCellLabel(this, selectedCellID);
            stylingPerCellsMap.put(selectedCellID, stylingPerCellLabel);
            return stylingPerCellLabel;
        }
    }

    public void setIsReadOnly(boolean isReadOnly){
        this.isReadonly = isReadOnly;
    }

    public void createGridPane(){
        gridPane.setHgap(8);
        gridPane.setVgap(8);
        gridPane.setPadding(new Insets(20));

        gridPane.getStylesheets().add(SkinManager.getCurrentSkinMode().getSheet());

        createGridFromSheetDTO(gridPane, sheetDTOObjectProperty.getValue());
        initializeDragAndDrop();
        gridPane.setGridLinesVisible(true); // קווי רשת ברורים
    }

    public void clearGrid(boolean isFullClear) {
        gridPane.getChildren().clear();

        gridPaneProperty.set(new GridPane());

        sheetDTOProperty().set(null);
        if(isFullClear){
            cellLabelMap.clear(); // נקה את המפה כדי להבטיח שאין מידע ישן
        }
    }

    public GridPane getGridPane() {
        return gridPane;
    }

    public void createGridFromSheetDTO(GridPane gridPane, SheetDTO sheetDTO) {
        Map<CoordinateDTO, CellDTO> cells = sheetDTO.getCells();
        int rows = sheetDTO.getRows();
        int columns = sheetDTO.getColumns();
        rowsHeightUnits = sheetDTO.getRowSize();
        columnWidthUnits = sheetDTO.getColumnSize();
        StylingPerColumn stylingPerColumn = new StylingPerColumn(this);
        stylingPerColumn.setColumnWidth((double)columnWidthUnits);
        StylingPerCellLabel stylingPerCellLabel = new StylingPerCellLabel(this);

        // יצירת כותרות לעמודות (A, B, C...)
        for (int col = 0; col < columns; col++) {
            String colHeader = String.valueOf((char) ('A' + col));

            if(!stylingPerColumnMap.containsKey(colHeader)){
                stylingPerColumnMap.put(colHeader,new StylingPerColumn(this,colHeader));
            } else {
                stylingPerColumn = stylingPerColumnMap.get(colHeader);
            }

            Label colLabel = createCellLabel(colHeader, "header", colHeader, stylingPerColumn, stylingPerCellLabel);
            cellLabelMap.put(colHeader, colLabel);


            final int columnIndex = col; // שמירת האינדקס של העמודה
            colLabel.setOnMouseClicked(event -> {
                selectedColumnIndex.set(-1);//reset
                selectedColumnIndex.set(columnIndex + 1);
                System.out.println("Selected column: " + selectedColumnIndex);
                mainController.setUpdateButtonDisale(true);
                highlightCell(colHeader);
            });


            colLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
                colLabel.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
                    double mouseX = e.getX(); // מיקום העכבר על ציר ה-X בתוך ה-Label
                    double mouseY = e.getY(); // מיקום העכבר על ציר ה-Y בתוך ה-Label
                    double width = colLabel.getWidth(); // רוחב ה-Label
                    double height = colLabel.getHeight(); // גובה ה-Label

                    double edgeThreshold = 3.0; // מרחק של 3 פיקסלים מקצה ה-Label

                    boolean isNearLeftEdge = mouseX <= edgeThreshold;
                    boolean isNearRightEdge = mouseX >= (width - edgeThreshold);
                    boolean isNearTopEdge = mouseY <= edgeThreshold;
                    boolean isNearBottomEdge = mouseY >= (height - edgeThreshold);

                    // אם העכבר קרוב לאחד מהקצוות, נציג סמן ידית; אחרת, נשאיר את הסמן הרגיל
                    if (isNearLeftEdge || isNearRightEdge || isNearTopEdge || isNearBottomEdge) {
                        colLabel.setCursor(Cursor.H_RESIZE); // שנה את סמן העכבר ליד כאשר הוא קרוב לקצה
                    } else {
                        colLabel.setCursor(Cursor.DEFAULT);
                    }
                });
            });


            colLabel.addEventHandler(MouseEvent.DRAG_DETECTED, event ->{
                selectedColumnIndex.set(columnIndex+1);
                isResizing = true;
                startXResize[0] = event.getSceneX(); // שמירת מיקום X של העכבר בהתחלה
            });
            colLabel.addEventHandler(MouseEvent.MOUSE_DRAGGED,event ->{
                double currentX = event.getSceneX(); // מיקום עכבר נוכחי
                double deltaX = currentX - startXResize[0]; // ההפרש בין מיקום נוכחי למיקום התחלתי
                if (commandController!=null){
                    commandController.setWidthSlider(deltaX);
                }
            });
            colLabel.addEventHandler(MouseEvent.MOUSE_RELEASED,event->{
                selectedColumnIndex.set(columnIndex+1);
                isResizing = false;
            });

            gridPane.add(colLabel, col + 1, 0);
        }

        // יצירת כותרות לשורות (1, 2, 3...)
        for (int row = 0; row < rows; row++) {
            String rowHeader = String.valueOf(row + 1);
            Label rowLabel = createCellLabel(rowHeader, "header", rowHeader, stylingPerColumn, stylingPerCellLabel);
            cellLabelMap.put(rowHeader, rowLabel);

            final int rowIndex = row;
            rowLabel.setOnMouseClicked(event -> {
                selectedRowIndex.set(-1); //reset
                selectedRowIndex.set(rowIndex + 1);
                System.out.println("Selected row: " + selectedRowIndex);
                mainController.setUpdateButtonDisale(true);
                highlightCell(rowHeader);
            });

            rowLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
                rowLabel.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
                    double mouseX = e.getX(); // מיקום העכבר על ציר ה-X בתוך ה-Label
                    double mouseY = e.getY(); // מיקום העכבר על ציר ה-Y בתוך ה-Label
                    double width = rowLabel.getWidth(); // רוחב ה-Label
                    double height = rowLabel.getHeight(); // גובה ה-Label

                    double edgeThreshold = 3.0; // מרחק של 3 פיקסלים מקצה ה-Label

                    boolean isNearLeftEdge = mouseX <= edgeThreshold;
                    boolean isNearRightEdge = mouseX >= (width - edgeThreshold);
                    boolean isNearTopEdge = mouseY <= edgeThreshold;
                    boolean isNearBottomEdge = mouseY >= (height - edgeThreshold);

                    // אם העכבר קרוב לאחד מהקצוות, נציג סמן ידית; אחרת, נשאיר את הסמן הרגיל
                    if (isNearLeftEdge || isNearRightEdge || isNearTopEdge || isNearBottomEdge) {
                        rowLabel.setCursor(Cursor.V_RESIZE); // שנה את סמן העכבר ליד כאשר הוא קרוב לקצה
                    } else {
                        rowLabel.setCursor(Cursor.DEFAULT);
                    }
                });
            });

            rowLabel.addEventHandler(MouseEvent.DRAG_DETECTED, event ->{
                selectedRowIndex.set(rowIndex+1);
                isResizing = true;
                startYResize[0] = event.getSceneY(); // שמירת מיקום Y של העכבר בהתחלה
            });
            rowLabel.addEventHandler(MouseEvent.MOUSE_DRAGGED,event ->{
                double currentY = event.getSceneY(); // מיקום עכבר נוכחי
                double deltaY = currentY - startYResize[0]; // ההפרש בין מיקום נוכחי למיקום התחלתי
                if (commandController!=null){
                    commandController.setHeightSlider(deltaY);
                }
            });
            rowLabel.addEventHandler(MouseEvent.MOUSE_RELEASED,event->{
                selectedRowIndex.set(rowIndex+1);
                isResizing = false;
            });

            gridPane.add(rowLabel, 0, row + 1);
        }

        // יצירת תאים בפועל
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                CoordinateDTO coordinate = new CoordinateDTO(row, col + 1);
                String cellValue = " "; // תא ריק כברירת מחדל
                final String selectedCellId = coordinate.createCellCoordinateString();
                stylingPerCellLabel = getStylingPerCellLabel(selectedCellId);


                String colIndexString =String.valueOf((char) ('A' + col));
                if(stylingPerColumnMap.containsKey(colIndexString)){
                    stylingPerColumn = stylingPerColumnMap.get(colIndexString);
                } else {
                    stylingPerColumn = new StylingPerColumn(this, colIndexString);
                }

                if (cells.containsKey(coordinate)) {
                    cellValue = cells.get(coordinate).getEffectiveValue();
                    cellValue = SheetDTO.formatAsDouble(cellValue);
                }

                Label cellLabel = createCellLabel(cellValue, "label", selectedCellId, stylingPerColumn, stylingPerCellLabel);
                cellLabelMap.put(selectedCellId, cellLabel);

                cellLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    if(!isDragging){
                        if (!draggedLabels.isEmpty()){
                            resetRangeHighlights(draggedLabels.getFirst(), draggedLabels.getLast());
                            draggedLabels.clear(); // Clear the list after dragging ends
                        }

                        selectedColumnIndex.set(coordinate.getColumn());
                        selectedRowIndex.set(coordinate.getRow() + 1);
                        selectedCellCoordinate.set(coordinate);

                        StylingPerCellLabel currStylingPerCell = getStylingPerCellLabel(selectedCellId);
                        if(commandController!=null){
                            commandController.setBackgroundColorPickerShowValue(currStylingPerCell.getBackgroundColor());
                            commandController.setTextColorPickerShowValue(currStylingPerCell.getTextColor());
                            commandController.handleOpeningCellStylePaneColSelected();
                        }

                        mainController.setUpdateButtonDisale(false);
                        mainController.updateCellSeclected(selectedCellId);
                        highlightDependedAndInfluened(coordinate);
                    }
                });

                gridPane.add(cellLabel, col + 1, row + 1);
            }
        }
    }

    public GridPane getGridReadOnly() {
        GridPane original = this.gridPane;
        GridPane clone = new GridPane();

        clone.getStylesheets().addAll(original.getStylesheets());
        clone.getStylesheets().add(SkinManager.getCurrentSkinMode().getSheet());
        clone.setHgap(original.getHgap());
        clone.setVgap(original.getVgap());
        clone.setPadding(original.getPadding());
        clone.setGridLinesVisible(true); // קווי רשת ברורים

        clone.getColumnConstraints().addAll(original.getColumnConstraints());
        clone.getRowConstraints().addAll(original.getRowConstraints());

        for (Node node : original.getChildren()) {

            if (node instanceof Label) {
                Label originalLabel = (Label) node;
                Label clonedLabel = new Label(originalLabel.getText());
                clonedLabel.setPrefHeight(originalLabel.getPrefHeight());
                clonedLabel.setPrefWidth(originalLabel.getPrefWidth());
                clonedLabel.setMaxWidth(Double.MAX_VALUE); // תא יתפרס על כל הרוחב המוקצה לו
                clonedLabel.setMaxHeight(Double.MAX_VALUE); // תא יתפרס על כל הגובה המוקצה לו
                clonedLabel.getStyleClass().removeAll(clonedLabel.getStyleClass()); //?
                clonedLabel.getStyleClass().addAll(originalLabel.getStyleClass());
                if(originalLabel.getStyleClass().contains("label") && !originalLabel.getStyleClass().contains("header")) {
                    CoordinateDTO coordinate = new CoordinateDTO(GridPane.getRowIndex(node)-1, GridPane.getColumnIndex(node));
                    final String selectedCellId = coordinate.createCellCoordinateString();
                    cellLabelReadOnlyMap.put(selectedCellId, clonedLabel);
                    clonedLabel.setOnMouseClicked(MouseEvent ->{
                        mainController.setUpdateButtonDisale(true);
                        this.isReadonly = true;
                        mainController.updateCellSeclectedReadOnlyVersion(selectedCellId);
                    });
                } else if(originalLabel.getStyleClass().contains("header")) {
                    cellLabelReadOnlyMap.put(originalLabel.getText(), clonedLabel);
                    clonedLabel.setOnMouseClicked(event -> {
                        highlightCell(originalLabel.getText());
                    });
                }

                int rowIndex = GridPane.getRowIndex(originalLabel);
                int columnIndex = GridPane.getColumnIndex(originalLabel);
                clone.add(clonedLabel, columnIndex, rowIndex);  // Add to the same grid position
            }
        }

        return clone;
    }

    @Override
    public void colorCellsOfRangeForReadOnly(String startCellID, String endCellID, String cssID) {
        Label start = cellLabelReadOnlyMap.get(startCellID);
        Label end = cellLabelReadOnlyMap.get(endCellID);

        int rowStart = GridPane.getRowIndex(start);
        int rowEnd = GridPane.getRowIndex(end);
        int colStart = GridPane.getColumnIndex(start);
        int colEnd = GridPane.getColumnIndex(end);

        if(rowStart == 0 || colStart==0){
            return;
        }

        for (int row = Math.min(rowStart, rowEnd); row <= Math.max(rowStart, rowEnd); row++) {
            for (int col = Math.min(colStart, colEnd); col <= Math.max(colStart, colEnd); col++) {
                CoordinateDTO coordinate = SheetMapper.toCoordinateDTO(row - 1, col);
                Label cell = cellLabelReadOnlyMap.get(coordinate.createCellCoordinateString());
                if (cell != null) {
                    cell.getStyleClass().remove(cssID);
                    cell.getStyleClass().add(cssID);
                }
            }
        }
    }

    private void highlightDependedAndInfluened(CoordinateDTO coordinate){
        Map<CoordinateDTO, CellDTO> cells = sheetDTOObjectProperty.get().getCells();
        Map<String, Label> cellMap = isReadonly ? cellLabelReadOnlyMap : cellLabelMap;

        cellMap.values().forEach(label -> {
            label.getStyleClass().remove("highlightInfluentedCells");
            label.getStyleClass().remove("highlightDependedCells");
        });

        highlightInfluened(cells, coordinate);
        highlightDepended(cells, coordinate);
    }

    private void highlightDepended(Map<CoordinateDTO, CellDTO> cells, CoordinateDTO coordinate){
        try {
            Map<String, Label> cellMap = isReadonly ? cellLabelReadOnlyMap : cellLabelMap;
            for (CoordinateDTO dependedCellCoord : cells.get(coordinate).getDependsOn()) {
                String dependedCellId = dependedCellCoord.createCellCoordinateString();
                if (cellMap.containsKey(dependedCellId)) {
                    Label dependedLabel = cellMap.get(dependedCellId);
                    dependedLabel.getStyleClass().remove("highlightDependedCells");
                    dependedLabel.getStyleClass().add("highlightDependedCells");
                }
            }
        } catch (NullPointerException e){
            return;
        }
    }

    private void highlightInfluened(Map<CoordinateDTO, CellDTO> cells, CoordinateDTO coordinate){
        try {
            Map<String, Label> cellMap = isReadonly ? cellLabelReadOnlyMap : cellLabelMap;
            for (CoordinateDTO influencedCellCoord : cells.get(coordinate).getInfluencingOn()) {
                String influencedCellId = influencedCellCoord.createCellCoordinateString();
                if (cellMap.containsKey(influencedCellId)) {
                    Label influencedLabel = cellMap.get(influencedCellId);
                    influencedLabel.getStyleClass().remove("highlightInfluentedCells");
                    influencedLabel.getStyleClass().add("highlightInfluentedCells");
                }
            }
        } catch (NullPointerException e){
            return;
        }
    }

    private Label createCellLabel(String text, String styleClass, String cellID, StylingPerColumn stylingPerColumn, StylingPerCellLabel stylingPerCellLabel) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE); // תא יתפרס על כל הרוחב המוקצה לו
        label.setMaxHeight(Double.MAX_VALUE); // תא יתפרס על כל הגובה המוקצה לו
        label.setPrefWidth(columnWidthUnits);
        label.setPrefHeight(rowsHeightUnits);

        label.getStyleClass().add(styleClass);
        if(styleClass == "label"){
            label.getStyleClass().removeAll("center", "right", "left");
            label.getStyleClass().remove(label.getBackground());

            label.getStyleClass().add(stylingPerColumn.getOverflowCss());
            label.getStyleClass().add(stylingPerColumn.getTextAlignmentCss());
            label.setTextFill(stylingPerCellLabel.getTextColor());
        }

        label.setPrefWidth(stylingPerColumn.getColumnWidth());

        return label;
    }

    private Label createCellLabeReadOnly(String text, String styleClass) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE); // תא יתפרס על כל הרוחב המוקצה לו
        label.setMaxHeight(Double.MAX_VALUE); // תא יתפרס על כל הגובה המוקצה לו

        label.getStyleClass().add(styleClass);
        if(styleClass == "label"){
            label.getStyleClass().remove(label.getBackground());
        }

        return label;
    }

    public void highlightCell(String cellId) {
        Label selectedLabel = null;
        Map<String,Label> cellMap = isReadonly ? cellLabelReadOnlyMap : cellLabelMap;

        // איפוס כל התאים תחילה
        cellMap.values().forEach(label -> label.getStyleClass().remove("highlight"));
        selectedLabel = cellMap.get(cellId);

        // סימון התא שנבחר
        if (selectedLabel != null) {
            selectedLabel.getStyleClass().add("highlight");
            Map<CoordinateDTO, CellDTO> cells = sheetDTOObjectProperty.get().getCells();
            CoordinateDTO coordinateDTO = SheetMapper.toCoordinateDTO(cellId, sheetDTOObjectProperty.get());
            highlightDependedAndInfluened(coordinateDTO);
        }
    }

    public int getSelectedColumnIndex() {
        return selectedColumnIndex.getValue();
    }

    public int getSelectedRowIndex() {
        return selectedRowIndex.getValue();
    }

    public Map<String, StylingPerColumn> getStylingPerColumnMap(){
        return stylingPerColumnMap;
    }

    @Override
    public void setStylingPerCellLabelMap(Map<String, StylingPerCellLabel> stylingPerCellLabelMap){
        this.stylingPerCellsMap = stylingPerCellLabelMap;
    }

    @Override
    public void setStylingPerColumnMap( Map<String, StylingPerColumn> stylingPerColumnMap){
        this.stylingPerColumnMap = stylingPerColumnMap;
    }

    public Map<String, StylingPerCellLabel> getStylingPerCellLabelMap(){
        return stylingPerCellsMap;
    }

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
        mainController.addSkinListener(this);
    }

    public Label getLableAcoordingToCoordinate(CoordinateDTO coordinate) {
        Label label = cellLabelMap.get(coordinate.createCellCoordinateString());
        return label;
    }

    public Label getLableAcoordingToIndex(int row, int col) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
                return (Label) node;
            }
        }
        return null;
    }
}
