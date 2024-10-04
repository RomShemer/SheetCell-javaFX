package controller.topTools;

import controller.AppController;
import controller.skinStyle.SkinChangeListener;
import controller.skinStyle.SkinManager;
import controller.skinStyle.SkinOption;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import main.UIManager;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class ExpressionTemplateController implements SkinChangeListener {

    private UIManager uiManager;
    private BooleanProperty isFunctionalExpression;
    private IntegerProperty numOfArgs = new SimpleIntegerProperty(0);
    StringProperty firstArg = new SimpleStringProperty(null);
    StringProperty secondArg = new SimpleStringProperty(null);
    StringProperty thirdArg = new SimpleStringProperty(null);

    private static enum OperationActionIndex{
        FINAL,
        FIRST_ARG,
        SECOND_ARG,
        THIRD_ARG;
    };

    @FXML private MenuButton operationMenuButton;
    @FXML private MenuButton operationMenuButtonArg1;
    @FXML private MenuButton operationMenuButtonArg2;
    @FXML private MenuButton operationMenuButtonArg3;

    private TextField leafTextFieldArg1 = new TextField();
    private TextField leafTextFieldArg2 = new TextField();
    private TextField leafTextFieldArg3 = new TextField();

    @FXML private MenuItem functionOperationMenuItem;
    @FXML private MenuItem functionOperationMenuItemArg1;
    @FXML private MenuItem functionOperationMenuItemArg2;
    @FXML private MenuItem functionOperationMenuItemArg3;
    @FXML private MenuItem leafOperationMenuItem;
    @FXML private MenuItem leafOperationMenuItemArg1;
    @FXML private MenuItem leafOperationMenuItemArg2;
    @FXML private MenuItem leafOperationMenuItemArg3;
    @FXML private Label arg1Lable;
    @FXML private Label arg2Lable;
    @FXML private Label arg3Lable;
    @FXML private TextField finalExpressionTxtField;
    @FXML private Label finalExpressionArg1;
    @FXML private Label finalExpressionArg2;
    @FXML private Label finalExpressionArg3;
    @FXML private ComboBox<String> expressionTypeComboBox;
    @FXML private GridPane mainContainer;

    @FXML
    public void initialize(UIManager uiManager, Boolean isFunctionalExpression) {

        mainContainer.getStylesheets().add(SkinManager.getCurrentSkinMode().getFilterPopUp());

        this.uiManager = uiManager;
        this.isFunctionalExpression = new SimpleBooleanProperty(isFunctionalExpression);

        finalExpressionArg1.textProperty().addListener((observable, oldValue, newValue) -> {
            firstArg.set(newValue);
        });

        finalExpressionArg2.textProperty().addListener((observable, oldValue, newValue) -> {
            secondArg.set(newValue);
        });

        finalExpressionArg3.textProperty().addListener((observable, oldValue, newValue) -> {
            thirdArg.set(newValue);
        });

        setListenersForLeafTextFieldArgs();
        initilaizeExpressionTypeComboBox();

        numOfArgs.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setVisableLableAndArgsAbilityAcoordingToNumOfArgs(numOfArgs.get());
            }
        });

        expressionTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String expressionOperationName = expressionTypeComboBox.getSelectionModel().getSelectedItem();
                List<Class> argsTypes = uiManager.getMenuLogic().getRequiredArgsForOperation(expressionOperationName);
                numOfArgs.set(argsTypes.size());
            }
        });

        finalExpressionTxtField.textProperty().bind(Bindings.createStringBinding(() -> buildFinalExpression(),
                expressionTypeComboBox.valueProperty(), firstArg, secondArg, thirdArg));

        initilaizeArgsFieldAcoordingToIsFunctionExpression();

        this.isFunctionalExpression.addListener((observable, oldValue, newValue) -> {
            if (newValue != null){
                boolean isEditable = (newValue==Boolean.FALSE)? true : false; //if the new value is false so it is not function expression => the text field should be editable
                finalExpressionTxtField.setEditable(isEditable);
                if (isEditable){
                    finalExpressionTxtField.textProperty().unbind();
                }
            }
        });
    }

    @Override
    public void onSkinChange(SkinOption newSkin) {
        mainContainer.getStylesheets().removeAll(SkinOption.DEFAULT.getExpression(), SkinOption.DARK_MODE.getExpression(),
                SkinOption.MONOCHROME.getExpression(), SkinOption.VIBRANT.getExpression());
        mainContainer.getStylesheets().add(newSkin.getExpression());
    }


    private void setListenersForLeafTextFieldArgs() {
        leafTextFieldArg1.textProperty().addListener((observable, oldValue, newValue) -> {
            finalExpressionArg1.setText(newValue);
        });

        leafTextFieldArg2.textProperty().addListener((observable, oldValue, newValue) -> {
            finalExpressionArg2.setText(newValue);
        });

        leafTextFieldArg3.textProperty().addListener((observable, oldValue, newValue) -> {
            finalExpressionArg3.setText(newValue);
        });
    }

    private void initilaizeExpressionTypeComboBox(){
        List<String> expressionTypeList = uiManager.getMenuLogic().getOperationsNameList();
        ObservableList<String> expressionList = FXCollections.observableArrayList(expressionTypeList);
        expressionTypeComboBox.setItems(expressionList);
    }

    private void initilaizeArgsFieldAcoordingToIsFunctionExpression() {
        if (!isFunctionalExpression.getValue()) {
            if (mainContainer.getChildren().contains(operationMenuButtonArg1)) {
                mainContainer.getChildren().remove(operationMenuButtonArg1);
                mainContainer.add(leafTextFieldArg1, 3, 3);
            } else {
                mainContainer.add(leafTextFieldArg1, 3, 3);
            }

            if (mainContainer.getChildren().contains(operationMenuButtonArg2)) {
                mainContainer.getChildren().remove(operationMenuButtonArg2);
                mainContainer.add(leafTextFieldArg2, 3, 4);
            } else {
                mainContainer.add(leafTextFieldArg2, 3, 4);
            }

            if (mainContainer.getChildren().contains(operationMenuButtonArg3)) {
                mainContainer.getChildren().remove(operationMenuButtonArg3);
                mainContainer.add(leafTextFieldArg3, 3, 5);
            } else {
                mainContainer.add(leafTextFieldArg3, 3, 5);
            }
        } else {
            if (mainContainer.getChildren().contains(leafTextFieldArg1)) {
                mainContainer.getChildren().remove(leafTextFieldArg1);
                mainContainer.add(operationMenuButtonArg1, 3, 3);
            }

            if (mainContainer.getChildren().contains(leafTextFieldArg2)) {
                mainContainer.getChildren().remove(leafTextFieldArg2);
                mainContainer.add(operationMenuButtonArg2, 3, 4);
            }

            if (mainContainer.getChildren().contains(leafTextFieldArg3)) {
                mainContainer.getChildren().remove(leafTextFieldArg3);
                mainContainer.add(operationMenuButtonArg3, 3, 5);
            }
        }

        setVisableLableAndArgsAbilityAcoordingToNumOfArgs(numOfArgs.get());
    }

    private void setVisableLableAndArgsAbilityAcoordingToNumOfArgs(int numberOfArgs){
        switch(numberOfArgs){
            case 0: {
                arg1Lable.setVisible(false);
                arg2Lable.setVisible(false);
                arg3Lable.setVisible(false);

                if (isFunctionalExpression.getValue()) {
                    operationMenuButtonArg1.setVisible(false);
                    operationMenuButtonArg2.setVisible(false);
                    operationMenuButtonArg3.setVisible(false);
                } else {
                    leafTextFieldArg1.setVisible(false);
                    leafTextFieldArg2.setVisible(false);
                    leafTextFieldArg3.setVisible(false);
                }
                break;
            }
            case 1: {
                arg1Lable.setVisible(true);
                arg2Lable.setVisible(false);
                arg3Lable.setVisible(false);

                if (isFunctionalExpression.getValue()) {
                    operationMenuButtonArg1.setVisible(true);
                    operationMenuButtonArg2.setVisible(false);
                    operationMenuButtonArg3.setVisible(false);
                } else {
                    leafTextFieldArg1.setVisible(true);
                    leafTextFieldArg2.setVisible(false);
                    leafTextFieldArg3.setVisible(false);
                }
                break;
            }
            case 2: {
                arg1Lable.setVisible(true);
                arg2Lable.setVisible(true);
                arg3Lable.setVisible(false);

                if (isFunctionalExpression.getValue()) {
                    operationMenuButtonArg1.setVisible(true);
                    operationMenuButtonArg2.setVisible(true);
                    operationMenuButtonArg3.setVisible(false);
                } else {
                    leafTextFieldArg1.setVisible(true);
                    leafTextFieldArg2.setVisible(true);
                    leafTextFieldArg3.setVisible(false);
                }
                break;
            }
            case 3: {
                arg1Lable.setVisible(true);
                arg2Lable.setVisible(true);
                arg3Lable.setVisible(true);

                if (isFunctionalExpression.getValue()) {
                    operationMenuButtonArg1.setVisible(true);
                    operationMenuButtonArg2.setVisible(true);
                    operationMenuButtonArg3.setVisible(true);
                } else {
                    leafTextFieldArg1.setVisible(true);
                    leafTextFieldArg2.setVisible(true);
                    leafTextFieldArg3.setVisible(true);
                }
                break;
            }
            default: break;
        }
    }

    public void leafOperationAction(ActionEvent actionEvent) {
        operationMenuButton.setText("Non-Function");
        isFunctionalExpression.setValue(false);
        expressionTypeComboBox.setDisable(true);
        System.out.println("Non-function selected");
    }


    public void leafOperationActionArg1(ActionEvent actionEvent) {
        mainContainer.add(leafTextFieldArg1, 4, 3);
        leafTextFieldArg1.setVisible(true);
    }

    public void leafOperationActionArg2(ActionEvent actionEvent) {
        mainContainer.add(leafTextFieldArg2, 4, 4);
        leafTextFieldArg2.setVisible(true);
    }

    public void leafOperationActionArg3(ActionEvent actionEvent) {
        mainContainer.add(leafTextFieldArg3, 4, 5);
        leafTextFieldArg3.setVisible(true);
    }


    @FXML
    public void functionOperationAction(ActionEvent actionEvent) {
        operationMenuButton.setText("Function");
        expressionTypeComboBox.setDisable(false);
        isFunctionalExpression.setValue(true);
        initilaizeArgsFieldAcoordingToIsFunctionExpression();
    }

    @FXML
    public void functionOperationActionArg3(ActionEvent actionEvent) {
        functionOperationHandler(actionEvent, OperationActionIndex.THIRD_ARG);
    }

    @FXML
    public void functionOperationActionArg2(ActionEvent actionEvent) {
        functionOperationHandler(actionEvent, OperationActionIndex.SECOND_ARG);
    }

    @FXML
    public void functionOperationActionArg1(ActionEvent actionEvent) {
        functionOperationHandler(actionEvent, OperationActionIndex.FIRST_ARG);
    }

    private void functionOperationHandler(ActionEvent actionEvent,OperationActionIndex index) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ExpressionTemplate.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("New Function Window");
            newStage.setScene(new Scene(root));

            ExpressionTemplateController controller = loader.getController();
            controller.initialize(uiManager,true);
            controller.operationMenuButton.setDisable(true);
            controller.operationMenuButton.setText("Function");
            controller.functionOperationAction(new ActionEvent());

            newStage.setOnHidden(event -> {
                String finalExpression = controller.finalExpressionTxtField.getText();
                switch (index){
                    case FIRST_ARG:{
                        finalExpressionArg1.setText(finalExpression);
                        finalExpressionArg1.setVisible(true);
                        break;
                    }
                    case SECOND_ARG: {
                        finalExpressionArg2.setText(finalExpression);
                        finalExpressionArg2.setVisible(true);
                        break;
                    }
                    case THIRD_ARG: {
                        finalExpressionArg3.setText(finalExpression);
                        finalExpressionArg3.setVisible(true);
                        break;
                    }
                    case FINAL:{
                        finalExpressionTxtField.setText(finalExpression);
                        finalExpressionTxtField.setVisible(true);
                        break;
                    }
                }
            });
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            AppController.showError(e.getMessage());
        }
    }

    public StringProperty getFinalExpression() {
        return finalExpressionTxtField.textProperty();
    }

    public void doneButtonAction(ActionEvent actionEvent) {
        Stage stage = (Stage) operationMenuButton.getScene().getWindow();
        stage.close();
    }

    private String buildFinalExpression() {
        String expressionType = expressionTypeComboBox.getSelectionModel().getSelectedItem() != null
                ? expressionTypeComboBox.getSelectionModel().getSelectedItem()
                : " ";

        String arg1 = firstArg.get() != null ? firstArg.get() : "";
        String arg2 = secondArg.get() != null ? secondArg.get() : "";
        String arg3 = thirdArg.get() != null ? thirdArg.get() : "";

        StringBuilder finalExpression = new StringBuilder("{" + expressionType);

        switch (numOfArgs.get()){
            case 1: {
                finalExpression.append(", ").append(arg1);
                break;
            }
            case 2: {
                finalExpression.append(", ").append(arg1);
                finalExpression.append(", ").append(arg2);
                break;
            }
            case 3: {
                finalExpression.append(", ").append(arg1);
                finalExpression.append(", ").append(arg2);
                finalExpression.append(", ").append(arg3);
                break;
            }
        }
        finalExpression.append("}");
        return finalExpression.toString();
    }
}
