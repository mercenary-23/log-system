package log.demo.exception.advice;

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
        final int code = ex.getMessage().contains("Read timed out") ? 504 : 502;
        return new ResponseEntity<>("Bad Gateway", HttpStatusCode.valueOf(code));
    }
}
