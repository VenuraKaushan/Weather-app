package com.example.WeatherApp.service;

import com.example.WeatherApp.dto.WeatherData;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@EnableAsync
public class WeatherService {

    @Autowired
    private WebClient webClient;

    private final String apiUrl;

    public WeatherService(WebClient webClient, @Value("${weather.api.url}") String apiUrl) {
        this.webClient = webClient;
        this.apiUrl = apiUrl;
    }

    @Cacheable(value = "weather", key = "#city", unless = "#result == null", cacheManager = "cacheManager")
    @Async
    public CompletableFuture<WeatherData> getWeatherData(String city) {
        return webClient.get()
                .uri(apiUrl + "?q=" + city + "&appid=c9708c3406d269519ae9e880a5e88dc4")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::processWeatherData)
                .toFuture();
    }


    //calculate and handle res data
    private WeatherData processWeatherData(JsonNode apiResponse) {
        WeatherData weatherData = new WeatherData();

        //extract city name from the response
        String cityName = apiResponse.get("city").get("name").asText();
        weatherData.setCity(cityName);

        //extract temperature data from the response
        JsonNode list = apiResponse.get("list");

        //get the date range for the last 7 days
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        //filter data to only include the last 7 days
        List<JsonNode> recentWeatherData = StreamSupport.stream(list.spliterator(), false)
                .filter(node -> {
                    String dateTime = node.get("dt_txt").asText();
                    String date = dateTime.split(" ")[0]; // Extract the date part
                    LocalDate nodeDate = LocalDate.parse(date, formatter);
                    return !nodeDate.isBefore(sevenDaysAgo) && !nodeDate.isAfter(today);
                })
                .collect(Collectors.toList());

        double totalTemperature = 0;
        double maxTemperature = Double.MIN_VALUE;
        double minTemperature = Double.MAX_VALUE;
        String hottestDay = "";
        String coldestDay = "";

        //maps to accumulate daily temperature data
        Map<String, Double> dailyTemperatureSum = new HashMap<>();
        Map<String, Integer> dailyTemperatureCount = new HashMap<>();

        //iterate through the filtered weather data list
        for (JsonNode node : recentWeatherData) {
            double temp = node.get("main").get("temp").asDouble();
            String dateTime = node.get("dt_txt").asText();
            String date = dateTime.split(" ")[0];

            //accumulate temperatures for the date
            dailyTemperatureSum.put(date, dailyTemperatureSum.getOrDefault(date, 0.0) + temp);
            dailyTemperatureCount.put(date, dailyTemperatureCount.getOrDefault(date, 0) + 1);

            if (temp > maxTemperature) {
                maxTemperature = temp;
                hottestDay = date;
            }
            if (temp < minTemperature) {
                minTemperature = temp;
                coldestDay = date;
            }
        }

        //calculate the average temperature over the last 7 days
        double totalTemperatureSum = dailyTemperatureSum.values().stream().mapToDouble(Double::doubleValue).sum();
        int totalCount = dailyTemperatureCount.values().stream().mapToInt(Integer::intValue).sum();
        double averageTemperature = totalCount > 0 ? totalTemperatureSum / totalCount : 0;

        //populate WeatherData DTO
        weatherData.setAverageTemperature(averageTemperature);
        weatherData.setHottestDay(hottestDay);
        weatherData.setColdestDay(coldestDay);

        return weatherData;
    }


}
