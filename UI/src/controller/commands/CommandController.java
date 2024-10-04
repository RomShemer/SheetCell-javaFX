package controller.commands;

import controller.AppController;
import controller.sheet.SheetController;
import controller.sheet.styling.StylingPerCellLabel;
import controller.sheet.styling.StylingPerColumn;
import controller.skinStyle.SkinChangeListener;
import controller.skinStyle.SkinManager;
import controller.skinStyle.SkinOption;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import logicStructure.sheet.DTO.CoordinateDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class CommandController implements SkinChangeListener {
    private ObjectProperty<GridPane> gridPaneProperty = new SimpleObjectProperty<>(null);
    private AppController mainController;
    private SheetController sheetController;
    private Map<Integer, Double> sliderLastPossByColumnIndex = new HashMap<>();
    private Map<Integer, Double> sliderLastPossByRowIndex = new HashMap<>();

    @FXML private ScrollPane mainContainer;

    //Column:
    @FXML private TitledPane columnStylePane;
    @FXML private Label selectedColLabel;
    @FXML private TextField selectedColumnTextField;
    @FXML private Label widthLabel;
    @FXML private Slider widthSlider;
    @FXML private Button resetWidthButton;
    @FXML private ComboBox<String> alignmentColumnComboBox;
    @FXML private Button applyCoulmnAlignmentButton;
    @FXML private ComboBox<String> overflowColumnComboBox;
    @FXML private Button applyColumnOverflowButton;
    @FXML private Button resetColumnStylesButton;

    //Row:
    @FXML private TitledPane rowStylePane;
    @FXML private Label selectedRowLabel;
    @FXML private TextField selectedRowTextField;
    @FXML private Label HeightLabel;
    @FXML private Slider rowHeightSlider;
    @FXML private Button resetHeighButton;

    //?
    @FXML private ComboBox<String> alignmentRowComboBox;
    @FXML private Button applyRowAlignmentButton;
    @FXML private ComboBox<String> overflowRowComboBox;
    @FXML private Button applyRowOverflowButton;

    //
    @FXML private Button resetRowStylesButton;

    //Cell:
    @FXML private TitledPane cellStylePane;
    @FXML private TextField selectedCellIDTextField;
    @FXML private ColorPicker backgroundColorPicker;
    @FXML private Button applyBackgroundColorButton;
    @FXML private ColorPicker textColorPicker;
    @FXML private Button applyTextColorButton;
    @FXML private Button resetCellStylesButton;


    @FXML
    public void initialize() {
        mainContainer.getStylesheets().add(SkinManager.getCurrentSkinMode().getCommands());
    }

    @Override
    public void onSkinChange(SkinOption newSkin) {
        mainContainer.getStylesheets().removeAll(SkinOption.DEFAULT.getCommands(), SkinOption.DARK_MODE.getCommands(),
                SkinOption.MONOCHROME.getCommands(), SkinOption.VIBRANT.getCommands());
        mainContainer.getStylesheets().add(newSkin.getCommands());
    }

    @FXML
    private void resetWidth(){
        widthSlider.setValue(0);
    }

    @FXML
    private void resetHeight(){
        rowHeightSlider.setValue(0);
    }


    // Apply Text Alignment
    @FXML
    private void applyColumnTextAlignment() {
        String alignment = alignmentColumnComboBox.getSelectionModel().getSelectedItem();
        int selectedColumnIndex = sheetController.getSelectedColumnIndex();
        setColumnAlignment(selectedColumnIndex, StylingPerColumn.TextAlignmentOptions.extractValue(alignment));
    }

    @FXML
    private void applyColumnTextOverflow() {
        String overflow = overflowColumnComboBox.getSelectionModel().getSelectedItem();
        int selectedColumnIndex = sheetController.getSelectedColumnIndex();
        setColumnOverflow(selectedColumnIndex, StylingPerColumn.OverflowOptions.extractValue(overflow));
    }

    //not available
    @FXML
    private void applyRowTextAlignment() {
        String alignment = alignmentRowComboBox.getSelectionModel().getSelectedItem();
        int selectedRowIndex = sheetController.getSelectedRowIndex();
    }

    //not available
    @FXML
    private void applyRowTextOverflow() {
        String overflow = overflowRowComboBox.getSelectionModel().getSelectedItem();
        int selectedRowIndex = sheetController.getSelectedRowIndex();
    }

    // Apply Background Color
    @FXML
    private void applyBackgroundColor() {
        Color backgroundColor = backgroundColorPicker.getValue();
        CoordinateDTO coordinate = sheetController.getSelectedCellCoordinate().get();
        setBackgroundColorPicker(coordinate.createCellCoordinateString(), backgroundColor);
    }

    // Apply Text Color
    @FXML
    private void applyTextColor() {
        Color textColor = textColorPicker.getValue();
        CoordinateDTO coordinate = sheetController.getSelectedCellCoordinate().get();
        setTextColorPicker(coordinate.createCellCoordinateString(), textColor);
    }

    // Reset Cell Styling
    @FXML
    private void resetCellStyling() {
        String selectedCellID = sheetController.getSelectedCellCoordinate().get().createCellCoordinateString();
        setBackgroundColorPicker(selectedCellID, StylingPerCellLabel.getDefaultBackgroundColor());
        setTextColorPicker(selectedCellID, StylingPerCellLabel.getDefaultTextColor());
    }

    @FXML
    private void resetColumnStyling() {
        resetWidth();
        Pos defaultPos = StylingPerColumn.TextAlignmentOptions.getDefaultAlignmentPosition();
        alignmentColumnComboBox.setValue(StylingPerColumn.TextAlignmentOptions.extractValueFromPosToString(defaultPos));
        applyColumnTextAlignment();


        overflowColumnComboBox.setValue(StylingPerColumn.OverflowOptions.getDefaultOverflow().toString());
        applyColumnTextOverflow();
    }

    @FXML
    private void resetRowStyling() {
        resetHeight();
        Pos defaultPos = Pos.CENTER;
        alignmentRowComboBox.setDisable(true);
        overflowRowComboBox.setDisable(true);
    }


    public void setMainController(AppController mainController) {
        this.mainController = mainController;
        mainController.addSkinListener(this);
    }

    public void setSheetController( SheetController sheetController) {
        this.gridPaneProperty.set(sheetController.getGridPane());
        this.sheetController = sheetController;
        widthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyWidth();
        });
        rowHeightSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyHeight();
        });
    }

    public void setAllPaneDisable(){
        setColumnPaneStyleDisable();
        setRowPaneStyleDisable();
        setCellPaneStyleDisable();
    }

    public ObjectProperty<GridPane> getGridPaneProperty() {
        return gridPaneProperty;
    }

    public void setColumnAlignmentComboBoxShowValue(String alignment) {
        alignmentColumnComboBox.setValue(alignment);
    }

    public void setColumnOverflowComboBoxShowValue(String overflow) {
        overflowColumnComboBox.setValue(overflow);
    }

    public void setBackgroundColorPickerShowValue(Color backgroundColor) {
        backgroundColorPicker.setValue(backgroundColor);
    }

    public void setTextColorPickerShowValue(Color textColor) {
        textColorPicker.setValue(textColor);
    }

    public void handleOpeningCellStylePaneColSelected() {
        CoordinateDTO selectedCoordinate = sheetController.getSelectedCellCoordinate().get();
        int selectedColumnIndex = sheetController.getSelectedColumnIndex();
        int selectedRowIndex = sheetController.getSelectedRowIndex();

        cellStylePane.setDisable(false);
        cellStylePane.expandedProperty().set(true);

        if (selectedColumnIndex != -1) {
            columnStylePane.expandedProperty().set(false);
            columnStylePane.setDisable(false);
            this.selectedColumnTextField.setText(StylingPerColumn.convertColumnIDFromIndex(selectedCoordinate.getColumn()));
        } else {
            setCellPaneStyleDisable();
        }
        if(selectedRowIndex != -1) {
            this.selectedRowTextField.setText(String.valueOf(selectedCoordinate.getRow() + 1));
            rowStylePane.expandedProperty().set(false);
            rowStylePane.setDisable(false);
        } else {
            setRowPaneStyleDisable();
        }

        setSelectedCellLabel(selectedCoordinate.createCellCoordinateString());
    }

    private void setSelectedCellLabel(String selectedCellID){
        selectedCellIDTextField.setText(selectedCellID);
    }

    public void handleOpeningColumnStylePane() {
        columnStylePane.setDisable(false);
        columnStylePane.expandedProperty().set(true);
        setSelectedColLabel();

        setRowPaneStyleDisable();
        setCellPaneStyleDisable();
    }

    public void handleOpeningRowStylePane() {
        rowStylePane.setDisable(false);
        rowStylePane.expandedProperty().set(true);
        setSelectedRowLabel();

        setColumnPaneStyleDisable();
        setCellPaneStyleDisable();
    }

    private void setSelectedColLabel() {
        if(sheetController.getSelectedColumnIndex() != -1) {
            String col = StylingPerColumn.convertColumnIDFromIndex(sheetController.getSelectedColumnIndex());
            this.selectedColumnTextField.setText(col);
        } else {
            this.selectedColumnTextField.clear();
        }
    }

    private void setSelectedRowLabel() {
        if(sheetController.getSelectedRowIndex() != -1) {
            String row = String.valueOf(sheetController.getSelectedRowIndex());
            this.selectedRowTextField.setText(row);
        } else {
            this.selectedRowTextField.clear();
        }
    }

    public void setAllTitlePanesDisable(){
        setColumnPaneStyleDisable();
        setRowPaneStyleDisable();
        setCellPaneStyleDisable();
    }

    public void setCellPaneStyleDisable(){
        this.selectedCellIDTextField.clear();
        this.cellStylePane.expandedProperty().set(false);
        this.cellStylePane.setDisable(true);
    }

    public void setColumnPaneStyleDisable(){
        this.selectedColumnTextField.clear();
        this.columnStylePane.expandedProperty().set(false);
        this.columnStylePane.setDisable(true);
    }

    public void setRowPaneStyleDisable(){
        this.selectedRowTextField.clear();
        this.rowStylePane.expandedProperty().set(false);
        this.rowStylePane.setDisable(true);
    }

    public void setWidthSlider(double value) {
        widthSlider.setValue(value);
    }

    public void setHeightSlider(double value) {
        rowHeightSlider.setValue(value);
    }

    public void resetColumnSliderPos(int columnIndex) {
        double poss;

        if(sliderLastPossByColumnIndex.containsKey(columnIndex)) {
            poss = sliderLastPossByColumnIndex.get(columnIndex);
        } else {
            poss = 0;
        }
        widthSlider.setValue(poss);
    }

    public void resetRowSliderPos(int rowIndex) {
        double poss;

        if(sliderLastPossByRowIndex.containsKey(rowIndex)) {
            poss = sliderLastPossByRowIndex.get(rowIndex);
        } else {
            poss = 0;
        }
        rowHeightSlider.setValue(poss);
    }

    private void applyWidth() {
        int selectedColumnIndex = sheetController.getSelectedColumnIndex();
        double size = widthSlider.getValue();

        if (selectedColumnIndex != -1) {
            sliderLastPossByColumnIndex.put(selectedColumnIndex, size);
            System.out.println("Applying width: " + size + " to column: " + selectedColumnIndex);
            setColumnWidth(selectedColumnIndex, size);
        }
    }

    private void applyHeight() {
        int selectedRowIndex = sheetController.getSelectedRowIndex();
        double size = rowHeightSlider.getValue();

        if (selectedRowIndex != -1) {
            sliderLastPossByRowIndex.put(selectedRowIndex, size);
            System.out.println("Applying height: " + size + " to row: " + selectedRowIndex);
            setRowHeight(selectedRowIndex, size);
        }
    }


    // Helper Methods
    private void setColumnWidth(int columnIndex, double width) {
        ColumnConstraints colConstraints = new ColumnConstraints();
        double initValue = sheetController.getInitColumnConstraints(Optional.of(columnIndex));
        width = initValue + (width/8.0);
        double min = gridPaneProperty.get().getColumnConstraints().get(columnIndex).getMinWidth();
        double max = gridPaneProperty.get().getWidth()- gridPaneProperty.get().getColumnCount();
        width = Math.max(Math.max(1, min), Math.min(width, max));
        colConstraints.setPrefWidth(width);//should be before setPercent?
        colConstraints.setPercentWidth(width);
        gridPaneProperty.getValue().getColumnConstraints().set(columnIndex, colConstraints);
    }

    private void setRowHeight(int rowIndex, double height) {
        RowConstraints rowConstraints = new RowConstraints();
        double initValue = sheetController.getInitRowConstraints();

        height = initValue + (height/8.0);
        double min = gridPaneProperty.get().getRowConstraints().get(rowIndex).getMinHeight();
        double max = gridPaneProperty.get().getHeight()- gridPaneProperty.get().getRowCount();

        height = Math.max(Math.max(1, min), Math.min(height, max));
        rowConstraints.setPrefHeight(height);
        rowConstraints.setPercentHeight(height);
        gridPaneProperty.getValue().getRowConstraints().set(rowIndex, rowConstraints);
    }

    private void setColumnAlignment(int columnIndex, Pos alignment) {
        StylingPerColumn stylingPerColumn =  sheetController.getStylingPerColumn(columnIndex);
        stylingPerColumn.setTextAlignment(alignment);
        sheetController.getSelectedColumnStylingProperty().set(null); // איפוס רגעי
        sheetController.getSelectedColumnStylingProperty().set(stylingPerColumn);
    }

    private void setColumnOverflow(int columnIndex, StylingPerColumn.OverflowOptions overflow) {
        StylingPerColumn stylingPerColumn =  sheetController.getStylingPerColumn(columnIndex);
        stylingPerColumn.setOverflow(overflow);
        sheetController.getSelectedColumnStylingProperty().set(null); // איפוס רגעי
        sheetController.getSelectedColumnStylingProperty().set(stylingPerColumn);
    }

    private void setBackgroundColorPicker(String selectedCellID, Color backgroundColor) {
        StylingPerCellLabel stylingPerCellLabel =  sheetController.getStylingPerCellLabel(selectedCellID);
        stylingPerCellLabel.setBackgroundColor(backgroundColor);
        sheetController.getSelectedCellStylingProperty().set(null); // איפוס רגעי
        sheetController.getSelectedCellStylingProperty().set(stylingPerCellLabel);
    }

    private void setTextColorPicker(String selectedCellID, Color textColor) {
        StylingPerCellLabel stylingPerCellLabel =  sheetController.getStylingPerCellLabel(selectedCellID);
        stylingPerCellLabel.setTextColor(textColor);
        sheetController.getSelectedCellStylingProperty().set(null); // איפוס רגעי
        sheetController.getSelectedCellStylingProperty().set(stylingPerCellLabel);
    }
}
