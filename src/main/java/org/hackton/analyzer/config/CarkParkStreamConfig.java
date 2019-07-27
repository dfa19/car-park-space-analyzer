package org.hackton.analyzer.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Properties;

@Configuration
public class CarkParkStreamConfig {

    @Value("${kafka.stream.config.streamConfig.bootstrap.servers}")
    private String bootstrapServers;

    @Value("${kafka.stream.config.streamConfig.application.id}")
    private String applicationId;

    @Value("${kafka.stream.config.streamConfig.auto.offset.reset}")
    private String autoOffsetReset;

    @Value("${kafka.stream.config.streamConfig.producer.compression.type}")
    private String compressionType;

    @Bean
    public Properties inputStreamProperties() {

        Properties inputStreamsConfiguration = new Properties();
        String stringSerdeName = Serdes.String().getClass().getName();
        inputStreamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerdeName);
        inputStreamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        inputStreamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        inputStreamsConfiguration.put("auto.offset.reset", autoOffsetReset);
        inputStreamsConfiguration.put(StreamsConfig.producerPrefix(ProducerConfig.COMPRESSION_TYPE_CONFIG), compressionType);
        inputStreamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, stringSerdeName);
        inputStreamsConfiguration.put(StreamsConfig.producerPrefix(ProducerConfig.MAX_REQUEST_SIZE_CONFIG), 10_000_000);
        inputStreamsConfiguration.put(StreamsConfig.producerPrefix(ProducerConfig.RETRIES_CONFIG), Integer.MAX_VALUE);
        inputStreamsConfiguration.put(StreamsConfig.producerPrefix(ProducerConfig.MAX_BLOCK_MS_CONFIG), Integer.MAX_VALUE);
        inputStreamsConfiguration.put(StreamsConfig.producerPrefix(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG), new Long(Duration.ofMinutes(5).toMillis()).intValue());
        inputStreamsConfiguration.put(StreamsConfig.producerPrefix(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG), Integer.MAX_VALUE);

        return inputStreamsConfiguration;
    }
}
