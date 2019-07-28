package org.hackton.analyzer.domain;

import lombok.Data;

@Data
public class BarrierEvent {

    private String eventId;
    private String barrierId;

    /**
     0 = General
     1 = Shift
     2 = Reserved
     */
    private String barrierType;

    /**
     * true (1) - Entry
     * false (0) - Exit
     */
    private boolean entry;
    private String carParkId;
    private String timestamp;
}
