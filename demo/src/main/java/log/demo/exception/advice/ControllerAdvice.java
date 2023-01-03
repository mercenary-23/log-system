package log.demo.exception.advice;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.URISyntaxException;

@org.springframework.web.bind.annotation.ControllerAdvice(basePackages = "log.demo.controller")
public class ControllerAdvice {

    @ExceptionHandler
    public ResponseEntity<String> httpClientErrorException(HttpClientErrorException ex) {
        final HttpStatusCode statusCode = ex.getStatusCode();
        final HttpStatus httpStatus = HttpStatus.resolve(statusCode.value());
        String reason = "";

        if (httpStatus != null) {
            reason = httpStatus.getReasonPhrase();
        }

        return new ResponseEntity<>(reason, statusCode);
    }

    @ExceptionHandler
    public ResponseEntity<String> httpServerErrorException(HttpServerErrorException ex) {
        final HttpStatusCode statusCode = ex.getStatusCode();
        final HttpStatus httpStatus = HttpStatus.resolve(statusCode.value());
        String reason = "";

        if (httpStatus != null) {
            reason = httpStatus.getReasonPhrase();
        }

        return new ResponseEntity<>(reason, statusCode);
    }

    @ExceptionHandler
    public ResponseEntity<String> uriSyntaxException(URISyntaxException ex) {
        return new ResponseEntity<>("Inadequate URI", HttpStatusCode.valueOf(404));
    }

    @ExceptionHandler
    public ResponseEntity<String> resourceAccessException(ResourceAccessException ex) {
        Throwable cause = ex.getCause();
        System.out.println(ex.getCause().toString());
        int code = 502;
        if (cause instanceof SocketTimeoutException) {
            code = 504;
        } else if (cause instanceof InterruptedException) {
            code = 500;
        }

        return new ResponseEntity<>(ex.getMessage(), HttpStatusCode.valueOf(code));
    }
}
