package com.procrastech.cruisevolume;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class Launcher extends AppCompatActivity {

    private final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 1;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 2;
    boolean mBound = false;
    CruiseService mCruiseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        checkForPermissions();
        checkPlayServices();

    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d("MY", "Permissions granted");


                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    protected void checkForPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Launcher.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
        } else {
            Log.d("MY", "Permissions already granted");
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CruiseService.myBinder mBinder = (CruiseService.myBinder) service;
            mCruiseService = mBinder.getService();
            mBound = true;
            Log.d("MY","Connection established");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCruiseService = null;
            mBound = false;
        }
    };

    public void onLauncherServiceButtonPressed(View view){
        if(!mBound){
            Intent intent = new Intent(this,CruiseService.class);
            bindService(intent,mConnection, Context.BIND_AUTO_CREATE);

        }else{

        }

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mBound)
            mCruiseService.cancelNotifications();
    }
}
