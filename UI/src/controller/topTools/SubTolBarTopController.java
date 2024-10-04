package controller.topTools;

import controller.AppController;
import controller.CommonResourcesPaths;
import controller.skinStyle.SkinChangeListener;
import controller.skinStyle.SkinManager;
import controller.skinStyle.SkinOption;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import logicStructure.sheet.DTO.CellDTO;
import logicStructure.sheet.DTO.CoordinateDTO;
import logicStructure.sheet.DTO.SheetDTO;
import logicStructure.sheet.DTO.SheetMapper;
import logicStructure.sheet.coordinate.CoordinateFactory;
import main.UIManager;

import java.util.ArrayList;
import java.util.List;

public class SubTolBarTopController implements SkinChangeListener {

    private UIManager uiManager;
    private ObjectProperty<SheetDTO> sheetDTO = new SimpleObjectProperty<>();
    private AppController mainController;
    private UpdateValueController updateValueController;
    private Stage updateStage;

    @FXML private ScrollPane mainContainer;
    @FXML private ComboBox<String> cellIdComboBox;
    @FXML private TextField originalValueTextFile;
    @FXML private TextField lastVersionUpdatedTextField;
    @FXML private ComboBox versionComboBox;
    @FXML private Button updateValueButton;


    @FXML
    public void initialize() {
        mainContainer.getStylesheets().add(SkinManager.getCurrentSkinMode().getActionLine());
        versionComboBox.setDisable(true);
    }

    @Override
    public void onSkinChange(SkinOption newSkin) {
        mainContainer.getStylesheets().removeAll(SkinOption.DEFAULT.getActionLine(), SkinOption.DARK_MODE.getActionLine(),
                SkinOption.MONOCHROME.getActionLine(), SkinOption.VIBRANT.getActionLine());
        mainContainer.getStylesheets().add(newSkin.getActionLine());
    }

    public ObjectProperty<SheetDTO> sheetDTOProperty() {
        return sheetDTO;
    }

    public void setUiManager(UIManager uiManager) {
        this.uiManager = uiManager;
        sheetDTOProperty().set(uiManager.getMenuLogic().display());
        createVersionCmboBox();
        populateComboBox();
        cellIdComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                cellIDComboBoxListenerAction(newValue));
        sheetDTOProperty().addListener((observable, oldValue, newValue) -> sheetDTOListenerAction());
    }

    private void cellIDComboBoxListenerAction(String newValue){
        if (newValue != null) {
            CoordinateDTO coordinate = SheetMapper.toCoordinateDTO(newValue, sheetDTOProperty().get());
            CellDTO cell = sheetDTOProperty().get().getCells().get(coordinate);
            String newOriginalValue = "Empty";
            String version = String.valueOf(1);

            if (cell != null) {
                newOriginalValue = cell.getOriginalValue() == null ? "Empty" : cell.getOriginalValue();
                version = String.valueOf(cell.getLastVersionUpdated());
            }

            originalValueTextFile.textProperty().unbind();
            originalValueTextFile.setText(newOriginalValue);
            originalValueTextFile.requestFocus();
            lastVersionUpdatedTextField.textProperty().unbind();
            lastVersionUpdatedTextField.setText(version);
            lastVersionUpdatedTextField.setAlignment(Pos.CENTER);
            versionComboBox.setDisable(false);

        } else {
            originalValueTextFile.textProperty().unbind();
            originalValueTextFile.setText(" ");
            lastVersionUpdatedTextField.textProperty().unbind();
            lastVersionUpdatedTextField.setText(" ");
        }
    }

    private void sheetDTOListenerAction(){
        versionComboBox.setDisable(false);
        createVersionCmboBox();
        cellIdComboBox.getSelectionModel().select(cellIdComboBox.getSelectionModel().getSelectedIndex());
    }

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
        mainController.addSkinListener(this);
    }

    public void addSkinListenerForSubControllers(SkinChangeListener listener){
        mainController.addSkinListener(listener);
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
        cellIdComboBox.setItems(cellIds);
    }

    public List<String> getCellIDList(){
        if (!cellIdComboBox.getItems().isEmpty()){
            return cellIdComboBox.getItems().stream().toList();
        }

        return null;
    }

    public void setUpdateButtonDisable(boolean IsDisable) {
        this.updateValueButton.setDisable(IsDisable);
    }

    public void setAllInformationPresented(String selectedCell, String originalValue, int lastVersionUpdated) {
        ChangeListener<String> cellIDComboBoxListener = (observable, oldValue, newValue) ->
                cellIDComboBoxListenerAction(newValue);
        cellIdComboBox.getSelectionModel().selectedItemProperty().removeListener(cellIDComboBoxListener);
        cellIdComboBox.setValue(null);
        cellIdComboBox.setValue(selectedCell);
        originalValueTextFile.setText(" ");
        originalValueTextFile.setText(originalValue);
        lastVersionUpdatedTextField.setText(String.valueOf(lastVersionUpdated));

        cellIdComboBox.getSelectionModel().selectedItemProperty().addListener(cellIDComboBoxListener);
    }

    public void handleCellSelection(ActionEvent actionEvent) {
        String selectedCell = cellIdComboBox.getSelectionModel().getSelectedItem();
        System.out.println("Selected Cell ID: " + selectedCell);
    }

    public void handleVersionSelection(ActionEvent actionEvent) {
        if(!cellIdComboBox.getSelectionModel().getSelectedItem().isEmpty()){
            versionComboBox.setDisable(false);
            try {
                int version = Integer.parseInt(versionComboBox.getSelectionModel().getSelectedItem().toString());
                mainController.showSheetAcoordingToVersionInTab(version);

            } catch (Exception e){
                AppController.showError(e.getMessage(), "An Error Occurred", true);
            }

        } else {
            versionComboBox.setDisable(true);
        }
    }

    public String getVersionComboBoxSelection(){
        Object value = versionComboBox.getSelectionModel().getSelectedItem();
        return (value == null) ? null : String.valueOf(value);
    }

    public void selectCellInComboBox(String cellId) {
        cellIdComboBox.getSelectionModel().select(cellId);
    }

    private void createVersionCmboBox(){
        List<Integer> versions = uiManager.getMenuLogic().showSheetVersionsInfo().keySet().stream().toList();
        ObservableList<Integer> versionsComboList = FXCollections.observableArrayList(versions);
        versionComboBox.setItems(versionsComboList);

        lastVersionUpdatedTextField.setText(String.valueOf(sheetDTO.get().getVersion()));
        lastVersionUpdatedTextField.setAlignment(Pos.CENTER);
        versionComboBox.setDisable(false);
    }

    public void handleUpdateValueAction(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(CommonResourcesPaths.UPDATE_RESOURCE_FXML));
            Parent root = loader.load();

            updateValueController = loader.getController();
            updateValueController.initialize(uiManager);
            updateValueController.setMainController(this);
            updateValueController.setSelectCellID(cellIdComboBox.getSelectionModel().getSelectedItem());
            cellIdComboBox.valueProperty().bindBidirectional(updateValueController.getCellIDProperty()); //todo - check this

            originalValueTextFile.textProperty().bind(updateValueController.getOriginalValueProperty());

            updateStage = new Stage();
            updateStage.setScene(new Scene(root));
            updateStage.setTitle("Update Value Window");
            updateValueController.setUpdateStageInMainController(updateStage);

            updateStage.show();

            updateStage.setOnCloseRequest(windowEvent ->{
                        mainController.setSheetDTOObjectProperty(sheetDTOProperty().get());
                        mainController.updateCellSeclected(cellIdComboBox.getSelectionModel().getSelectedItem());
                        originalValueTextFile.textProperty().unbind();
                        cellIdComboBox.valueProperty().unbind();
                    }
                );

        }catch (Exception e) {
            e.printStackTrace();
            AppController.showError(e.getMessage());
        }
    }

    public void setSheetDTO(SheetDTO sheetDTO){
        this.sheetDTO.set(sheetDTO);
    }

    public StringProperty getLastVersionUpdatedProperty() {
        return lastVersionUpdatedTextField.textProperty();
    }

    public StringProperty getOriginalValueProperty() {
        return originalValueTextFile.textProperty();
    }

    public ObjectProperty<String> getSelectedCellIDProperty() {
        return cellIdComboBox.valueProperty();
    }
}
