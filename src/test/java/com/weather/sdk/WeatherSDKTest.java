package com.weather.sdk;

import com.weather.sdk.enums.Mode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WeatherSDKTest extends BaseTest {

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

        Exception exception = assertThrows(IllegalArgumentException.class, () -> new WeatherSDK(Mode.ON_DEMAND));
        assertEquals("API key is required", exception.getMessage());

        System.clearProperty("config.file"); // Очистить после теста
    }

    @DisplayName("Should return cached weather data on second request")
    @Test
    void shouldReturnCachedWeatherData() throws Exception {
        String city = "Berlin";
        String expectedWeather = "{\"weather\":{\"main\":\"Clouds\",\"description\":\"scattered clouds\"}}";

        WeatherSDK spySDK = Mockito.spy(new WeatherSDK(Mode.ON_DEMAND));

        Mockito.doReturn(expectedWeather).when(spySDK).fetchWeather(city);

        spySDK.getWeather(city); // Первый вызов (кэшируется)
        spySDK.getWeather(city); // Второй вызов (должен брать из кэша)

        Mockito.verify(spySDK, Mockito.times(1)).fetchWeather(city);
    }

    @Test
    @DisplayName("Should remove oldest city when more than 10 cities are added to cache")
    void shouldRemoveOldestCityFromCache() throws IOException, InterruptedException {
        WeatherSDK spySDK = Mockito.spy(new WeatherSDK(Mode.ON_DEMAND));

        Mockito.doAnswer(invocation -> {
            String city = invocation.getArgument(0);
            return "weather for " + city;
        }).when(spySDK).fetchWeather(Mockito.anyString());

        // Add 11 cities to the cache
        for (int i = 0; i < 11; i++) {
            spySDK.getWeather("City" + i);
        }

        // When the 11th city is added, the oldest value (City0) should be removed
        // Now, we request City0 again – if it was removed, fetchWeather will be called again
        spySDK.getWeather("City0");

        Mockito.verify(spySDK, Mockito.times(2)).fetchWeather("City0");

        Mockito.verify(spySDK, Mockito.times(1)).fetchWeather("City10");
    }

}