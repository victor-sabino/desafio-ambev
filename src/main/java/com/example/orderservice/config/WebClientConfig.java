
package com.example.orderservice.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient externalAWebClient(@Value("${app.external-a.base-url}") String baseUrl,
                                        @Value("${app.external-a.timeout-ms}") long timeoutMs) {
        return build(baseUrl, timeoutMs);
    }

    @Bean
    public WebClient externalBWebClient(@Value("${app.external-b.base-url}") String baseUrl,
                                        @Value("${app.external-b.timeout-ms}") long timeoutMs) {
        return build(baseUrl, timeoutMs);
    }

    private WebClient build(String baseUrl, long timeoutMs) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) timeoutMs)
                .responseTimeout(Duration.ofMillis(timeoutMs))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler((int) (timeoutMs/1000)))
                        .addHandlerLast(new WriteTimeoutHandler((int) (timeoutMs/1000))));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                        .build())
                .build();
    }
}
