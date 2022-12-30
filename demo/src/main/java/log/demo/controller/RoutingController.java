package log.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import log.demo.service.routing.RoutingService;
import log.demo.service.routing.RoutingServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.net.URISyntaxException;

@Controller
@Slf4j
@RequiredArgsConstructor
public class RoutingController {

    private final RoutingService routingService;
    @Value("${log.destination.host}")
    private String destHost;

    @RequestMapping(value = "/**", method = {
            RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS, RequestMethod.HEAD,
            RequestMethod.TRACE, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.PUT
    })
    public ResponseEntity<byte[]> routes(@RequestBody(required = false) byte[] body, HttpServletRequest request) throws URISyntaxException {
        return routingService.passHttpRequest(body, request, destHost);
    }
}
