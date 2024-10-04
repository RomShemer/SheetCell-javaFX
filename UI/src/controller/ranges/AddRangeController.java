package controller.ranges;

import controller.AppController;
import controller.CommonResourcesPaths;
import controller.api.CellSelectionListener;
import controller.api.CellUnselectionListener;
import controller.skinStyle.SkinChangeListener;
import controller.skinStyle.SkinManager;
import controller.skinStyle.SkinOption;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AddRangeController implements CellSelectionListener, SkinChangeListener {

    private String startRange, endRange;
    private boolean isAllDetailsFilled = false;
    private CellUnselectionListener cellUnselectionListener;
    private RangesController mainController;

    @FXML private GridPane mainContainer;
    @FXML private TextField rangeNameField;
    @FXML private TextField manualRangeField;
    @FXML private RadioButton manualEntryOption;
    @FXML private RadioButton selectFromSheetOption;
    @FXML private Button okButton;

    @FXML
    public void initialize() {
        mainContainer.getStylesheets().add(SkinManager.getCurrentSkinMode().getAddRange());
        okButton.setDisable(true);
        rangeNameField.clear();
        manualRangeField.clear();
        manualRangeField.promptTextProperty().set("Enter manual range (e.g., A1:B2)");
        manualRangeField.setVisible(false);

        // כאשר בוחרים הזנה ידנית, אפשר להזין טווח ידנית
        manualEntryOption.setOnAction(event -> {
            manualRangeField.setDisable(false);
            manualRangeField.setVisible(true);
        });

        manualEntryOption.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                manualRangeField.setDisable(false);
                manualRangeField.setVisible(true);
                selectFromSheetOption.setSelected(false);
            }
        });

        selectFromSheetOption.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                manualRangeField.setDisable(true);
                manualRangeField.setVisible(false);
                manualEntryOption.setSelected(false);
            }
        });

        rangeNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.trim().isEmpty()) {
                okButton.setDisable(true);
            } else if((startRange!=null && endRange!=null) || !manualRangeField.getText().isEmpty()){
                okButton.setDisable(false);
            }
        });

        manualRangeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.trim().isEmpty()) {
                okButton.setDisable(true);
            } else if(!rangeNameField.getText().isEmpty()){
                okButton.setDisable(false);
            }
        });
    }

    public void setMainController(RangesController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void onSkinChange(SkinOption newSkin) {
        mainContainer.getStylesheets().removeAll(SkinOption.DEFAULT.getAddRange(), SkinOption.DARK_MODE.getAddRange(),
                SkinOption.MONOCHROME.getAddRange(), SkinOption.VIBRANT.getAddRange());
        mainContainer.getStylesheets().add(newSkin.getAddRange());
    }

    @FXML
    public void handleOk() {
        String rangeName = rangeNameField.getText();
        String rangeSelection;
        rangeSelection = manualRangeField.getText();

        if (manualEntryOption.isSelected()) {
            if (rangeSelection.contains(":") && rangeSelection.split(":").length == 2) {
                String[] split = rangeSelection.split(":");
                startRange = split[0].toUpperCase();
                endRange = split[1].toUpperCase();
                System.out.println("Manual range selected: " + rangeSelection + " -> " + startRange + " -> " + endRange);
            } else {
                AppController.showError("Invalid manual range format. Please enter a range in the format 'A1:B2'.");

            }

        }
    }

    @Override
    public void onCellSelection(String startCell, String endCell, CellUnselectionListener cellUnselectionListener){
        this.startRange = startCell;
        this.endRange = endCell;
        this.cellUnselectionListener = cellUnselectionListener;
        this.selectFromSheetOption.setSelected(true);

        if(startRange!=null && endRange!=null) {
            String rangeSelection = startRange + ":" + endRange;
            manualRangeField.setText(rangeSelection);
            manualRangeField.setVisible(true);
            manualRangeField.setEditable(false);
        }

        Stage stage = (Stage) rangeNameField.getScene().getWindow();
        if(startRange!=null && endRange!=null){
            Stage newStage = createModalStage(stage);
            if(newStage!=null){
                stage.close();
                stage = newStage;
            }
        }

        stage.setOnCloseRequest(event -> {
            cellUnselectionListener.removeFromOnCellSelectionListener(this);
        });

        stage.toFront();
        stage.requestFocus();
    }

    private Stage createModalStage(Stage prevStage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/AddNewRangeDialog.fxml"));
            Parent dialogContent = fxmlLoader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Range");
            dialogStage.setScene(prevStage.getScene());
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            dialogStage.setOnCloseRequest(event -> {
                cellUnselectionListener.removeFromOnCellSelectionListener(this);
            });

            dialogStage.show();
            return dialogStage;
        } catch (Exception e){
            AppController.showError(e.getMessage());
        }

        return null;
    }


    public String getRangeName(){
        return rangeNameField.getText();
    }

    public String getRangeStartSelection(){
        return startRange;
    }

    public String getRangeEndSelection(){
        return endRange;
    }

    public Button getOkButton() {
        return okButton;
    }

    public CellUnselectionListener getCellUnselectionListener() {
        return cellUnselectionListener;
    }

    public void setOkButton(Button okButton) {
        this.okButton = okButton;
        this.okButton.getStylesheets().add(CommonResourcesPaths.ADD_RANGE_CSS);
    }
}
