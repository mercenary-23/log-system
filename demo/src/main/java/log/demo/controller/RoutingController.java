package log.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import log.demo.service.routing.RoutingService;
import log.demo.service.dto.PassRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@Slf4j
@RequiredArgsConstructor
public class RoutingController {

    private final RoutingService routingService;

    @RequestMapping(value = "/**", method = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS, RequestMethod.HEAD,
        RequestMethod.TRACE, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.PUT
    })
    public ResponseEntity<byte[]> routes(@RequestBody(required = false) byte[] body,
        HttpServletRequest request) throws URISyntaxException {
        MultiValueMap<String, String> headers = getHeaders(request);
        PassRequestDTO passRequestDTO = PassRequestDTO.builder()
            .queryString(request.getQueryString())
            .uri(request.getRequestURI())
            .body(body)
            .method(request.getMethod())
            .headers(headers)
            .build();
        return routingService.passHttpRequest(passRequestDTO);
    }

    private MultiValueMap<String, String> getHeaders(HttpServletRequest request) {
        final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            String headerValue = request.getHeader(headerName);
            headers.add(headerName, headerValue);
        });
        return headers;
    }
}
