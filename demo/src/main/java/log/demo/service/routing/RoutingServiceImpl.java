package log.demo.service.routing;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


@Service
@Slf4j
public class RoutingServiceImpl implements RoutingService {

    private final RestTemplate restTemplate;

    @Autowired
    public RoutingServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<byte[]> passHttpRequest(byte[] body, HttpServletRequest request,
        String destHost) throws URISyntaxException {
        final URI uri = makeURI(request, destHost);

        final HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
        final MultiValueMap<String, String> headers = addHeader(request);
        final HttpEntity<byte[]> httpEntity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(uri, httpMethod, httpEntity, byte[].class);
    }

    private URI makeURI(HttpServletRequest request, String destHost) throws URISyntaxException {
        final String destURI = makeDestURIString(request, destHost);
        return new URI(destURI);
    }

    private String makeDestURIString(HttpServletRequest request, String destHost) {
        final String requestURI = request.getRequestURI();
        final String originQueryString = request.getQueryString();
        String uriString = destHost + requestURI;
        uriString = (originQueryString == null) ? uriString : uriString + "?" + originQueryString;
        return uriString;
    }

    private MultiValueMap<String, String> addHeader(HttpServletRequest request) {
        final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            String headerValue = request.getHeader(headerName);
            headers.add(headerName, headerValue);
        });
        return headers;
    }
}
