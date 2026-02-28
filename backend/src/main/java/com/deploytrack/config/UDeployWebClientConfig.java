package com.deploytrack.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.util.Base64;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class UDeployWebClientConfig {

    private final UDeployProperties uDeployProperties;

    @Bean
    public WebClient udeployWebClient() throws SSLException {
        HttpClient httpClient;

        if (!uDeployProperties.isSslVerify()) {
            log.warn("SSL verification disabled for uDeploy WebClient — not recommended for production.");
            SslContext sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
        } else {
            httpClient = HttpClient.create();
        }

        // uDeploy uses token-based auth via Authorization header
        String authHeader = "Bearer " + uDeployProperties.getAuthToken();

        return WebClient.builder()
                .baseUrl(uDeployProperties.getBaseUrl())
                .defaultHeader("Authorization", authHeader)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
