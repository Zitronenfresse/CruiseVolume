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

    private int updateInterval;
    private boolean mConnectedToAPI = false;
    private boolean startLocationUpdatesOnAPIConnected = false;

    private CruiseService cruiseService;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private final Context c;
    private  LocationSettingsRequest mLocationSettingsRequest;

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
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

        c = context;
        this.cruiseService = cruiseService;
        createGoogleAPIClient();

    }



    public void startLocationUpdates(){
        Log.d("TESTONE","start Locationupdates call from service");
            if(mGoogleApiClient.isConnected()){
                Log.d("TESTONE","is connected to API and requesting loc updates");

                requestLocationUpdates();
            }else{
                Log.d("TESTONE","is not connected to API and setting startOnApiConnected");

                startLocationUpdatesOnAPIConnected = true;
            }

    }



    public void stopLocationUpdates(){
        if(mGoogleApiClient.isConnected()){
            removeLocationRequests();
        }
    }

    public void setUpdateInterval(int updateInterval){

        if(this.updateInterval!=updateInterval){
            this.updateInterval = updateInterval;
            createLocationRequest();
            if(mGoogleApiClient.isConnected()&&cruiseService.updatingLocation){
                removeLocationRequests();
                requestLocationUpdates();
            }

        }
    }

    private void removeLocationRequests(){
        if(mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,cruiseService);
        }

    }

    private void createGoogleAPIClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(c)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            Log.d("MY", "GoogleAPIClient built");
        }
        mGoogleApiClient.connect();
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(updateInterval);
        mLocationRequest.setFastestInterval(1);
        Log.d("MY", "Location request built");


    }

    private void requestLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(c, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

        }
        Log.d("TESTONE","requesting Location updates for cruise service");
        if(mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,cruiseService);
        }
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("MY", "Connected to API");
        mConnectedToAPI = true;
        Log.d("TESTONE","is connected to API");

        if(startLocationUpdatesOnAPIConnected){
            Log.d("TESTONE","is connected to API and requesting loc updates onConnected");

            requestLocationUpdates();
            startLocationUpdatesOnAPIConnected = false;
        }
    }

    public void disconnect(){
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mConnectedToAPI = false;
        Log.d("TESTONE","is not connected to API anymore");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("TESTONE","is not connected to API , failed");

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
