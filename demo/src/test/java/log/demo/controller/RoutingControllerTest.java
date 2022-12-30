package log.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import log.demo.service.routing.RoutingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoutingController.class)
@Import(RoutingControllerTest.RoutingServiceTest.class)
@AutoConfigureMockMvc
class RoutingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("GET METHOD - text/plain 형식")
    void getTextTest() throws Exception {
        //given
        String path = "/get/text";
        String bodyString = "getText";
        byte[] requestBodyBytes = bodyString.getBytes(StandardCharsets.UTF_8);


        //when
        MvcResult mvcResult = mockMvc.perform(
                    get(path)
                            .content(requestBodyBytes)
                            .contentType(MediaType.TEXT_PLAIN)
                )
                .andExpect(status().isOk())
                .andReturn();


        //then
        String responseBody = new String(mvcResult.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        assertThat(responseBody).isEqualTo(bodyString);
    }

    @Test
    @DisplayName("POST METHOD - application/json 형식")
    void postJsonTest() throws Exception {
        //given
        String path = "/post/json";
        String bodyString = "{\"head\":\"snake\",\"tail\":\"dog\"}";
        byte[] requestBodyBytes = bodyString.getBytes(StandardCharsets.UTF_8);


        //when
        MvcResult mvcResult = mockMvc.perform(
                    post(path)
                            .content(requestBodyBytes)
                            .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();


        //then
        String responseBody = new String(mvcResult.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        assertThat(responseBody).isEqualTo(bodyString);
    }

    @Test
    @DisplayName("POST METHOD - application/x-www-form-urlencoded 형식")
    void postFormTest() throws Exception {
        //given
        String path = "/post/form";
        String bodyString = "name=kim&age=23";
        byte[] requestBodyBytes = bodyString.getBytes(StandardCharsets.UTF_8);


        //when
        MvcResult mvcResult = mockMvc.perform(
                        post(path)
                                .content(requestBodyBytes)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                )
                .andExpect(status().isOk())
                .andReturn();


        //then
        String responseBody = new String(mvcResult.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        assertThat(responseBody).isEqualTo(bodyString);
    }

    static class RoutingServiceTest implements RoutingService {
        @Override
        public ResponseEntity<byte[]> passHttpRequest(byte[] body, HttpServletRequest request, String destHost) throws URISyntaxException {
            return ResponseEntity.ok(body);
        }
    }

}