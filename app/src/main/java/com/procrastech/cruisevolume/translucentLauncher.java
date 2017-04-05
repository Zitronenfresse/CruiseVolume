package com.procrastech.cruisevolume;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


public class translucentLauncher extends AppCompatActivity {

    private final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 1;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 2;
    private static String action = "";
    private static boolean requestCalledBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (intent.hasExtra("news_only")) {
            finish();
        }
        if (intent.hasExtra("click_action")) {
            ClickActionHelper.startActivity(intent.getStringExtra("click_action"), intent.getExtras(), this);
            finish();
        }else{
            action = "";
            if(intent.getAction()!=null){
                action = intent.getAction();
            }

            if(checkPlayServices()){
                if(checkForPermissions()){
                    handleIntent();
                };
            }
        }







    }

    private void handleIntent(){
        Log.d("Intent", "Intentaction is "+action+".");




        switch (action){
            case "" :
                initializeCruiseService();
                break;
            case Intent.ACTION_MAIN :
                initializeCruiseService();
                break;
            default:
        }
        finish();
    }

    private void initializeCruiseService() {
        Intent intent = new Intent(this, CruiseService.class);
        intent.setAction(CruiseService.ACTION_START_UPDATES);
        startService(intent);
    }
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                requestCalledBack = true;

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MY", "Permissions granted");
                    handleIntent();


                } else {
                    finish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
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

    protected boolean checkForPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(translucentLauncher.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
            return false;
        } else {
            Log.d("MY", "Permissions already granted");
            return true;
        }
    }




}
