package com.weather.app.RemoteDataSource;

import java.io.IOException;

public class WeatherRemoteDataSource {

    // Reemplaza con tu API Key de OpenWeatherMap
    private static final String API_KEY = "TU_API_KEY_AQUI";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    // Interfaz para el callback al obtener los datos del tiempo
    public interface WeatherDataCallback {
        void onSuccess(String weatherData);
        void onFailure(Exception e);
    }

    // Método que realiza la petición asíncrona a la API
    public static void fetchWeatherData(double lat, double lon, WeatherDataCallback callback) {
        OkHttpClient client = new OkHttpClient();
        String url = BASE_URL + "?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    String responseData = response.body().string();
                    callback.onSuccess(responseData);
                } else {
                    callback.onFailure(new IOException("Error en la respuesta: " + response));
                }
            }
        });
    }
}
