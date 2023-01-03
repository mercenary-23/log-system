package log.demo.service.routing;

import log.demo.service.dto.PassRequestDTO;
import org.springframework.http.ResponseEntity;

import java.net.URISyntaxException;

public interface RoutingService {

    ResponseEntity<byte[]> passHttpRequest(PassRequestDTO passRequestDTO)
        throws URISyntaxException;
}
