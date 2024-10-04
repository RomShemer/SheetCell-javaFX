package controller.sheet.styling;

import controller.sheet.SheetController;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import logicStructure.sheet.DTO.CoordinateDTO;
import logicStructure.sheet.DTO.SheetMapper;

public class StylingPerCellLabel {

    private SheetController sheetController;
    private String cellID;
    private CoordinateDTO cellCoordinate;
    private Color backgroundColor;
    private Color textColor;

    public StylingPerCellLabel(SheetController sheetController) {
        this.sheetController = sheetController;
        backgroundColor = Color.TRANSPARENT;
        textColor = Color.BLACK;
    }

    public StylingPerCellLabel(SheetController sheetController, CoordinateDTO cellCoordinate) {
        this.sheetController = sheetController;
        this.cellCoordinate = cellCoordinate;
        this.cellID = cellCoordinate.createCellCoordinateString();
        backgroundColor = Color.TRANSPARENT;
        textColor = Color.BLACK;
    }

    public StylingPerCellLabel(SheetController sheetController, String cellID) {
        this.sheetController = sheetController;
        this.cellID = cellID;
        CoordinateDTO coordinate = SheetMapper.toCoordinateDTO(cellID, sheetController.sheetDTOProperty().get());
        this.cellCoordinate = SheetMapper.toCoordinateDTO(coordinate.getRow(), coordinate.getColumn());
        backgroundColor = Color.TRANSPARENT;
        textColor = Color.BLACK;
    }

    public CoordinateDTO getCellCoordinate() {
        return cellCoordinate;
    }

    public String getCellID() {
        return cellID;
    }

    public String getBackgroundInString(){
        if(backgroundColor == Color.TRANSPARENT){
            return "-fx-background-color: transparent;";
        }
        return "-fx-background-color:" + toHexString(backgroundColor) + ";";
    }

    public Background getBackground(){
        BackgroundFill backgroundFill = new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY);
        Background background = new Background(backgroundFill);
        return background;
    }

    public Color getBackgroundColor(){
        return backgroundColor;
    }

    public static Color getDefaultBackgroundColor(){
        return Color.TRANSPARENT;
    }

    public static Color getDefaultTextColor(){
        return Color.BLACK;
    }

    public Color getTextColor(){
        return textColor;
    }

    public void setCellID(String cellID) {
        this.cellID = cellID;
    }

    public void setCellCoordinate(CoordinateDTO cellCoordinate) {
        this.cellCoordinate = cellCoordinate;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}
