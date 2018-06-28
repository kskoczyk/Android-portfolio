package pl.kskoczyk.mayweather;

import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// WAŻNE INFO: API podaje informacje w jednostkach STANDARDOWYCH (czyli temperatura w K, ciśnienie W hPa), można zmienić zapytania na metryczny i °C, ale mi się nie chce
// zaokrągla współrzędne do 2 miejsc po przecinku, w przypadku wsi może wybrać sąsiednią
// radzi sobie z nazwami miast oddzielonych spacją
public class ApiRequest {
    WeatherActivity weatherActivity;
    WeatherConditions weatherConditions;

    // MUSI BYĆ http://
    String baseUrl = "http://api.openweathermap.org/data/2.5/weather?"; // q=, lat= & lon=
    String APIkey = "&APPID=78eb19548ca861892d298123baa2f2f6";

    public ApiRequest(WeatherActivity mainActivity, WeatherConditions conditions) {
        weatherActivity = mainActivity;
        weatherConditions = conditions;
    }

    public void makeRequest() {
        String additionalParams = "";
        if(weatherActivity.mode.equals("GPS")) {
            additionalParams = "lat="+weatherConditions.getLatitude()+"&lon="+weatherConditions.getLongitude();
        }
        // City mode
        else {
            additionalParams = "q="+weatherConditions.getCityName()+","+weatherConditions.getCountry();
        }
        String requestUrl = baseUrl + additionalParams + APIkey;

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(weatherActivity);

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONObject receivedJSON = response;
                try {
                    int code = receivedJSON.getInt("cod"); // sprawdź status odpowiedzi
                    // coś się stało po stronie API - chwilowo niedostępne, zbyt dużo żądań, nieprawidłowe parametry
                    if(code != 200) {
                        weatherActivity.temperatureText.setText("#ERR_API");
                        weatherActivity.pressureText.setText("#ERR_API");
                        weatherActivity.windText.setText("#ERR_API");

                        // setup the alert builder
                        AlertDialog.Builder builder = new AlertDialog.Builder(weatherActivity);
                        builder.setTitle("API error");
                        builder.setMessage("Weather API returned an unexpected response code: " + code + ". Please visit openweathermap.org for more information regarding this error.");

                        // add a button
                        builder.setPositiveButton("OK", null); // null wciąż pozwala zamknąć alert

                        // create and show the alert dialog
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return;
                    }

                    // przetwórz otrzymanego JSONa
                    // temperatura i ciśnienie
                    JSONObject main  = receivedJSON.getJSONObject("main");
                    weatherConditions.setTemperature(main.getDouble("temp") - 273.15);
                    weatherConditions.setAirPressure(main.getDouble("pressure"));
                    // wiatr
                    JSONObject wind = receivedJSON.getJSONObject("wind");
                    weatherConditions.setWindSpeed(wind.getDouble("speed"));
                    // id warunków pogodowych
                    JSONArray weather = receivedJSON.getJSONArray("weather");
                    JSONObject weatherObject = weather.getJSONObject(0); // weatherArray ma TYLKO JEDEN JSONobject - po kiego wała zrobili z tego tablicę?
                    weatherConditions.setCondition(weatherObject.getInt("id"));

                    String halt = "halt"; // debug
                    weatherActivity.update(weatherConditions);
                    Toast.makeText(weatherActivity, "Updated weather conditions", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    receivedJSON = null;
                }

                if(receivedJSON == null) {
                    Toast.makeText(weatherActivity, "JSON parsing error", Toast.LENGTH_SHORT).show();
                    weatherActivity.temperatureText.setText("#ERR_PARSING");
                    weatherActivity.pressureText.setText("#ERR_PARSING");
                    weatherActivity.windText.setText("#ERR_PARSING");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // nie udało się połączyć
                // code 401 (invalid API key) ląduje tutaj, Volley ma własne zabezpieczenia - VolleyAuthError

                weatherActivity.temperatureText.setText("#ERR_REQUEST");
                weatherActivity.pressureText.setText("#ERR_REQUEST");
                weatherActivity.windText.setText("#ERR_REQUEST");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }
}
