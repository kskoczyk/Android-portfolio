package pl.kskoczyk.mayweather;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

/*---------- Listener class to get coordinates ------------- */
public class CustomLocationListener implements LocationListener {

    WeatherActivity weatherActivity;
    WeatherConditions weatherConditions;

    public CustomLocationListener(WeatherActivity mainActivity, WeatherConditions conditions) {
        weatherActivity = mainActivity;
        weatherConditions = conditions;
    }

    // trochę hakowe, jeśli lokalizacja nie ulegnie zmianie, apka tego nie wykryje (nie korzysta z ostatniej znanej lokalizacji, gdy GPS wyłączony)
    @Override
    public void onLocationChanged(Location loc) {
        // debug
        //Toast.makeText(weatherActivity, "Location changed: Lat: " + loc.getLatitude() + " Lon: " + loc.getLongitude(), Toast.LENGTH_SHORT).show();

        DecimalFormat df = new DecimalFormat("#.##"); // do 4 miejsc (androidowy decimalFormat wymaga API 24)
        String latitude = "Latitude: " + df.format(loc.getLatitude());
        String longitude = "Longitude: " + df.format(loc.getLongitude()); // toString() niepotrzebny
        weatherConditions.setLatitude(loc.getLatitude());
        weatherConditions.setLongitude(loc.getLongitude());

        /*------- To get city name from coordinates -------- */
        String cityName = null; // ciekawostka: wyświetlenie null stringa da "null", bez wyjątku
        Geocoder gcd = new Geocoder(weatherActivity, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            if (addresses.size() > 0) {
                System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        /* --------------------------------------------------- */

        // LocationListener będzie odpowiedzialny za aktualizację pól lokalizacji na ekranie i w WeatherConditions
        // uaktualnia informacje bez przerwy, więc nadaje się idealnie

        // aktualizuj pole lokalizacji, tylko jeśli apka polega na GPS urządzenia - w przeciwnym razie bierz dane ze spinnera
        if(weatherActivity.mode.equals("GPS")) {
            weatherConditions.setCityName(cityName);
            weatherActivity.locationText.setText("Location: " + cityName + "\n" + latitude + "\n" + longitude);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(weatherActivity, "I won't be able to find your location without GPS turned on!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(weatherActivity, "Thanks!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // szczerze mówiąc nie wiem, kiedy to się odpala, lepiej nie tykać
    }
}