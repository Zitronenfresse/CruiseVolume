package com.procrastech.cruisevolume;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import static com.procrastech.cruisevolume.SettingsActivity.PREFS_NAME;

public class CruiseService extends Service implements com.google.android.gms.location.LocationListener,GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,SensorEventListener{
    private final myBinder mBinder = new myBinder();
    AudioManager mAudioManager;
    LocationRequest mLocationRequest;
    private double mSpeed;
    protected int mUpdateInterval;
    protected int cachedUpdateInterval = -1;
    protected int quickUpdateCounter = 0;
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
    protected static boolean updatingLocation;
    protected int[] boundarr;
    protected int[] volarr;
    protected boolean slowGainMode;
    protected boolean accMode;
    private boolean mConnectedToAPI = false;
    public static final String ACTION_STOP_UPDATES = "com.procrastech.cruisevolume.ACTION_STOP_UPDATES";
    public static final String ACTION_STOP_SERVICE = "com.procrastech.cruisevolume.ACTION_STOP_SERVICE";
    public static final String ACTION_START_UPDATES = "com.procrastech.cruisevolume.ACTION_START_UPDATES";
    public static final String ACTION_UPDATE_PREFS = "com.procrastech.cruisevolume.ACTION_UPDATE_PREFS";
    private boolean startLocationUpdatesOnAPIConnected = false;

    private int accelerationThreshold = 5;

    private SensorManager senSensorManager;
    private Sensor senLinearAcceleration;

    //TODO: Profiles
    //TODO: Translate (at least to german)
    //TODO: Pause-mode when Location is not changing
    //TODO: Awareness API integration
    //TODO: Calculate Speed inbetween updates via Linear Acceleration and correct at update
    //TODO: Initial Wizard
    //TODO: Option for User Volume Input to disable VolControl


    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION&&updatingLocation) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            double a = Math.sqrt(x*x+y*y+z*z);

            if(a>accelerationThreshold){
                Log.d("ACCELERATION","threshold triggered " + a);
                Log.d("ACCELERATION","updating = "+updatingLocation+"  connectedToAPI = " +mConnectedToAPI+"  accmode = "+accMode+"  updateInterval = "+mUpdateInterval +  "  cachedUpdateInterval = "+ cachedUpdateInterval);
                requestQuickLocationUpdates();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void requestQuickLocationUpdates(){
        if(mConnectedToAPI&&updatingLocation&&accMode&&cachedUpdateInterval==-1&&mUpdateInterval>3000){
            cachedUpdateInterval = mUpdateInterval;
            mUpdateInterval = 1;
            quickUpdateCounter = 30;
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
            requestLocationUpdates();
            Log.d("Location","Requesting Quick Loc Updates");
        }
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        Log.d("myIntent","Onstartcommand called");
        super.onStartCommand(intent,flags,startId);
        if(intent!=null){
            handleIntent(intent);
        }
        startForeground(1,buildForegroundNotification());

        return START_REDELIVER_INTENT;
    }

    private void restorePreferences() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        slowGainMode = settings.getBoolean("slowGainMode", true);
        startVol = settings.getInt("volSetOne",5);
        endVol = settings.getInt("volSetTwo",10);
        startSpeed = settings.getInt("speedSetOne",50);
        endSpeed = settings.getInt("speedSetTwo", 99) + startSpeed;
        mUpdateInterval = settings.getInt("updateInterval",1000);
        accelerationThreshold = settings.getInt("accelerationThreshold",10);
        accMode = settings.getBoolean("accMode",false);

    }

    private void handleIntent(Intent intent) {
        String action = "";
        if(intent.getAction()!=null){
            action = intent.getAction();

        }


        Log.d("myIntent","Intent received, action is:" + action);

        switch (action){
            case ACTION_STOP_UPDATES :
                updatingLocation = false;

                stopLocationUpdates();
                break;
            case ACTION_START_UPDATES :
                updatingLocation = true;

                if(mConnectedToAPI){
                    startLocationUpdates();
                }else{
                    startLocationUpdatesOnAPIConnected = true;
                }
                break;
            case ACTION_STOP_SERVICE :
                if(mConnectedToAPI){
                    stopLocationUpdates();
                }
                stopForeground(true);
                stopSelf();
                break;
            case ACTION_UPDATE_PREFS :
                Boolean slowgain = intent.getBooleanExtra("slowGainMode",slowGainMode);
                Boolean accM = intent.getBooleanExtra("accMode",accMode);
                int startVolume = intent.getIntExtra("startVol",startVol);
                int endVolume = intent.getIntExtra("endVol",endVol);
                int startingSpeed = intent.getIntExtra("startSpeed",startSpeed);
                int endingSpeed = intent.getIntExtra("endSpeed",endSpeed);
                int updateInterval = intent.getIntExtra("mUpdateInterval",mUpdateInterval);
                int threshold = intent.getIntExtra("accelerationThreshold",accelerationThreshold);
                updateParams(slowgain,startingSpeed,endingSpeed,startVolume,endVolume,updateInterval,threshold,accM);
                createBoundaries();
                break;
            default:
        }
    }

    private void updateParams(boolean slowgain, int startspeed, int endspeed, int startVol, int endVol,int updateInterval,int accelerationThreshold, boolean accMode) {
        slowGainMode = slowgain;
        this.accMode = accMode;
        startSpeed = startspeed;
        endSpeed = endspeed;
        this.startVol = startVol;
        this.endVol = endVol;
        this.accelerationThreshold = accelerationThreshold;
        if(mConnectedToAPI&&updatingLocation&&mUpdateInterval!=updateInterval){
            mUpdateInterval = updateInterval;
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
            requestLocationUpdates();
        }else{
            mUpdateInterval = updateInterval;
        }
    }


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

        restorePreferences();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        createLocationRequest();
        createGoogleAPIClient();
        mGoogleApiClient.connect();
        initAccSensor();
    }

    private void initAccSensor() {
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senLinearAcceleration = senSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        senSensorManager.registerListener(this, senLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private Notification buildForegroundNotification(){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        mBuilder.setContentTitle("CruiseVolume");
        mBuilder.setSmallIcon(R.drawable.noticon);
        mBuilder.setOngoing(true);

        Intent stopTargetIntent = new Intent(this, CruiseService.class);
        stopTargetIntent.setAction(ACTION_STOP_SERVICE);
        PendingIntent stopIntent = PendingIntent.getService(this, 0, stopTargetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.drawable.ic_not_stop,"Stop",stopIntent);

        Intent pauseTargetIntent = new Intent(this, CruiseService.class);
        String pauseResume;
        if(updatingLocation){
            pauseResume = "Pause";
            pauseTargetIntent.setAction(ACTION_STOP_UPDATES);
            mBuilder.setContentText("is running");
        }else{
            pauseTargetIntent.setAction(ACTION_START_UPDATES);
            pauseResume = "Resume";
            mBuilder.setContentText("is paused");
        }
        PendingIntent pauseIntent = PendingIntent.getService(this, 0, pauseTargetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.drawable.ic_not_PAUSE,pauseResume,pauseIntent);

        Intent settingsTargetIntent = new Intent(this, SettingsActivity.class);
        PendingIntent settingsIntent = PendingIntent.getActivity(this, 0, settingsTargetIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.drawable.ic_not_settings,"Settings",settingsIntent);

        mBuilder.setPriority(Notification.PRIORITY_MAX);
        return mBuilder.build();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(mUpdateInterval);
        mLocationRequest.setFastestInterval(1);
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

    protected void requestLocationUpdates(){
        createLocationRequest();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,CruiseService.this);
    }

    protected void startLocationUpdates() {

        requestLocationUpdates();
        Log.d("LOCATION", "Starting Location updates");
        updatingLocation = true;
        if(boundarr==null||volarr==null){
            createBoundaries();
        }

    }


    protected void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        updatingLocation = false;
        Log.d("LOCATION", "Stopping Location updates");

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("LOCATION","updating = "+updatingLocation+"  connectedToAPI = " +mConnectedToAPI+"  accmode = "+accMode+"  updateInterval = "+mUpdateInterval +  "  cachedUpdateInterval = "+ cachedUpdateInterval);

        if(cachedUpdateInterval!=-1){
            if(quickUpdateCounter > 0){
                quickUpdateCounter--;
            }else{
                mUpdateInterval = cachedUpdateInterval;
                cachedUpdateInterval = -1;
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
                requestLocationUpdates();
            }
        }

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
                    if(curVol<=volarr[0]){
                        initVol = curVol;
                    }else{
                        initVol = volarr[0];
                    }
                    Log.d("CONTROL", "VOL CONTROL set Initial Volume to "+initVol+".");
                }
                for(int i = boundarr.length-1; i >= 0;i--){
                    if(speed < boundarr[i]){
                        continue;
                    }
                    setVolume(volarr[i]);
                    Log.d("CONTROL", "VOL CONTROL set Goal Volume to "+ volarr[i]+" at " +speed+"km/h");
                    break;
                }
            }

            //TODO: Move updateVolume to own handler with adjustable update-frequency
            updateVolume();
        }
    }

    @Override public void onDestroy(){
        cancelNotifications();
        super.onDestroy();
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
        Log.d("CONTROL", "VOL CONTROL set Current Volume to "+ curVol);

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
        mConnectedToAPI = true;
        if(startLocationUpdatesOnAPIConnected){
            startLocationUpdates();
            startLocationUpdatesOnAPIConnected = false;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mConnectedToAPI = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onUnbind(Intent i){
        return true;
    }

    @Override
    public void onRebind(Intent i){
        super.onRebind(i);
    }

    public void cancelNotifications(){
        mNotificationManager.cancelAll();
    }



    class myBinder extends Binder {
        //TODO: Use Binder as Interface

        CruiseService getService(){
            return CruiseService.this;
        }
    }


}
