package org.hackton.analyzer.domain;

public enum BarrierType {
    MAIN(0),
    SHIFT(2),
    GENERAL(3),
    RESERVED(4);

    private final int type;

    BarrierType(int type){
        this.type = type;
    }
}
