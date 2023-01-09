package log.demo.exception.advice;

import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

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
    public ResponseEntity<String> resourceAccessException(ResourceAccessException ex) {
        Throwable cause = ex.getCause();
        int code = 500;
        if (cause instanceof SocketTimeoutException) {
            code = 504;
        }

        return new ResponseEntity<>(ex.getMessage(), HttpStatusCode.valueOf(code));
    }

    @ExceptionHandler
    public ResponseEntity<String> uriSyntaxException(URISyntaxException ex) {
        return new ResponseEntity<>("Inadequate URI", HttpStatusCode.valueOf(404));
    }

}
