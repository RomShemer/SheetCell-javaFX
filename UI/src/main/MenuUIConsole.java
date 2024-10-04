package main;

import logicStructure.sheet.DTO.CellDTO;
import main.menuItem.MenuItemUI;
import java.util.Map;
import java.util.Scanner;

public class MenuUIConsole {

    public static void printMenuItem(MenuItemUI menuItemUI){
        System.out.println(menuItemUI.toString());
    }

    public static int getValidUserChoice(MenuItemUI menuItem) {
        Scanner scanner = new Scanner(System.in);
        String userInputString;
        int userChoice;

        while (true) {
            System.out.println(menuItem.getInputRangeMenuMessage());
            userInputString = scanner.nextLine();

            try {
                menuItem.validateUserChoice(userInputString);
                userChoice = Integer.parseInt(userInputString);
                break;
            } catch (Exception e) {
                printer(e.getMessage());
            }
        }

        return userChoice;
    }

    public static String getCellIDFromUser(){
        printer("Please enter the cell ID to update");
        return getInputFromUser();
    }

    public static String getXmlPathFromUser(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter a XML file path.");
        return scanner.nextLine();
    }

    public static void printer(String message){
        System.out.println(message);
    }

    public static String getInputFromUser() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    public static void printCellInfoBeforeUpdateAction(CellDTO cell, String cellID){

            try {
                String cellInfo = String.format(
                        "Cell ID: %s%n" +
                                "Original Value: %s%n" +
                                "Effective Value: %s%n",
                        cellID,
                        cell.getOriginalValue() != null ? cell.getOriginalValue() : " ",
                        cell.getEffectiveValue() != null ? cell.getEffectiveValue() : " "
                );
                String separatorLine = "=".repeat(cellInfo.length()) + "\n";
                printer(separatorLine + cellInfo + separatorLine);
            } catch (NullPointerException e) {
                while (true) {
                    printer(String.format("Cell %s is empty.%n(1)Enter value for cell %s%n(0) Back", cellID.toUpperCase(), cellID.toUpperCase()));
                    String userChoice = getInputFromUser();
                    if (userChoice.equals("1") || userChoice.equals("0")) {
                        break;
                    } else {
                        printer("Invalid input. Please try again.");
                    }
                }
            }
    }

    public static void printVersionsInfo(Map<Integer, Integer> versionInfo){
        StringBuilder versionInfoTableBuilder = new StringBuilder();

        versionInfoTableBuilder.append("=".repeat(35)).append("\n".repeat(2));
        // Row header
        versionInfoTableBuilder.append(String.format("%-10s | %-20s%n", "Version", "Number of Changed Cells"));
        versionInfoTableBuilder.append("-".repeat(37)).append("\n");

        //adding the cells values
        versionInfo.forEach((version, changedCells) -> {
            versionInfoTableBuilder.append(String.format("%-10d | %-20d%n", version, changedCells));
        });

        printer(versionInfoTableBuilder.toString());
    }
}
