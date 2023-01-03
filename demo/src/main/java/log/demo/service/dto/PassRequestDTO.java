package log.demo.service.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.util.MultiValueMap;

@Builder
@Getter
public class PassRequestDTO {
    private byte[] body;
    private String uri;
    private String queryString;
    private String method;
    private MultiValueMap<String, String> headers;
}
