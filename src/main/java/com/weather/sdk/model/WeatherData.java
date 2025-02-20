package com.weather.sdk.model;

public class WeatherData {
    private final double temperature;
    private final double feelsLike;
    private final int visibility;
    private final String description;
    private final long datetime;
    private final long sunrise;
    private final long sunset;
    private final int timezone;
    private final String cityName;

    public WeatherData(double temperature, double feelsLike, int visibility, String description,
                       long datetime, long sunrise, long sunset, int timezone, String cityName) {
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.visibility = visibility;
        this.description = description;
        this.datetime = datetime;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.timezone = timezone;
        this.cityName = cityName;
    }

    public double getTemperature() { return temperature; }
    public double getFeelsLike() { return feelsLike; }
    public int getVisibility() { return visibility; }
    public String getDescription() { return description; }
    public long getDatetime() { return datetime; }
    public long getSunrise() { return sunrise; }
    public long getSunset() { return sunset; }
    public int getTimezone() { return timezone; }
    public String getCityName() { return cityName; }

    @Override
    public String toString() {
        return "WeatherData{" +
                "temperature=" + temperature +
                ", feelsLike=" + feelsLike +
                ", visibility=" + visibility +
                ", description='" + description + '\'' +
                ", datetime=" + datetime +
                ", sunrise=" + sunrise +
                ", sunset=" + sunset +
                ", timezone=" + timezone +
                ", cityName='" + cityName + '\'' +
                '}';
    }
}