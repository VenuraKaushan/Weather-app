package com.example.WeatherApp.dto;

public class WeatherData {
    private String city;
    private double averageTemperature;
    private String hottestDay;
    private String coldestDay;

    //setters
    public void setCity(String city) {
        this.city = city;
    }

    public void setAverageTemperature(double averageTemperature) {
        this.averageTemperature = averageTemperature;
    }

    public void setHottestDay(String hottestDay) {
        this.hottestDay = hottestDay;
    }

    public void setColdestDay(String coldestDay) {
        this.coldestDay = coldestDay;
    }

    //getters
    public String getCity() {
        return city;
    }

    public double getAverageTemperature() {
        return averageTemperature;
    }

    public String getHottestDay() {
        return hottestDay;
    }

    public String getColdestDay() {
        return coldestDay;
    }
}
