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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import static com.procrastech.cruisevolume.tabSettingsActivity.ACTION_STOP_SETTINGS;
import static com.procrastech.cruisevolume.tabSettingsActivity.KEY_ACTIVE_PROFILE_NUMBER;
import static com.procrastech.cruisevolume.tabSettingsActivity.KEY_MODE_PREFS;
import static com.procrastech.cruisevolume.tabSettingsActivity.KEY_PROFILE_PREFS;

public class CruiseService extends Service implements com.google.android.gms.location.LocationListener,GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,SensorEventListener{

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
    private boolean startLocationUpdatesOnAPIConnected = false;

    private Handler handler;
    private long mVolumeUpdateDelay = 1000;

    SharedPreferences mode_prefs;
    SharedPreferences profile_prefs;
    private int active_profile_number;

    SharedPreferences.OnSharedPreferenceChangeListener profile_changed_listener;
    SharedPreferences.OnSharedPreferenceChangeListener mode_changed_listener;

    private int accelerationThreshold = 5;

    private SensorManager senSensorManager;
    private Sensor senLinearAcceleration;


    //TODO: Pause-mode when Location is not changing - NOT IMPORTANT
    //TODO: Awareness API integration- NOT IMPORTANT
    //TODO: Calculate Speed inbetween updates via Linear Acceleration and correct at update- NOT IMPORTANT BUT COOL


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
        if(mConnectedToAPI&&updatingLocation&&accMode&&cachedUpdateInterval==-1&&mUpdateInterval>4000){
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
        initSharedPreferences();

        return START_REDELIVER_INTENT;
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
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_STOP_SETTINGS));
                break;
            default:
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
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        createLocationRequest();
        createGoogleAPIClient();
        mGoogleApiClient.connect();
        initAccSensor();
    }

    private void initSharedPreferences(){
        mode_changed_listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                active_profile_number = mode_prefs.getInt(KEY_ACTIVE_PROFILE_NUMBER,1);
                profile_prefs.unregisterOnSharedPreferenceChangeListener(profile_changed_listener);
                profile_prefs = getSharedPreferences(KEY_PROFILE_PREFS+active_profile_number,0);
                profile_prefs.registerOnSharedPreferenceChangeListener(profile_changed_listener);
                loadProfilePreferences();
            }
        };

        profile_changed_listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                loadProfilePreferences();
            }
        };


        mode_prefs = getSharedPreferences(KEY_MODE_PREFS,0);
        active_profile_number = mode_prefs.getInt(KEY_ACTIVE_PROFILE_NUMBER,1);
        mode_prefs.registerOnSharedPreferenceChangeListener(mode_changed_listener);

        profile_prefs = getSharedPreferences(KEY_PROFILE_PREFS+active_profile_number,0);
        profile_prefs.registerOnSharedPreferenceChangeListener(profile_changed_listener);
        loadProfilePreferences();
    }

    private void loadProfilePreferences() {

        Log.d("PREF",KEY_PROFILE_PREFS+active_profile_number+" loaded in Service");

        slowGainMode = profile_prefs.getBoolean("slowGainMode", true);
        startVol = profile_prefs.getInt("volSetOne",5);
        endVol = profile_prefs.getInt("volSetTwo",10);
        startSpeed = profile_prefs.getInt("speedSetOne",50);
        endSpeed = profile_prefs.getInt("speedSetTwo", 99) + startSpeed;
        int updateInterval = profile_prefs.getInt("updateInterval",1000);
        accelerationThreshold = profile_prefs.getInt("accelerationThreshold",10);
        accMode = profile_prefs.getBoolean("accMode",false);
        if(mUpdateInterval!=updateInterval){
            if(mConnectedToAPI&&updatingLocation){
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
            }
            mUpdateInterval = updateInterval;
            cachedUpdateInterval = -1;
            if(mConnectedToAPI&&updatingLocation){
                requestLocationUpdates();
            }

        }
        createBoundaries();

    }

    private void initAccSensor() {
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senLinearAcceleration = senSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        senSensorManager.registerListener(this, senLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private Notification buildForegroundNotification(){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        mBuilder.setContentTitle(getString(R.string.notification_title_shortappname_text));
        mBuilder.setSmallIcon(R.drawable.noticon);
        mBuilder.setOngoing(true);
        if( Build.VERSION.SDK_INT < 23){
            mBuilder.setColor(ContextCompat.getColor(getBaseContext(), R.color.color_not_background));
        }else{
            mBuilder.setColor(getResources().getColor(R.color.color_not_background,getTheme()));
        }
        Intent stopTargetIntent = new Intent(this, CruiseService.class);
        stopTargetIntent.setAction(ACTION_STOP_SERVICE);
        PendingIntent stopIntent = PendingIntent.getService(this, 0, stopTargetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action stopaction = new NotificationCompat.Action.Builder(R.drawable.ic_not_stop, getString(R.string.notification_action_stop_text), stopIntent).build();
        mBuilder.addAction(stopaction);

        Intent pauseTargetIntent = new Intent(this, CruiseService.class);
        String pauseResume;
        if(updatingLocation){
            pauseResume = getString(R.string.notification_action_pause_text);
            pauseTargetIntent.setAction(ACTION_STOP_UPDATES);
            mBuilder.setContentText(getString(R.string.notification_content_is_running_text));
        }else{
            pauseTargetIntent.setAction(ACTION_START_UPDATES);
            pauseResume = getString(R.string.notification_action_resume_text);
            mBuilder.setContentText(getString(R.string.notification_content_is_paused_text));
        }
        PendingIntent pauseIntent = PendingIntent.getService(this, 0, pauseTargetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action pauseaction = new NotificationCompat.Action.Builder(R.drawable.ic_not_pause, pauseResume, pauseIntent).build();
        mBuilder.addAction(pauseaction);

        Intent tabsettingsTargetIntent = new Intent(this, tabSettingsActivity.class);
        PendingIntent tabsettingsIntent = PendingIntent.getActivity(this, 0, tabsettingsTargetIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action settingsaction = new NotificationCompat.Action.Builder(R.drawable.ic_not_settings, getString(R.string.notification_action_settings_text), tabsettingsIntent).build();
        mBuilder.addAction(settingsaction);

        mBuilder.setPriority(Notification.PRIORITY_MAX);
        return mBuilder.build();
    }

    private Runnable runnableVolAdjuster = new Runnable() {
        @Override
        public void run() {
      /* do what you need to do */
            updateVolume();
      /* and here comes the "trick" */
            handler.postDelayed(this, mVolumeUpdateDelay);
        }
    };

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

        handler = new Handler();
        handler.postDelayed(runnableVolAdjuster, mVolumeUpdateDelay);

        requestLocationUpdates();
        Log.d("LOCATION", "Starting Location updates");
        updatingLocation = true;
        if(boundarr==null||volarr==null){
            createBoundaries();
        }

    }

    protected void stopLocationUpdates(){



        handler.removeCallbacks(runnableVolAdjuster);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        updatingLocation = false;
        Log.d("LOCATION", "Stopping Location updates");

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("LOCATION","updating = "+updatingLocation+"  connectedToAPI = " +mConnectedToAPI+"  accmode = "+accMode+"  updateInterval = "+mUpdateInterval +  "  cachedUpdateInterval = "+ cachedUpdateInterval);
        Log.d("PREF",active_profile_number+" active profile number");

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
            //Log.d("CONTROL", "VOL CONTROL Cycle with speed "+speed+" :");
            curVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

            if(speed>=boundarr[0]){


                for(int i = boundarr.length-1; i >= 0;i--){
                    if(speed < boundarr[i]){
                        continue;
                    }
                    setVolume(volarr[i]);
                    Log.d("CONTROL", "VOL CONTROL set Goal Volume to "+ volarr[i]+" at " +speed+"km/h");
                    break;
                }
            }
        }
    }

    @Override public void onDestroy(){
        cancelNotifications();
        mode_prefs.unregisterOnSharedPreferenceChangeListener(mode_changed_listener);
        profile_prefs.unregisterOnSharedPreferenceChangeListener(profile_changed_listener);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
        if(boundarr[0]<=mSpeed){
            if(slowGainMode){
                incrementVol();
            }

            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,curVol,0);
            Log.d("CONTROL", "VOL CONTROL set Current Volume to "+ curVol);


        }

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




}
