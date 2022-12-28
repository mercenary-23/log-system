package log.demo.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import log.demo.constant.DatePatternConst;
import log.demo.http.dto.HttpBodyDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static net.logstash.logback.argument.StructuredArguments.fields;
import static net.logstash.logback.argument.StructuredArguments.kv;

public class LoggingHttpMessage {

    private final ContentCachingRequestWrapper requestWrapper;
    private final ContentCachingResponseWrapper responseWrapper;
    private String statusCode;
    private String responseBody;
    private long requestTimeMillis;
    private long responseTimeMillis;
    private final Logger logger = LoggerFactory.getLogger("HTTP");

    public LoggingHttpMessage(ContentCachingRequestWrapper requestWrapper, ContentCachingResponseWrapper responseWrapper) {
        this.requestWrapper = requestWrapper;
        this.responseWrapper = responseWrapper;
    }

    //헤더 부분 따로 빼서 찍기?
    public void logHttpRequest() throws IOException {
        MultiValueMap<String, String> headers = getRequestHeaders();
        Map<String, String> parameters = getRequestParameters();
        String requestTime = millisToLocalDateTime(requestTimeMillis);
        String requestBody = new String(requestWrapper.getContentAsByteArray(), requestWrapper.getCharacterEncoding());
        try {
            Map requestBodyMap = stringToMapForJSON(requestBody);
            logger.info("REQUEST",
                    kv("request_id", requestWrapper.getRequestId()), kv("method", requestWrapper.getMethod()),
                    kv("path", requestWrapper.getRequestURI()), kv("headers", headers),
                    kv("parameters", parameters), kv("timestamp", requestTime),
                    kv("body", requestBodyMap),
                    kv("protocol", requestWrapper.getProtocol()), kv("url", requestWrapper.getRequestURL()),
                    kv("remote_ip", requestWrapper.getRemoteAddr()), kv("remote_host", requestWrapper.getRemoteHost()),
                    kv("remote_port", requestWrapper.getRemotePort())
            );
        } catch (JsonProcessingException e) {
            logger.info("REQUEST",
                    kv("request_id", requestWrapper.getRequestId()), kv("method", requestWrapper.getMethod()),
                    kv("path", requestWrapper.getRequestURI()), kv("headers", headers),
                    fields(new HttpBodyDTO(requestBody)),
                    kv("parameters", parameters), kv("timestamp", requestTime),
                    kv("protocol", requestWrapper.getProtocol()), kv("url", requestWrapper.getRequestURL()),
                    kv("remote_ip", requestWrapper.getRemoteAddr()), kv("remote_host", requestWrapper.getRemoteHost()),
                    kv("remote_port", requestWrapper.getRemotePort())
            );
        }
    }

    public void logHttpResponse() {
        MultiValueMap<String, String> responseHeader = getResponseHeader();
        String responseTime = millisToLocalDateTime(responseTimeMillis);
        long turnaroundTimeMillis = responseTimeMillis - requestTimeMillis;

        try {
            Map responseBodyMap = stringToMapForJSON(responseBody);
            logger.info("RESPONSE",
                    kv("request_id", requestWrapper.getRequestId()), kv("status_code", statusCode),
                    kv("path", requestWrapper.getRequestURI()), kv("headers", responseHeader),
                    kv("timestamp", responseTime), kv("turnaround_time", turnaroundTimeMillis),
                    kv("body", responseBodyMap),
                    kv("url", requestWrapper.getRequestURL())
            );
        } catch (JsonProcessingException e) {
            logger.info("RESPONSE",
                    kv("request_id", requestWrapper.getRequestId()), kv("status_code", statusCode),
                    kv("path", requestWrapper.getRequestURI()), kv("headers", responseHeader),
                    kv("timestamp", responseTime), kv("turnaround_time", turnaroundTimeMillis),
                    fields(new HttpBodyDTO(responseBody)),
                    kv("url", requestWrapper.getRequestURL())
            );
        }
    }

    private MultiValueMap<String, String> getRequestHeaders() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        requestWrapper.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            String headerValue = requestWrapper.getHeader(headerName);
            headers.add(headerName, headerValue);
        });
        return headers;
    }

    private MultiValueMap<String, String> getResponseHeader() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        responseWrapper.getHeaderNames().iterator().forEachRemaining(headerName -> {
            String headerValue = responseWrapper.getHeader(headerName);
            headers.add(headerName, headerValue);
        });
        return headers;
    }

    private Map<String, String> getRequestParameters() {
        Map<String, String> params = new HashMap<>();
        requestWrapper.getParameterNames().asIterator().forEachRemaining(paramName -> {
            String paramValue = requestWrapper.getParameter(paramName);
            params.put(paramName, paramValue);
        });

        return params;
    }

    private String millisToLocalDateTime(long requestTimeMillis) {
        return new Timestamp(requestTimeMillis).toLocalDateTime().format(
                DateTimeFormatter.ofPattern(DatePatternConst.LogTimeStampPattern)
        );
    }

    private Map stringToMapForJSON(String requestBody) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map map = new HashMap<>();
        map = mapper.readValue(requestBody, Map.class);
        return map;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = Optional
                .ofNullable(HttpStatus.resolve(statusCode))
                .map(HttpStatus::toString)
                .orElse(String.valueOf(statusCode));
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }
    public void setRequestTimeMillis(long requestTimeMillis) {
        this.requestTimeMillis = requestTimeMillis;
    }
    public void setResponseTimeMillis(long responseTimeMillis) {
        this.responseTimeMillis = responseTimeMillis;
    }

}
