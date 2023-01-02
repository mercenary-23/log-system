package log.demo.service.routing;

import jakarta.servlet.http.HttpServletRequest;
import log.demo.service.routing.dto.PassRequestDTO;
import org.springframework.http.ResponseEntity;

import java.net.URISyntaxException;

public interface RoutingService {

    ResponseEntity<byte[]> passHttpRequest(PassRequestDTO passRequestDTO)
        throws URISyntaxException;
}
