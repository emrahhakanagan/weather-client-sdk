package com.weather.sdk;

import com.weather.sdk.enums.Mode;
import com.weather.sdk.model.WeatherData;
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
        WeatherData response = weatherSDK.getWeather(city); // Используем объект

        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getDescription(), "Weather description should not be null");

        // Проверяем, что описание содержит ключевые слова, например "clouds" или "clear sky"
        assertTrue(response.getDescription().toLowerCase().contains("cloud"),
                "Weather description should contain expected weather keyword");
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

    @Test
    void shouldReturnCachedWeatherData() throws Exception {
        String city = "Berlin";

        // Создаем объект WeatherData с 9 аргументами
        WeatherData expectedWeather = new WeatherData(
                25.0,  // temperature
                23.5,  // feelsLike
                10000, // visibility
                "Clouds", // description
                1675744800L, // datetime
                1675751262L, // sunrise
                1675787560L, // sunset
                3600, // timezone
                city  // cityName
        );

        WeatherSDK spySDK = Mockito.spy(new WeatherSDK(Mode.ON_DEMAND));

        Mockito.doReturn(expectedWeather).when(spySDK).fetchWeather(city);

        spySDK.getWeather(city);

        spySDK.getWeather(city);

        Mockito.verify(spySDK, Mockito.times(1)).fetchWeather(city);
    }

    @Test
    @DisplayName("Should remove oldest city when more than 10 cities are added to cache")
    void shouldRemoveOldestCityFromCache() throws IOException, InterruptedException {
        WeatherSDK spySDK = Mockito.spy(new WeatherSDK(Mode.ON_DEMAND));

        Mockito.doAnswer(invocation -> {
            String city = invocation.getArgument(0);
            return new WeatherData(
                    25.0,  // temperature
                    23.5,  // feelsLike
                    10000, // visibility
                    "clear_sky", // description
                    1675744800L, // datetime
                    1675751262L, // sunrise
                    1675787560L, // sunset
                    3600, // timezone
                    city  // cityName
            );
        }).when(spySDK).fetchWeather(Mockito.anyString());

        for (int i = 0; i < 11; i++) {
            spySDK.getWeather("City" + i);
        }

        spySDK.getWeather("City0");

        Mockito.verify(spySDK, Mockito.times(2)).fetchWeather("City0");
    }

    @Test
    @DisplayName("Test JSON parsing and data structure")
    void testWeatherDataParsing() throws IOException, InterruptedException {
        String city = "London";

        WeatherData weatherData = weatherSDK.getWeather(city);

        assertNotNull(weatherData, "WeatherData should not be null");
        assertTrue(weatherData.getTemperature() > 0, "Temperature should be a positive value");
        assertTrue(weatherData.getFeelsLike() > 0, "FeelsLike should be a positive value");
        assertTrue(weatherData.getVisibility() > 0, "Visibility should be a positive value");
        assertNotNull(weatherData.getDescription(), "Description should not be null");
        assertTrue(weatherData.getDatetime() > 0, "Datetime should be a positive value");
        assertTrue(weatherData.getSunrise() > 0, "Sunrise should be a positive value");
        assertTrue(weatherData.getSunset() > 0, "Sunset should be a positive value");
        assertNotNull(weatherData.getCityName(), "City name should not be null");
    }
}