package logicStructure.sheet.cell.api;

public enum NegativeFunctionType {
    NAN{
        public Double getValue() {
            return Double.NaN;
        }
    },
    UNDEFINED {
        public String getValue() {
            return "!UNDEFINED!";
        }
    },
    UNKNOWN {
        public String getValue() {return "UNKNOWN";}
    }
}
