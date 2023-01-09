package log.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.util.Enumeration;
import log.demo.service.dto.PassRequestDTO;
import log.demo.service.routing.RoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
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

    @GetMapping("/test")
    public ResponseEntity<String> doGet() {
        return ResponseEntity.ok("get");
    }

    private MultiValueMap<String, String> getHeaders(HttpServletRequest request) {
        final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            Enumeration<String> headerValues = request.getHeaders(headerName);
            headerValues.asIterator()
                .forEachRemaining(headerValue -> headers.add(headerName, headerValue));
        });
        return headers;
    }
}
