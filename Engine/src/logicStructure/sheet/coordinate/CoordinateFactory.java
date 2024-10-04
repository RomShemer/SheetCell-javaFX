package logicStructure.sheet.coordinate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CoordinateFactory implements Serializable {

    private static Map<String, Coordinate> coordinatesHashMap = new HashMap<>();

    public static Coordinate createCoordinate(int row, int column) {

        String key = row + ":" + column;
        if (coordinatesHashMap.containsKey(key)) {
            return coordinatesHashMap.get(key);
        }

        CoordinateImpl coordinate = new CoordinateImpl(row, column);
        coordinatesHashMap.put(key, coordinate);

        return coordinate;
    }
}
