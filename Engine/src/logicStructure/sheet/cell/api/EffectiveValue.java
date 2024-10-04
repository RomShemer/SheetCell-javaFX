package logicStructure.sheet.cell.api;

import logicStructure.sheet.cell.impl.EffectiveValueImpl;

public interface EffectiveValue extends Cloneable{
        CellType getCellType();
        Object getValue();
        <T> T extractValueWithExpectation(Class<T> type);
        EffectiveValue clone();
        int compareTo(EffectiveValue other);
}
