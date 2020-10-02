package com.marcostfg.precision_tester;

import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.accent_systems.ibks_sdk.scanner.ASBleScanner;
import com.nexenio.bleindoorpositioning.location.multilateration.Multilateration;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import static com.marcostfg.precision_tester.CalculosComunes.average;
import static com.marcostfg.precision_tester.CalculosComunes.maxError;
import static com.marcostfg.precision_tester.CalculosComunes.minError;

public class BleActivity extends AppCompatActivity {

    Paint pSol;
    Paint pBeacon;
    Paint pRange;
    Paint pAct;

    private final int TXPOWER = -54; //Media de señal a un metro
    private int seconds;

    private View view;
    private Bitmap map;
    double[] centroid;
    private ASBleScanner scanner;
    private ImageView imageMap;
    private TextView logTV;

    private double[] distances = new double[3];
    String resultLog = "";
    private ArrayList<Double> errors = new ArrayList<Double>();
    private int counter = 0;
    private double[] position = new double[]{580, 200};
    private final double beacons[][] = new double[][]{
            {368.0, 58.5}, {505.5, 41.5}, {445.5, 267.5} //50px = 1M
    };
    LeastSquaresOptimizer.Optimum optimum;

    private BluetoothLeScanner bluetoothLeScanner;
    ScanCallback scanCallback;
    ScanSettings settings;
    ArrayList<ScanFilter> filters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new MyView(this));

        seconds = getIntent().getIntExtra("seconds", 5);

        pSol = new Paint();
        pSol.setStyle(Paint.Style.FILL);
        pSol.setColor(Color.GREEN);

        pAct = new Paint();
        pAct.setStyle(Paint.Style.FILL);
        pAct.setColor(Color.WHITE);

        pBeacon = new Paint();
        pBeacon.setStyle(Paint.Style.FILL);
        pBeacon.setColor(Color.BLUE);

        pRange = new Paint();
        pRange.setStyle(Paint.Style.FILL);
        pRange.setColor(Color.RED);
        pRange.setAlpha(50);

        logTV = findViewById(R.id.logTV);

        //Bluetooth
        BluetoothManager mSensorBluetooth = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BleFiltersNSettings();
        inicializarScanner();
        bluetoothLeScanner = mSensorBluetooth.getAdapter().getBluetoothLeScanner();
        bluetoothLeScanner.startScan(filters, settings, scanCallback);



        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bluetoothLeScanner.stopScan(scanCallback);
                double[] arrayErrors = new double[errors.size()];
                counter = 0;
                for(double error : errors) {
                    arrayErrors[counter] = error;
                    resultLog += error + "\n";
                    counter++;
                }
                String result = "Average: " + average(arrayErrors) + "\nMax Error: " + maxError(arrayErrors) + "\nMin Error: " + minError(arrayErrors);
                writeToFile(resultLog);
                System.out.println(resultLog);
                System.out.println(result);
                Toast.makeText(getApplicationContext(),"Archivo Guardado", Toast.LENGTH_SHORT).show();
            }
        }, seconds*1000);

    }

    private void BleFiltersNSettings() {
        settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

        filters = new ArrayList<>();
        String[] filterList = {
                "C9:18:D4:BD:25:D8", "F3:CF:9B:BA:B3:2C", "EB:FA:E4:E1:CB:2E"
        };
        for (int i = 0; i < filterList.length; i++) {
            ScanFilter filter = new ScanFilter.Builder().setDeviceAddress(filterList[i]).build();
            filters.add(filter);
            Log.v("Filter: ", "" + filters.get(i));
        }
    }

    private void inicializarScanner() {
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                double rssi = result.getRssi();
                String address = result.getDevice().getAddress();
                switch (address) {
                    case "C9:18:D4:BD:25:D8":
                        distances[0] = getDistance(rssi);
                        Log.i("beacon1", " " + rssi);
                        break;
                    case "F3:CF:9B:BA:B3:2C":
                        distances[1] = getDistance(rssi);
                        Log.i("beacon2", " " + rssi);
                        break;
                    case "EB:FA:E4:E1:CB:2E":
                        distances[2] = getDistance(rssi);
                        Log.i("beacon3", " " + rssi);
                        break;
                }


                if(address.equals("C9:18:D4:BD:25:D8") || address.equals("F3:CF:9B:BA:B3:2C")
                        || address.equals("EB:FA:E4:E1:CB:2E")) {
                        //Calculus position

                        optimum = Multilateration.findOptimum(beacons, distances);

                        // the answer
                        centroid = optimum.getPoint().toArray();
                        errors.add(Math.sqrt(Math.pow(centroid[0] - position[0], 2) + Math.pow(centroid[1] - position[1], 2)) / 50);
                        View myView = new MyView(BleActivity.this);
                        setContentView(myView);
                }
            }
        };
    }

    private double getDistance(double rssi) {
        double ratio = rssi / TXPOWER;
        if (ratio < 1.0) return Math.pow(ratio, 10);
        return (0.89976 * Math.pow(ratio, 7.7095) + 0.111) * 50;
    }

    private void writeToFile(String data) {
        try {
            FileOutputStream stream = openFileOutput("data.txt", MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(stream);
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (Exception e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private class MyView extends View {

        Bitmap map;

        public MyView(Context context) {
            super(context);
            try {
                map = BitmapFactory.decodeStream(getAssets().open("planoCasa.png"));
                map.isMutable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(map.getWidth(), map.getHeight());
        }

        @Override
        protected void onDraw(Canvas cv) {
            super.onDraw(cv);
            cv.drawBitmap(map, null, (Rect) new Rect(0, 0, map.getWidth(), map.getHeight()), null);


            if (centroid != null) {
                for (int i = 0; i < 3; i++) {
                    cv.drawCircle((float) beacons[i][0], (float) beacons[i][1], (float) distances[i], pRange);
                    cv.drawCircle((float) beacons[i][0], (float) beacons[i][1], 5, pBeacon);
                }
                cv.drawCircle((float) centroid[0], (float) centroid[1], 20, pSol);
                cv.drawCircle((float) position[0], (float) position[1], 20, pAct);
            }
        }
    }
}
