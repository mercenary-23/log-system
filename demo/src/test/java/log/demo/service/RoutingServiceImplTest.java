package log.demo.service;

import log.demo.service.routing.RoutingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.client.MockRestServiceServer;
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
    String destURL = "http://localhost:8081";

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
        mockServer.expect(requestTo(destURL + path))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("get", "test"))
                .andRespond(withSuccess("Get Method Test Success", MediaType.TEXT_PLAIN));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("get", "test");
        mockRequest.setRequestURI(path);
        mockRequest.setMethod("GET");

        //when
        ResponseEntity<byte[]> responseEntity = routingServiceImpl.passHttpRequest(null, mockRequest, destURL);

        //then
        mockServer.verify();
        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThat(new String(responseEntity.getBody())).isEqualTo("Get Method Test Success");
    }

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
        mockServer.expect(requestTo(destURL + path))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI(path);
        mockRequest.setMethod("GET");

        //when
        ResponseEntity<byte[]> responseEntity = routingServiceImpl.passHttpRequest(null, mockRequest, destURL);

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
        mockServer.expect(requestTo(destURL + path + "?p1=q1&p2=q2"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("p1", "q1"))
                .andExpect(queryParam("p2", "q2"))
                .andRespond(withSuccess());

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI(path);
        mockRequest.setMethod("GET");
        mockRequest.setQueryString("p1=q1&p2=q2");

        //when
        ResponseEntity<byte[]> responseEntity = routingServiceImpl.passHttpRequest(null, mockRequest, destURL);

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
        mockServer.expect(requestTo(destURL + path))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("{\"name\": \"kim\",\"age\": 20}"))
                .andRespond(withCreatedEntity(new URI(destURL + "/1")));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI(path);
        mockRequest.setMethod("POST");
        String body = "{\"name\": \"kim\",\"age\": 20}";

        //when
        ResponseEntity<byte[]> responseEntity = routingServiceImpl.passHttpRequest(body.getBytes(StandardCharsets.UTF_8), mockRequest, destURL);

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
        mockServer.expect(requestTo(destURL + path))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withBadRequest());

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI(path);
        mockRequest.setMethod("GET");

        //when && then
        assertThatThrownBy(() -> routingServiceImpl.passHttpRequest(null, mockRequest, destURL))
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
        mockServer.expect(requestTo(destURL + path))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withGatewayTimeout());

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI(path);
        mockRequest.setMethod("GET");

        //when && then
        assertThatThrownBy(() -> routingServiceImpl.passHttpRequest(null, mockRequest, destURL))
                .isInstanceOf(HttpServerErrorException.class);
    }

}