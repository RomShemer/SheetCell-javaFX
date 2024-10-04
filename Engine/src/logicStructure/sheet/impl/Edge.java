package logicStructure.sheet.impl;

import logicStructure.sheet.coordinate.Coordinate;

import java.io.Serializable;

public class Edge implements Serializable {
    private final Coordinate from;
    private final Coordinate to;

    public Edge(Coordinate from, Coordinate to) {
        this.from = from;
        this.to = to;
    }

    public Coordinate getFrom() {
        return from;
    }

    public Coordinate getTo() {
        return to;
    }
}
