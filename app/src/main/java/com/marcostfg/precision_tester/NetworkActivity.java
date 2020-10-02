package com.marcostfg.precision_tester;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

import static com.marcostfg.precision_tester.CalculosComunes.average;
import static com.marcostfg.precision_tester.CalculosComunes.maxError;
import static com.marcostfg.precision_tester.CalculosComunes.minError;
import static com.marcostfg.precision_tester.CalculosComunes.secsFromStart;

public class NetworkActivity extends AppCompatActivity {
    TextView result;
    Button fiveTest;
    Button tenTest;
    ArrayList<Location> locations = new ArrayList<Location>();
    double[] distances;
    LocationManager locationManager;
    LocationListener locationListener;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        result = (TextView) findViewById(R.id.result);
        final EditText lat = (EditText) findViewById(R.id.lat);
        final EditText lon = (EditText) findViewById(R.id.lon);
        fiveTest = findViewById(R.id.fiveTest);
        tenTest = findViewById(R.id.tenTest);

        fiveTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tenTest.setActivated(false);
                fiveTest.setActivated(false);
                try {
                    startTest(5, Double.valueOf(lat.getText().toString()), Double.valueOf(lon.getText().toString()));
                } catch (Exception e) {
                    Toast.makeText(NetworkActivity.this, "Faltan los datos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tenTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tenTest.setActivated(false);
                fiveTest.setActivated(false);
                try {
                    startTest(10, Double.valueOf(lat.getText().toString()), Double.valueOf(lon.getText().toString()));
                } catch (Exception e) {
                    Toast.makeText(NetworkActivity.this, "Faltan los datos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Location
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locations.add(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, locationListener);

    }

    private void startTest(final int seconds, Double lat, Double lng) {
        locations.clear();

        final Location location = new Location("FusedLocation");
        location.setLatitude(lat);
        location.setLongitude(lng);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                locationManager.removeUpdates(locationListener);

                tenTest.setActivated(true);
                fiveTest.setActivated(true);

                //TODO
                distances = new double[locations.size()];
                for (int i=0; i<secsFromStart(locations, seconds);i++) {
                    distances[i] = locations.get(i).distanceTo(location);
                }

                result.setText("Average: " + average(distances) + "\nMax error: " + maxError(distances) + "\nMin error: " + minError(distances));
            }
        }, (seconds + 5) * 1000);
    }

}
