package controller.ranges;

import controller.AppController;
import controller.api.CellSelectionListener;
import controller.skinStyle.SkinChangeListener;
import controller.skinStyle.SkinManager;
import controller.skinStyle.SkinOption;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import logicStructure.sheet.DTO.*;
import main.UIManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RangesController implements SkinChangeListener {

    @FXML private ScrollPane mainContainer;
    @FXML private TableView<RangeDTO> rangeTableView;
    @FXML private TableColumn<RangeDTO, String> rangeNameColumn, topLeftCellColumn, bottomRightCellColumn;
    @FXML private Button addRangeButton,  deleteRangeButton, viewRangeButton, stopViewButton;

    private UIManager uiManager;
    private ObjectProperty<SheetDTO> sheetDTO = new SimpleObjectProperty<>();
    private AppController mainController;
    private List<CoordinateDTO> highlightedCells = new ArrayList<>();
    private AddRangeController addRangeController;

    @FXML
    public void initialize() {
        mainContainer.getStylesheets().add(SkinManager.getCurrentSkinMode().getRanges());
        addRangeButton.setDisable(true);
        setDeleteAndViewButtonDisable(true);
    }

    @Override
    public void onSkinChange(SkinOption newSkin) {
        mainContainer.getStylesheets().removeAll(SkinOption.DEFAULT.getRanges(), SkinOption.DARK_MODE.getRanges(),
                SkinOption.MONOCHROME.getRanges(), SkinOption.VIBRANT.getRanges());
        mainContainer.getStylesheets().add(newSkin.getRanges());
    }

    private void setDeleteAndViewButtonDisable(boolean isDisable){
        deleteRangeButton.setDisable(isDisable);
        viewRangeButton.setDisable(isDisable);
    }

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
        mainController.addSkinListener(this);
    }

    public void setUiManager(UIManager uiManager) {
        this.uiManager = uiManager;
        sheetDTO.set(uiManager.getMenuLogic().display());
        addRangeButton.setDisable(false);

        setRangeTableView(null);

        viewRangeButton.setOnAction(event -> setRangesTableView());
        stopViewButton.setOnAction(event -> rangeTableView.getSelectionModel().select(null));
    }

    private void setCenterWrappedCellFactory(TableColumn<RangeDTO, String> column) {
        column.setCellFactory(col -> new TableCell<RangeDTO, String>() {
            private final Label label = new Label();

            {
                label.setWrapText(true); // מאפשר שבירת שורות בטקסט
                label.setMaxWidth(Double.MAX_VALUE); // מאפשר לטקסט לקחת את כל הרוחב האפשרי
                setGraphic(label); // שימוש בלייבל בתוך התא
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    System.out.println("Setting text for cell: " + item);
                    label.setText(item);
                    setWrapText(true);
                    setGraphic(label);
                }
        }});
    }

    private ObservableList<RangeDTO> createRangeNamesList(){
        List<RangeDTO> ranges = sheetDTO.get().getRanges().entrySet().stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        ranges.removeIf(range -> range == null || range.getName().trim().isEmpty());

        return FXCollections.observableArrayList(ranges);
    }

    private void setRangesTableView(){
        rangeTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showRangeDetails(newSelection);
                setDeleteAndViewButtonDisable(false);
                stopViewButton.setVisible(true);
            } else {
                hideRangeDetails();
                viewRangeButton.setText("View Range");
                setDeleteAndViewButtonDisable(true);
                stopViewButton.setVisible(false);
            }
        });
    }

    private void setRangeTableView(ObservableList<RangeDTO> items){
        rangeNameColumn.setCellFactory(null);
        rangeNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        setCenterWrappedCellFactory(rangeNameColumn);

        topLeftCellColumn.setCellFactory(null);
        topLeftCellColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStartCoordinate().createCellCoordinateString()));
        setCenterWrappedCellFactory(topLeftCellColumn);

        bottomRightCellColumn.setCellFactory(null);
        bottomRightCellColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEndCoordinate().createCellCoordinateString()));
        setCenterWrappedCellFactory(bottomRightCellColumn);

        if(items == null){
            setItemsAndListernersToTableView(createRangeNamesList());
        } else {
            setItemsAndListernersToTableView(items);
        }
    }

    private void setItemsAndListernersToTableView(ObservableList<RangeDTO> items){
        rangeTableView.setItems(items);
        setRangesTableView();
    }

    // Display range details when a range is selected
    private void showRangeDetails(RangeDTO range) {
        List<CoordinateDTO> ranges = range.getCellsInRange();

        mainController.removeHighlightCellRangeAccordingToCoordinate(highlightedCells);
        highlightedCells.clear();

        highlightedCells.addAll(ranges);
        mainController.highlightCellRangeAccordingToCoordinate(ranges);
    }

    // Hide range details when no range is selected
    private void hideRangeDetails() {
        mainController.removeHighlightCellRangeAccordingToCoordinate(highlightedCells);
        highlightedCells.clear();
    }

    // Delete the selected range
    @FXML
    public void deleteRange() {
        RangeDTO selectedRange = rangeTableView.getSelectionModel().getSelectedItem();
        if (selectedRange != null) {
            try {
                uiManager.getMenuLogic().deleteRange(selectedRange.getName());

                ObservableList<RangeDTO> rangesList = rangeTableView.getItems();
                rangesList.remove(selectedRange);

                sheetDTO.set(uiManager.getMenuLogic().display());
                rangeTableView.setItems(null);
                rangeTableView.setItems(rangesList);
                setRangeTableView(rangesList);

                hideRangeDetails();
                showPopup(String.format("Range '%s' deleted successfully!", selectedRange.getName()));
            } catch (Exception e){
                AppController.showError(e.getMessage());
            }
        } else {
            AppController.showError("No range selected for deletion.");
        }
    }

    private void showPopup(String message) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL); // חוסם אינטראקציות עם חלונות אחרים אם נדרש

        Label messageLabel = new Label(message);
        if(mainController.getIsWithAnimations()){
            popupStage.initStyle(StageStyle.UNDECORATED); // הסרת מסגרת החלון
            messageLabel.setStyle(
                    "-fx-background-color: #eefcff; " +  // רקע כהה
                            "-fx-text-fill: #020202; " +           // טקסט לבן
                            "-fx-font-size: 18px; " +            //// גודל פונט גדול יותר
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 15px 30px; " +// ריווח פנימי
                            "-fx-alignment: center;"+
                            "-fx-wrap-text: true;"+
                            "-fx-border-color: #00bd0d; " +      // גבול בצבע אדום כהה
                            "-fx-border-width: 5px; " +          // עובי הגבול
                            "-fx-border-radius: 10px; " +        // פינות עגולות לגבול
                            "-fx-background-radius: 10px; "      // פינות עגולות לרקע
            );
        } else {
            popupStage.initStyle(StageStyle.DECORATED);
            messageLabel.setStyle(  "-fx-font-size: 18px; " +            //// גודל פונט גדול יותר
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 15px 30px; " +// ריווח פנימי
                    "-fx-alignment: center;"+
                    "-fx-wrap-text: true;");
        }

        // הגדרת פריסת ההודעה
        StackPane layout = new StackPane(messageLabel);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        popupStage.setScene(scene);
        popupStage.sizeToScene();

        if(mainController.getIsWithAnimations()){
            mainController.blurrMainWindow(true);
            // יצירת Fade Transition עבור האנימציה של ההופעה (שקיפות)
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), layout); // הופעה חלקה ב-500 מילישניות
            fadeIn.setFromValue(0.0); // מתחיל בשקיפות מלאה
            fadeIn.setToValue(1.0); // הופך לגלוי במלואו
            fadeIn.play(); // הפעלת האנימציה

            popupStage.show();

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
                popupStage.close();
                mainController.blurrMainWindow(false);
            }));
            timeline.setCycleCount(1);
            timeline.play();
        } else {
            popupStage.show();
        }
    }

    @FXML
    public void addRange() {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/AddNewRangeDialog.fxml"));
                Parent dialogContent = fxmlLoader.load();
                addRangeController = fxmlLoader.getController();
                addRangeController.setMainController(this);
                mainController.addSkinListener(addRangeController);

                Stage dialogStage = new Stage();
                dialogStage.setTitle("Add New Range");
                dialogStage.setScene(new Scene(dialogContent));
                dialogStage.initModality(Modality.NONE);  // מאפשר אינטראקציה עם הגיליון בזמן שהדיאלוג פתוח
                dialogStage.show();

                mainController.setOnCellSelectionListener(addRangeController);


                addRangeController.getOkButton().setOnAction(event -> {
                    addRangeController.handleOk(); // עיבוד הנתונים
                    String rangeName = addRangeController.getRangeName();
                    String rangeStartSelection = addRangeController.getRangeStartSelection();
                    String rangeEndSelection = addRangeController.getRangeEndSelection();

                    try {
                        uiManager.getMenuLogic().addNewRange(rangeName, rangeStartSelection, rangeEndSelection);
                        sheetDTO.set(uiManager.getMenuLogic().display());
                        setRangeTableView(null);
                        Stage stage = (Stage) addRangeController.getOkButton().getScene().getWindow();
                        stage.close();
                        addRangeController.getCellUnselectionListener().removeFromOnCellSelectionListener(addRangeController);

                        if(addRangeController.getCellUnselectionListener() != null){
                            addRangeController.getCellUnselectionListener().onCellUnselection(rangeStartSelection, rangeEndSelection);
                        }
                    } catch (Exception e) {
                        AppController.showError(e.getMessage());
                    }
                });

                dialogStage.setOnCloseRequest(event -> {
                    addRangeController.getCellUnselectionListener().removeFromOnCellSelectionListener(addRangeController);
                });
            } catch (Exception e) {
                AppController.showError(e.getMessage());
            }
    }


    @FXML
    public void viewRange() {
        RangeDTO selectedRange = rangeTableView.getSelectionModel().getSelectedItem();
        showRangeDetails(selectedRange);
    }
}
