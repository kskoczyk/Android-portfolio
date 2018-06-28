package pl.kskoczyk.mayweather;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class SpinnerCity {
    WeatherActivity weatherActivity;
    WeatherConditions weatherConditions;
    Spinner citySpinner;
    ArrayList<City> cities;
    // potrzebne?
    ArrayAdapter<City> citiesAdapter;

    public SpinnerCity(final WeatherActivity weatherActivity, final WeatherConditions weatherConditions, Spinner citySpinner, ArrayList<City> cities) {
        this.weatherActivity = weatherActivity;
        this.weatherConditions = weatherConditions;
        this.citySpinner = citySpinner;
        this.cities = cities;

        // obiekty z listy można modyfikować, ale nie mam dodanych listenerów zmian, bo mi to niepotrzebne
        citiesAdapter = new ArrayAdapter<City>(this.weatherActivity, R.layout.spinner_row, R.id.textViewCity, this.cities) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) { // hint
                    return false;
                }
                return true;
            }
        };
        // dodaj adapter do spinnera
        this.citySpinner.setAdapter(citiesAdapter);

        this.citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // hint, wybiera się sam na początku, ale TYLKO RAZ - ignoruj
                if(position == 0) {
                    return;
                }

                City selectedItem = (City)parent.getItemAtPosition(position);
                // GPS mode
                if(position == 1) {
                    weatherActivity.mode = "GPS";
                    weatherConditions.setCountry("N/A");
                    weatherConditions.setLatitude(-181.0);
                    weatherConditions.setLongitude(-181.0);
                    weatherActivity.startGPS();
                    return;
                }
                // City mode
                else {
                    weatherActivity.mode = selectedItem.toString();
                    weatherActivity.stopGPS();
                }

                // pobierz miasto i zaktualizuj dane odnośnie lokalizacji w Weather Conditions
                weatherConditions.setCityName(selectedItem.toString());
                weatherConditions.setCountry(selectedItem.getCountryCode());
                weatherConditions.setLatitude(selectedItem.getLatitude());
                weatherConditions.setLongitude(selectedItem.getLongitude());

                weatherActivity.locationText.setText(selectedItem.toLocationString()); // aktualizacja tekstu lokalizacji osobno
                weatherActivity.update(weatherConditions);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Another interface callback
            }
        });
    }
}