package org.hackton.analyzer.config;

import org.hackton.analyzer.domain.BarrierType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ZoneCapacity {

    private final Map<String, String> capacity = new HashMap<>();

    @Value("${car.park.zone.capacity.SHIFT}")
    private String shiftCapacity;

    @Value("${car.park.zone.capacity.RESERVED}")
    private String reservedCapacity;

    @Value("${car.park.zone.capacity.GENERAL}")
    private String generalCapacity;

    @Value("${car.park.zone.capacity.MAIN}")
    private String totalCapacity;

    @Bean
    public Map<String, String> capacity() {
        capacity.put(BarrierType.SHIFT.name(), shiftCapacity);
        capacity.put(BarrierType.RESERVED.name(), reservedCapacity);
        capacity.put(BarrierType.GENERAL.name(), generalCapacity);
        capacity.put(BarrierType.MAIN.name(), totalCapacity);
        return capacity;
    }
}
