package org.hackton.analyzer.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.Properties;

@Configuration
@ConfigurationProperties("kafka.stream.config")
public class StreamAppConfig {

    @Getter
    @Setter
    private Map<String, String> streamConfig;

    @Bean
    public Properties inputStreamProperties() {

        Properties inputStreamsConfiguration = new Properties();
        inputStreamsConfiguration.putAll(streamConfig);
        String stringSerdeName = Serdes.String().getClass().getName();
        inputStreamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerdeName);
        inputStreamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, stringSerdeName);
        inputStreamsConfiguration.put(StreamsConfig.producerPrefix(ProducerConfig.MAX_REQUEST_SIZE_CONFIG), 10_000_000);
        //Exactly Once Semantics
        //inputStreamsConfiguration.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
        //Resilience to Broker Outages
        inputStreamsConfiguration.put(StreamsConfig.producerPrefix(ProducerConfig.RETRIES_CONFIG), Integer.MAX_VALUE);
        inputStreamsConfiguration.put(StreamsConfig.producerPrefix(ProducerConfig.MAX_BLOCK_MS_CONFIG), Integer.MAX_VALUE);
        inputStreamsConfiguration.put(StreamsConfig.producerPrefix(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG), new Long(Duration.ofMinutes(5).toMillis()).intValue());
        inputStreamsConfiguration.put(StreamsConfig.producerPrefix(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG), Integer.MAX_VALUE);

        return inputStreamsConfiguration;
    }
}
