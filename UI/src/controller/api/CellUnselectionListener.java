package controller.api;

public interface CellUnselectionListener {
    void onCellUnselection(String startCell, String endCell);
    void removeFromOnCellSelectionListener(CellSelectionListener listenerToRemove);
}
