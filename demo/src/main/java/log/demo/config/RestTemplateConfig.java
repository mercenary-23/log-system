package log.demo.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


@Configuration
public class RestTemplateConfig {

    @Value("${log.response.timeout}")
    private int responseTimeout;

    @Bean
    public RestTemplate restTemplate() {
        final HttpComponentsClientHttpRequestFactory httpRequestFactory = getHttpRequestFactory();
        return new RestTemplate(httpRequestFactory);
    }

    private HttpComponentsClientHttpRequestFactory getHttpRequestFactory() {
        final HttpClient client = getHttpClient(responseTimeout);

        final HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setHttpClient(client);
        httpRequestFactory.setConnectionRequestTimeout(15000);
        httpRequestFactory.setConnectTimeout(15000);
        return httpRequestFactory;
    }

    private HttpClient getHttpClient(int readTimeoutMilliSeconds) {
        final SocketConfig socketConfig = SocketConfig.custom()
            .setSoTimeout(Timeout.ofMilliseconds(readTimeoutMilliSeconds)).build();
        final PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setDefaultSocketConfig(socketConfig).build();
        final HttpClient client = HttpClientBuilder.create()
            .setConnectionManager(connectionManager)
            .build();
        return client;
    }
}
