package org.hackton.analyzer.domain;

public class CarParkStatus {
    /**
      Enumerated Type: General, Shift, Reserved
     */
    private String carParkTypeId;
    /**
     * count = FULL (if used == total) else count = used
     */
    private String count;
}
