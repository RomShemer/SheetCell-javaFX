package logicStructure.sheet.version;

import logicStructure.sheet.impl.SheetImpl;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VersionHistory implements Serializable {
    private final Map<Integer, SheetVersionInfo> versions;

    public VersionHistory(SheetImpl sheetToSave) {
        this.versions = new HashMap<>();
        versions.put(sheetToSave.getVersion() ,new SheetVersionInfo(sheetToSave, sheetToSave.getCells().size(), sheetToSave.getVersion()));
    }

    public void saveVersionSheet(int versionNumber, SheetImpl sheetToSave, Integer changedCellsCount)  {
        versions.put(versionNumber,new SheetVersionInfo(sheetToSave, changedCellsCount, versionNumber));
    }

    public void saveVersionSheet(int versionNumber,SheetVersionInfo sheetVersionInfoToSave)  {
        versions.put(versionNumber, sheetVersionInfoToSave);
    }

    public SheetImpl getVersionSheet(int versionNumber) {
        return versions.get(versionNumber).getSheet();
    }

    public boolean isValidVersion(int versionNumber) {
        return versions.containsKey(versionNumber);
    }

    public int getNumberOfChangedCellsInSheetVersion(int versionNumber) {
        return versions.get(versionNumber).getChangedCellsCount();
    }

    public Set<Integer> getAllVersions() {
        return versions.keySet();
    }

    public Map<Integer, Integer> getSheetVersionsInfo() {
        Map<Integer, Integer> versionsInfo = new HashMap<>();

        versions.forEach((versionNum, value) -> {
            versionsInfo.put(versionNum, value.getChangedCellsCount());
        });

        return versionsInfo;
    }
}
