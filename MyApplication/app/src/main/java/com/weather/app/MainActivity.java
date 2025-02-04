package com.weather.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.weather.app.RemoteDataSource.WeatherRemoteDataSource;
import com.weather.app.RemoteDataSource.WeatherRemoteDataSource.WeatherDataCallback;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private TextView textViewWeather;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Asegúrate de tener en tu activity_main.xml un TextView con id "textViewWeather"
        textViewWeather = findViewById(R.id.textViewWeather);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Verifica el permiso de ubicación y, de ser posible, obtiene la última ubicación conocida
        checkLocationPermissionAndGetLocation();
    }

    // Método que comprueba el permiso y lo solicita si es necesario
    private void checkLocationPermissionAndGetLocation(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solicita el permiso
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // El permiso ya fue concedido, se obtiene la ubicación
            getLastLocation();
        }
    }

    // Obtiene la última ubicación conocida del dispositivo
    private void getLastLocation(){
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location != null) {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        // Una vez obtenida la ubicación, se llama a la API para obtener el tiempo
                        fetchWeatherData(lat, lon);
                    } else {
                        Toast.makeText(MainActivity.this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (SecurityException e){
            e.printStackTrace();
        }
    }

    // Realiza la petición para obtener los datos del tiempo
    private void fetchWeatherData(double lat, double lon){
        WeatherRemoteDataSource.fetchWeatherData(lat, lon, new WeatherDataCallback() {
            @Override
            public void onSuccess(String weatherData) {
                // Actualiza la interfaz en el hilo principal
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewWeather.setText(weatherData);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        textViewWeather.setText("Error al obtener datos del tiempo: " + e.getMessage());
                    }
                });
            }
        });
    }   

    // Maneja el resultado de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == LOCATION_PERMISSION_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLastLocation();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
