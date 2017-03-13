package com.procrastech.cruisevolume;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class CruiseService extends Service implements com.google.android.gms.location.LocationListener,GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{
    private final myBinder mBinder = new myBinder();
    AudioManager mAudioManager;
    LocationRequest mLocationRequest;
    private double mSpeed;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private double mBoundaryOne = 50;
    private double mBoundaryTwo = 100;
    private int mInitialVolume = -1;
    NotificationManager mNotificationManager;

    @Override
    public void onCreate(){
        super.onCreate();
        createLocationRequest();
        createGoogleAPIClient();
        mGoogleApiClient.connect();
        //showNotification();
    }

    public double getLastSpeed(){
        return mSpeed;
    }

    private void showNotification(){



        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        mBuilder.setContentTitle("Cruise Control");
        mBuilder.setContentText("Cruise Control is running");
        mBuilder.setSmallIcon(R.drawable.common_google_signin_btn_icon_dark);
        mBuilder.setOngoing(true);
        Intent targetIntent = new Intent(this, Launcher.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);

        mNotificationManager.notify(11, mBuilder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        return mBinder;
    }

    public void lowerVolume(){
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,0);
    }

    public void raiseVolume(){
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE,0);
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        Log.d("MY", "Location request built");


    }

    private void createGoogleAPIClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            Log.d("MY", "GoogleAPIClient built");
        }
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,CruiseService.this);
        Log.d("MY", "Starting Location updates");

    }

    protected void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
    }


    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        updateSpeed();
    }

    public void volumeControl(int speed){
        if( speed >= mBoundaryOne){
            if(mInitialVolume == -1 ){
                mInitialVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            }
            raiseVolume();
        }else{
            if(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > mInitialVolume && mInitialVolume != -1){
                lowerVolume();
            }else{
                mInitialVolume = -1;
            }
        }

    }

    private void updateSpeed() {
        if(mLastLocation.hasSpeed()){
            mSpeed =  ((mLastLocation.getSpeed()*3600)/1000);
            Log.d("MY","Speed is "+mSpeed);
            volumeControl((int)mSpeed);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("MY", "Connected to API");
        startLocationUpdates();


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onUnbind(Intent i){
        mNotificationManager.cancelAll();
        return false;
    }

    public void cancelNotifications(){
        mNotificationManager.cancelAll();
    }

    public class myBinder extends Binder {
        CruiseService getService(){
            return CruiseService.this;
        }
    }
}
