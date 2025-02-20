package com.weather.sdk;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.weather.sdk.enums.Mode;
import com.weather.sdk.model.WeatherData;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WeatherSDK {
    private String apiKey;
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String CONFIG_FILE_PATH = "src/main/resources/config.properties";
    private final Mode mode;

    private final Cache<String, WeatherData> cache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(10)
            .build();

    public WeatherSDK(Mode mode) {
        this.mode = mode;
        this.apiKey = loadApiKey();

        if (this.mode == Mode.POLLING) {
            startPolling();
        }
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public WeatherData getWeather(String city) throws IOException, InterruptedException {
        WeatherData cachedWeather = cache.getIfPresent(city);
        if (cachedWeather != null) {
            return cachedWeather;
        }

        WeatherData weatherData = fetchWeather(city);

        if (cache.estimatedSize() >= 10) {
            String oldestCity = cache.asMap().keySet().iterator().next();
            cache.invalidate(oldestCity);
            System.out.println("Removed oldest city from cache: " + oldestCity);
        }

        cache.put(city, weatherData);
        return weatherData;
    }


    private void startPolling() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Polling weather data...");
            for (String city : cache.asMap().keySet()) {
                try {
                    WeatherData weatherData = fetchWeather(city);
                    cache.put(city, weatherData);
                    System.out.println("Updated weather for " + city);
                } catch (IOException | InterruptedException e) {
                    System.err.println("Failed to update weather for " + city);
                }
            }
        }, 0, 10, TimeUnit.MINUTES);
    }

    protected WeatherData fetchWeather(String city) throws IOException, InterruptedException {
        String url = BASE_URL + "?q=" + city + "&appid=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Error fetching weather data: " + response.statusCode());
        }

        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

        JsonObject main = jsonResponse.getAsJsonObject("main");
        double temperature = main.get("temp").getAsDouble();
        double feelsLike = main.get("feels_like").getAsDouble();
        int visibility = jsonResponse.get("visibility").getAsInt();

        JsonObject weather = jsonResponse.getAsJsonArray("weather").get(0).getAsJsonObject();
        String description = weather.get("description").getAsString();

        long datetime = jsonResponse.get("dt").getAsLong();
        JsonObject sys = jsonResponse.getAsJsonObject("sys");
        long sunrise = sys.get("sunrise").getAsLong();
        long sunset = sys.get("sunset").getAsLong();
        int timezone = jsonResponse.get("timezone").getAsInt();
        String cityName = jsonResponse.get("name").getAsString();

        return new WeatherData(temperature, feelsLike, visibility, description, datetime, sunrise, sunset, timezone, cityName);
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