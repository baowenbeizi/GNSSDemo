package com.example.gnssdemo;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private TextView mTvLatitude, mTvLongitude, mTvSatelliteCount, mTvBDSCount, mTvBDSInFixCount;
    private int satelliteCount = 0, BDSatelliteCount = 0, BDSInFix = 0;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        // get location permission again
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return;
        }
        // get locationManager
        LocationManager mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        insureGPSisOpen(mLocationManager);
        // register locationListener
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000, 0,locationListener);
        // register Gnss.Callback
        mLocationManager.registerGnssStatusCallback(mGNSSCallback);
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        // callback func when the location changed
        public void onLocationChanged(@NonNull Location location) {
            mTvLatitude.setText("纬度：" + location.getLatitude());
            mTvLongitude.setText("经度：" + location.getLongitude());
            Log.d(TAG, "onLocationChanged");
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    GnssStatus.Callback mGNSSCallback = new GnssStatus.Callback() {
        @Override
        public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
            super.onSatelliteStatusChanged(status);
            // reset the counts
            BDSatelliteCount = 0;
            BDSInFix = 0;
            // get satellite count
            satelliteCount = status.getSatelliteCount();
            mTvSatelliteCount.setText("共收到卫星信号：" + satelliteCount + "个");
            // traverse satellite data
            if(satelliteCount > 0) {
                for (int i = 0; i < satelliteCount; i++) {
                    // get satellite type
                    int type = status.getConstellationType(i);
                    if(GnssStatus.CONSTELLATION_BEIDOU == type) {
                        // increase if type == BEIDOU
                        BDSatelliteCount++;
                        if (status.usedInFix(i)) {
                            // increase if the satellite used in localization
                            BDSInFix++;
                        }
                    }
                }
                mTvBDSCount.setText("北斗卫星信号个数：" + BDSatelliteCount + "个");
                mTvBDSInFixCount.setText("用于定位的北斗卫星个数：" + BDSInFix +"个");
            }
        }
    };

    /**
     * insure GPS is open
     * */
    void insureGPSisOpen(LocationManager locationManager) {
        // judge whether the GPS is open
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getApplicationContext(), "请开启GPS导航...", Toast.LENGTH_SHORT).show();
            // turn to the GPS setting page
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }
        Log.d(TAG, "insureGPSisOpen");
        return;
    }

    private void init() {
        mTvLatitude = findViewById(R.id.tv_main_latitude);
        mTvLongitude = findViewById(R.id.tv_main_longitude);
        mTvSatelliteCount = findViewById(R.id.tv_main_count);
        mTvBDSCount = findViewById(R.id.tv_main_bd_count);
        mTvBDSInFixCount = findViewById(R.id.tv_main_bdInFix_count);
    }

}