package logicStructure.sheet.version;

import logicStructure.sheet.impl.SheetImpl;

import java.io.Serializable;

public class SheetVersionInfo implements Serializable {
    private final SheetImpl sheet;
    private final int changedCellsCount;
    private final int version;

    public SheetVersionInfo(SheetImpl sheet, int changedCellsCount, int version) {
        this.sheet = sheet.clone();
        this.changedCellsCount = changedCellsCount;
        this.version = version;
    }

    public SheetImpl getSheet() {
        return sheet;
    }

    public int getChangedCellsCount() {
        return changedCellsCount;
    }

    public Integer getVersionNumber(){
        return version;
    }

    public int getVersion() {
        return version;
    }
}
