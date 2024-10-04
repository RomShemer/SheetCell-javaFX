package main;

import logicStructure.sheet.DTO.CellDTO;
import logicStructure.sheet.DTO.SheetDTO;
import logicStructure.sheet.version.VersionHistoryManager;
import menu.MenuActionLogic;
import main.menuItem.MainMenuUI;
import main.menuItem.ContinueOrBackSubMenuUI;
import main.menuItem.saveOrLoadSubMenuUI;
import java.io.*;

public class UIManager {

    private VersionHistoryManager versionHistoryManager;
    private MenuActionLogic menuLogic;
    private MainMenuUI mainMenuUI;
    private ContinueOrBackSubMenuUI continueOrBackSubMenuUI;

    public UIManager() {
        this.versionHistoryManager = new VersionHistoryManager();
        this.mainMenuUI = new MainMenuUI();
        this.continueOrBackSubMenuUI = new ContinueOrBackSubMenuUI();
    }

    public void clear(){
        versionHistoryManager.clearHistory();
    }

    public void run() {
        while (true) {
            MenuUIConsole.printMenuItem(mainMenuUI);
            int userChoiceInt = MenuUIConsole.getValidUserChoice(mainMenuUI);
            MainMenuUI.SheetActionType userChoice = MainMenuUI.SheetActionType.fromNumber(userChoiceInt);

            try {
                switch (userChoice) {
                    case READ_FILE -> readFile();
                    case DISPLAY_SPREADSHEET -> displaySheet();
                    case DISPLAY_SINGLE_CELL -> displayCell();
                    case UPDATE_SINGLE_CELL -> updateCell();
                    case DISPLAY_VERSIONS -> displayVersions();
                    case SAVE_OR_LOAD_SYSTEM_STATE -> saveOrLoadSystemState();
                    case EXIT -> {
                        MenuUIConsole.printer("Thank you for using SheetCell application. Goodbye!");
                        return;
                    }
                }
            } catch (Exception e) {
                MenuUIConsole.printer(e.getMessage());
            }
        }
    }

    public void readFile() {
        try {
            menuLogic = MenuActionLogic.loadSheet(MenuUIConsole.getXmlPathFromUser(), versionHistoryManager);
            MenuUIConsole.printer("XML file loaded successfully");
            MenuUIConsole.printer(String.format("Showing \"%s\" Sheet:\n", menuLogic.getSheetName()));
        } catch (Exception e) {
            MenuUIConsole.printer("\n" + e.getMessage() + "\nFailed to load the XML file.\n");
            if (menuLogic != null) {
                MenuUIConsole.printer(String.format("Showing \"%s\" Sheet Menu:\n", menuLogic.getSheetName()));
            }
        }
    }

    public void setFile(File file){
        menuLogic = MenuActionLogic.loadSheet(file.getPath(), versionHistoryManager);
    }

    private void displaySheet() {
        if (menuLogic != null) {
            MenuUIConsole.printer(menuLogic.display().toString());
        } else {
            throw new IllegalStateException("Invalid: no Sheet was loaded\n");
        }
    }

    private void displayCell() {
        if (menuLogic != null) {
            while (true) {
                try {
                    MenuUIConsole.printer("Please enter the cell ID you want to display: ");
                    String cellID = MenuUIConsole.getInputFromUser();
                    MenuUIConsole.printer(menuLogic.showCell(cellID).toString());
                    break;
                } catch (Exception e) {
                    MenuUIConsole.printer(e.getMessage());
                }
            }
        } else {
            throw new IllegalStateException("Invalid: no Sheet was loaded\n");
        }
    }

    private void updateCell() {
        ContinueOrBackSubMenuUI.ContinueOrBackOption.CONTINUE.setDescription("Continue to enter value");

        if (menuLogic != null) {
            while (true) {
                MenuUIConsole.printMenuItem(continueOrBackSubMenuUI);
                int userChoiceInt = MenuUIConsole.getValidUserChoice(continueOrBackSubMenuUI);
                ContinueOrBackSubMenuUI.ContinueOrBackOption userChoice = ContinueOrBackSubMenuUI.ContinueOrBackOption.fromNumber(userChoiceInt);
                try {
                    switch (userChoice) {
                        case CONTINUE -> {
                            if (continueToGetUpdateCellInfo()) {
                                return;
                            }
                        }
                        case BACK -> {
                            return;
                        }
                        default -> throw new Exception("Invalid input. Please try again.");
                    }
                } catch (Exception e) {
                    MenuUIConsole.printer(e.getMessage());
                }
            }
        } else {
            throw new IllegalStateException("Invalid: no Sheet was loaded\n");
        }
    }

    private Boolean continueToGetUpdateCellInfo() {
        String cellID = MenuUIConsole.getCellIDFromUser();
        CellDTO cell = menuLogic.showCell(cellID);
        MenuUIConsole.printCellInfoBeforeUpdateAction(cell, cell.getCoordinate().toString());
        return updateCellAction(cellID);
    }

    private Boolean updateCellAction(String cellID) {
        MenuUIConsole.printer("Please enter the value to update");
        String value = MenuUIConsole.getInputFromUser();
        cellID = cellID.toUpperCase();
        try {
            menuLogic.setCellValue(cellID, value);
            MenuUIConsole.printer(String.format("Value of Cell %s updated successfully!%n", cellID));
            displaySheet();
            return true;
        } catch (Exception e) {
            MenuUIConsole.printer("Failed to update the value of Cell " + cellID +"\nDetails of the failure: " + e.getMessage());
            return false;
        }
    }

    public void updateCellActionJavaFX(String cellID, String value) throws Exception {
        cellID = cellID.toUpperCase();
        try {
            menuLogic.setCellValue(cellID, value);
            MenuUIConsole.printer(String.format("Value of Cell %s updated successfully!%n", cellID));
            displaySheet();
        } catch (Exception e) {
            MenuUIConsole.printer("Failed to update the value of Cell " + cellID + "\nDetails of the failure: " + e.getMessage());
            throw e;
        }
    }

    private void displayVersions() {
        ContinueOrBackSubMenuUI.ContinueOrBackOption.CONTINUE.setDescription("Continue to Display Versions");

        if (menuLogic != null) {
            while (true) {
                MenuUIConsole.printVersionsInfo(menuLogic.showSheetVersionsInfo());
                MenuUIConsole.printMenuItem(continueOrBackSubMenuUI);
                int userChoiceInt = MenuUIConsole.getValidUserChoice(continueOrBackSubMenuUI);
                ContinueOrBackSubMenuUI.ContinueOrBackOption userChoice = ContinueOrBackSubMenuUI.ContinueOrBackOption.fromNumber(userChoiceInt);
                try {
                    switch (userChoice) {
                        case CONTINUE -> {
                            if (continueToDisplayVersions()) {
                                return;
                            }
                        }
                        case BACK -> {
                            MenuUIConsole.printer("Version selection canceled.\n");
                            return;
                        }
                        default -> throw new Exception("Invalid input. Please try again.");
                    }
                } catch (Exception e) {
                    MenuUIConsole.printer(e.getMessage());
                }
            }
        } else {
            throw new IllegalStateException("Invalid: no Sheet was loaded\n");
        }
    }

    private Boolean continueToDisplayVersions() {
        MenuUIConsole.printer("Please enter the version number you would like to preview:");
        String version = MenuUIConsole.getInputFromUser();
        if (menuLogic.isValidVersionNum(version)) {
            SheetDTO sheetAccordingToVersion = menuLogic.getSheetAccordingToVersion(Integer.parseInt(version));
            MenuUIConsole.printer(sheetAccordingToVersion.toString());
            return true;
        } else {return false;}
    }

    private void saveOrLoadSystemState(){
        if(menuLogic != null){
            while (true) {
                MenuUIConsole.printMenuItem(mainMenuUI.getSubMenuSaveOrLoad());
                int userChoiceInt = MenuUIConsole.getValidUserChoice(mainMenuUI.getSubMenuSaveOrLoad());
                saveOrLoadSubMenuUI.SaveOrLoadSystemStateSubAction userChoice = saveOrLoadSubMenuUI.SaveOrLoadSystemStateSubAction.fromNumber(userChoiceInt);

                switch (userChoice) {
                    case SAVE_SYSTEM_STATE -> {
                        MenuUIConsole.printer("Please enter the full file path to save the system state (without extension):");
                        String filePath = MenuUIConsole.getInputFromUser();
                        MenuUIConsole.printer("Please enter desire file name to save the system state:");
                        String fileName = MenuUIConsole.getInputFromUser();

                        if (!isValidFileName(fileName)) {
                            MenuUIConsole.printer("Invalid file name. Please avoid using special characters like \\ / : * ? \" < > | and try again.");
                            break;
                        }

                        String fullPath = filePath + File.separator + fileName + ".dat";

                        if(saveSystemState(fullPath)){
                            return;
                        }
                    }
                    case LOAD_SYSTEM_STATE -> {
                        MenuUIConsole.printer("Please enter the full file path to load the system state (without extension):");
                        String filePath = MenuUIConsole.getInputFromUser();
                        if(loadSystemState(filePath + ".dat")) {
                            return;
                        }
                    }
                    case BACK -> {return;}
                }
            }
        } else {
            throw new IllegalStateException("Invalid: no Sheet was loaded\n");
        }

    }

    public boolean saveSystemState(String filePath) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(menuLogic);
            System.out.println("System state saved successfully to " + filePath);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving system state: " + e.getMessage());
            return false;
        }
    }

    public boolean loadSystemState(String filePath) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            menuLogic = (MenuActionLogic) in.readObject();
            System.out.println("System state loaded successfully from " + filePath);
            return true;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading system state: " + e.getMessage());
            return false;
        }
    }

    private boolean isValidFileName(String fileName) {
        String invalidChars = "[\\\\/:*?\"<>|]";
        return !fileName.matches(".*" + invalidChars + ".*");
    }


    //test
    public SheetDTO getSheetDto(){
        return menuLogic.display();
    }

    public MenuActionLogic getMenuLogic() {
        return menuLogic;
    }

    public SheetDTO displaySheetDTOByVersion(String version) {
        if (menuLogic.isValidVersionNum(version)) {
            SheetDTO sheetAccordingToVersion = menuLogic.getSheetAccordingToVersion(Integer.parseInt(version));
            return sheetAccordingToVersion;
        } else {
            return null;
        }
    }
}
