package log.demo.service.routing;

import java.net.URI;
import java.net.URISyntaxException;
import log.demo.service.dto.PassRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@Slf4j
@RequiredArgsConstructor
public class RoutingServiceImpl implements RoutingService {

    private final RestTemplate restTemplate;
    @Value("${log.destination.host}")
    private String destHost;

    public ResponseEntity<byte[]> passHttpRequest(PassRequestDTO passRequestDTO) throws URISyntaxException {
        final URI uri = makeURI(passRequestDTO.getUri(), passRequestDTO.getQueryString());
        final HttpMethod httpMethod = HttpMethod.valueOf(passRequestDTO.getMethod());
        final HttpEntity<byte[]> httpEntity = new HttpEntity<>(passRequestDTO.getBody(),
            passRequestDTO.getHeaders());

        return restTemplate.exchange(uri, httpMethod, httpEntity, byte[].class);
    }

    private URI makeURI(String uri, String queryString) throws URISyntaxException {
        final String destURI = makeDestURIString(uri, queryString);
        return new URI(destURI);
    }

    private String makeDestURIString(String uri, String queryString) {
        String uriString = destHost + uri;
        uriString = (queryString == null) ? uriString : uriString + "?" + queryString;
        return uriString;
    }

}
