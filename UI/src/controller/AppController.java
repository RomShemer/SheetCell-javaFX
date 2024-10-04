package controller;

import controller.api.CellSelectionListener;
import controller.commands.CommandController;
import controller.graphs.GraphsController;
import controller.ranges.RangesController;
import controller.sheet.SheetController;
import controller.sheet.api.SheetControllerFilterAction;
import controller.sheet.api.SheetControllerSortActions;
import controller.sheet.api.SheetInTabAction;
import controller.skinStyle.SkinChangeListener;
import controller.skinStyle.SkinManager;
import controller.skinStyle.SkinOption;
import controller.sortAndFilter.SortFilterController;
import controller.topTools.SubTolBarTopController;
import controller.topTools.TopBarController;
import javafx.animation.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import logicStructure.sheet.DTO.*;
import main.MainController;
import main.UIManager;
import java.io.File;
import java.util.*;

import javafx.scene.effect.GaussianBlur;

public class AppController implements SkinChangeListener {

    private UIManager uiManager = new UIManager();
    private SkinManager skinManager = new SkinManager();
    private File choosenFile;
    private SheetController sheetComponentController;
    private CommandController commandsComponentController;
    private RangesController rangesComponentController;
    private SortFilterController sortFilterComponentController;
    private GraphsController graphsController;
    private Parent graphPopUp;
    private Stage graphPopUpStage;
    private ObjectProperty<SheetDTO> sheetDTOObjectProperty = new SimpleObjectProperty<>(null);
    private MainController mainController;
    private Map<Integer, GridPane> gridPaneMap = new HashMap<>();
    private Map.Entry<Integer, GridPane> currentActiveGridPane;
    private boolean isWithAnimations;
    private Map<String, SheetInTabAction> TabSheetControllers = new HashMap<>();
    private SheetInTabAction currentSheetController;


    @FXML private ScrollPane headerComponent;
    @FXML private ScrollPane actionLineComponent;
    @FXML private TopBarController headerComponentController;
    @FXML private SubTolBarTopController actionLineComponentController;
    @FXML private TabPane centerTabPane;
    @FXML private Tab firstTab;
    @FXML private ScrollPane sheetComponent;
    @FXML private ScrollPane commandsComponent;
    @FXML private ScrollPane rangesComponent;
    @FXML private ScrollPane sortAndFilterComponent;
    @FXML private VBox leftVbox;


    @FXML
    public void initialize() {
        try {
            if (headerComponentController != null && actionLineComponentController != null) {
                headerComponentController.setMainController(this);
                actionLineComponentController.setMainController(this);
                centerTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

                // טעינת הקובץ newCommands.fxml בצורה מפורשת
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/newCommands.fxml"));
                ScrollPane commandsPane = loader.load();
                commandsComponent.setContent(commandsPane); // מגדיר את תוכן ה-ScrollPane
                commandsComponentController = loader.getController(); // קבלת הקונטרולר המשויך

                FXMLLoader loaderRange = new FXMLLoader(getClass().getResource("/Ranges.fxml"));
                rangesComponent.setContent(loaderRange.load());
                rangesComponentController = loaderRange.getController(); // קבלת הקונטרולר המשויך
                rangesComponentController.setMainController(this);

                FXMLLoader loaderSortAndFilter = new FXMLLoader(getClass().getResource("/SortAndFilter.fxml"));
                sortAndFilterComponent.setContent(loaderSortAndFilter.load());
                sortFilterComponentController = loaderSortAndFilter.getController(); // קבלת הקונטרולר המשויך
                sortFilterComponentController.setMainController(this);

                FXMLLoader loaderGraph = new FXMLLoader(getClass().getResource("/Graphs.fxml"));
                graphPopUp = loaderGraph.load();
                graphsController = loaderGraph.getController();
                graphsController.setMainController(this);

                sheetComponentController = new SheetController();
                sheetComponentController.setMainController(this);
                sheetComponent.setFitToWidth(true);
                sheetComponent.setFitToHeight(true);

                // מוודא חיבור הקונטרולרים
                commandsComponentController.setMainController(this);
                commandsComponentController.setSheetController(sheetComponentController);

                sheetDTOObjectProperty.addListener((obs, oldSheet, newSheet) -> {
                    if (newSheet != null) {
                        setSheet(newSheet, false);
                    }
                });

                centerTabPane.getStylesheets().add(SkinManager.getCurrentSkinMode().getTabs());
                centerTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
                    if(firstTab != null && newTab.equals(firstTab)){
                        setUpdateButtonDisale(false);
                        rangesComponent.setDisable(false);
                        commandsComponent.setDisable(false);
                        currentSheetController = null;
                    } else if(newTab != null){
                        setUpdateButtonDisale(true);
                        rangesComponent.setDisable(true);
                        commandsComponent.setDisable(true);
                        currentSheetController = TabSheetControllers.get(newTab.getId());
                    }

                    if(isWithAnimations){
                        openTabAnimations(newTab, newTab.getContent());
                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSkinChange(SkinOption newSkin) {
        centerTabPane.getStylesheets().removeAll(SkinOption.DEFAULT.getTabs(), SkinOption.DARK_MODE.getTabs(),
                SkinOption.MONOCHROME.getTabs(), SkinOption.VIBRANT.getTabs());
        centerTabPane.getStylesheets().add(newSkin.getTabs());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        skinManager.addSkinChangeListener(this);
    }

    public void createGraphAcion(){
        try {
            if(graphPopUp == null){
                FXMLLoader loaderGraph = new FXMLLoader(getClass().getResource("/Graphs.fxml"));
                graphPopUp = loaderGraph.load();
                graphsController = loaderGraph.getController();
                graphsController.setMainController(this);
            }

            graphsController.setSheet(uiManager.getMenuLogic().display());
            sheetComponentController.setOnCellSelectionListener(graphsController);

            if(graphPopUpStage == null){
                graphPopUpStage = new Stage();
                graphPopUpStage.setTitle("Create Graph");
                graphPopUpStage.setScene(new Scene(graphPopUp));
                graphPopUpStage.initModality(Modality.NONE);
                Button graphsButton = sortFilterComponentController.getGraphButton();
                graphPopUpStage.initOwner(graphsButton.getScene().getWindow());

                Bounds buttonBounds = graphsButton.localToScreen(graphsButton.getBoundsInLocal());
                graphPopUpStage.setX(buttonBounds.getMinX());
                graphPopUpStage.setY(buttonBounds.getMaxY());
            }

            graphPopUpStage.show();

            graphPopUpStage.setOnCloseRequest(event ->{
                graphsController.clearPrevInfo();
            });

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public void setSheetDTOObjectProperty(SheetDTO sheetDTO){
        this.sheetDTOObjectProperty.set(sheetDTO);
    }

    public List<String> getCellIDList(){
        return uiManager.getMenuLogic().display().getColumnNamesList();
    }

    public String getFirstCellIDInSheet(){
        return uiManager.getMenuLogic().display().getMinCoordinateInSheet().createCellCoordinateString();
    }

    public String getLastCellIDInSheet(){
        return uiManager.getMenuLogic().display().getMaxCoordinateInSheet().createCellCoordinateString();
    }

    public void updateChosenFile(File file){
        if(choosenFile != null){
            clearAllFSheetDataForCurrentFile();
        }

        choosenFile = file;
        uiManager.setFile(file);
        actionLineComponentController.setUiManager(uiManager);
        rangesComponentController.setUiManager(uiManager);
        sheetComponent.setFitToWidth(true);
        sheetComponent.setFitToHeight(true);
        setSheet(uiManager.getMenuLogic().display(), true);
        graphsController.setSheet(uiManager.getMenuLogic().display());
        sheetComponentController.setOnCellSelectionListener(sortFilterComponentController);
        commandsComponentController.setSheetController(sheetComponentController);
        sheetComponentController.setCommandController(commandsComponentController);
        commandsComponentController.getGridPaneProperty().bindBidirectional(sheetComponentController.getGridPaneProperty());
    }

    public void clearAllFSheetDataForCurrentFile(){

        for(Tab tab : centerTabPane.getTabs()){
            if(tab.getId() != firstTab.getId()){
                centerTabPane.getTabs().remove(tab);
            } else {
                sheetComponent.setContent(null);
                tab.setContent(sheetComponent);
            }
        }
        sheetDTOObjectProperty.set(null);
        gridPaneMap.clear();
        currentActiveGridPane = null;
        TabSheetControllers.clear();
        currentSheetController = null;
        uiManager.clear();
        uiManager = new UIManager();
        rangesComponent.setContent(null);
        commandsComponent.setContent(null);

        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/subTolBarTop.fxml"));
            ScrollPane pane = loader.load();
            actionLineComponent.setContent(null);
            actionLineComponent.setContent(pane); // מגדיר את תוכן ה-ScrollPane
            actionLineComponentController = loader.getController(); // קבלת הקונטרולר המשויך
            initialize();

        } catch (Exception e){
            e.printStackTrace();
            showError("Error loading new commands file");
        }
    }

    public void systemSkinOnAction(SkinOption skinOption){
        skinManager.changeSkin(skinOption);
    }

    public void addSkinListener(SkinChangeListener listener){
        skinManager.addSkinChangeListener(listener);
    }

    public void showSheetAcoordingToVersion(Integer version) {
        if(currentActiveGridPane!=null && currentActiveGridPane.getKey() == version){
            setUpdateButtonDisale(false);
            sheetComponentController.setIsReadOnly(false);
            commandsComponent.setDisable(false);
            sheetComponent.setContent(null);
            sheetComponent.setContent(currentActiveGridPane.getValue());
        } else {
            setUpdateButtonDisale(true);
            sheetComponentController.setIsReadOnly(true);
            commandsComponent.setDisable(true);
            sheetComponent.setContent(null);
            sheetComponent.setContent(gridPaneMap.get(version));
        }
    }

    public void showSheetAcoordingToVersionInTab(Integer version){
        try {
            SheetDTO sheet = uiManager.displaySheetDTOByVersion(String.valueOf(version));
            showeSheetAcoordingToVersionInTabs(sheet, sheet.getName() + " Version: " + String.valueOf(version));
        } catch (Exception e){
            e.printStackTrace();
            showError(e.getMessage());
        }

    }

    public void showeSheetAcoordingToVersionInTabs(SheetDTO versionSheet, String name){
        ScrollPane newSheetComponent = new ScrollPane();
        SheetControllerSortActions newSheetController = new SheetController();
        newSheetController.setIsReadOnly(true);
        newSheetController.setMainController(this);

        newSheetController.clearGrid(true);
        newSheetController.setSheetDTO(versionSheet);
        newSheetController.createGridPane();
        GridPane gridPane = newSheetController.getGridReadOnly();
        GridPane.setHgrow(gridPane, Priority.ALWAYS);
        GridPane.setVgrow(gridPane, Priority.ALWAYS);
        newSheetComponent.setContent(gridPane);
        newSheetComponent.setFitToWidth(true);
        newSheetComponent.setFitToHeight(true);

        Tab newTab = new Tab();
        newTab.setClosable(true);
        newTab.setText(name);
        String uniqueTabID = UUID.randomUUID().toString();  // מזהה ייחודי
        newTab.setId(uniqueTabID);

        newTab.setContent(newSheetComponent);
        newTab.setOnClosed(event -> {
            setUpdateButtonDisale(false);
            rangesComponent.setDisable(false);
            commandsComponent.setDisable(false);
        });

        TabSheetControllers.put(newTab.getId(), newSheetController);
        newSheetController.setTabID(newTab.getId());
        newSheetController.setTabName(newTab.getText());

        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.setMaxHeight(Double.MAX_VALUE);

        newSheetComponent.setMaxWidth(Double.MAX_VALUE);
        newSheetComponent.setMaxHeight(Double.MAX_VALUE);

        centerTabPane.getTabs().add(newTab);
        centerTabPane.getSelectionModel().select(newTab);
    }

    public void showStageInNewTab(Node content, String name){
        Tab newTab = new Tab();
        ScrollPane component = new ScrollPane();
        newTab.setClosable(true);
        newTab.setText(name);
        String uniqueTabID = UUID.randomUUID().toString();  // מזהה ייחודי
        newTab.setId(uniqueTabID);

        VBox containerVBox = new VBox();
        containerVBox.setAlignment(Pos.CENTER);
        HBox containerHBox = new HBox();
        containerHBox.setAlignment(Pos.CENTER);
        containerHBox.getChildren().add(content);
        containerVBox.getChildren().add(containerHBox);

        component.setContent(containerVBox);
        component.setFitToWidth(true);
        component.setFitToHeight(true);

        newTab.setContent(component);
        newTab.setOnClosed(event -> {
            setUpdateButtonDisale(false);
            rangesComponent.setDisable(false);
            commandsComponent.setDisable(false);
        });

        centerTabPane.getTabs().add(newTab);
        centerTabPane.getSelectionModel().select(newTab);
    }

    public void updateCellSeclectedReadOnlyVersion(String selectedCellId) {
        String originalValue;
        int lastVersion = 1;
        CoordinateDTO selectedCellCoordinate = SheetMapper.toCoordinateDTO(selectedCellId);
        String version = actionLineComponentController.getVersionComboBoxSelection();
        version = (version == null) ? String.valueOf(lastVersion) : version;
        CellDTO selectedCellDTO = uiManager.displaySheetDTOByVersion(version).getCells().get(selectedCellCoordinate);
        if (selectedCellDTO!=null){
            originalValue = selectedCellDTO.getOriginalValue();
            lastVersion = selectedCellDTO.getLastVersionUpdated();
        } else {
            originalValue = "Empty";
        }

        actionLineComponentController.setAllInformationPresented(selectedCellId,originalValue,lastVersion);
        if(currentSheetController == null){
            sheetComponentController.highlightCell(selectedCellId);
        } else {
            currentSheetController.highlightCell(selectedCellId);
        }
    }

    public void setSheet(SheetDTO sheet, boolean isNewSheet) {
        setUpdateButtonDisale(false);
        sheetComponentController.clearGrid(isNewSheet);
        sheetComponentController.setSheetDTO(sheet);
        actionLineComponentController.setSheetDTO(sheet);
        sheetComponentController.createGridPane();
        GridPane gridPane = sheetComponentController.getGridPane();

        firstTab.setText(sheet.getName());
        firstTab.setClosable(false);
        TabSheetControllers.put(firstTab.getId(), sheetComponentController);
        sheetComponentController.setTabID(firstTab.getId());
        sheetComponentController.setTabName(sheet.getName());

        sheetComponent.setContent(gridPane);

        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.setMaxHeight(Double.MAX_VALUE);
        currentActiveGridPane = new AbstractMap.SimpleEntry<>(sheet.getVersion(), sheetComponentController.getGridPane());
        gridPaneMap.put(sheet.getVersion(), sheetComponentController.getGridReadOnly());
    }

    public void updateCellSeclected(String selectedCellId) {
        actionLineComponentController.selectCellInComboBox(selectedCellId);
        sheetComponentController.highlightCell(selectedCellId);
    }

    public void setUpdateButtonDisale(boolean isDisable){
        actionLineComponentController.setUpdateButtonDisable(isDisable);
    }

    public void removeHighlightCellRangeAccordingToCoordinate(List<CoordinateDTO> rangeCells) {
        sheetComponentController.removeHighlightRangeCells(rangeCells);
    }

    public void highlightCellRangeAccordingToCoordinate(List<CoordinateDTO> rangeCells) {
        sheetComponentController.highlightSelectedRangeCells(rangeCells);
    }

    public void setOnCellSelectionListener(CellSelectionListener listener) {
        if(sheetComponentController != null){
            sheetComponentController.setOnCellSelectionListener(listener);
        }
    }

    public SheetDTO getSortedSheetDTO(RangeDTO range, List<String> columnOrder){
        if(uiManager.getMenuLogic() != null){
            try {
                return uiManager.getMenuLogic().sort(range, columnOrder);
            } catch (Exception e){
                e.printStackTrace();
                showError(e.getMessage());
            }
        }
        return null;
    }

    public void showSortedSheetInTabs(SheetDTO sortedSheet, String name, String startRange, String endRange){
        ScrollPane newSheetComponent = new ScrollPane();
        SheetControllerSortActions newSheetController = new SheetController();
        newSheetController.setStylingPerCellLabelMap(sheetComponentController.getStylingPerCellLabelMap());
        newSheetController.setStylingPerColumnMap(sheetComponentController.getStylingPerColumnMap());
        newSheetController.setIsReadOnly(true);
        newSheetController.setMainController(this);

        newSheetController.clearGrid(true);
        newSheetController.setSheetDTO(sortedSheet);
        newSheetController.createGridPane();
        GridPane gridPane = newSheetController.getGridReadOnly();
        GridPane.setHgrow(gridPane, Priority.ALWAYS);
        GridPane.setVgrow(gridPane, Priority.ALWAYS);
        newSheetComponent.setContent(gridPane);
        newSheetComponent.setFitToWidth(true);
        newSheetComponent.setFitToHeight(true);

        newSheetController.colorCellsOfRangeForReadOnly(startRange, endRange, "highlight-Range-ReadOnly");

        Tab newTab = new Tab();
        newTab.setClosable(true);
        newTab.setText(name);
        String uniqueTabID = UUID.randomUUID().toString();  // מזהה ייחודי
        newTab.setId(uniqueTabID);

        newTab.setContent(newSheetComponent);
        newTab.setOnClosed(event -> {
            setUpdateButtonDisale(false);
            sortFilterComponentController.clearAllSortFields();
            rangesComponent.setDisable(false);
            commandsComponent.setDisable(false);
        });

        TabSheetControllers.put(newTab.getId(), newSheetController);
        newSheetController.setTabID(newTab.getId());
        newSheetController.setTabName(newTab.getText());

        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.setMaxHeight(Double.MAX_VALUE);

        newSheetComponent.setMaxWidth(Double.MAX_VALUE);
        newSheetComponent.setMaxHeight(Double.MAX_VALUE);

        centerTabPane.getTabs().add(newTab);
        centerTabPane.getSelectionModel().select(newTab);
    }

    public SheetControllerFilterAction showFilterSheetInTabs(String name, String startRange, String endRange){
        ScrollPane newSheetComponent = new ScrollPane();
        SheetControllerFilterAction newSheetController = new SheetController();
        newSheetController.setIsReadOnly(false);
        newSheetController.setMainController(this);

        newSheetController.clearGrid(false);
        newSheetController.setSheetDTO(uiManager.getMenuLogic().display());
        newSheetController.setStylingPerCellLabelMap(sheetComponentController.getStylingPerCellLabelMap());
        newSheetController.setStylingPerColumnMap(sheetComponentController.getStylingPerColumnMap());
        newSheetController.createGridPane();
        GridPane gridPane = newSheetController.getGridPane();
        GridPane.setHgrow(gridPane, Priority.ALWAYS);
        GridPane.setVgrow(gridPane, Priority.ALWAYS);
        //newSheetController.setGridPane(gridPane);
        newSheetComponent.setContent(gridPane);
        newSheetComponent.setFitToWidth(true);
        newSheetComponent.setFitToHeight(true);

        Tab newTab = new Tab();
        newTab.setClosable(true);
        newTab.setText(name);
        String uniqueTabID = UUID.randomUUID().toString();  // מזהה ייחודי
        newTab.setId(uniqueTabID);

        newTab.setContent(newSheetComponent);
        newTab.setOnClosed(event -> {
            setUpdateButtonDisale(false);
            sortFilterComponentController.unmarkFilterRange();
            sortFilterComponentController.clearAllFilterFields();
            rangesComponent.setDisable(false);
            commandsComponent.setDisable(false);
        });

        TabSheetControllers.put(newTab.getId(), newSheetController);
        newSheetController.setTabID(newTab.getId());
        newSheetController.setTabName(newTab.getText());

        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.setMaxHeight(Double.MAX_VALUE);

        newSheetComponent.setMaxWidth(Double.MAX_VALUE);
        newSheetComponent.setMaxHeight(Double.MAX_VALUE);

        centerTabPane.getTabs().add(newTab);
        centerTabPane.getSelectionModel().select(newTab);

        return newSheetController;
    }

    public void replaceFilterGridPaneInTab(GridPane newGridPane, RangeDTO range, String name, String tabID){
        Tab tab = centerTabPane.getTabs().stream()
                .filter(tabItem -> tabItem.getId().equals(tabID) && tabItem.getText().equals(name))
                .findFirst().orElse(null);

        if(tab != null){
            ScrollPane component = (ScrollPane) tab.getContent();
            component.setContent(null);
            component.setContent(newGridPane);
            component.setFitToWidth(true);
            component.setFitToHeight(true);
            tab.setContent(component);
        } else {
            showFilterSheetInTabs(name, range.getStartCoordinate().createCellCoordinateString(), range.getEndCoordinate().createCellCoordinateString());
            System.out.println("no open tab found for id: " + tabID + " created a new tab with the data");
        }
    }

    public SheetDTO filterAction(RangeDTO range, char column ,List<String> filterValueList, String tabID) throws Exception {
        return uiManager.getMenuLogic().filterByColumn(range, column, filterValueList,tabID);
    }

    public void updateCellSeclectedReadOnlyForNewTab(String selectedCellId) {
        String originalValue;
        int lastVersion = 1;
        CoordinateDTO selectedCellCoordinate = SheetMapper.toCoordinateDTO(selectedCellId);
        String version = actionLineComponentController.getVersionComboBoxSelection();
        version = (version == null) ? String.valueOf(lastVersion) : version;
        CellDTO selectedCellDTO = uiManager.displaySheetDTOByVersion(version).getCells().get(selectedCellCoordinate);
        if (selectedCellDTO!=null){
            originalValue = selectedCellDTO.getOriginalValue();
            lastVersion = selectedCellDTO.getLastVersionUpdated();
        } else {
            originalValue = "Empty";
        }

        actionLineComponentController.setAllInformationPresented(selectedCellId,originalValue,lastVersion);
    }

    public static void showError(String message) {
        showError(message, "Error", null,false);
    }

    public static void showError(String message, boolean isShowAndWait) {
        showError(message, "Error", null, isShowAndWait);
    }

    public static void showError(String message, String header, boolean isShowAndWait) {
        showError(message, "Error", header, isShowAndWait);
    }

    public static void showError(String message,String title, String header, boolean isShowAndWait) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add(SkinManager.getCurrentSkinMode().getSystemMessage());

        alert.setTitle(title);
        if(header != null){
            alert.setHeaderText(header);
        }

        alert.setContentText(message);

        if(isShowAndWait){
            alert.showAndWait();
        } else {
            alert.show();
        }
    }

    public static void showSystemMessage(Stage stage) {
        Scene scene = stage.getScene();
        scene.getStylesheets().add(SkinManager.getCurrentSkinMode().getSystemMessage());
        stage.show();
    }

    public boolean getIsWithAnimations(){
        return isWithAnimations;
    }

    public void setIsWithAnimations(boolean isWithAnimations){
        this.isWithAnimations = isWithAnimations;
    }

    public void blurrMainWindow(boolean isBlurr) {
        // השגת ה-Stage מתוך ה-Button (או כל רכיב אחר)
        Stage mainStage = (Stage) commandsComponent.getScene().getWindow();
        if (isBlurr && isWithAnimations){
            GaussianBlur blurEffect = new GaussianBlur(7);
            mainStage.getScene().getRoot().setEffect(blurEffect);
        } else {
            mainStage.getScene().getRoot().setEffect(null);
        }
    }

    public void openTabAnimations(Tab tab, Node node){
        // אנימציית שקיפות (fade-in)
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), node);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        // אנימציית הגדלה (scale-in) רוחבית מהירה יותר
        ScaleTransition scaleInX = new ScaleTransition(Duration.millis(500), node); // כיווץ מהיר יותר
        scaleInX.setFromX(0.0);  // מתחיל בכיווץ רוחבי מוחלט
        scaleInX.setToX(1.0);    // מתרחב לגודל מלא

        // אנימציית הגדלה (scale-in) אנכית מהירה יותר
        ScaleTransition scaleInY = new ScaleTransition(Duration.millis(500), node); // כיווץ מהיר יותר
        scaleInY.setFromY(0.5);  // מתחיל בגובה חצי
        scaleInY.setToY(1.0);    // מתרחב לגודל מלא

        // אנימציית רוטציה קלה מהירה יותר
        RotateTransition rotate = new RotateTransition(Duration.millis(500), node); // רוטציה מהירה יותר
        rotate.setFromAngle(-10);
        rotate.setToAngle(0);

        // יצירת אפקט מקבילי לפתיחה
        ParallelTransition parallelTransition = new ParallelTransition(fadeIn, scaleInX, scaleInY, rotate);

        // הגדלה ב-3% בגמר האנימציה הראשונית
        ScaleTransition enlarge = new ScaleTransition(Duration.millis(300), node);
        enlarge.setFromX(1.0);
        enlarge.setFromY(1.0);
        enlarge.setToX(1.03); // הגדלה ב-3%
        enlarge.setToY(1.03);

        // חזרה לגודל רגיל לאחר ההגדלה
        ScaleTransition resetSize = new ScaleTransition(Duration.millis(300), node);
        resetSize.setFromX(1.03);
        resetSize.setFromY(1.03);
        resetSize.setToX(1.0); // חזרה לגודל המקורי
        resetSize.setToY(1.0);

        // שילוב כל האנימציות ברצף
        SequentialTransition sequentialTransition = new SequentialTransition(
                parallelTransition, enlarge, resetSize
        );

        sequentialTransition.setOnFinished(event -> {
            FadeTransition blinkText = new FadeTransition(Duration.millis(500), tab.getTabPane().lookup(".tab:selected .text")); // הבהוב הטקסט
            blinkText.setFromValue(1.0);
            blinkText.setToValue(0.3);
            blinkText.setCycleCount(6);  // מספר ההבהובים
            blinkText.setAutoReverse(true);  // הלוך וחזור

            // שימוש בעיצוב CSS זמני לטקסט ההבהוב של ה-Tab
            tab.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;"); // הדגשה עם צבע כחול

            blinkText.setOnFinished(e -> {
                // החזרת הטקסט לעיצוב המקורי אחרי ההבהוב
                tab.setStyle("");  // מחיקת העיצוב המיוחד
            });

            blinkText.play();
        });

        // הפעלת האנימציה
        sequentialTransition.play();
    }
}
