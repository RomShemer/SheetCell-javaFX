package controller.sortAndFilter.sort;

import controller.skinStyle.SkinChangeListener;
import controller.skinStyle.SkinManager;
import controller.skinStyle.SkinOption;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import java.util.Collections;
import java.util.List;

public class SubSortPopUpController implements SkinChangeListener {

    @FXML private VBox mainContainer;
    @FXML private ComboBox<String> columnComboBox;
    @FXML private RadioButton ascRadioButton;
    @FXML private RadioButton descRadioButton;
    @FXML private Button addSortColumnButton;

    private SortPopUpController mainController;
    private String selectedSortOrder = "ASC";
    private Integer index = 1;
    private List<String> columnList;

    @FXML
    private void initialize() {
        mainContainer.getStylesheets().add(SkinManager.getCurrentSkinMode().getSortPopUp());

        ascRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue && !descRadioButton.isSelected()) {
                columnComboBox.setDisable(false);
                addSortColumnButton.setDisable(false);
            } else if(newValue && descRadioButton.isSelected()) {
                descRadioButton.selectedProperty().set(false);
            }
        });

        descRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue && !ascRadioButton.isSelected()) {
                columnComboBox.setDisable(false);
                addSortColumnButton.setDisable(false);
            } else if(newValue && ascRadioButton.isSelected()) {
                ascRadioButton.selectedProperty().set(false);
            }
        });

        columnComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                mainController.updateSortColumnSelected(columnComboBox.getValue(), index);
            }
        });

        columnComboBox.setOnAction(event -> {
            if(columnComboBox.getSelectionModel().getSelectedItem() != null) {
                mainController.updateSortColumnSelected(columnComboBox.getValue(), index);
            }
        });

        columnComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.isEmpty()){
                columnComboBox.setDisable(true);
                addSortColumnButton.setDisable(true);
            } else {
                columnComboBox.setDisable(false);
            }
        });
    }

    public void setupSortButtonTooltip(Button sortButton, boolean rangeSelected) {
        if (!rangeSelected) {
            Tooltip tooltip = new Tooltip("No range selected.\nPlease select a range of cells in the sheet or choose.");
            sortButton.setTooltip(tooltip);
        }
    }

    public void setMainController(SortPopUpController mainController) {
        this.mainController = mainController;
    }

    public void setColumnComboBox(List<String> columns) {
        if(columns == null) {
            columnComboBox.setDisable(true);
            addSortColumnButton.setDisable(true);
            return;
        }

        columnComboBox.setDisable(false);
        this.columnList = columns;
        String ascText = String.valueOf(columns.getFirst().charAt(0)).toUpperCase() + " to " +String.valueOf(columns.getLast().charAt(0)).toUpperCase();
        String descText = String.valueOf(columns.getLast().charAt(0)).toUpperCase() + " to " + String.valueOf(columns.getFirst().charAt(0)).toUpperCase();
        ascRadioButton.setText(ascText);
        descRadioButton.setText(descText);
        columnComboBox.setItems(FXCollections.observableArrayList(columns));
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getIndex(){
        return index;
    }

    public void setAddSortColumnButtonDisable(boolean disable) {
        addSortColumnButton.setDisable(disable);
    }

    @FXML
    private void addSortColumn(ActionEvent actionEvent){
        mainController.addSortColumn();
    }

    @FXML
    public void AtoZColumnOrder(ActionEvent actionEvent) {
        if(!selectedSortOrder.equals("ASC")) {
            selectedSortOrder = "ASC";
        }

        columnComboBox.setDisable(true);
        addSortColumnButton.setDisable(true);
        if(!columnList.isEmpty()){
            Collections.sort(columnList);
            mainController.updateAllSortColumnsSelectedList(columnList);
        }
    }

    @FXML
    public void ZtoAColumnOrder(ActionEvent actionEvent) {
        if(!selectedSortOrder.equals("DESC")) {
            selectedSortOrder = "DESC";
        }

        columnComboBox.setDisable(true);
        addSortColumnButton.setDisable(true);
        if(!columnList.isEmpty()){
            Collections.sort(columnList, Collections.reverseOrder());
            mainController.updateAllSortColumnsSelectedList(columnList);
        }
    }

    @Override
    public void onSkinChange(SkinOption newSkin) {
        mainContainer.getStylesheets().removeAll(SkinOption.DEFAULT.getSortPopUp(), SkinOption.DARK_MODE.getSortPopUp(),
                SkinOption.MONOCHROME.getSortPopUp(), SkinOption.VIBRANT.getSortPopUp());
        mainContainer.getStylesheets().add(newSkin.getSortPopUp());
    }
}
