package org.hackton.analyzer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties("car.park.zone.capacity")
public class ZoneCapacity {

    private final Map<String, String> capacity = new HashMap<>();

    public Map<String, String> getCapacity() {
        return capacity;
    }
}
