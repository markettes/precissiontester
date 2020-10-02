package com.marcostfg.precision_tester;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.w3c.dom.Text;

import java.util.ArrayList;

import static com.marcostfg.precision_tester.CalculosComunes.average;
import static com.marcostfg.precision_tester.CalculosComunes.maxError;
import static com.marcostfg.precision_tester.CalculosComunes.minError;
import static com.marcostfg.precision_tester.CalculosComunes.secsFromStart;

public class FLActivity extends AppCompatActivity {

    TextView result;
    Button fiveTest;
    Button tenTest;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;
    ArrayList<Location> locations = new ArrayList<Location>();
    double[] distances;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fl);

        result = (TextView) findViewById(R.id.result);
        final EditText lat = (EditText) findViewById(R.id.lat);
        final EditText lon = (EditText) findViewById(R.id.lon);
        fiveTest = findViewById(R.id.fiveTest);
        tenTest = findViewById(R.id.tenTest);

        fiveTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tenTest.setEnabled(false);
                fiveTest.setEnabled(false);
                startTest(5, Double.valueOf(lat.getText().toString()), Double.valueOf(lon.getText().toString()));
            }
        });

        tenTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tenTest.setEnabled(false);
                fiveTest.setEnabled(false);
                startTest(10, Double.valueOf(lat.getText().toString()), Double.valueOf(lon.getText().toString()));
            }
        });

        //Location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                locations.add(locationResult.getLastLocation());
            }
        };
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

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
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);

                tenTest.setEnabled(true);
                fiveTest.setEnabled(true);

                //TODO
                distances = new double[locations.size()];
                for (int i = 0; i < secsFromStart(locations, seconds); i++) {
                    distances[i] = locations.get(i).distanceTo(location);
                }

                result.setText("Average: " + average(distances) + "\nMax error: " + maxError(distances) + "\nMin error: " + minError(distances));
                String str = "";

                for (Location location : locations) {
                    str += location.getAltitude() + " ";
                }

                Log.d("Altitudes", str);
            }
        }, (seconds + 5) * 1000);
    }


}
