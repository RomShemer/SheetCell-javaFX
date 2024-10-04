package controller.api;

public interface CellSelectionListener {
    void onCellSelection(String startCell, String endCell, CellUnselectionListener cellUnselectionListener);
}
