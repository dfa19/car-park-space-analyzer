package org.hackton.analyzer.domain.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("kafka-topology")
@Setter
@Getter
public class TopologyConfiguration {

    String bootstrapServers;
    String applicationId;
    String securityProtocol;
    String truststoreLocation;
    String truststorePassword;
    String keystoreLocation;
    String keystorePassword;
    String keyPassword;
    Map<String, String> topics;
    int threads;
    boolean validation;
    String schemaPath;

}
