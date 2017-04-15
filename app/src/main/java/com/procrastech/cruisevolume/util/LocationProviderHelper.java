package com.procrastech.cruisevolume.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.procrastech.cruisevolume.CruiseService;
import com.procrastech.cruisevolume.tabSettingsActivity;

/**
 * Created by Test on 14.04.2017.
 */

public class LocationProviderHelper implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,ResultCallback<LocationSettingsResult>{

    private static int updateInterval;
    private static boolean mConnectedToAPI = false;
    private static boolean startLocationUpdatesOnAPIConnected = false;
    protected static boolean updating = false;

    private static CruiseService cruiseService;
    private static Context context;
    static LocationRequest mLocationRequest;
    static GoogleApiClient mGoogleApiClient;
    private  LocationSettingsRequest mLocationSettingsRequest;

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
        mConnectedToAPI = false;
    }

    public void checkLocationSettings() {
        createLocationRequest();
        buildLocationSettingsRequest();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }


    public LocationProviderHelper(Context context, CruiseService cruiseService){
        setContext(context);
        LocationProviderHelper.cruiseService = cruiseService;
        createGoogleAPIClient();
        mGoogleApiClient.connect();
    }

    public static void setContext(Context context){
        LocationProviderHelper.context = context;
    }

    public static void startLocationUpdates(){
        if(!updating){
            if(mConnectedToAPI){
                requestLocationUpdates();
            }else{
                startLocationUpdatesOnAPIConnected = true;
            }
        }

    }



    public static void stopLocationUpdates(){
        if(mConnectedToAPI){
            removeLocationRequests();
        }
    }

    public static void setUpdateInterval(int updateInterval){

        if(LocationProviderHelper.updateInterval!=updateInterval){
            LocationProviderHelper.updateInterval = updateInterval;
            createLocationRequest();
            if(mConnectedToAPI&&CruiseService.updatingLocation){
                removeLocationRequests();
                requestLocationUpdates();
            }

        }
    }

    protected static void removeLocationRequests(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,cruiseService);

    }

    private void createGoogleAPIClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(cruiseService.getApplicationContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            Log.d("MY", "GoogleAPIClient built");
        }
    }

    private static void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(updateInterval);
        mLocationRequest.setFastestInterval(1);
        Log.d("MY", "Location request built");


    }

    protected static void requestLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,cruiseService);
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("MY", "Connected to API");
        mConnectedToAPI = true;
        if(startLocationUpdatesOnAPIConnected){
            requestLocationUpdates();
            startLocationUpdatesOnAPIConnected = false;
        }
    }

    public static void disconnect(){
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mConnectedToAPI = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i("LSR", "All location settings are satisfied.");
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i("LSR", "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");


                PendingIntent pI = status.getResolution();
                mGoogleApiClient.getContext().startActivity(new Intent(mGoogleApiClient.getContext(), tabSettingsActivity.class)
                        .putExtra("resolution", pI).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));

                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i("LSR", "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }
}
