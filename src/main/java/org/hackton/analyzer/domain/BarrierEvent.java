package org.hackton.analyzer.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BarrierEvent {

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
    private boolean direction;
    private String carParkId;
    private LocalDateTime timestamp;
}
