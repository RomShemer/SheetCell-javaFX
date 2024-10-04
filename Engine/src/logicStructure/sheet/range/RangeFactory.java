package logicStructure.sheet.range;

import logicStructure.expression.parser.Convertor;
import logicStructure.sheet.coordinate.Coordinate;

import java.util.HashMap;
import java.util.Map;

public class RangeFactory {
    private static Map<String, Range> rangeHashMap = new HashMap<>();

    public static Range createCoordinate(String key, Coordinate fromCoordinate, Coordinate toCoordinate) {
        if (rangeHashMap.containsKey(key.toUpperCase())) {
            return rangeHashMap.get(key.toUpperCase());
        }

        Range range = new RangeImpl(key, fromCoordinate, toCoordinate);
        rangeHashMap.put(key.toUpperCase(), range);

        return range;
    }

    public static void clear(){
        rangeHashMap.clear();
    }

    public static void removeRange(String rangeName){
        if(rangeHashMap.containsKey(rangeName.toUpperCase())){
            rangeHashMap.remove(rangeName.toUpperCase());
        } else {
            throw new RuntimeException(String.format("Range %s not found", rangeName));
        }
    }

    public static Boolean isRangeExist(String key) {
        return rangeHashMap.containsKey(key.toUpperCase());
    }

    public static Range getRange(String key) {
        if(rangeHashMap.containsKey(key.toUpperCase())){
            return rangeHashMap.get(key.toUpperCase());
        } else {
            throw new RuntimeException(String.format("Range %s not found", key.toUpperCase()));
        }
    }
}
