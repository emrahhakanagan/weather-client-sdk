package com.weather.sdk;

import com.weather.sdk.enums.Mode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class BaseTest {

    @InjectMocks
    protected WeatherSDK weatherSDK;

    @BeforeAll
    static void beforeAll() {
        WeatherSDK.clearInstances();
    }

    @BeforeEach
    @DisplayName("Setup mock API key before each test")
    void setUp() {
        WeatherSDK.clearInstances();
        weatherSDK = new WeatherSDK(Mode.ON_DEMAND);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("config.file");
        WeatherSDK.clearInstances();
    }
}
