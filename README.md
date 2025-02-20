# WeatherSDK

## Overview
**WeatherSDK** is a Java-based library that provides real-time weather data using the OpenWeatherMap API. It supports two modes of operation:
- **On-Demand Mode**: Fetches weather data only when explicitly requested.
- **Polling Mode**: Automatically updates weather data at a specified interval.

The SDK includes **caching**, **error handling**, and **API request optimizations** to ensure efficiency and reliability.

---

## **Installation**

### **1. Add WeatherSDK to Your Project**
#### **Using Maven**
Add the following dependency to your `pom.xml`:
```xml
<dependency>
    <groupId>com.weather.sdk</groupId>
    <artifactId>weathersdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### **Using Gradle**
```
dependencies {
implementation 'com.weather.sdk:weathersdk:1.0.0'
}
```

### **2. Set Up API Key**
Create a config.properties file in src/main/resources/:
```
api.key=YOUR_OPENWEATHER_API_KEY
```

Replace YOUR_OPENWEATHER_API_KEY with your actual API key from [OpenWeatherMap](https://openweathermap.org/).

---

## **Usage**
### **1. Initialize WeatherSDK**
On-Demand Mode (Manual Requests)

```java
WeatherSDK weatherSDK = new WeatherSDK(Mode.ON_DEMAND, 10, TimeUnit.MINUTES);
WeatherData weather = weatherSDK.getWeather("London");

System.out.println("Temperature: " + weather.getTemperature() + "°C");
System.out.println("Description: " + weather.getDescription());
```

#### **Polling Mode (Auto Updates Every 2 Minutes)**

```java
WeatherSDK weatherSDK = new WeatherSDK(Mode.POLLING, 2, TimeUnit.MINUTES);
WeatherData weather = weatherSDK.getWeather("Berlin");

System.out.println("Temperature: " + weather.getTemperature() + "°C");
System.out.println("Description: " + weather.getDescription());
```

In POLLING mode, the SDK will automatically refresh weather data for all cached cities.

---

### **Features**
- Supports On-Demand & Polling Modes
- Caching (stores up to 10 cities, removes the oldest when full)
- Automatic Updates (in POLLING mode)
- Error Handling & Logging
- Easy API Key Configuration
- Built-in Unit Tests

---

## **Cache Mechanism**
* Stores weather data for up to 10 cities.
* Data is automatically removed after 10 minutes or when a new city exceeds the cache limit.
* Polling Mode: Updates weather data automatically without repeated API calls.

---

## **Exception Handling**

| Scenario            | Exception                                                   |
|---------------------|-------------------------------------------------------------|
| Missing API Key    | `IllegalArgumentException("API key is required")`           |
| Invalid API Key    | `IOException("Error fetching weather data: 401")`           |
| API Server Issues  | `IOException("Error fetching weather data: <status_code>")` |
| Request Timeout    | `InterruptedException`                                      |

---

## **Unit Tests**
**The SDK includes JUnit & Mockito-based tests to verify:**
* API key handling.
* Weather data retrieval.
* Polling mode updates.
* Cache management.

*To run tests:*
```
mvn test
```

## **Contact & Support**
For issues or support, create a GitHub issue or contact us via *[emrahhakanagan@gmail.com](mailto:emrahhakanagan@gmail.com)*.
