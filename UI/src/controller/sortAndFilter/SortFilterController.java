package controller.sortAndFilter;


import controller.AppController;
import controller.sheet.api.SheetControllerFilterAction;
import controller.api.CellSelectionListener;
import controller.api.CellUnselectionListener;
import controller.skinStyle.SkinChangeListener;
import controller.skinStyle.SkinManager;
import controller.skinStyle.SkinOption;
import controller.sortAndFilter.filter.FilterPopUpController;
import controller.sortAndFilter.sort.SortPopUpController;
import javafx.animation.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import logicStructure.sheet.DTO.RangeDTO;
import logicStructure.sheet.DTO.SheetDTO;
import logicStructure.sheet.DTO.SheetMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SortFilterController implements CellSelectionListener, SkinChangeListener {

    @FXML private ScrollPane mainContainer;
    @FXML private TextField rangeField;
    @FXML private Button unselectRangeButton;
    @FXML private Button selectedColumnButton;
    @FXML private TextField columnsOrderTextField;
    @FXML private ContextMenu contextMenu;
    @FXML private Button filterSelectedColumnsButton;
    @FXML private TextField filterValueField;
    @FXML private Button createGraphButton;

    private String startRange;
    private String endRange;
    private RangeDTO range;
    private Timeline typingDelay;
    private boolean isProgrammaticChange = false;
    private SheetControllerFilterAction filterSheetController;
    private FilterPopUpController filterPopUpController;
    private SortPopUpController sortPopUpController;
    private CellUnselectionListener cellUnselectionListener;
    private AppController mainController;
    private List<String> sortColumnOrder = new ArrayList<>();
    private Map<Character, List<String>> filterValuePerColumn = new HashMap<>();
    private Tooltip tooltip = new Tooltip();

    @FXML
    private void initialize() {
        mainContainer.getStylesheets().add(SkinManager.getCurrentSkinMode().getSortAndFilter());

        unselectRangeButton.setOnAction(event ->{
            unselectRangeCells();
        });
        filterSelectedColumnsButton.setOnAction(event -> {
            openFilterSheet();
        });

        setRangeFieldListener();
    }

    @Override
    public void onSkinChange(SkinOption newSkin) {
        mainContainer.getStylesheets().removeAll(SkinOption.DEFAULT.getSortAndFilter(), SkinOption.DARK_MODE.getSortAndFilter(),
                SkinOption.MONOCHROME.getSortAndFilter(), SkinOption.VIBRANT.getSortAndFilter());

        mainContainer.getStylesheets().add(newSkin.getSortAndFilter());
    }

    private void setRangeFieldListener() {
        typingDelay = new Timeline(new KeyFrame(Duration.millis(700), event -> {
            if (!isProgrammaticChange) {
                splitRangeTextFieldAction();
            }
        }));

        typingDelay.setCycleCount(1);

        rangeField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                isProgrammaticChange = false;
                typingDelay.stop();
                typingDelay.playFromStart();
            }
        });
    }

    private void splitRangeTextFieldAction(){
        String rangeSelection = rangeField.getText();
        if (rangeSelection.contains(":") && rangeSelection.split(":").length == 2) {
            String[] split = rangeSelection.split(":");
            startRange = split[0];
            endRange = split[1];
        } else if(!rangeSelection.isEmpty()){
            AppController.showError("Invalid range selection!");
        }
    }

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
        mainController.addSkinListener(this);
        createGraphButton.setOnAction(event -> mainController.createGraphAcion());
    }

    public void addSkinListenerForSubControllers(SkinChangeListener listener){
        mainController.addSkinListener(listener);
    }

    public void clearAllFilterFields(){
        filterValueField.clear();
        filterValueField.setTooltip(null);
    }

    public void clearAllSortFields(){
        columnsOrderTextField.clear();
    }

    public void clearRange(){
        rangeField.clear();
        this.range = null;
        startRange = null;
        endRange = null;
    }

    public void onCellSelection(String startCell, String endCell, CellUnselectionListener cellUnselectionListener){
        this.startRange = startCell;
        this.endRange = endCell;
        this.cellUnselectionListener = cellUnselectionListener;

        if(startRange!=null && endRange!=null) {
            isProgrammaticChange = true;
            String rangeSelection = startRange + ":" + endRange;
            rangeField.setText(rangeSelection);
            this.range = SheetMapper.toRangeDTO(startRange, endCell);
            if(sortPopUpController != null){
                sortPopUpController.updateRange(startRange, endRange);
                sortPopUpController.setInitStateOfToggleSwitch(true);
            }
        }
    }

    public void unselectRangeCells(){
        if(cellUnselectionListener != null && startRange != null && endRange != null){
            cellUnselectionListener.onCellUnselection(startRange, endRange);
        }

        clearRange();
    }

    public void handleFilterAction(List<String> filterValues, Character column){
        if(startRange == null && endRange == null){
            return;
        }

        filterValuePerColumn.put(column, filterValues);
        try {
            SheetDTO filteredSheet = mainController.filterAction(range, column, filterValues, filterSheetController.getTabID());
            filterSheetController.clearGrid(true);
            filterSheetController.setSheetDTO(filteredSheet);
            filterSheetController.createGridPane();
            GridPane gridPane = filterSheetController.getGridPane();

            GridPane.setHgrow(gridPane, Priority.ALWAYS);
            GridPane.setVgrow(gridPane, Priority.ALWAYS);

            mainController.replaceFilterGridPaneInTab(gridPane, range, filterSheetController.getTabName(),
                    filterSheetController.getTabID());

            addPopUpToFilterSheet();
            addFilterValuesToTextField();

        } catch (Exception e){
            e.printStackTrace();
            AppController.showError(e.getMessage());
        }
    }

    private void addFilterValuesToTextField(){
        List<String> values = new ArrayList<>();

        for(Map.Entry<Character, List<String>> entry : filterValuePerColumn.entrySet()){
            char column = entry.getKey();
            String filterValues = entry.getValue().stream()
                    .collect(Collectors.joining(","));
            filterValues = String.valueOf(column).toUpperCase() + ": " + filterValues;
            values.add(filterValues);
        }

        String valueForTextField = values.stream().collect(Collectors.joining(" -> "));

        Double maxLength = filterValueField.getPrefWidth();
        if (valueForTextField.length() > maxLength) {
            valueForTextField = valueForTextField.substring(0, maxLength.intValue() - 3) + "...";
        }

        filterValueField.setText(valueForTextField);
        addFilterValuesToToolTip();
    }

    private void addFilterValuesToToolTip(){
        TableView<List<String>> tableView = new TableView<>();
        int maxRows = filterValuePerColumn.values().stream().mapToInt(List::size).max().orElse(0);

        for (Map.Entry<Character, List<String>> entry : filterValuePerColumn.entrySet()) {
            TableColumn<List<String>, String> column = new TableColumn<>(String.valueOf(entry.getKey()));

            column.setCellValueFactory(data -> {
                int index = tableView.getItems().indexOf(data.getValue());
                if (index < entry.getValue().size()) {
                    return new SimpleStringProperty(entry.getValue().get(index));
                } else {
                    return new SimpleStringProperty("");
                }
            });

            tableView.getColumns().add(column);
        }

        List<List<String>> rows = new ArrayList<>();
        for (int i = 0; i < maxRows; i++) {
            List<String> row = new ArrayList<>();
            for (Map.Entry<Character, List<String>> entry : filterValuePerColumn.entrySet()) {
                if (i < entry.getValue().size()) {
                    row.add(entry.getValue().get(i));
                } else {
                    row.add("");
                }
            }
            rows.add(row);
        }

        tableView.getItems().addAll(rows);
        tableView.setPrefSize(300, 300);
        tableView.getStylesheets().add(SkinManager.getCurrentSkinMode().getTabelView());

        VBox vBox = new VBox(tableView);
        Tooltip tooltip = new Tooltip();
        tooltip.setGraphic(vBox);
        Scene scene = tooltip.getScene();
        scene.getStylesheets().add(SkinManager.getCurrentSkinMode().getToolTip());

        filterValueField.setTooltip(null);
        filterValueField.setTooltip(tooltip);
    }

    private void filterByColumn(char currentColumn) throws Exception{
        if(!filterValuePerColumn.containsKey(currentColumn)){
            return;
        }

        SheetDTO filteredSheet = mainController.filterAction(range, currentColumn, filterValuePerColumn.get(currentColumn), filterSheetController.getTabID());
        filterSheetController.clearGrid(true);
        filterSheetController.setSheetDTO(filteredSheet);
        filterSheetController.createGridPane();
        GridPane gridPane = filterSheetController.getGridPane();

        GridPane.setHgrow(gridPane, Priority.ALWAYS);
        GridPane.setVgrow(gridPane, Priority.ALWAYS);

        mainController.replaceFilterGridPaneInTab(gridPane, range, filterSheetController.getTabName(),
                filterSheetController.getTabID());

        addPopUpToFilterSheet();
    }

    private void openFilterSheet() {
        if (!rangeField.getText().isEmpty() && range == null) {
            splitRangeTextFieldAction();
        } else if(startRange == null && endRange == null){
            AppController.showError("No range selected!");
            return;
        }

        if(startRange != null && endRange != null) {
            String name = "Filter " + startRange + ":" + endRange;
            filterSheetController = mainController.showFilterSheetInTabs(name, startRange, endRange);
            addPopUpToFilterSheet();
        }
    }

    private void addPopUpToFilterSheet() {
        if (filterSheetController == null || range == null) {
            return;
        }

        GridPane gridPane = filterSheetController.getGridPane();

        if (gridPane == null) {
            return;
        }

        int startCol = range.getStartCoordinate().getColumn(); // עמודה התחלתית
        int endCol = range.getEndCoordinate().getColumn(); // עמודה סופית

        markFilterRange();

        for (int col = startCol; col <= endCol; col++) {
            Button filterButton = new Button("▼");
            filterButton.getStyleClass().add("filter-button"); // הוספת מחלקת העיצוב לכפתור
            int finalCol = col;
            filterButton.setOnAction(event -> {
                showFilterPopUp(finalCol, filterButton);
            });

            // מקבל את ה-Label של כותרת העמודה ומוסיף את כפתור החץ לידו
            Label headerLabel = filterSheetController.getLableAcoordingToIndex(0, col);
            if (headerLabel != null) {
                headerLabel.getStyleClass().add("header-label");
                StackPane stackPane = new StackPane();
                StackPane.setAlignment(headerLabel, Pos.CENTER); // יישור התווית במרכז
                StackPane.setAlignment(filterButton, Pos.BOTTOM_RIGHT); // יישור החץ לימין
                stackPane.getStyleClass().add("stack-pane-filter-button");
                stackPane.getChildren().addAll(headerLabel, filterButton);
                GridPane.setColumnIndex(stackPane, col);
                GridPane.setRowIndex(stackPane, 0);
                gridPane.getChildren().add(stackPane); // Replace with headerLabel + filterButton
            }
        }
    }

    private void markFilterRange(){
        int startRow = range.getStartCoordinate().getRow()+1;
        int endRow = range.getEndCoordinate().getRow()+1;
        int startCol = range.getStartCoordinate().getColumn(); // עמודה התחלתית
        int endCol = range.getEndCoordinate().getColumn(); // עמודה סופית

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                Label label = filterSheetController.getLableAcoordingToIndex(row, col);
                if (label != null) {
                    label.getStyleClass().remove("highlight-Filter-Range");
                    label.getStyleClass().add("highlight-Filter-Range");
                }
            }
        }
    }

    public void unmarkFilterRange(){
        int startRow = range.getStartCoordinate().getRow()+1;
        int endRow = range.getEndCoordinate().getRow()+1;
        int startCol = range.getStartCoordinate().getColumn(); // עמודה התחלתית
        int endCol = range.getEndCoordinate().getColumn(); // עמודה סופית

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                Label label = filterSheetController.getLableAcoordingToIndex(row, col);
                if (label != null) {
                    label.getStyleClass().remove("highlight-Filter-Range");
                }
            }
        }
    }

    private void showFilterPopUp(int colIndex, Button filterButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Filter.fxml"));
            Parent popUp = loader.load();
            filterPopUpController = loader.getController();
            filterPopUpController.setMainController(this);
            mainController.addSkinListener(filterPopUpController);
            filterPopUpController.setColumn(colIndex);
            // איסוף הערכים הייחודיים בעמודה עבור הסינון
            List<String> uniqueValues = getColumnUniqueValues(colIndex);
            filterPopUpController.setListViewItems(uniqueValues);

            // יצירת ה-Popup מתחת לכפתור הסינון
            Stage stage = new Stage();
            stage.setTitle("Filter Options");
            stage.setScene(new Scene(popUp));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(filterButton.getScene().getWindow());

            Bounds buttonBounds = filterButton.localToScreen(filterButton.getBoundsInLocal());
            stage.setX(buttonBounds.getMinX());
            stage.setY(buttonBounds.getMaxY());

            if(mainController.getIsWithAnimations()) {
                filterPopUpAnimations(stage,popUp,filterButton);
            } else {
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AppController.showError(e.getMessage());
        }
    }

    private void filterPopUpAnimations(Stage stage, Parent popUp, Button filterButton){
        filterButton.getScene().setCursor(Cursor.WAIT);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), popUp);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(2), popUp);
        scaleUp.setFromX(0.5);
        scaleUp.setFromY(0.5);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);

        TranslateTransition translateUp = new TranslateTransition(Duration.seconds(2), popUp);
        translateUp.setFromY(50);
        translateUp.setToY(0);

        fadeIn.play();

        ParallelTransition parallelTransition = new ParallelTransition(scaleUp, translateUp);
        parallelTransition.setOnFinished(event -> {
            stage.show();
            filterButton.getScene().setCursor(Cursor.DEFAULT);
        });

        parallelTransition.play();
    }

    private List<String> getColumnUniqueValues(int colIndex) {
        List<String> uniqueValues = new ArrayList<>();

        GridPane gridPane = filterSheetController.getGridPane();

        // עובר על כל השורות בעמודה ומוסיף את הערכים הייחודיים לרשימה
        for (int row = 1; row < gridPane.getRowCount(); row++) {
            Label cell = filterSheetController.getLableAcoordingToIndex(row, colIndex);

            if (cell != null && !uniqueValues.contains(cell.getText())) {
                if(cell.getText().equals("") || cell.getText() == null || cell.getText().equals(" ")){
                    uniqueValues.add("empty");
                }
                uniqueValues.add(cell.getText());
            }
        }

        return uniqueValues;
    }

    public void handleSortAction() {
        if(startRange.length() == 1 && endRange.length() == 1) {
            startRange = mainController.getFirstCellIDInSheet();
            endRange = mainController.getLastCellIDInSheet();
            sortPopUpController.updateRange(startRange, endRange);
        } else if(startRange == null || endRange == null) {
            throw new RuntimeException( "No range selected! Please mark the range of cells in the sheet or choose all sheet");
        }

        if(sortColumnOrder.size() == 1){
            columnsOrderTextField.setText(sortColumnOrder.getFirst());
        } else {
            columnsOrderTextField.setText(sortColumnOrder.getFirst() + "..." + sortColumnOrder.getLast());
        }

        sortColumnOrder.forEach(column -> {
            Label label = new Label(column);
            CustomMenuItem customMenuItem = new CustomMenuItem(label, false);
            contextMenu.getItems().add(customMenuItem);
        });

        columnsOrderTextField.setOnMouseClicked(mouseEvent -> {
            if (!contextMenu.isShowing()){
                contextMenu.show(columnsOrderTextField, Side.BOTTOM, 0, 0);
            }
            else {
                contextMenu.hide();
                mouseEvent.consume(); // מונע הפצה של האירוע הלאה
            }
        });

        SheetDTO sortedSheet = mainController.getSortedSheetDTO(SheetMapper.toRangeDTO(startRange,endRange), sortColumnOrder);
        mainController.showSortedSheetInTabs(sortedSheet, "Sorted " + startRange + " :" + endRange, startRange, endRange);
    }

    @FXML
    public void selectSortedColumnAction(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SortPopUp.fxml"));
            Parent popUp = loader.load();
            sortPopUpController = loader.getController();
            sortPopUpController.setMainController(this);
            mainController.addSkinListener(sortPopUpController);

            this.mainController.setOnCellSelectionListener(this);

            if(startRange != null && endRange != null){
                sortPopUpController.updateRange(startRange, endRange);
            }

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Select sort column");
            dialogStage.setScene(new Scene(popUp));

            dialogStage.show();

        } catch (Exception e){
            e.printStackTrace();
            AppController.showError(e.getMessage());
        }

    }

    public void setSelectedColumns(List<String> columns){
        sortColumnOrder.clear();
        if(columns != null){
            sortColumnOrder.addAll(columns);
        }

        if(startRange == null && endRange == null){
            this.startRange = columns.getFirst();
            this.endRange = columns.getLast();
        }
    }

    public List<String> getCellsIDList(){
        return mainController.getCellIDList();
    }

    public boolean getIsWithAnimations(){
        return mainController.getIsWithAnimations();
    }

    public Button getGraphButton(){
        return createGraphButton;
    }
}
