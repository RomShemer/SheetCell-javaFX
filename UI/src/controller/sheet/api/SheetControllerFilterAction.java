package controller.sheet.api;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public interface SheetControllerFilterAction extends SheetInTabAction {
    Label getLableAcoordingToIndex(int row, int col);
    void setGridPane(GridPane gridPane);
}
