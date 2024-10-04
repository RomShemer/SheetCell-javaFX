package main.menuItem;

import java.util.Arrays;

public class MainMenuUI implements MenuItemUI {

    public static enum SheetActionType {

        READ_FILE(1, "Read File"),
        DISPLAY_SPREADSHEET(2, "Display Spreadsheet"),
        DISPLAY_SINGLE_CELL(3, "Display Single Cell"),
        UPDATE_SINGLE_CELL(4, "Update Single Cell"),
        DISPLAY_VERSIONS(5, "Display Versions"),
        SAVE_OR_LOAD_SYSTEM_STATE(6, "Save/Load System State"),
        EXIT(7, "Exit");

        private final int actionNumber;
        private final String description;

        SheetActionType(int actionNumber, String description) {
            this.actionNumber = actionNumber;
            this.description = description;
        }

        public int getActionNumber() {
            return actionNumber;
        }

        public String getDescription() {
            return description;
        }

        public static int getFirstIndex(){
            return 1;
        }

        public static int getLastIndex(){
            return 7;
        }

        public static int getMaxDescriptionLength(){
            return Arrays.stream(values())
                    .mapToInt(value -> value.description.length())
                    .max()
                    .orElse(0);
        }

        @Override
        public String toString() {
            return String.format("(%d) %s", actionNumber, description);
        }

        public static SheetActionType fromNumber(int number) {
            for (SheetActionType action : values()) {
                if (action.getActionNumber() == number) {
                    return action;
                }
            }
            throw new IllegalArgumentException("Invalid action number: " + number);
        }
    }

    private final saveOrLoadSubMenuUI subMenuSaveOrLoad;

    public MainMenuUI() {
        subMenuSaveOrLoad = new saveOrLoadSubMenuUI();
    }

    @Override
    public String toString(){
        return  "=".repeat(SheetActionType.getMaxDescriptionLength() + 6) + "\n" +
                "Menu:\n" +
                SheetActionType.READ_FILE.toString() + "\n" +
                SheetActionType.DISPLAY_SPREADSHEET.toString() + "\n" +
                SheetActionType.DISPLAY_SINGLE_CELL.toString() + "\n" +
                SheetActionType.UPDATE_SINGLE_CELL.toString() + "\n" +
                SheetActionType.DISPLAY_VERSIONS.toString() + "\n" +
                SheetActionType.SAVE_OR_LOAD_SYSTEM_STATE.toString() + "\n" +
                SheetActionType.EXIT.toString();
    }

    public void validateUserChoice(String userChoice) {
        int userChoiceInt;

        try{
            userChoiceInt = Integer.parseInt(userChoice);
        } catch (NumberFormatException e){
            throw new NumberFormatException("Invalid input format. Input should be an Integer");
        }

        if (userChoiceInt < SheetActionType.getFirstIndex() || userChoiceInt > SheetActionType.getLastIndex()){
            String message =String.format("%nInput out of range. Input should be an integer between %d - %d%n",  SheetActionType.getFirstIndex(), SheetActionType.getLastIndex());
            throw new IndexOutOfBoundsException(message);
        }
    }

    public String getInputRangeMenuMessage(){
        return String.format("Please enter a number between %d and %d:", SheetActionType.getFirstIndex(), SheetActionType.getLastIndex());
    }

    public saveOrLoadSubMenuUI getSubMenuSaveOrLoad() {
        return subMenuSaveOrLoad;
    }
}
