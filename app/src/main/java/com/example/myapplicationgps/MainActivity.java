package com.example.myapplicationgps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    private static final String TAG = "GPSApp";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private TextView tvInfo, tvResultado;
    private Button btnMarcarPosicao, btnCalcular;

    private LocationManager locationManager;
    private Location posicaoInicial = null;
    private Location posicaoAtual = null;

    private SensorManager sensorManager;
    private Sensor magnetometer;
    private Sensor accelerometer;
    private float[] rotationMatrix = new float[9];
    private float[] orientationAngles = new float[3];
    private float[] magnetometerReading = new float[3];
    private float[] accelerometerReading = new float[3];
    private float anguloBussola = 0;

    private static final int REQUEST_ENABLE_GPS = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate iniciado");

        // inicializar views
        tvInfo = findViewById(R.id.tvInfo);
        tvResultado = findViewById(R.id.tvResultado);
        btnMarcarPosicao = findViewById(R.id.btnMarcarPosicao);
        btnCalcular = findViewById(R.id.btnCalcular);

        // inicializar LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // inicializar sensores
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //  listeners dos botões
        btnMarcarPosicao.setOnClickListener(v -> marcarPosicaoInicial());
        btnCalcular.setOnClickListener(v -> calcularDistancia());

        // verificar e solicitar permissões
        verificarPermissoes();
    }

    private void verificarPermissoes() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        } else {
            verificarGPSHabilitado();
        }
    }

    private void verificarGPSHabilitado() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS desligado, pedir para usuário habilitar
            Toast.makeText(this, "por favor, ative o GPS", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, REQUEST_ENABLE_GPS);
        } else {
            iniciarAtualizacoesLocalizacao();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_GPS) {
            verificarGPSHabilitado(); // Tenta novamente após usuário ajustar configurações
        }
    }



    @SuppressLint("SetTextI18n")
    private void marcarPosicaoInicial() {
        if (posicaoAtual != null) {
            posicaoInicial = new Location(posicaoAtual);
            tvResultado.setText("posição inicial marcada:\n" +
                    "Lat: " + posicaoInicial.getLatitude() + "\n" +
                    "Lon: " + posicaoInicial.getLongitude() + "\n" +
                    "Alt: " + posicaoInicial.getAltitude() + "m\n" +
                    "Ângulo: " + anguloBussola + "°");
        } else {
            Toast.makeText(this, "aguardando dados de GPS...", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void calcularDistancia() {
        if (posicaoInicial != null && posicaoAtual != null) {
            float distancia = posicaoInicial.distanceTo(posicaoAtual);
            tvResultado.setText(tvResultado.getText() + "\n\ndistância calculada:\n" +
                    distancia + " metros\n" +
                    "posição atual:\n" +
                    "Lat: " + posicaoAtual.getLatitude() + "\n" +
                    "Lon: " + posicaoAtual.getLongitude() + "\n" +
                    "Alt: " + posicaoAtual.getAltitude() + "m\n" +
                    "Ângulo: " + anguloBussola + "°");
        } else {
            Toast.makeText(this, "marque a posição inicial primeiro", Toast.LENGTH_SHORT).show();
        }
    }

    // Adicione estes métodos na sua MainActivity (que implementa LocationListener)
    @Override
    public void onProviderDisabled(String provider) {
        runOnUiThread(() -> {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, "GPS foi desativado. por favor, ative-o para continuar.", Toast.LENGTH_LONG).show();
                showGpsDisabledAlert();
            }
        });
    }

    @Override
    public void onProviderEnabled(String provider) {
        runOnUiThread(() -> {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, "GPS ativado. buscando localização...", Toast.LENGTH_SHORT).show();
                iniciarAtualizacoesLocalizacao();
            }
        });
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

        Log.d(TAG, "status do provedor " + provider + " mudou para: " + status);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarAtualizacoesLocalizacao();
            } else {
                Toast.makeText(this, "permissão de localização é necessária", Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void iniciarAtualizacoesLocalizacao() {
        try {
            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (manager == null) {
                throw new IllegalStateException("LocationManager não disponível");
            }

            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showGpsDisabledAlert();
                return;
            }

            try {
                Location lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null && (System.currentTimeMillis() - lastLocation.getTime()) < 60000) {
                    onLocationChanged(lastLocation);
                }
            } catch (Exception e) {
                Log.w(TAG, "erro ao obter última localização", e);
            }

            //  atualizações
            manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,
                    1,
                    this,
                    Looper.getMainLooper()
            );

        } catch (Exception e) {
            Log.e(TAG, "erro ao iniciar GPS", e);
            runOnUiThread(() -> {
                new AlertDialog.Builder(this)
                        .setTitle("erro")
                        .setMessage("erro ao acessar GPS: " + e.getMessage())
                        .setPositiveButton("OK", null)
                        .show();
            });
        }
    }
    private void showGpsDisabledAlert() {
        new AlertDialog.Builder(this)
                .setTitle("GPS Desativado")
                .setMessage("para usar este aplicativo, ative o GPS nas configurações. ")
                .setPositiveButton("configurações", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("cancelar", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onLocationChanged(Location location) {
        try {
            if (location != null) {
                posicaoAtual = location;
                runOnUiThread(() -> {
                    tvInfo.setText("posição atual:\n" +
                            "Latitude: " + location.getLatitude() + "\n" +
                            "Longitude: " + location.getLongitude() + "\n" +
                            "Altitude: " + location.getAltitude() + "m\n" +
                            "Ângulo: " + anguloBussola + "°");
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "erro ao atualizar localização", e);
            runOnUiThread(() ->
                    Toast.makeText(this, "erro ao processar localização", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.length);
        }
        atualizarAnguloBussola();
    }

    private void atualizarAnguloBussola() {
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles);
            float azimuthInRadians = orientationAngles[0];
            float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);
            anguloBussola = (azimuthInDegrees + 360) % 360;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Não implementado
    }

    @Override
    protected void onResume() {
        super.onResume();
        registrarSensores();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            iniciarAtualizacoesLocalizacao();
        }
    }

    private void registrarSensores() {
        if (magnetometer != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(this);
            } catch (SecurityException e) {
                Log.e(TAG, "erro de segurança ao remover atualizações", e);
            }
        }
    }
}