package pl.kskoczyk.mayweather;

import java.text.DecimalFormat;

public class City {
    String name;
    String countryCode;
    Double latitude ;
    Double longitude;

    public City(String name, String countryCode, Double latitude, Double longitude) {
        this.name = name;
        this.countryCode = countryCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
       return name;
    }

    public String toLocationString() {
        DecimalFormat df = new DecimalFormat("#.##");
        return "Location: " + name + ", " + countryCode + "\n" +
                "Latitude: " + df.format(latitude) + "\n" +
                "Longitude: " + df.format(longitude);
    }

    public String getCountryCode() {
        return countryCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
