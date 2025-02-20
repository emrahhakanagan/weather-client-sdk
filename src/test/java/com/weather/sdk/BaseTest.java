package com.weather.sdk;

import com.weather.sdk.enums.Mode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
public abstract class BaseTest {

    protected WeatherSDK weatherSDK;

    @BeforeAll
    static void beforeAll() {
        WeatherSDK.clearInstances();
    }

    @BeforeEach
    @DisplayName("Setup mock API key before each test")
    void setUp() {
        WeatherSDK.clearInstances();
        weatherSDK = new WeatherSDK(Mode.ON_DEMAND, 2, TimeUnit.SECONDS);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("config.file");
        WeatherSDK.clearInstances();
    }
}
