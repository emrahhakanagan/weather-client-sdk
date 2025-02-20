package com.weather.sdk;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class BaseTest {

    @InjectMocks
    protected WeatherSDK weatherSDK;

    @BeforeEach
    @DisplayName("Setup mock API key before each test")
    void setUp() {
        weatherSDK = new WeatherSDK();
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("config.file");
    }
}
