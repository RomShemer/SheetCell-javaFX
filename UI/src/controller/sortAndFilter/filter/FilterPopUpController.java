package controller.sortAndFilter.filter;

import controller.AppController;
import controller.skinStyle.SkinChangeListener;
import controller.skinStyle.SkinManager;
import controller.skinStyle.SkinOption;
import controller.sortAndFilter.SortFilterController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import logicStructure.sheet.DTO.RangeDTO;
import logicStructure.sheet.DTO.SheetMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FilterPopUpController implements SkinChangeListener {
    @FXML private ScrollPane mainContainer;
    @FXML private MenuButton filterOptionsButton;
    @FXML private TextField searchTextFeild;
    @FXML private Button clearSelectionButton;
    @FXML private Button clearSearchButton;
    @FXML private Button selectAllButton;
    @FXML private ListView<HBox> filterListView;
    @FXML private Button cancelButton;
    @FXML private Button applyButton;

    private FXMLLoader listViewItemloader = new FXMLLoader();
    private RangeDTO range;
    private char column;
    private SortFilterController mainController;
    private List<String> columnsInRange = new ArrayList<>();
    private List<String> columnsSortOrderList = new ArrayList<>();
    private List<String> selectedFilterValuesList = new ArrayList<>();
    private List<String> allFilterValuesList = new ArrayList<>();
    private List<HBox> originalListViewItemsOrder = new ArrayList<>();
    private Tooltip tooltip = new Tooltip();


    @FXML
    public void initialize() {
        listViewItemloader =  new FXMLLoader(getClass().getResource("/FilterListViewItem.fxml"));

        mainContainer.getStylesheets().add(SkinManager.getCurrentSkinMode().getFilterPopUp());

        clearSearchButton.setOnAction(event -> searchTextFeild.setText(""));

        clearSelectionButton.setOnAction(event -> clearSelection());

        selectAllButton.setOnAction(event -> selectAll());

        cancelButton.setOnAction(event -> closePopup());

        applyButton.setOnAction(event -> handleFilter());

        searchTextFeild.textProperty().addListener((observable, oldValue, newValue) -> {
            filterListViewBySearch(newValue);
        });
    }

    public void setMainController(SortFilterController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void onSkinChange(SkinOption newSkin) {
        mainContainer.getStylesheets().removeAll(SkinOption.DEFAULT.getSheet(), SkinOption.DARK_MODE.getSheet(),
                SkinOption.MONOCHROME.getSheet(), SkinOption.VIBRANT.getSheet());
        mainContainer.getStylesheets().add(newSkin.getSheet());
    }

    public void setColumn(int columnIndex){
        this.column = (char) ('A' + (columnIndex -1));
    }

    private void handleFilter() {
        if(!filterListView.getItems().isEmpty() && mainController != null){
            this.selectedFilterValuesList = filterListView.getItems().stream()
                    .filter(item -> {
                        CheckBox checkBox = (CheckBox) item.getChildren().get(0);
                        return checkBox.isSelected();
                    }).map(item -> {
                        Label label = (Label) item.getChildren().get(1);
                        return label.getText();
                    })
                    .collect(Collectors.toList());
        } else if(mainController == null){
            return;
        }

        closePopup();
        mainController.handleFilterAction(this.selectedFilterValuesList, column);
    }

    private void clearSelection() {
        for (HBox item : filterListView.getItems()) {
            for (Node node : item.getChildren()) {
                if (node instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) node;
                    checkBox.setSelected(false);
                    break;
                }
            }
        }
    }

    private void selectAll() {
        for (HBox item : filterListView.getItems()) {
            for (Node node : item.getChildren()) {
                if (node instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) node;
                    checkBox.setSelected(true);
                    break;
                }
            }
        }
    }

    public void setListViewItems(List<String> items) {
        this.allFilterValuesList = items;
        filterListView.getItems().clear();
        for (String item : items) {
            try {
                listViewItemloader =  new FXMLLoader(getClass().getResource("/FilterListViewItem.fxml"));
                HBox listItem = listViewItemloader.load();
                listItem.getStylesheets().add(SkinManager.getCurrentSkinMode().getFilterListViewItem());

                CheckBox checkBox = (CheckBox) listItem.getChildren().get(0);
                Label textLabel = (Label) listItem.getChildren().get(1);
                textLabel.setText(item);
                checkBox.setSelected(false);

                filterListView.getItems().add(listItem);
            } catch (Exception e) {
                e.printStackTrace();
                AppController.showError(e.getMessage());
            }
        }
        this.originalListViewItemsOrder = filterListView.getItems();
    }

    private void filterListViewBySearch(String searchText) {
        List<HBox> allItems = new ArrayList<>(filterListView.getItems());

        allItems.sort((item1, item2) -> {
            Label label1 = (Label) item1.getChildren().get(1);
            Label label2 = (Label) item2.getChildren().get(1);

            boolean label1Matches = label1.getText().toLowerCase().contains(searchText.toLowerCase());
            boolean label2Matches = label2.getText().toLowerCase().contains(searchText.toLowerCase());

            // מי שתואם לחיפוש יופיע ראשון
            if (label1Matches && !label2Matches) {
                return -1;
            } else if (!label1Matches && label2Matches) {
                return 1;
            } else {
                return label1.getText().compareTo(label2.getText()); // מיון אלפביתי ברירת מחדל
            }
        });

        // החזרת כל הפריטים ל-ListView בסדר חדש
        filterListView.getItems().clear();
        filterListView.getItems().addAll(allItems);
    }

    public void updateRange(String start, String end){
        this.range = SheetMapper.toRangeDTO(start, end);
        this.columnsSortOrderList.clear();
        this.columnsInRange.clear();
        this.columnsInRange.addAll(range.getColumnNamesListInRange());

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
}
