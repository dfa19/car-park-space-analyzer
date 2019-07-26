package org.hackton.analyzer.stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.hackton.analyzer.domain.BarrierEvent;
import org.hackton.analyzer.factory.StreamFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Properties;

import static org.hackton.analyzer.serde.StreamSerdes.barrierEventSerde;

@Slf4j
@Component
public class StreamDataFlowPipe {

    private KafkaStreams streams;

    @Value("${car.park.barrier.event.topic}")
    private String rawSourceTopic;

    @Value("${car.park.store}")
    private String carkParkZoneStore;

    @Value("{car.park.availability.output.topic}")
    private String outputTopic;

    private final Properties inputStreamProperties;

    public StreamDataFlowPipe(Properties inputStreamProperties){
        this.inputStreamProperties = inputStreamProperties;
    }

    Topology topology() {

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, BarrierEvent> barrierEventKStream = builder.stream(rawSourceTopic, Consumed.with(Serdes.String(), barrierEventSerde));

        // Create a window state store
        builder.addStateStore(StreamFactory.createInMemoryStore(carkParkZoneStore));

        barrierEventKStream.to(outputTopic, Produced.with(Serdes.String(), barrierEventSerde));

        return builder.build();
    }

    @PostConstruct
    public void runStream() {

        Topology topology = topology();
        log.info("topology description {}", topology.describe());
        streams = new KafkaStreams(topology, inputStreamProperties);
        streams.setUncaughtExceptionHandler((t, e) -> {
            log.error("Thread {} had a fatal error {}", t, e, e);
            closeStream();
        });

        KafkaStreams.StateListener stateListener = (newState, oldState) -> {
            if (newState == KafkaStreams.State.RUNNING && oldState == KafkaStreams.State.REBALANCING) {
                log.info("Application has gone from REBALANCING to RUNNING ");
                log.info("Topology Layout {}", topology.describe());
            }

            if (newState == KafkaStreams.State.REBALANCING) {
                log.info("Application is entering REBALANCING phase");
            }

            if (!newState.isRunning()) {
                log.info("Application is not in a RUNNING phase");
            }
        };

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            closeStream();
        }));
        streams.start();
    }

    @PreDestroy
    public void closeStream() {
        log.info("Closing Car Park Analyzer..");
        streams.close();
        streams.cleanUp();
    }
}
