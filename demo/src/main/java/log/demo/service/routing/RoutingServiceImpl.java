package log.demo.service.routing;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;


@Service
@Slf4j
public class RoutingServiceImpl implements RoutingService {

    private RestTemplate restTemplate;

    @Autowired
    public RoutingServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<byte[]> passHttpRequest(byte[] body, HttpServletRequest request, String destHost) throws URISyntaxException {
        URI uri = makeURI(request, destHost);

        HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
        MultiValueMap<String, String> headers = addHeader(request);
        HttpEntity<byte[]> httpEntity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(uri, httpMethod, httpEntity, byte[].class);
    }

    private URI makeURI(HttpServletRequest request, String destHost) throws URISyntaxException {
        String destURI = makeDestURIString(request, destHost);
        return new URI(destURI);
    }

    private String makeDestURIString(HttpServletRequest request, String destHost) {
        String requestURI = request.getRequestURI();
        String originQueryString = request.getQueryString();
        String uriString = destHost + requestURI;
        uriString = (originQueryString == null) ? uriString : uriString + "?" + originQueryString;
        return uriString;
    }

    private MultiValueMap<String, String> addHeader(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        while(headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.add(headerName, headerValue);
        }
        return headers;
    }
}
