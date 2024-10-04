package main.menuItem;

public class ContinueOrBackSubMenuUI implements MenuItemUI{

    public static enum ContinueOrBackOption {

        CONTINUE(1, null),
        BACK(0, "Back");

        private final int actionNumber;
        private String description;

        ContinueOrBackOption(int actionNumber, String description) {
            this.actionNumber = actionNumber;
            this.description = description;
        }

        public int getActionNumber() {
            return actionNumber;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) { this.description = description;}

        public static int getFirstIndex(){
            return 0;
        }

        public static int getLastIndex(){
            return 1;
        }

        @Override
        public String toString() {
            return String.format("(%d) %s", actionNumber, description);
        }

        public static ContinueOrBackOption fromNumber(int number) {
            for (ContinueOrBackOption action : values()) {
                if (action.getActionNumber() == number) {
                    return action;
                }
            }
            throw new IllegalArgumentException("Invalid action number: " + number);
        }
    }

    @Override
    public String toString(){
        return  "=".repeat(ContinueOrBackOption.CONTINUE.description.length() + 6) + "\n" +
                ContinueOrBackOption.CONTINUE.toString() + "\n" +
                ContinueOrBackOption.BACK.toString() + "\n";
    }

    public void validateUserChoice(String userChoice) {
        int userChoiceInt;

        try{
            userChoiceInt = Integer.parseInt(userChoice);
        } catch (NumberFormatException e){
            throw new NumberFormatException("Invalid input format. Input should be an Integer");
        }

        if (userChoiceInt < ContinueOrBackOption.getFirstIndex() || userChoiceInt > ContinueOrBackOption.getLastIndex()){
            String message =String.format("%nInput out of range. Input should be an integer between %d - %d%n", ContinueOrBackOption.getFirstIndex(), ContinueOrBackOption.getLastIndex());
            throw new IndexOutOfBoundsException(message);
        }
    }

    public String getInputRangeMenuMessage(){
        return String.format("Please enter a number between %d and %d:", ContinueOrBackOption.getFirstIndex(), ContinueOrBackOption.getLastIndex());
    }

}
