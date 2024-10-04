package controller.topTools;

import controller.AppController;
import controller.skinStyle.SkinChangeListener;
import controller.skinStyle.SkinManager;
import controller.skinStyle.SkinOption;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.scene.input.KeyCode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TopBarController implements SkinChangeListener {

    private AppController mainController;
    private File selectedFile;
    private SubTolBarTopController subTolBarTopController;
    private ProgressBar progressBar;
    private Stage progressStage;

    @FXML private ScrollPane mainContainer;
    @FXML private Button loadFileButton;
    @FXML private TextField filePathField;
    @FXML private MenuButton systemSkinMenuButton;
    @FXML private ToggleButton animationsButton;

    @FXML
    private void initialize() {
        mainContainer.getStylesheets().add(SkinManager.getCurrentSkinMode().getTopBar());
        animationsButton.setSelected(false);
    }

    @Override
    public void onSkinChange(SkinOption newSkin) {
        mainContainer.getStylesheets().removeAll(SkinOption.DEFAULT.getTopBar(), SkinOption.DARK_MODE.getTopBar(),
                SkinOption.MONOCHROME.getTopBar(), SkinOption.VIBRANT.getTopBar());
        mainContainer.getStylesheets().add(newSkin.getTopBar());
    }

    @FXML
    private void handleLoadFileAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                new FileChooser.ExtensionFilter("DOT Files", "*.dot"),
                new FileChooser.ExtensionFilter("All Supported Files", "*.xml", "*.dot")
        );

        Stage stage = (Stage) loadFileButton.getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            filePathField.setText(selectedFile.getPath());
            showProgressBarInPopup();
        }
    }

    private void showProgressBarInPopup() {
        // יצירת Stage חדש (חלון קופץ)
        progressStage = new Stage();
        progressStage.setTitle("Loading...");

        // יצירת ProgressBar בתוך VBox
        ProgressBar popupProgressBar = new ProgressBar(0);
        popupProgressBar.setPrefWidth(300); // הגדרת רוחב מותאם
        popupProgressBar.getStyleClass().add(SkinManager.getCurrentSkinMode().getProgressBar()); // הוספת מחלקת CSS מותאמת

        // יצירת VBox כ-Root Node
        VBox vbox = new VBox(popupProgressBar);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        // יצירת סצנה והוספתה לחלון
        Scene scene = new Scene(vbox);
        //scene.getStylesheets().add(SkinManager.getCurrentSkinMode().getProgressBar());
        progressStage.setScene(scene);

        progressStage.setOnShown(event -> {
            Stage primaryStage = (Stage) loadFileButton.getScene().getWindow(); // קבלת החלון הראשי

            // חישוב מרכז החלון הראשי
            double centerXPosition = primaryStage.getX() + primaryStage.getWidth() / 2d - progressStage.getWidth() / 2d;
            double centerYPosition = primaryStage.getY() + primaryStage.getHeight() / 2d - progressStage.getHeight() / 2d;

            // הגדרת המיקום החדש עבור progressStage
            progressStage.setX(centerXPosition);
            progressStage.setY(centerYPosition);
        });

        AppController.showSystemMessage(progressStage);
        //progressStage.show();

        loadFileInBackground(popupProgressBar);
    }

    private void loadFileInBackground(ProgressBar popupProgressBar) {
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                long totalBytes = selectedFile.length();
                long bytesLoaded = 0;
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];

                // דוגמה לטעינת הקובץ ומעקב אחרי התקדמות
                try (var inputStream = Files.newInputStream(selectedFile.toPath())) {
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        bytesLoaded += bytesRead;
                        updateProgress(bytesLoaded, totalBytes);

                        // לחקות עיכוב במקרים מסוימים (הסרת עיכוב בתהליך אמיתי)
                        Thread.sleep(1000);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        // קישור ה-ProgressBar להתקדמות
        popupProgressBar.progressProperty().bind(loadTask.progressProperty());

        progressStage.setOnCloseRequest(event -> {
            if (loadTask.isRunning()) {
                loadTask.cancel(); // Cancel the task if the dialog is closed
            }
        });


        // סגירת החלון הקופץ לאחר סיום
        loadTask.setOnSucceeded(event -> {
            progressStage.close(); // סגירת חלון ה-ProgressBar
            mainController.updateChosenFile(selectedFile); // עדכון הקובץ הנבחר
        });

        new Thread(loadTask).start();
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
        addAllSystemSkinMenuItems();
        mainController.addSkinListener(this);

        animationsButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            mainController.setIsWithAnimations(animationsButton.isSelected());
        });
    }

    @FXML
    private void handleKeyPress(KeyEvent keyEvent) {
        if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.C) {
            return;
        } else {
            handleLoadFileAction();
            keyEvent.consume();
        }
    }

    private void addAllSystemSkinMenuItems() {
        for (SkinOption skinOption : SkinOption.values()) {
            MenuItem item = new MenuItem(skinOption.getModeName());
            item.setOnAction(event -> mainController.systemSkinOnAction(skinOption));
            systemSkinMenuButton.getItems().add(item);
        }
    }
}