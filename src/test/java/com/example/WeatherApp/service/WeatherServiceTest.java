package com.example.WeatherApp.service;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class WeatherServiceTest {

    static ClientAndServer mockServer;

    @BeforeAll
    static void beforeAll() {
        mockServer = startClientAndServer();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("weather.api.url", () -> "http://localhost:" + mockServer.getPort());
    }

    @AfterAll
    static void afterAll() {
        mockServer.stop();
    }

    @BeforeEach
    void setUp() {
        mockServer.reset();
    }


    @Autowired
    MockMvc mockMvc;


    @Test
    void testGetWeatherData() throws Exception {
        String cityName = "London";
        String apiKey = "c9708c3406d269519ae9e880a5e88dc4";

        //define the mock response for MockServer
        mockServer.when(request().withMethod("GET")
                        .withPath("/weather")
                        .withQueryStringParameter("q", cityName)
                        .withQueryStringParameter("appid", apiKey))
                .respond(response().withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                        {
                            "coord": {},
                            "weather": [],
                            "base": "stations",
                            "main": {},
                            "visibility": 10000,
                            "wind": {
                                "speed": 4.63,
                                "deg": 230
                            },
                            "clouds": {},
                            "dt": 1733392439,
                            "sys": {},
                            "timezone": 0,
                            "id": 2643743,
                            "name": "London",
                            "cod": 200
                        }
                        """));

        //perform the request and store the result
        ResultActions resultActions = this.mockMvc.perform(get("/weather")
                        .param("city", cityName)
                        .param("appid", apiKey))
                .andExpect(status().isOk());

    }



}
