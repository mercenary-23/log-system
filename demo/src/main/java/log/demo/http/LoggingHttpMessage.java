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

import static net.logstash.logback.argument.StructuredArguments.kv;

public class LoggingHttpMessage {

    private final ContentCachingRequestWrapper requestWrapper;
    private final ContentCachingResponseWrapper responseWrapper;
    private int statusCode;
    private String responseBody;
    private long requestTimeMillis;
    private long responseTimeMillis;
    private final Logger logger = LoggerFactory.getLogger("HTTP");

    public LoggingHttpMessage(ContentCachingRequestWrapper requestWrapper,
        ContentCachingResponseWrapper responseWrapper) {
        this.requestWrapper = requestWrapper;
        this.responseWrapper = responseWrapper;
    }

    public void logHttpRequest() throws IOException {
        final MultiValueMap<String, String> headers = getRequestHeaders();
        final Map<String, String> parameters = getRequestParameters();
        final String requestTime = millisToLocalDateTime(requestTimeMillis);
        final String requestBody = new String(requestWrapper.getContentAsByteArray(),
            requestWrapper.getCharacterEncoding());

        final Map<String, Object> requestBodyMap = stringToMapOrNullForJSON(requestBody);
        logger.info("REQUEST",
            kv("request-id", requestWrapper.getRequestId()),
            kv("method", requestWrapper.getMethod()),
            kv("path", requestWrapper.getRequestURI()), kv("headers", headers),
            kv("parameters", parameters), kv("timestamp", requestTime),
            kv("body", (requestBodyMap == null) ? new HttpBodyDTO(requestBody) : requestBodyMap),
            kv("protocol", requestWrapper.getProtocol()), kv("url", requestWrapper.getRequestURL()),
            kv("remote-ip", requestWrapper.getRemoteAddr()),
            kv("remote-host", requestWrapper.getRemoteHost()),
            kv("remote-port", requestWrapper.getRemotePort())
        );
    }

    public void logHttpResponse() {
        final MultiValueMap<String, String> responseHeader = getResponseHeader();
        final String responseTime = millisToLocalDateTime(responseTimeMillis);
        final long turnaroundTimeMillis = responseTimeMillis - requestTimeMillis;

        final Map<String, Object> responseBodyMap = stringToMapOrNullForJSON(responseBody);
        logger.info("RESPONSE",
            kv("request-id", requestWrapper.getRequestId()),
            kv("status-code", statusCodeToString()),
            kv("path", requestWrapper.getRequestURI()), kv("headers", responseHeader),
            kv("timestamp", responseTime), kv("turnaround-time", turnaroundTimeMillis),
            kv("body", (responseBodyMap == null) ? new HttpBodyDTO(responseBody) : responseBodyMap),
            kv("url", requestWrapper.getRequestURL()), kv("code-type", getCodeType())
        );

    }

    private MultiValueMap<String, String> getRequestHeaders() {
        final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        requestWrapper.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            String headerValue = requestWrapper.getHeader(headerName);
            headers.add(headerName, headerValue);
        });
        return headers;
    }

    private MultiValueMap<String, String> getResponseHeader() {
        final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        responseWrapper.getHeaderNames().iterator().forEachRemaining(headerName -> {
            String headerValue = responseWrapper.getHeader(headerName);
            headers.add(headerName, headerValue);
        });
        return headers;
    }

    private Map<String, String> getRequestParameters() {
        final Map<String, String> params = new HashMap<>();
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

    private Map<String, Object> stringToMapOrNullForJSON(String requestBody) {
        final ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = null;
        try {
            map = mapper.readValue(requestBody, Map.class);
        } catch (JsonProcessingException ignored) {
        }
        return map;
    }

    private String statusCodeToString() {
        return Optional
            .ofNullable(HttpStatus.resolve(statusCode))
            .map(HttpStatus::toString)
            .orElse(String.valueOf(statusCode));
    }

    private String getCodeType() {
        int type = statusCode / 100;
        return type + "XX";
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
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
