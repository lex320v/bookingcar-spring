package com.bookingcar.integration.controller;

import com.bookingcar.BaseIntegrationTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@AutoConfigureMockMvc
@RequiredArgsConstructor
public class LoginControllerIT extends BaseIntegrationTest {

    private final MockMvc mockMvc;

    @Test
    void createRequestView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("user/login")
                );
    }
}
