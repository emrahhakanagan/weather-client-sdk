package com.weather.sdk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WeatherSDKTest extends BaseTest {
    @Mock
    private Properties mockProperties; // Теперь это поле будет мокироваться автоматически

    @Test
    @DisplayName("Test API key initialization")
    void testApiKeyInitialization() {
        assertNotNull(weatherSDK.getApiKey(), "API key should not be null");
        assertEquals("1f15183e78544e303ccdc80eadfa7b5a", weatherSDK.getApiKey(), "API key should match the mocked value");
    }

    @Test
    @DisplayName("Test successful weather retrieval")
    void testGetWeatherSuccess() throws IOException, InterruptedException {
        String city = "London";
        String response = weatherSDK.getWeather(city);
        assertNotNull(response, "Response should not be null");
        assertTrue(response.contains("weather"), "Response should contain 'weather'");
    }

    @Test
    @DisplayName("Should throw exception when city is invalid")
    void shouldThrowExceptionWhenCityIsInvalid() {
        Exception exception = assertThrows(IOException.class, () ->
                weatherSDK.getWeather("InvalidCity"));
        assertTrue(exception.getMessage().contains("Error fetching weather data"),
                "Exception message should contain expected error text");
    }

    @Test
    @DisplayName("Should throw exception when API key is missing")
    void shouldThrowExceptionWhenApiKeyIsMissing() {
        System.setProperty("config.file", "src/test/resources/config-test-empty-key.properties");

        Exception exception = assertThrows(IllegalArgumentException.class, WeatherSDK::new);
        assertEquals("API key is required", exception.getMessage());

        System.clearProperty("config.file"); // Очистить после теста
    }
}