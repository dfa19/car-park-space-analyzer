package org.hackton.analyzer.stream.TestDriverExtention;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;

import java.util.Properties;

public class TopologyTestDriverExtention extends TopologyTestDriver {
    public TopologyTestDriverExtention(Topology topology, Properties config) {
        super(topology, config);
    }

    public TopologyTestDriverExtention(Topology topology, Properties config, long initialWallClockTimeMs) {
        super(topology, config, initialWallClockTimeMs);
    }

    @Override
    public <K, V> ProducerRecord<K, V> readOutput(final String topic,
                                                  final Deserializer<K> keyDeserializer,
                                                  final Deserializer<V> valueDeserializer) {
        final ProducerRecord<byte[], byte[]> record = readOutput(topic);
        if (record == null) {
            return null;
        }
        final K key = keyDeserializer.deserialize(record.topic(), record.key());
        final V value = (record.value() == null)
                ? null
                : valueDeserializer.deserialize(record.topic(), record.value());
        return new ProducerRecord<>(record.topic(), record.partition(), record.timestamp(), key, value, record.headers());
    }
}
