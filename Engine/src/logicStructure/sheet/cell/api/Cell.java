package logicStructure.sheet.cell.api;

import java.util.List;

import logicStructure.sheet.cell.impl.CellImpl;
import logicStructure.sheet.impl.SheetImpl;
import logicStructure.sheet.coordinate.Coordinate;

public interface Cell extends Cloneable{

    Coordinate getCoordinate();

    String getOriginalValue();

    EffectiveValue getEffectiveValue();

    void calculateEffectiveValue(SheetImpl sheet) throws Exception;

    int getVersion();

    List<Coordinate> getDependsOn();

    List<Coordinate> getInfluencingOn();

    void setOriginalValue(String newValue, SheetImpl sheet) throws Exception;

    void setEffectiveValueAsOriginal();

    void setVersion(Integer newVersion);

    void addToDependsOn(Coordinate coordinate);

    void addToInfluencingOn(Coordinate coordinate);

    int compareCells(Cell other);

    Cell clone();
}
