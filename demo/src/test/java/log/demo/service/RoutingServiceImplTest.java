package log.demo.service;

import log.demo.service.routing.RoutingServiceImpl;
import log.demo.service.routing.dto.PassRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;


//테스트 케이스 좀 더 세세하게 분류
class RoutingServiceImplTest {

    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer mockServer;
    RoutingServiceImpl routingServiceImpl;
    @Value("${log.destination.host}")
    String destHost;

    @BeforeEach
    void setUp() {
        mockServer = bindTo(restTemplate).build();
        routingServiceImpl = new RoutingServiceImpl(restTemplate);
    }

    /*
     *  GET METHOD 테스트
     *  Request Body 와 Response Body 는 text/plain 형식으로 주고 받는다.
     */
    @Test
    @DisplayName("GET METHOD TEST - content-type : text/plain")
    void getMethodTest() throws URISyntaxException {
        //given
        String path = "/get";
        mockServer.expect(requestTo(destHost + path))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("get", "test"))
                .andRespond(withSuccess("Get Method Test Success", MediaType.TEXT_PLAIN));

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("get", "test");
        PassRequestDTO passRequestDTO = PassRequestDTO.builder()
            .headers(headers)
            .uri(path)
            .method("GET")
            .build();


        //when
        ResponseEntity<byte[]> responseEntity = routingServiceImpl.passHttpRequest(passRequestDTO);

        //then
        mockServer.verify();
        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThat(new String(responseEntity.getBody())).isEqualTo("Get Method Test Success");
    }

    /*
    /*
     *   GET METHOD 테스트
     *   Response Body 의 형식이 JSON 이다.
     */
    @Test
    @DisplayName("GET METHOD TEST - content-type : application/json")
    void getJSONTest() throws URISyntaxException {
        //given
        String path = "/json";
        String body = "{\"name\": \"kim\",\"age\": 20,\"height\": 175.73}";
        mockServer.expect(requestTo(destHost + path))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        PassRequestDTO passRequestDTO = PassRequestDTO.builder()
            .uri(path)
            .method("GET")
            .build();


        //when
        ResponseEntity<byte[]> responseEntity = routingServiceImpl.passHttpRequest(passRequestDTO);

        //then
        mockServer.verify();
        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThat(new String(responseEntity.getBody())).isEqualTo(body);
    }

    /*
     *   GET METHOD 테스트
     *   쿼리 파라미터를 보내는 경우
     */
    @Test
    @DisplayName("GET METHOD TEST - Query Parameter")
    void queryParameterTest() throws URISyntaxException {
        //given
        String path = "/param";
        mockServer.expect(requestTo(destHost + path + "?p1=q1&p2=q2"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("p1", "q1"))
                .andExpect(queryParam("p2", "q2"))
                .andRespond(withSuccess());

        PassRequestDTO passRequestDTO = PassRequestDTO.builder()
            .uri(path)
            .method("GET")
            .queryString("p1=q1&p2=q2")
            .build();


        //when
        ResponseEntity<byte[]> responseEntity = routingServiceImpl.passHttpRequest(passRequestDTO);

        //then
        mockServer.verify();
        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
    }

    /*
     *   POST METHOD 테스트
     *   Request Body 의 형식이 JSON 이다.
     *   Status-Code 는 201
     */
    @Test
    @DisplayName("POST METHOD TEST - content-type : application/json")
    void postMethodTest() throws URISyntaxException {
        //given
        String path = "/post";
        String queryString = "p1=q1&p2=q2";
        mockServer.expect(requestTo(destHost + path + "?" + queryString))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("{\"name\": \"kim\",\"age\": 20}"))
                .andRespond(withCreatedEntity(new URI(destHost + "/1")));

        String body = "{\"name\": \"kim\",\"age\": 20}";
        PassRequestDTO passRequestDTO = PassRequestDTO.builder()
            .uri(path)
            .method("POST")
            .queryString(queryString)
            .body(body.getBytes(StandardCharsets.UTF_8))
            .build();


        //when
        ResponseEntity<byte[]> responseEntity = routingServiceImpl.passHttpRequest(passRequestDTO);

        //then
        mockServer.verify();
        assertThat(responseEntity.getStatusCode().value()).isEqualTo(201);
    }

    /*
     * ClientErrorException 테스트
     * status-code 400번대를 응답하면 ClientErrorException 이 발생한다.
     * 응답 status-code : 400
     */
    @Test
    @DisplayName("ClientErrorException 발생 - 400")
    void clientErrorExceptionTest() throws URISyntaxException {
        //given
        String path = "/clientError";
        mockServer.expect(requestTo(destHost + path))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withBadRequest());

        PassRequestDTO passRequestDTO = PassRequestDTO.builder()
            .uri(path)
            .method("GET")
            .build();

        //when && then
        assertThatThrownBy(() -> routingServiceImpl.passHttpRequest(passRequestDTO))
                .isInstanceOf(HttpClientErrorException.class);
    }

    /*
     *  HttpServerErrorException 테스트
     *  status-code 500번대를 응답하면 HttpServerErrorException 이 발생한다.
     *  응답 status-code : 504
     */
    @Test
    @DisplayName("HttpServerErrorException 발생 - 504")
    void httpServerErrorException() {
        //given
        String path = "/serverError";
        mockServer.expect(requestTo(destHost + path))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withGatewayTimeout());

        PassRequestDTO passRequestDTO = PassRequestDTO.builder()
            .uri(path)
            .method("GET")
            .build();

        //when && then
        assertThatThrownBy(() -> routingServiceImpl.passHttpRequest(passRequestDTO))
                .isInstanceOf(HttpServerErrorException.class);
    }

}