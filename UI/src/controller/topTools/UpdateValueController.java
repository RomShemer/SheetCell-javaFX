package controller.topTools;

import controller.AppController;
import controller.skinStyle.SkinChangeListener;
import controller.skinStyle.SkinManager;
import controller.skinStyle.SkinOption;
import javafx.animation.PauseTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import logicStructure.sheet.DTO.SheetDTO;
import logicStructure.sheet.coordinate.CoordinateFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import main.UIManager;

public class UpdateValueController implements SkinChangeListener {

    private UIManager uiManager;
    private ExpressionTemplateController expressionCoontroller;
    private SubTolBarTopController mainController;
    private Stage updateStageInMainController;

    @FXML private GridPane mainContainer;
    @FXML private ComboBox<String> selectCellIDUpdateComboBox;
    @FXML private javafx.scene.control.TextField originalValueTextFiled;
    @FXML private javafx.scene.control.TextField EffectiveValueTextField;
    @FXML private javafx.scene.control.TextField newValueTextField;
    @FXML private javafx.scene.control.Label messageLable;
    @FXML private javafx.scene.control.CheckBox predefinedTemplateCheckBox;

    @FXML
    public void initialize(UIManager uiManager) {
        mainContainer.getStylesheets().add(SkinManager.getCurrentSkinMode().getUpdateValue());

        this.uiManager = uiManager;
        predefinedTemplateCheckBox.setSelected(true);
        populateComboBox();
        selectCellIDUpdateComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue!= null) {
                    loadCellData(newValue);
                }
            }
        });
    }


    @Override
    public void onSkinChange(SkinOption newSkin) {
        mainContainer.getStylesheets().removeAll(SkinOption.DEFAULT.getUpdateValue(), SkinOption.DARK_MODE.getUpdateValue(),
                SkinOption.MONOCHROME.getUpdateValue(), SkinOption.VIBRANT.getUpdateValue());
        mainContainer.getStylesheets().add(newSkin.getUpdateValue());
    }

    public void setMainController(SubTolBarTopController mainController) {
        this.mainController = mainController;
        mainController.addSkinListenerForSubControllers(this);
    }

    public void setUpdateStageInMainController(Stage updateStage){
        this.updateStageInMainController = updateStage;
    }

    private void loadCellData(String cellId) {
        if (cellId != null) {
            mainController.sheetDTOProperty().setValue(uiManager.getMenuLogic().display());
            originalValueTextFiled.setText(uiManager.getMenuLogic().showCell(cellId).getOriginalValue());
            EffectiveValueTextField.setText(uiManager.getMenuLogic().showCell(cellId).getEffectiveValue());
            newValueTextField.clear();
            messageLable.setText("");
            messageLable.setVisible(false);
        }

    }

    @FXML
    public void handleCellIDComboBoxInUpdateAction(ActionEvent actionEvent) {
        String selectedCell = selectCellIDUpdateComboBox.getSelectionModel().getSelectedItem();
        loadCellData(selectedCell);
    }

    @FXML
    public void handleUpdateButtonAction(ActionEvent actionEvent) {
        try {
            String selectedCell = selectCellIDUpdateComboBox.getSelectionModel().getSelectedItem();
            uiManager.updateCellActionJavaFX(selectedCell, newValueTextField.getText());
            loadCellData(selectedCell);
            displaySucccessMessagePopup("Value updated successfully!");

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("An Error Occurred");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void displaySucccessMessagePopup(String message) {
        Stage successStage = new Stage();
        successStage.initModality(Modality.APPLICATION_MODAL);
        successStage.setTitle("Success");

        Label successLabel = new Label(message);
        VBox layout = new VBox(10);
        layout.getChildren().add(successLabel);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 300, 100);
        successStage.setScene(scene);

        // הצגת החלון הקופץ
        //successStage.show();
        AppController.showSystemMessage(successStage);

        // הגדרת משך זמן של כמה שניות לפני סגירת החלון
        PauseTransition delay = new PauseTransition(Duration.seconds(3)); // נמתין 3 שניות
        delay.setOnFinished(event -> {
            successStage.close(); // סגירת חלון ההצלחה
        });

        delay.play();
    }

    private void populateComboBox() {
        List<String> cellIdsList = new ArrayList<>();
        SheetDTO sheetDTO = uiManager.getSheetDto();

        for (int col = 1; col <= sheetDTO.getColumns(); col++) {
            for (int row = 0; row <= sheetDTO.getRows(); row++) {
                String cellId = CoordinateFactory.createCoordinate(row, col).createCellCoordinateString();
                cellIdsList.add(cellId);
            }
        }
        ObservableList<String> cellIds = FXCollections.observableArrayList(cellIdsList);
        selectCellIDUpdateComboBox.setItems(cellIds);
    }

    @FXML
    public void predefinedTemplateCheckBoxAction(ActionEvent actionEvent) {
        if (predefinedTemplateCheckBox.isSelected()){
            newValueTextField.setEditable(false);
        }
        else {
            newValueTextField.setEditable(true);
        }
    }

    public void clickNewValueTextFieldAction(MouseEvent mouseEvent) {
        if (predefinedTemplateCheckBox.isSelected()){
            try {
                URL fxmlUrl = getClass().getResource("/ExpressionTemplate.fxml");
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();
                Stage newStage = new Stage();
                newStage.setTitle("New Function Window");
                newStage.setScene(new Scene(root));
                expressionCoontroller = loader.getController();
                mainController.addSkinListenerForSubControllers(expressionCoontroller);
                expressionCoontroller.initialize(uiManager, predefinedTemplateCheckBox.isSelected());
                newStage.show();
                newValueTextField.textProperty().bind(expressionCoontroller.getFinalExpression());

            } catch (Exception e){
                //todo
                e.printStackTrace();
                AppController.showError(e.getMessage());
            }
        } else {
            //todo
        }
    }

    public void setSelectCellID(String cellID) {
        selectCellIDUpdateComboBox.setValue(cellID);
    }

    public ObjectProperty<String> getCellIDProperty() {
        return selectCellIDUpdateComboBox.valueProperty();
    }

    public StringProperty getOriginalValueProperty() {
        return originalValueTextFiled.textProperty();
    }
}
