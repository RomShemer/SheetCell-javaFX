package controller.sheet.api;

import controller.AppController;
import controller.sheet.styling.StylingPerCellLabel;
import controller.sheet.styling.StylingPerColumn;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.GridPane;
import logicStructure.sheet.DTO.SheetDTO;

import java.util.Map;

public interface SheetInTabAction {
    void setSheetDTO(SheetDTO sheetDTO);
    void setTabID(String tabID);
    void setTabName(String name);
    String getTabID();
    String getTabName();
    ObjectProperty<GridPane> getGridPaneProperty();
    void clearGrid(boolean isFullClear);
    void createGridPane();
    GridPane getGridPane();
    void createGridFromSheetDTO(GridPane gridPane, SheetDTO sheetDTO);
    GridPane getGridReadOnly();
    void highlightCell(String cellId);
    void setMainController(AppController mainController);
    void setIsReadOnly(boolean isReadOnly);
    void colorCellsOfRangeForReadOnly(String startCellID, String endCellID, String cssID);
    void setStylingPerCellLabelMap(Map<String, StylingPerCellLabel> stylingPerCellLabelMap);
    void setStylingPerColumnMap( Map<String, StylingPerColumn> stylingPerColumnMap);
}
