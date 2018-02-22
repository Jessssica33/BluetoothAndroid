package com.example.jessica.showlocation;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    private TextView latitude;
    private TextView longitude;
    private double latValue;
    private double lonValue;
    private LocationManager locationManager;
    private Location location;

    private boolean isGPSEnable = false;
    private boolean isNetworkEnable = false;

    private final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 1;

    private static final int MIN_DISTANCE_CHANGE_FOR_UPDATE = 5;   //5 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minutes

    private BluetoothServer mBluetoothServer;

    private static final String TAG = "MainActivity";

    //private Timer mTimer = new Timer("hexTronik Pair", true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitude = (TextView) findViewById(R.id.lat);
        longitude = (TextView) findViewById(R.id.lon);

        mBluetoothServer = new BluetoothServer(mHandler);
        mBluetoothServer.init();

        showLocationPermission();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        getLocation();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (isNetworkEnable) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATE, locationListener);
            }else if (isGPSEnable) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATE, locationListener);
            }
        }

        //resetPairedTimer();

    }

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch(msg.what) {
                case MessageConstants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.i(TAG, "Message read: " + readMessage);
                    break;
                case MessageConstants.MESSAGE_CONNECT:
                    Log.i(TAG, "Server --- client connect success.");
                    setPosition(location);
                    break;
                case MessageConstants.MESSAGE_WRITE:
                    /*byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf, 0, msg.arg1);
                    Log.i(TAG, "Message write: " + writeMessage);*/
                    break;
            }
        }
    };



    /*private class PairTask extends TimerTask {

        @Override
        public void run() {

            mBluetoothServer.write("xxx yyy".getBytes());
            Log.e("error", "=====send data aa out=====");
            //Toast.makeText(MainActivity.this, "aa", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetPairedTimer() {

        mTimer.purge();
        mTimer.cancel();
        mTimer = new Timer("hexTronik Pair", true);
        mTimer.schedule(new PairTask(), 1000, 5000);
    }*/

    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (isNetworkEnable) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATE, locationListener);

                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } else if (isGPSEnable) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATE, locationListener);

                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

                if (location != null) {
                setPosition(location);
            } else {
                Log.e("error", "======Location is null======");
            }

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            setPosition(location);
            Log.i("MainActivity:", "Reset position");
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            setPosition(location);
        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_FINE_LOCATION:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                   getLocation();
                }

        }
    }

    private void showLocationPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                showExplanation("Permission Needed", "Rationale",
                        Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_ACCESS_FINE_LOCATION);
            } else {

                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                        REQUEST_PERMISSION_ACCESS_FINE_LOCATION);
            }
        } else {
            Toast.makeText(MainActivity.this, "Permission already Granted!",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void showExplanation(String title, String message, final String permission,
                                 final int permissionRequestcode) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermission(permission, permissionRequestcode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permissionName},
                permissionRequestCode);
    }

    private void setPosition(Location location) {
        latValue = location.getLatitude();
        lonValue = location.getLongitude();

        latitude.setText(Double.toString(latValue));
        longitude.setText(Double.toString(lonValue));

        String s = Double.toString(latValue) + " " + Double.toString(lonValue);

        mBluetoothServer.write(s.getBytes());
        Toast.makeText(MainActivity.this,"set position", Toast.LENGTH_SHORT).show();
    }


}
