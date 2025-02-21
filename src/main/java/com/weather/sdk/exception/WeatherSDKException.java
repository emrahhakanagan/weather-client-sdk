package com.weather.sdk.exception;

public class WeatherSDKException extends RuntimeException {
    public WeatherSDKException(String message, Throwable cause) {
        super(message, cause);
    }

    public WeatherSDKException(String message) {
        super(message);
    }
}
