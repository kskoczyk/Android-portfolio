package pl.kskoczyk.mayweather;

import android.databinding.Bindable;

public class WeatherConditions {
    // w WeatherActivity update(): jeśli wartość niemożliwa, ustaw "N/A"
    String cityName = "N/A";
    String country = "N/A"; // tylko do zapytań bez użycia GPS
    Double latitude = -181.0;
    Double longitude = -181.0; // lat i lon mogą być dodatnie i ujemne, ale na pewno w granicach (-180, 180)
    Double temperature = -274.0; // °C - wyjście poza zakres Kelvina, na wszelki wypadek
    Double airPressure = -1.0; // hPa
    Double windSpeed = -1.0; // m/s
    Integer condition = -1;

    public WeatherConditions() {
    }

    // auto-create
    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getAirPressure() {
        return airPressure;
    }

    public void setAirPressure(Double airPressure) {
        this.airPressure = airPressure;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public Integer getCondition() { return condition; }

    public void setCondition(Integer condition) { this.condition = condition; }

    public String getCountry() { return country; }

    public void setCountry(String country) { this.country = country; }
}
