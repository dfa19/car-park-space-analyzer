package org.hackton.analyzer.domain.stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.hackton.analyzer.domain.config.TopologyConfiguration;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractStreamBuilder {
    private final String brokers;
    private final String applicationId;
    private final TopologyConfiguration topologyConfiguration;
    private final Properties extra;
    private KafkaStreams kafkaStreams;

    public AbstractStreamBuilder(String brokers, String applicationId, TopologyConfiguration topologyConfiguration, Properties extra) {
        this.brokers = brokers;
        this.applicationId = applicationId;
        this.topologyConfiguration = topologyConfiguration;
        this.extra = extra == null ? new Properties() : extra;
    }

    @Bean
    public AdminClient adminClient() {
        return AdminClient.create(createConfig());
    }

    private Properties createConfig() {
        Properties properties = new Properties();

        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, topologyConfiguration.getBootstrapServers());
        properties.put(StreamsConfig.SECURITY_PROTOCOL_CONFIG, topologyConfiguration.getSecurityProtocol());
        properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, topologyConfiguration.getTruststoreLocation());
        properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, topologyConfiguration.getTruststorePassword());
        properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, topologyConfiguration.getKeystoreLocation());
        properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, topologyConfiguration.getKeystorePassword());
        properties.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, topologyConfiguration.getKeyPassword());

        return properties;
    }

    @PostConstruct
    protected Topology createKafkaStream() {
        Topology topology = topology();

        if (kafkaStreams == null) {
            String groupId = applicationId;
            kafkaStreams = new KafkaStreams(
                    topology,
                    kafkaStreamsProperties(brokers, groupId, groupId, topologyConfiguration, extra)
            );
            kafkaStreams.cleanUp();

            kafkaStreams.setUncaughtExceptionHandler((t, e) -> {
                if (e instanceof Exception) {
                    log.error("Uncaught Exception: ", e);
                    kafkaStreams.close(5, TimeUnit.SECONDS);
                } else {
                    log.error("Uncaught Error: ", e);
                }
            });
            kafkaStreams.setStateListener((newState, oldState) ->
                    log.info("Kafka Stream changed state.  Was [{}], now [{}]", oldState, newState)
            );
            kafkaStreams.start();
        }
        return topology;
    }

    @PreDestroy
    protected void destroy() {
        if (kafkaStreams != null) kafkaStreams.close();
    }

    public KafkaStreams.State getState() {
        return kafkaStreams == null ? null : kafkaStreams.state();
    }

    public KafkaStreams getStreams() {
        return kafkaStreams;
    }

    protected abstract Topology topology();

    private static Properties kafkaStreamsProperties(String kafkaServers, String applicationId, String clientId, TopologyConfiguration topologyConfiguration, Properties extra) {

        Properties properties = new Properties();
        String stringSerdeName = Serdes.String().getClass().getName();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        properties.put(StreamsConfig.CLIENT_ID_CONFIG, clientId);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerdeName);
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, stringSerdeName);
        properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100);
        properties.put(StreamsConfig.SECURITY_PROTOCOL_CONFIG, topologyConfiguration.getSecurityProtocol());
        properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, topologyConfiguration.getTruststoreLocation());
        properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, topologyConfiguration.getTruststorePassword());
        properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, topologyConfiguration.getKeystoreLocation());
        properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, topologyConfiguration.getKeystorePassword());
        properties.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, topologyConfiguration.getKeyPassword());
        properties.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, topologyConfiguration.getThreads());
        properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);

        properties.putAll(extra);

        return properties;
    }


}
