package log.demo.service.routing;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.net.URISyntaxException;

public interface RoutingService {

    ResponseEntity<byte[]> passHttpRequest(byte[] body, HttpServletRequest request,
        String destHost) throws URISyntaxException;
}
