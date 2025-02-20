package com.weather.sdk;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;

class WeatherSDKTest {

    @Test
    @DisplayName("Should throw IOException when API key is invalid")
    void shouldThrowExceptionWhenApiKeyIsInvalid() {
        WeatherSDK sdk = new WeatherSDK("INVALID_KEY");
        Exception exception = assertThrows(IOException.class, () -> sdk.getWeather("London"));
        assertTrue(exception.getMessage().contains("Error fetching weather data"));
    }
}