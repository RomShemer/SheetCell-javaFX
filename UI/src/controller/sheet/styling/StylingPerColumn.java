package controller.sheet.styling;

import controller.sheet.SheetController;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;

import java.util.Optional;

public class StylingPerColumn {

    public static enum OverflowOptions{
        WRAP("Wrap"),
        CLIP("Clip");

        private String overflowType;

        OverflowOptions(String overflowType) {
            this.overflowType = overflowType;
        }

        public String getOverflowLabelCss(){
            if(overflowType.equals("Wrap")){
                return "wrap";
            } else {
                return "clip";
            }
        }

        public static OverflowOptions extractValue(String overflowType){
            overflowType = overflowType.toLowerCase();
            if(overflowType.equals("wrap")){
                return OverflowOptions.WRAP;
            } if(overflowType.equals("clip")){
                return OverflowOptions.CLIP;
            } else {
                throw new IllegalArgumentException("Invalid overflowType: " + overflowType);
            }
        }

        public static OverflowOptions getDefaultOverflow(){
            return OverflowOptions.WRAP;
        }

        @Override
        public String toString() {
            return overflowType;
        }
    }

    public static enum TextAlignmentOptions{
        LEFT("Left"),
        CENTER("Center"),
        RIGHT("Right");

        private Pos alignmentPosition;

        TextAlignmentOptions(String alignmentPos) {
            switch(alignmentPos){
                case "Left":
                    this.alignmentPosition = Pos.CENTER_LEFT;
                    break;
                case "Center":
                    this.alignmentPosition = Pos.CENTER;
                    break;
                case "Right":
                    this.alignmentPosition = Pos.CENTER_RIGHT;
                    break;
            }
        }

        public String getAlignmentLabelCss(){
            switch(alignmentPosition){
                case CENTER -> {
                    return "center";
                }
                case CENTER_LEFT -> {
                    return "left";
                }
                case CENTER_RIGHT -> {
                    return "right";
                }
                default -> throw new IllegalArgumentException("Invalid alignmentPosition: " + alignmentPosition);
            }
        }

        public static TextAlignmentOptions getAlignmentOption(Pos alignmentPosition){
            switch(alignmentPosition){
                case CENTER -> {
                    return TextAlignmentOptions.CENTER;
                }
                case CENTER_LEFT -> {
                    return TextAlignmentOptions.LEFT;
                }
                case CENTER_RIGHT -> {
                    return TextAlignmentOptions.RIGHT;
                }
                default -> throw new IllegalArgumentException("Invalid alignmentPosition: " + alignmentPosition);
            }
        }

        public static Pos extractValue(String alignmentPos){
            alignmentPos = alignmentPos.toLowerCase();
            switch(alignmentPos){
                case "left":
                    return Pos.CENTER_LEFT;
                case "center":
                    return Pos.CENTER;
                case "right":
                    return Pos.CENTER_RIGHT;
                default:
                    throw new IllegalArgumentException("Invalid alignmentPosition: " + alignmentPos);
            }
        }

        public static String extractValueFromPosToString(Pos alignmentPos){
            switch(alignmentPos){
                case Pos.CENTER_LEFT:
                    return "Left";
                case Pos.CENTER:
                    return "Center";
                case Pos.CENTER_RIGHT:
                    return "Right";
                default:
                    throw new IllegalArgumentException("Invalid alignmentPosition: " + alignmentPos);
            }
        }

        public static Pos getDefaultAlignmentPosition(){
            return Pos.CENTER;
        }
    }

    private SheetController sheetController;
    private String columnID = null;
    private Integer columnIndex = -1;
    private double columnWidth;
    private TextAlignmentOptions textAlignment;
    private OverflowOptions overflow;
    private Color backgroundColor; //todo
    public Color textColor; //todo

    public StylingPerColumn(SheetController sheetController){
        this.sheetController = sheetController;
        this.columnWidth = sheetController.getInitColumnConstraints(Optional.empty());
        this.textAlignment = TextAlignmentOptions.CENTER;
        this.overflow = OverflowOptions.WRAP;
        this.backgroundColor = Color.BLACK; //todo
        this.textColor = Color.WHITE; //todo
    }

    public StylingPerColumn(SheetController sheetController, String columnID){
        this.sheetController = sheetController;
        this.columnID = columnID;
        this.columnIndex = (columnID.charAt(0) - 'A') + 1;
        this.columnWidth = sheetController.getInitColumnConstraints(Optional.of(columnIndex));
        this.textAlignment = TextAlignmentOptions.CENTER;
        this.overflow = OverflowOptions.WRAP;
        this.backgroundColor = Color.BLACK; //todo
        this.textColor = Color.WHITE; //todo
    }

    public StylingPerColumn(SheetController sheetController, Integer columnIndex){
        this.sheetController = sheetController;
        this.columnID = String.valueOf((char) ('A' + ((columnIndex-1)%26)));
        this.columnIndex = columnIndex;
        this.columnWidth = sheetController.getInitColumnConstraints(Optional.of(columnIndex));
        this.textAlignment = TextAlignmentOptions.CENTER;
        this.overflow = OverflowOptions.WRAP;
        this.backgroundColor = Color.BLACK; //todo
        this.textColor = Color.WHITE; //todo
    }

    public static String convertColumnIDFromIndex(Integer columnIndex){
        return String.valueOf((char) ('A' + ((columnIndex -1)%26)));
    }

    public static Integer convertColumnIndexFromString(String columnID){
        return (columnID.charAt(0) - 'A') + 1;
    }

    public Integer getColumnIndex(){
        return columnIndex;
    }

    public String getColumnID(){
        return columnID;
    }

    public double getColumnWidth(){
        return columnWidth;
    }

    public Pos getTextAlignment(){
        return textAlignment.alignmentPosition;
    }

    public String getTextAlignmentCss(){
        return textAlignment.getAlignmentLabelCss();
    }

    public OverflowOptions getOverflowOption(){
        return overflow;
    }

    public String getOverflowCss(){
        return overflow.getOverflowLabelCss();
    }

//    public Color getBackgroundColor(){
//        return backgroundColor;
//    }
//
//    public Color getTextColor(){
//        return textColor;
//    }

    public void setColumnID(String columnID) {
        this.columnID = columnID;
        this.columnIndex = convertColumnIndexFromString(columnID);
    }

    public void setColumnIndex(Integer columnIndex) {
        this.columnIndex = columnIndex;
        this.columnID = convertColumnIDFromIndex(columnIndex);
    }

    public void setColumnWidth(Double columnWidth) {
        this.columnWidth = columnWidth;
    }

    public void setTextAlignment(Pos textAlignment) {
        this.textAlignment = TextAlignmentOptions.getAlignmentOption(textAlignment);
    }

    public void setTextAlignment(String textAlignment) {
        Pos pos = TextAlignmentOptions.extractValue(textAlignment);
        this.textAlignment = TextAlignmentOptions.getAlignmentOption(pos);
    }

    public void setOverflow(OverflowOptions overflow) {
        this.overflow = overflow;
    }

    public void setOverflow(String overflow) throws IllegalArgumentException { //checked excep?
        try {
            this.overflow = OverflowOptions.extractValue(overflow);
        } catch (Exception e) {
            throw e;
        }
    }

//    public void setBackgroundColor(Color backgroundColor) { //todo
//        this.backgroundColor = backgroundColor;
//    }
//
//    public void setTextColor(Color textColor) { //todo
//        this.textColor = textColor;
//    }

}
