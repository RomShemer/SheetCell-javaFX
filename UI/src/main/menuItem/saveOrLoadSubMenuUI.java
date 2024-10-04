package main.menuItem;

import java.util.Arrays;

public class saveOrLoadSubMenuUI implements MenuItemUI {

    public static enum SaveOrLoadSystemStateSubAction {
        SAVE_SYSTEM_STATE(1, "Save System State"),
        LOAD_SYSTEM_STATE(2, "Load System State"),
        BACK(0, "Back");

        private final int actionNumber;
        private final String description;

        SaveOrLoadSystemStateSubAction(int actionNumber, String description) {
            this.actionNumber = actionNumber;
            this.description = description;
        }

        @Override
        public String toString() {
            return String.format("(%d) %s", actionNumber, description);
        }

        public static String toStringHeaderSubMenu(){
            return "Save/Load System State Sub Menu:";
        }

        public int getActionNumber() {
            return actionNumber;
        }

        public String getDescription() {
            return description;
        }

        public static int getFirstIndex(){
            return 0;
        }

        public static int getLastIndex(){
            return 2;
        }

        public static int getMaxDescriptionLength(){
            return Arrays.stream(values())
                    .mapToInt(value -> value.description.length())
                    .max()
                    .orElse(0);
        }

        public static SaveOrLoadSystemStateSubAction fromNumber(int number) {
            for (SaveOrLoadSystemStateSubAction action : values()) {
                if (action.getActionNumber() == number) {
                    return action;
                }
            }
            throw new IllegalArgumentException("Invalid action number: " + number);
        }
    }

    @Override
    public String toString(){
        return  "=".repeat(SaveOrLoadSystemStateSubAction.getMaxDescriptionLength() + 6) + "\n" +
                SaveOrLoadSystemStateSubAction.toStringHeaderSubMenu() + "\n" +
                SaveOrLoadSystemStateSubAction.SAVE_SYSTEM_STATE.toString() + "\n" +
                SaveOrLoadSystemStateSubAction.LOAD_SYSTEM_STATE.toString() + "\n" +
                SaveOrLoadSystemStateSubAction.BACK.toString();
    }

    public void validateUserChoice(String userChoice) {
        int userChoiceInt;

        try{
            userChoiceInt = Integer.parseInt(userChoice);
        } catch (NumberFormatException e){
            throw new NumberFormatException("Invalid input format. Input should be an Integer");
        }

        if (userChoiceInt < SaveOrLoadSystemStateSubAction.getFirstIndex() || userChoiceInt > SaveOrLoadSystemStateSubAction.getLastIndex()){
            String message =String.format("%nInput out of range. Input should be an integer between %d - %d%n",
                    saveOrLoadSubMenuUI.SaveOrLoadSystemStateSubAction.getFirstIndex(),  saveOrLoadSubMenuUI.SaveOrLoadSystemStateSubAction.getLastIndex());
            throw new IndexOutOfBoundsException(message);
        }
    }

    public String getInputRangeMenuMessage(){
        return String.format("Please enter a number between %d and %d:",
                saveOrLoadSubMenuUI.SaveOrLoadSystemStateSubAction.getFirstIndex(),  saveOrLoadSubMenuUI.SaveOrLoadSystemStateSubAction.getLastIndex());
    }

}
