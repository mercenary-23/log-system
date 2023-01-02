package log.demo.http;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingHttpMessageTest {

    ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        //ListAppender
        //로그 찍히는 걸 리스트에 저장해준다.
        listAppender = new ListAppender<>();
        Logger logger = (Logger) LoggerFactory.getLogger("HTTP");
        logger.addAppender(listAppender);
        listAppender.start();
    }

    @Test
    @DisplayName("GET METHOD TEST")
    void getMethodTest() throws IOException {
        //given
        //MockHttpServletRequest
        String path = "/get";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("get", "test");
        mockRequest.setRequestURI(path);
        mockRequest.setMethod("GET");
        mockRequest.setContentType("text/plain");
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(mockRequest);

        //MockHttpServletResponse
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        mockResponse.addHeader("get", "test");
        mockResponse.setContentType("text/plain");
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(mockResponse);

        //LoggingHttpMessage
        LoggingHttpMessage loggingHttpMessage = new LoggingHttpMessage(requestWrapper, responseWrapper);
        loggingHttpMessage.setRequestTimeMillis(100);
        loggingHttpMessage.setCodeAndResBodyAndResTimeMillis(200, "", 150);


        //when
        loggingHttpMessage.logHttpRequest();
        loggingHttpMessage.logHttpResponse();


        //then
        List<ILoggingEvent> testLogs = listAppender.list;

        //request test
        Object[] requestArgArray = testLogs.get(0).getArgumentArray();
        for (Object argument : requestArgArray) {
            String arg = argument.toString();
            Tuple tuple = getKeyValue(arg);

            switch (tuple.key) {
                case "path" -> assertThat(tuple.value).isEqualTo("/get");
                case "method" -> assertThat(tuple.value).isEqualTo("GET");
                case "headers" -> assertThat(tuple.value).isEqualTo("{get=[test], Content-Type=[text/plain]}");
                case "protocol" -> assertThat(tuple.value).isEqualTo("HTTP/1.1");
                case "url" -> assertThat(tuple.value).isEqualTo("http://localhost/get");
                case "remote_ip" -> assertThat(tuple.value).isEqualTo("127.0.0.1");
                case "remote_host" -> assertThat(tuple.value).isEqualTo("localhost");
            }
        }

        //response test
        Object[] responseArgArray = testLogs.get(1).getArgumentArray();
        for (Object argument : responseArgArray) {
            String arg = argument.toString();
            Tuple tuple = getKeyValue(arg);

            switch (tuple.key) {
                case "path" -> assertThat(tuple.value).isEqualTo("/get");
                case "status_code" -> assertThat(tuple.value).isEqualTo("200 OK");
                case "headers" -> assertThat(tuple.value).isEqualTo("{get=[test], Content-Type=[text/plain]}");
                case "turnaround_time" -> assertThat(tuple.value).isEqualTo("50");
                case "url" -> assertThat(tuple.value).isEqualTo("http://localhost/get");
                case "code-type" -> assertThat(tuple.value).isEqualTo("2XX");
            }
        }
    }

    @Test
    @DisplayName("Query Parameter TEST")
    void queryParameter() throws IOException {
        //given
        //MockHttpServletRequest
        String path = "/query";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI(path);
        mockRequest.setMethod("GET");
        mockRequest.setParameter("head", "snake");
        mockRequest.setParameter("tail", "dog");
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(mockRequest);

        //MockHttpServletResponse
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(mockResponse);

        //LoggingHttpMessage
        LoggingHttpMessage loggingHttpMessage = new LoggingHttpMessage(requestWrapper, responseWrapper);
        loggingHttpMessage.setRequestTimeMillis(100);
        loggingHttpMessage.setCodeAndResBodyAndResTimeMillis(200, "", 150);


        //when
        loggingHttpMessage.logHttpRequest();
        loggingHttpMessage.logHttpResponse();


        //then
        List<ILoggingEvent> testLogs = listAppender.list;

        //request test
        Object[] requestArgArray = testLogs.get(0).getArgumentArray();
        for (Object argument : requestArgArray) {
            String arg = argument.toString();
            Tuple tuple = getKeyValue(arg);

            switch (tuple.key) {
                case "path" -> assertThat(tuple.value).isEqualTo("/query");
                case "method" -> assertThat(tuple.value).isEqualTo("GET");
                case "protocol" -> assertThat(tuple.value).isEqualTo("HTTP/1.1");
                case "url" -> assertThat(tuple.value).isEqualTo("http://localhost/query");
                case "remote_ip" -> assertThat(tuple.value).isEqualTo("127.0.0.1");
                case "remote_host" -> assertThat(tuple.value).isEqualTo("localhost");
                case "parameters" -> assertThat(tuple.value).isEqualTo("{head=snake, tail=dog}");
            }
        }

        //response test
        Object[] responseArgArray = testLogs.get(1).getArgumentArray();
        for (Object argument : responseArgArray) {
            String arg = argument.toString();
            Tuple tuple = getKeyValue(arg);

            switch (tuple.key) {
                case "path" -> assertThat(tuple.value).isEqualTo("/query");
                case "status_code" -> assertThat(tuple.value).isEqualTo("200 OK");
                case "turnaround_time" -> assertThat(tuple.value).isEqualTo("50");
                case "url" -> assertThat(tuple.value).isEqualTo("http://localhost/query");
                case "code-type" -> assertThat(tuple.value).isEqualTo("2XX");
            }
        }
    }

    @Test
    @DisplayName("POST METHOD TEST")
    void postMethodTest() throws IOException {
        //given
        //MockHttpServletRequest
        String path = "/post";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI(path);
        mockRequest.setMethod("POST");
        mockRequest.setContentType("application/json");
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(mockRequest);

        //MockHttpServletResponse
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(mockResponse);

        //LoggingHttpMessage
        LoggingHttpMessage loggingHttpMessage = new LoggingHttpMessage(requestWrapper, responseWrapper);
        loggingHttpMessage.setRequestTimeMillis(100);
        loggingHttpMessage.setCodeAndResBodyAndResTimeMillis(200, "", 200);


        //when
        loggingHttpMessage.logHttpRequest();
        loggingHttpMessage.logHttpResponse();


        //then
        List<ILoggingEvent> testLogs = listAppender.list;

        //request test
        Object[] requestArgArray = testLogs.get(0).getArgumentArray();
        for (Object argument : requestArgArray) {
            String arg = argument.toString();
            Tuple tuple = getKeyValue(arg);

            switch (tuple.key) {
                case "path" -> assertThat(tuple.value).isEqualTo("/post");
                case "method" -> assertThat(tuple.value).isEqualTo("POST");
                case "protocol" -> assertThat(tuple.value).isEqualTo("HTTP/1.1");
                case "url" -> assertThat(tuple.value).isEqualTo("http://localhost/post");
                case "remote_ip" -> assertThat(tuple.value).isEqualTo("127.0.0.1");
                case "remote_host" -> assertThat(tuple.value).isEqualTo("localhost");
                case "headers" -> assertThat(tuple.value).isEqualTo("{Content-Type=[application/json]}");
            }
        }

        //response test
        Object[] responseArgArray = testLogs.get(1).getArgumentArray();
        for (Object argument : responseArgArray) {
            String arg = argument.toString();
            Tuple tuple = getKeyValue(arg);

            switch (tuple.key) {
                case "path" -> assertThat(tuple.value).isEqualTo("/post");
                case "status_code" -> assertThat(tuple.value).isEqualTo("201 CREATED");
                case "turnaround_time" -> assertThat(tuple.value).isEqualTo("100");
                case "url" -> assertThat(tuple.value).isEqualTo("http://localhost/post");
                case "code-type" -> assertThat(tuple.value).isEqualTo("2XX");
            }
        }
    }

    @Test
    @DisplayName("ClientErrorException TEST")
    void clientErrorExceptionTest() throws IOException {
        String path = "/clientError";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI(path);
        mockRequest.setMethod("GET");
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(mockRequest);

        //MockHttpServletResponse
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(mockResponse);

        //LoggingHttpMessage
        LoggingHttpMessage loggingHttpMessage = new LoggingHttpMessage(requestWrapper, responseWrapper);
        loggingHttpMessage.setRequestTimeMillis(100);
        loggingHttpMessage.setCodeAndResBodyAndResTimeMillis(404, "", 200);


        //when
        loggingHttpMessage.logHttpRequest();
        loggingHttpMessage.logHttpResponse();


        //then
        List<ILoggingEvent> testLogs = listAppender.list;

        //request test
        Object[] requestArgArray = testLogs.get(0).getArgumentArray();
        for (Object argument : requestArgArray) {
            String arg = argument.toString();
            Tuple tuple = getKeyValue(arg);

            switch (tuple.key) {
                case "path" -> assertThat(tuple.value).isEqualTo("/clientError");
                case "method" -> assertThat(tuple.value).isEqualTo("GET");
                case "protocol" -> assertThat(tuple.value).isEqualTo("HTTP/1.1");
                case "url" -> assertThat(tuple.value).isEqualTo("http://localhost/clientError");
                case "remote_ip" -> assertThat(tuple.value).isEqualTo("127.0.0.1");
                case "remote_host" -> assertThat(tuple.value).isEqualTo("localhost");
            }
        }

        //response test
        Object[] responseArgArray = testLogs.get(1).getArgumentArray();
        for (Object argument : responseArgArray) {
            String arg = argument.toString();
            System.out.println(arg);
            Tuple tuple = getKeyValue(arg);

            switch (tuple.key) {
                case "path" -> assertThat(tuple.value).isEqualTo("/clientError");
                case "status_code" -> assertThat(tuple.value).isEqualTo("404 NOT_FOUND");
                case "turnaround_time" -> assertThat(tuple.value).isEqualTo("100");
                case "url" -> assertThat(tuple.value).isEqualTo("http://localhost/clientError");
                case "code-type" -> assertThat(tuple.value).isEqualTo("4XX");
            }
        }
    }

    @Test
    @DisplayName("HttpServerException TEST")
    void httpServerErrorExceptionTest() throws IOException {
        String path = "/serverError";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI(path);
        mockRequest.setMethod("GET");
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(mockRequest);

        //MockHttpServletResponse
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(mockResponse);

        //LoggingHttpMessage
        LoggingHttpMessage loggingHttpMessage = new LoggingHttpMessage(requestWrapper, responseWrapper);
        loggingHttpMessage.setRequestTimeMillis(100);
        loggingHttpMessage.setCodeAndResBodyAndResTimeMillis(500, "", 200);


        //when
        loggingHttpMessage.logHttpRequest();
        loggingHttpMessage.logHttpResponse();


        //then
        List<ILoggingEvent> testLogs = listAppender.list;

        //request test
        Object[] requestArgArray = testLogs.get(0).getArgumentArray();
        for (Object argument : requestArgArray) {
            String arg = argument.toString();
            Tuple tuple = getKeyValue(arg);

            switch (tuple.key) {
                case "path" -> assertThat(tuple.value).isEqualTo("/serverError");
                case "method" -> assertThat(tuple.value).isEqualTo("GET");
                case "protocol" -> assertThat(tuple.value).isEqualTo("HTTP/1.1");
                case "url" -> assertThat(tuple.value).isEqualTo("http://localhost/serverError");
                case "remote_ip" -> assertThat(tuple.value).isEqualTo("127.0.0.1");
                case "remote_host" -> assertThat(tuple.value).isEqualTo("localhost");
            }
        }

        //response test
        Object[] responseArgArray = testLogs.get(1).getArgumentArray();
        for (Object argument : responseArgArray) {
            String arg = argument.toString();
            System.out.println(arg);
            Tuple tuple = getKeyValue(arg);

            switch (tuple.key) {
                case "path" -> assertThat(tuple.value).isEqualTo("/serverError");
                case "status_code" -> assertThat(tuple.value).isEqualTo("500 INTERNAL_SERVER_ERROR");
                case "turnaround_time" -> assertThat(tuple.value).isEqualTo("100");
                case "url" -> assertThat(tuple.value).isEqualTo("http://localhost/serverError");
                case "code-type" -> assertThat(tuple.value).isEqualTo("5XX");
            }
        }
    }

    Tuple getKeyValue(String arg) {
        int idx = arg.indexOf('=');
        String key = "";
        String value = "";
        if (idx == -1) {
            key = arg;
        } else {
            key = arg.substring(0, idx);
            value = arg.substring(idx + 1);
        }

        return new Tuple(key, value);
    }

    static class Tuple {
        public String key;
        public String value;

        public Tuple(String key, String value) {
            this.key = key;
            this.value = value;
        }

    }

}