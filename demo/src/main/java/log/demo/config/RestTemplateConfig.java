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
        //대상 서버와 소켓 연결을 맺을 때 timeout 설정(3-way handshake)
        httpRequestFactory.setConnectTimeout(2000);
        //connection manager(connection pool)로부터 connection을 꺼내올 때의 timeout 설정
        httpRequestFactory.setConnectionRequestTimeout(2500);
        return httpRequestFactory;
    }

    private HttpClient getHttpClient(int readTimeoutMilliSeconds) {
        //setSoTimeout은 대상 서버로부터 request에 대한 응답에 걸리는 시간에 대한 설정
        final SocketConfig socketConfig = SocketConfig.custom()
            .setSoTimeout(Timeout.ofMilliseconds(readTimeoutMilliSeconds)).build();
        final PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setDefaultSocketConfig(socketConfig)
            .setMaxConnTotal(200)
            .setMaxConnPerRoute(200)
            .build();
        return HttpClientBuilder.create()
            .setConnectionManager(connectionManager)
            .build();
    }
}
