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
    protected int startSpeed;
    protected int endSpeed;
    protected int startVol;
    protected int endVol;
    protected int goalVol;
    protected int curVol;
    protected int initVol = -1;
    NotificationManager mNotificationManager;
    protected boolean updatingLocation = false;
    protected int[] boundarr;
    protected int[] volarr;
    protected boolean slowGainMode = true;


    //TODO: Add expansive logs (Done:VolumeControl)
    //TODO: Adjustable LocUpdate-frequency
    //TODO: API Level 3.0 Support
    //TODO: Pause-mode when Location is not changing
    //TODO: Awareness API integration
    //TODO: Acceleration-sensor input to trigger single Location-requests
    //TODO: Disclaimer
    //TODO: Local Preference Saves
    //TODO: Initial Wizard
    //TODO: Handle Exceptions and Weak Signal
    //TODO: Create App Logo/Icon and revise working title
    //TODO: Analyse memory- and data-usage
    //TODO: performance tests
    //TODO: Option for User Volume Input to disable VolControl

    protected void createBoundaries(){
        int number = endVol - startVol;
        boundarr = new int[number+1];
        volarr = new int[number+1];
        double speedInterval = (endSpeed - startSpeed)/(number);
        Log.d("CONTROL","Creating Speed/Vol Arrays");

        for(int i = 0; i < number+1; i++){
            volarr[i]=startVol+i;
            boundarr[i]=(int) (startSpeed+(speedInterval*i));

            Log.d("CONTROL","Volume level "+(i+startVol)+" at Speed " + boundarr[i]);

        }

    }

    @Override
    public void onCreate(){
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createLocationRequest();
        createGoogleAPIClient();
        mGoogleApiClient.connect();
    }

    private void showNotification(){


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        mBuilder.setContentTitle("Cruise Control");
        mBuilder.setContentText("Cruise Control is running");
        mBuilder.setSmallIcon(R.drawable.common_google_signin_btn_icon_dark);
        mBuilder.setOngoing(true);
        Intent targetIntent = new Intent(this, Launcher.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);

        mNotificationManager.notify(11, mBuilder.build());
        Log.d("NOTIFICATIONS", "Building Notification");

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        return mBinder;
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
        Log.d("LOCATION", "Starting Location updates");
        updatingLocation = true;
        showNotification();
        if(boundarr==null||volarr==null){
            createBoundaries();
        }


    }

    protected void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        updatingLocation = false;
        cancelNotifications();
        Log.d("LOCATION", "Stopping Location updates");

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        updateSpeed();
    }

    public void volumeControl(int speed){
        if(boundarr!=null&&volarr!=null){
            Log.d("CONTROL", "VOL CONTROL Cycle with speed "+speed+" :");
            curVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

            if(speed<boundarr[0]){
                if(initVol!=-1){
                    Log.d("CONTROL", "VOL CONTROL set Volume back to Initial Volume at "+initVol+".");
                    setVolume(initVol);
                    initVol = -1;
                }

            }else{
                if(initVol == -1){
                    initVol = curVol;
                    Log.d("CONTROL", "VOL CONTROL set Initial Volume to "+initVol+".");
                }
                for(int i = boundarr.length-1; i >= 0;i--){
                    if(speed < boundarr[i]){
                        continue;
                    }
                    setVolume(volarr[i]);
                    Log.d("CONTROL", "VOL CONTROL set Volume to "+ volarr[i]+" at " +speed+"km/h");
                    break;
                }
            }

            //TODO: Move updateVolume to own handler with adjustable update-frequency
            updateVolume();
        }
    }

    protected void setSlowGainMode(boolean x){
        slowGainMode = x;
    }

    private void incrementVol() {
        if(curVol<goalVol){
            curVol++;
        }else{
            if(curVol>goalVol){
                curVol--;
            }
        }
    }

    private void updateVolume() {
        if(slowGainMode){
            incrementVol();
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,curVol,0);
    }

    public void setVolume(int vol){
        if (slowGainMode) {
            goalVol = vol;
        }else{
            curVol = vol;
        }

    }

    private void updateSpeed() {
        if(mLastLocation.hasSpeed()){
            mSpeed =  ((mLastLocation.getSpeed()*3600)/1000);
            volumeControl((int)mSpeed);
            Log.d("SPEED","Speed is "+mSpeed);

        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("MY", "Connected to API");


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

    @Override
    public void onRebind(Intent i){
        super.onRebind(i);
    }

    public void cancelNotifications(){
        mNotificationManager.cancelAll();
        Log.d("NOTIFICATIONS", "Starting Location updates");

    }

    public class myBinder extends Binder {
        //TODO: Use Binder as Interface

        CruiseService getService(){
            return CruiseService.this;
        }
    }
}
