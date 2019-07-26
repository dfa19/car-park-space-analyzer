package org.hackton.analyzer.domain.stream;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KTable;
import org.hackton.analyzer.domain.config.TopologyConfiguration;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class CarParkTopology extends AbstractStreamBuilder {
    public CarParkTopology(TopologyConfiguration topologyConfiguration) {
        super(topologyConfiguration.getBootstrapServers(), topologyConfiguration.getApplicationId(), topologyConfiguration, null);
    }

    @VisibleForTesting
    @Override
    public Topology topology() {
        StreamsBuilder builder = new StreamsBuilder();

        KTable<String, String> items = builder.table("available-spaces");

        return builder.build();
    }
}
