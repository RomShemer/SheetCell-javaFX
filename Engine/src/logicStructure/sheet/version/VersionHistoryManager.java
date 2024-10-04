package logicStructure.sheet.version;

import logicStructure.sheet.range.RangeFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class VersionHistoryManager implements Serializable {
    private static Map<String, VersionHistory> versionHistories = new HashMap<>();

    public VersionHistoryManager() { }

    public static void saveVersionHistory(String xmlPath, VersionHistory history) {
        versionHistories.put(xmlPath, history);
    }

    public static VersionHistory getVersionHistory(String xmlPath) {
        return versionHistories.get(xmlPath);
    }

    public void deleteVersionHistory(String xmlPath) {
        versionHistories.remove(xmlPath);
    }

    public void clearHistory() {
        versionHistories.clear();
        RangeFactory.clear();
    }
}
