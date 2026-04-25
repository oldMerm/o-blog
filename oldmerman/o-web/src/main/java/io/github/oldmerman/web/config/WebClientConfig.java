package io.github.oldmerman.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("oldmerman-agent")
                .maxConnections(100)
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .responseTimeout(Duration.ofSeconds(30));

        return WebClient.builder()
                .baseUrl("http://localhost:8000")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }

}
