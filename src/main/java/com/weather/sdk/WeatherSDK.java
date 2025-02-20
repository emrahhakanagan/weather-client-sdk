package com.weather.sdk;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WeatherSDK {
    private String apiKey = "1f15183e78544e303ccdc80eadfa7b5a";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    private final Cache<String, String> cache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(100)
            .build();

    public WeatherSDK(String apiKey) {
        this.apiKey = apiKey;
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
}