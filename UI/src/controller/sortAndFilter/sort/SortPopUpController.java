package controller.sortAndFilter.sort;

import controller.AppController;
import controller.skinStyle.SkinChangeListener;
import controller.skinStyle.SkinManager;
import controller.skinStyle.SkinOption;
import controller.sortAndFilter.SortFilterController;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import logicStructure.sheet.DTO.RangeDTO;
import logicStructure.sheet.DTO.SheetMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SortPopUpController implements SkinChangeListener {

    @FXML private Label rangeLabel;
    @FXML private Button cancelButton; // כפתור ביטול
    @FXML private Button sortButton; // כפתור מיון
    @FXML private VBox firstSubComponent;
    @FXML private HBox buttonsHBox;
    @FXML private ScrollPane mainContainer;
    @FXML private VBox mainVBox;
    @FXML private ToggleButton rangeToggleSwitch;
    @FXML private Label toggleLabel;
    @FXML private Circle rangeCircleOfToggleButton;

    private SubSortPopUpController subSortPopUpController;
    private Map<Integer,String> selectedSortColumnsMap = new HashMap<>();
    private boolean hasHeaderRow = false; // האם יש שורת כותרות
    private Map<Integer, SubSortPopUpController> subSortPopUpControllerMap = new HashMap<>();
    private Map<Integer, VBox> subSortPopUpComponentMap = new HashMap<>();
    private SortFilterController mainController;
    private RangeDTO range;
    private List<String> columnsInRange = new ArrayList<>();
    private List<String> columnsSortOrderList = new ArrayList<>();
    private boolean isAllColumnsSelectedAtOnce = false;
    private List<String> allCellsIDList;
    private Tooltip tooltip = new Tooltip();
    private Double toggleSwitchLeftInit;
    private Double toggleSwitchRightInit;

    @FXML
    public void initialize() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/subSortPopUp.fxml"));
                firstSubComponent.getChildren().clear();
                firstSubComponent.getChildren().add(loader.load());
                subSortPopUpController = loader.getController();
                subSortPopUpController.setMainController(this);

                subSortPopUpControllerMap.put(subSortPopUpController.getIndex(), subSortPopUpController);
                subSortPopUpComponentMap.put(subSortPopUpController.getIndex(), firstSubComponent);

            } catch (Exception e){
                e.printStackTrace();
            }

        mainContainer.getStylesheets().add(SkinManager.getCurrentSkinMode().getSortPopUp());

        toggleSwitchRightInit = rangeToggleSwitch.getWidth() - (rangeCircleOfToggleButton.getRadius()*2);
        toggleSwitchLeftInit = rangeCircleOfToggleButton.getRadius()*2;


        setTooltip();

        rangeToggleSwitch.setOnAction(e -> {
                if (rangeToggleSwitch.isSelected()) {
                    toggleLabel.setText("");
                    moveCircleLeft();
                    setTooltip();

                    if(rangeToggleSwitch.isSelected() && columnsInRange.isEmpty()) {
                        showRangeSelectionWarning();
                        this.rangeLabel.setText("");
                    } else {
                        subSortPopUpController.setColumnComboBox(columnsInRange);
                        if(range != null){
                            String start = range.getStartCoordinate().createCellCoordinateString();
                            String end = range.getEndCoordinate().createCellCoordinateString();
                            this.rangeLabel.setText(start +":" + end);
                        } else {
                            this.rangeLabel.setText("");
                        }
                    }
                } else {
                    toggleLabel.setText("");
                    rangeLabel.setText("All Sheet");
                    subSortPopUpController.setColumnComboBox(mainController.getCellsIDList());
                    moveCircleRight();
                    setTooltip();
                }
            });

        cancelButton.setOnAction(event -> closePopup());

        sortButton.setOnAction(event -> handleSort());
    }

    @Override
    public void onSkinChange(SkinOption newSkin) {
        mainContainer.getStylesheets().removeAll(SkinOption.DEFAULT.getSortPopUp(), SkinOption.DARK_MODE.getSortPopUp(),
                SkinOption.MONOCHROME.getSortPopUp(), SkinOption.VIBRANT.getSortPopUp());
        mainContainer.getStylesheets().add(newSkin.getSortPopUp());
    }

    public void setMainController(SortFilterController mainController) {
        this.mainController = mainController;
        this.allCellsIDList = mainController.getCellsIDList();
        subSortPopUpController.setColumnComboBox(allCellsIDList);
        mainController.addSkinListenerForSubControllers(this);
    }

    private void showRangeSelectionWarning() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Please select a range of cells from the sheet before sorting.");
        alert.setTitle("Selection");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void setTooltip(){
        String message = null;
        if(rangeToggleSwitch.isSelected() && columnsInRange.isEmpty()) {
            message = "Select a range of cells in the sheet";
            tooltip.setText(message);
            sortButton.setTooltip(tooltip);
        } else {
            sortButton.setTooltip(null);
        }
    }

    public void setInitStateOfToggleSwitch(boolean isRange){

        if(isRange){
            toggleLabel.setText("");
            rangeCircleOfToggleButton.setLayoutX(0); // העברת ה-Circle שמאלה
            toggleLabel.setText("Range");

        } else {
            toggleLabel.setText("");
            rangeCircleOfToggleButton.setLayoutX(toggleSwitchRightInit); // העברת ה-Circle שמאלה
            toggleLabel.setText("All Sheet");
        }

        this.rangeToggleSwitch.selectedProperty().set(isRange);
    }

    private void handleSort() {
        if(!isAllColumnsSelectedAtOnce && !selectedSortColumnsMap.isEmpty() && mainController != null){
            this.columnsSortOrderList = selectedSortColumnsMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .toList();
        }

        if(range == null && rangeToggleSwitch.isSelected()){
            throw new RuntimeException( "No range selected! Please mark the range of cells in the sheet");
        }

        if(!columnsSortOrderList.isEmpty()){
            mainController.setSelectedColumns(this.columnsSortOrderList);
            closePopup();
            mainController.handleSortAction();

        } else {
            AppController.showError("No columns selected");
        }
    }

    public void addSortColumn() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/subSortPopUp.fxml"));
            VBox newComponent = loader.load();

            int index = mainVBox.getChildren().indexOf(buttonsHBox);
            mainVBox.getChildren().add(index, newComponent);

            SubSortPopUpController newComponentController = loader.getController();
            newComponentController.setMainController(this);
            mainController.addSkinListenerForSubControllers(newComponentController);
            newComponentController.setIndex(subSortPopUpControllerMap.size() + 1);
            newComponentController.setColumnComboBox(allCellsIDList);


            subSortPopUpControllerMap.put(newComponentController.getIndex(), newComponentController);
            subSortPopUpComponentMap.put(newComponentController.getIndex(), newComponent);

        } catch (Exception e){
            e.printStackTrace();
            AppController.showError(e.getMessage());
        }

        if (selectedSortColumnsMap.size() == (columnsInRange.size()-1)) {
            subSortPopUpControllerMap.values().forEach(controller ->
                    controller.setAddSortColumnButtonDisable(true));
            return;
        } else {
            subSortPopUpControllerMap.values().forEach(controller ->
                    controller.setAddSortColumnButtonDisable(false));
        }
    }

    public void updateSortColumnSelected(String columnName, Integer index) {
        selectedSortColumnsMap.put(index, columnName);
    }

    public void updateAllSortColumnsSelectedList(List<String> columnsList) {
        this.columnsSortOrderList = columnsList;
    }

    public void updateRange(String start, String end){
        this.rangeLabel.setText(start +":" + end);
        this.range = SheetMapper.toRangeDTO(start, end);
        this.columnsSortOrderList.clear();
        this.allCellsIDList = range.getColumnNamesListInRange();
        this.columnsInRange.clear();
        this.columnsInRange.addAll(allCellsIDList);
        subSortPopUpController.setColumnComboBox(columnsInRange);
        try {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.toFront();
            stage.requestFocus();
        } catch (Exception e){
            return;
        }
    }

    private void closePopup() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public List<String> getSelectedSortColumns() {
        return selectedSortColumnsMap.values().stream().toList();
    }

    private void moveCircleLeft() {

        if(mainController.getIsWithAnimations()){
            TranslateTransition translateTransition = new TranslateTransition();
            translateTransition.setNode(rangeCircleOfToggleButton); // רכיב ה-Circle שמוזז
            translateTransition.setDuration(Duration.seconds(0.3)); // משך האנימציה
            translateTransition.setToX(-rangeToggleSwitch.getWidth()+ (rangeCircleOfToggleButton.getRadius()*2)); // העברת ה-Circle שמאלה
            translateTransition.play(); // הפעלת האנימציה

            translateTransition.setOnFinished(event -> {
                toggleLabel.setText("Range");
                toggleLabel.setTranslateX(+ (rangeCircleOfToggleButton.getRadius()*2));
            });
        } else {
            rangeCircleOfToggleButton.setTranslateX(-rangeToggleSwitch.getWidth()+ (rangeCircleOfToggleButton.getRadius()*2)); // העברת ה-Circle שמאלה
            toggleLabel.setText("Range");
            toggleLabel.setTranslateX(+ (rangeCircleOfToggleButton.getRadius()*2));
        }

        toggleLabel.getStyleClass().add("selected");

    }

    private void moveCircleRight() {

        if(mainController.getIsWithAnimations()){
            TranslateTransition translateTransition = new TranslateTransition();
            translateTransition.setNode(rangeCircleOfToggleButton);
            translateTransition.setDuration(Duration.seconds(0.3));
            translateTransition.setToX(0); // העברת ה-Circle שמאלה
            translateTransition.play();

            translateTransition.setOnFinished(event -> {
                toggleLabel.setText("All Sheet");
                toggleLabel.setTranslateX(+5);
            });
        } else {
            rangeCircleOfToggleButton.setTranslateX(+rangeToggleSwitch.getWidth() - (rangeCircleOfToggleButton.getRadius()*2));
            toggleLabel.setText("All Sheet");
            toggleLabel.setTranslateX(+5);
        }

        toggleLabel.getStyleClass().remove("selected");
    }
}
