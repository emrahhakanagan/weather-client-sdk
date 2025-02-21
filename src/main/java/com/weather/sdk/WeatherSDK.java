package com.weather.sdk;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.weather.sdk.enums.Mode;
import com.weather.sdk.exception.WeatherSDKException;
import com.weather.sdk.exception.WeatherSDKExceptionHandler;
import com.weather.sdk.model.WeatherData;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WeatherSDK {
    private String apiKey;
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String CONFIG_FILE_PATH = "src/main/resources/config.properties";
    private final Mode mode;
    private static final Map<String, WeatherSDK> instances = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    private final Cache<String, WeatherData> cache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(10)
            .build();

    public WeatherSDK(Mode mode, long pollingInterval, TimeUnit unit) {
        this.apiKey = loadApiKey();

        if (instances.containsKey(apiKey)) {
            throw new IllegalStateException("An instance of WeatherSDK with this API key already exists.");
        }

        this.mode = mode;
        instances.put(apiKey, this);

        if (this.mode == Mode.POLLING) {
            startPolling(pollingInterval, unit);
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

    void startPolling(long interval, TimeUnit unit) {
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Polling weather data...");
            for (String city : cache.asMap().keySet()) {
                try {
                    WeatherData weatherData = fetchWeather(city);
                    cache.put(city, weatherData);
                    System.out.println("Updated weather for " + city);
                } catch (WeatherSDKException e) {
                    WeatherSDKExceptionHandler.handleException(e);
                }
            }
        }, 0, interval, unit);
    }

    protected WeatherData fetchWeather(String city) throws WeatherSDKException {
        String url = BASE_URL + "?q=" + city + "&appid=" + apiKey;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new WeatherSDKException("Error fetching weather data: " + response.statusCode());
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

        } catch (IOException | InterruptedException e) {
            WeatherSDKExceptionHandler.handleException(e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new WeatherSDKException("Failed to fetch weather data for " + city, e);
        } catch (JsonSyntaxException e) {
            WeatherSDKExceptionHandler.handleException(e);
            throw new WeatherSDKException("Failed to parse weather data for " + city, e);
        }
    }

    private static String loadApiKey() {
        Properties properties = new Properties();
        String configFilePath = System.getProperty("config.file", CONFIG_FILE_PATH);

        try (FileInputStream fis = new FileInputStream(configFilePath)) {
            properties.load(fis);
            String apiKey = properties.getProperty("api.key", "").trim();

            if (apiKey.isEmpty()) {
                throw new WeatherSDKException("API key is required");
            }
            return apiKey;
        } catch (IOException e) {
            WeatherSDKExceptionHandler.handleException(e);
            throw new WeatherSDKException("Failed to load API key", e);
        }
    }

    public static void clearInstances() {
        synchronized (instances) {
            instances.clear();
        }
    }
}