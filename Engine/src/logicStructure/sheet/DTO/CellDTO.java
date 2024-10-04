package logicStructure.sheet.DTO;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.List;

public class CellDTO {
    private final CoordinateDTO coordinate;
    private final String originalValue;
    private final String effectiveValue;
    private final List<CoordinateDTO> dependsOn;
    private final List<CoordinateDTO> influencingOn;
    private int lastVersionUpdated;


    public CellDTO(CoordinateDTO coordinate, String originalValue, String effectiveValue, int version,List<CoordinateDTO> dependsOn, List<CoordinateDTO> influencingOn ) {
        this.coordinate = coordinate;
        this.originalValue = originalValue;
        this.effectiveValue = effectiveValue;
        this.lastVersionUpdated = version;
        this.dependsOn = dependsOn;
        this.influencingOn = influencingOn;
    }

    public CoordinateDTO getCoordinate() {
        return coordinate;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public StringProperty getOriginalValueProperty(){
        return new SimpleStringProperty(originalValue);
    }

    public String getEffectiveValue() {
        return effectiveValue;
    }

    public List<CoordinateDTO> getDependsOn() {
        return dependsOn;
    }

    public List<CoordinateDTO> getInfluencingOn() {
        return influencingOn;
    }

    public StringProperty getEffectiveValueProperty(){
        return new SimpleStringProperty(effectiveValue);
    }

    public int getLastVersionUpdated() {
        return lastVersionUpdated;
    }

    public StringProperty getLastVersionUpdatedProperty(){
        return new SimpleStringProperty(String.valueOf(lastVersionUpdated));
    }

    @Override
    public String toString() {
        return String.format(
                "Cell ID: %s%n" +
                        "Original Value: %s%n" +
                        "Effective Value: %s%n" +
                        "Last Version Update: %d%n" +
                        "Depending on Cells ID: %s%n" +
                        "Affecting on Cells ID: %s%n",
                coordinate != null ? coordinate.toString() : "N/A",
                originalValue != null ? originalValue : " ",
                effectiveValue != null ? effectiveValue : " ",
                lastVersionUpdated,
                dependsOn.isEmpty() ? "N/A" : dependsOn,
                influencingOn.isEmpty() ? "N/A" : influencingOn
        );
    }
}
