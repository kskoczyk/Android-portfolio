//TODO: Zmiana orientacji wywołuje onCreate(), wszystkie textView są czyszczone - zablokować orientację?

package pl.kskoczyk.mayweather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class WeatherActivity extends AppCompatActivity {

    WeatherConditions weatherConditions;
    ApiRequest apiRequest;
    String mode = "GPS"; // defaultowo aplikacja ma wysyłać żadądania na podstawie lokalizacji
    LocationManager locationManager;
    LocationListener locationListener;

    RelativeLayout relativeLayout; // do ustawiania tła
    Drawable baseBackground;
    TextView locationText;
    TextView temperatureText;
    TextView pressureText;
    TextView windText;
    Spinner citySpinner;
    ArrayList<City> cities;
    SpinnerCity spinnerCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // inicjalizacja głównego layoutu
        relativeLayout = findViewById(R.id.relativeLayout);
        baseBackground = relativeLayout.getBackground();

        // inicjlizacja klas pomocniczych
        weatherConditions = new WeatherConditions();
        apiRequest = new ApiRequest(this, weatherConditions);

        // inicjalizacja pól do prezentowania danych
        locationText = findViewById(R.id.textViewLocation);
        temperatureText = findViewById(R.id.textViewTemperature);
        pressureText = findViewById(R.id.textViewPressure);
        windText = findViewById(R.id.textViewWind);

        // inicjalizacja spinnera, listy do wyświetlenia i adaptera tejże listy
        citySpinner = findViewById(R.id.spinnerCities);
        cities = new ArrayList<City>();
        cities.add(new City("Select a city...", "", -181.0, -181.0)); // tzw. hint, ten item będzie wyłączony z wyboru
        cities.add(new City("GPS mode", "", -181.0, -181.0));
        cities.add(new City("Kraków", "PL", 50.083328, 19.91667 ));
        cities.add(new City("Warsaw", "PL", 52.229771,  21.01178 ));
        cities.add(new City("Poznań", "PL", 52.406921,  16.92993 ));
        cities.add(new City("Wien", "AT", 48.208199, 16.371691 ));
        cities.add(new City("Lipnica Wielka", "PL", 49.5, 19.61 )); // moja dzielnia xD

        spinnerCity = new SpinnerCity(this, weatherConditions, citySpinner, cities);

        // inicjalizacja reszty widoku (guziki, spinnery)
        Button requestButton = findViewById(R.id.buttonRequest);
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // jeśli jeszcze nie znaleziono lokalizacji
                if(weatherConditions.getLatitude() < -180 || weatherConditions.getLongitude() < -180) {
                    Toast.makeText(getBaseContext(), "Wait for your device's GPS to get a fix", Toast.LENGTH_LONG).show();
                    return;
                }
                apiRequest.makeRequest();
            }
        });

        // włącz GPS na starcie
        startGPS();
        update(weatherConditions);
    }

    // aktualizacja textViews na podstawie danych w klasie WeatherConditions
    void update(WeatherConditions weatherConditions) {
        DecimalFormat df = new DecimalFormat("#.##");

        if(weatherConditions.getTemperature() > -274) {
            temperatureText.setText("Temperature: " + "\n" + df.format(weatherConditions.getTemperature()) + " °C");
        }
        else {
            temperatureText.setText("Temperature: " + "\n" + "N/A");
        }

        if(weatherConditions.getAirPressure() > -1) {
            pressureText.setText("Air pressure: " + "\n" + df.format(weatherConditions.getAirPressure()) + " hPa");
        }
        else {
           pressureText.setText("Air pressure: " + "\n" + "N/A");
        }

        if(weatherConditions.getWindSpeed() > -1) {
           windText.setText("Wind speed: " + "\n" + df.format(weatherConditions.getWindSpeed()) + " m/s");
        }
        else {
            windText.setText("Wind speed: " + "\n" + "N/A");
        }

        // zmień tło zależnie od warunków pogodowych - nie uwzględniam dnia i nocy
        if(weatherConditions.getCondition() == -1) { // N/A
            relativeLayout.setBackground(baseBackground); // od API 16
        }
        else if(weatherConditions.getCondition()/100 == 2) { // burza
            relativeLayout.setBackgroundResource(R.drawable.thunderstorm);
        }
        else if(weatherConditions.getCondition()/100 == 3) { // mżawka
            relativeLayout.setBackgroundResource(R.drawable.drizzle);
        }
        else if(weatherConditions.getCondition()/100 == 5) { // deszcz
            relativeLayout.setBackgroundResource(R.drawable.rain);
        }
        else if((weatherConditions.getCondition()/100 == 6)) { // śnieg
            relativeLayout.setBackgroundResource(R.drawable.snow);
        }
        else if((weatherConditions.getCondition()/100 == 7)) { // mgły, tornada, wulkany... - ja zostanę przy samej mgle xD
            relativeLayout.setBackgroundResource(R.drawable.mist);
        }
        else if(weatherConditions.getCondition() == 800) { // czyste niebo
            relativeLayout.setBackgroundResource(R.drawable.clear_sky);
        }
        else { // chmury
            relativeLayout.setBackgroundResource(R.drawable.clouds);
        }
    }

    void startGPS() {
        //-- trochę brzydki kod
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new CustomLocationListener(this, weatherConditions); // TODO: do góry?
        // TODO: aktualnie muszę ręcznie ustawić permisję z menu aplikacji - wyświetlić odpowiedni komunikat w pętli
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(getBaseContext(), "No GPS permission", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
    }

    void stopGPS() {
        if(locationManager != null) { // nie usuwaj GPS dwa razy (NullPointerException)
            locationManager.removeUpdates(locationListener);
            locationManager = null;
        }
    }
}
