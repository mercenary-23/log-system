package log.demo.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import log.demo.http.LoggingHttpMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Slf4j
public class LogFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);

        LoggingHttpMessage loggingHttpMessage = new LoggingHttpMessage(requestWrapper, responseWrapper);
        loggingHttpMessage.setRequestTimeMillis(System.currentTimeMillis());

        Integer statusCode = null;
        String responseBody = "";

        try {
            chain.doFilter(requestWrapper, responseWrapper);
            statusCode = responseWrapper.getStatus();
            responseBody = new String(responseWrapper.getContentAsByteArray(), responseWrapper.getCharacterEncoding());
        } catch (Exception e) {
            statusCode = 502;
            responseWrapper.setStatus(502);
            responseBody = e.getMessage();
        } finally {
            loggingHttpMessage.setStatusCode(statusCode);
            loggingHttpMessage.setResponseBody(responseBody);
            loggingHttpMessage.setResponseTimeMillis(System.currentTimeMillis());

            loggingHttpMessage.logHttpRequest();
            loggingHttpMessage.logHttpResponse();

            responseWrapper.copyBodyToResponse();
        }
    }

    @Override
    public void destroy() {
    }
}
