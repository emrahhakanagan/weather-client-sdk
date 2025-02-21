package com.weather.sdk.exception;

import java.io.IOException;
import com.google.gson.JsonSyntaxException;

public class WeatherSDKExceptionHandler {

    public static void handleException(Exception e) {
        if (e instanceof IOException) {
            System.err.println("[WeatherSDK] API error: " + e.getMessage());
            throw new WeatherSDKException("API error occurred", e);
        } else if (e instanceof IllegalArgumentException) {
            System.err.println("[WeatherSDK] Invalid argument: " + e.getMessage());
            throw new WeatherSDKException("Invalid input parameter", e);
        } else if (e instanceof InterruptedException) {
            System.err.println("[WeatherSDK] Thread was interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Восстанавливаем флаг прерывания
            throw new WeatherSDKException("Thread interruption error", e);
        } else if (e instanceof JsonSyntaxException) {
            System.err.println("[WeatherSDK] JSON parsing error: " + e.getMessage());
            throw new WeatherSDKException("Invalid JSON format received", e);
        } else {
            System.err.println("[WeatherSDK] Unexpected error: " + e.getMessage());
            throw new WeatherSDKException("Unexpected SDK error", e);
        }
    }
}