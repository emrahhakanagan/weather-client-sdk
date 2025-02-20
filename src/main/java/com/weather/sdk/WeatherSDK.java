package com.weather.sdk;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WeatherSDK {
    private String apiKey;
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String CONFIG_FILE_PATH = "src/main/resources/config.properties";

    private final Cache<String, String> cache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(100)
            .build();

    public WeatherSDK() {
        this.apiKey = loadApiKey();
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public String getWeather(String city) throws IOException, InterruptedException {
        String cachedWeather = cache.getIfPresent(city);
        if (cachedWeather != null) {
            return cachedWeather;
        }

        String url = BASE_URL + "?q=" + city + "&appid=" + apiKey + "&units=metric";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Error fetching weather data: " + response.statusCode());
        }

        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        String weatherData = jsonResponse.toString();

        cache.put(city, weatherData);
        return weatherData;
    }

    private static String loadApiKey() {
        Properties properties = new Properties();
        String configFilePath = System.getProperty("config.file", CONFIG_FILE_PATH);

        try (FileInputStream fis = new FileInputStream(configFilePath)) {
            properties.load(fis);
            String apiKey = properties.getProperty("api.key", "").trim();

            if (apiKey.isEmpty()) {
                throw new IllegalArgumentException("API key is required");
            }
            return apiKey;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load API key", e);
        }
    }
}