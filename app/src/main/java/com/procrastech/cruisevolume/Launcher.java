package com.procrastech.cruisevolume;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class Launcher extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, Switch.OnCheckedChangeListener {

    private final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 1;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 2;
    boolean mBound = false;
    CruiseService mCruiseService;
    SeekBar speedBarOne;
    SeekBar speedBarTwo;
    TextView speedTextOne;
    TextView speedTextTwo;
    SeekBar volBarOne;
    SeekBar volBarTwo;
    SeekBar updateIntervalBar;
    TextView updateIntervalText;
    TextView volTextOne;
    TextView volTextTwo;
    Switch slowGainModeSwitch;
    Switch serviceSwitch;
    protected int mSpeedSetOne;
    protected int mSpeedSetTwo;
    protected int mSpeedSetTwoTotal;
    protected int mVolSetOne;
    protected int mVolSetTwo;
    protected boolean mSlowGainMode;
    protected int mUpdateInterval;
    protected int mUpdateIntervalProg;
    CruiseService.myBinder mBinder;

    public static final String PREFS_NAME = "CruisePrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        restorePreferences();

        checkForPermissions();
        checkPlayServices();

        initializeUI();


    }

    @Override
    protected void onStart(){
        super.onStart();
        initializeCruiseService();
    }

    @Override
    protected void onStop(){
        super.onStop();
        savePreferences();
        if(mBound){
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(isFinishing()){
            mCruiseService.cancelNotifications();
            stopService(new Intent(this,CruiseService.class));
        }

    }

    private void restorePreferences() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mSlowGainMode = settings.getBoolean("slowGainMode", true);
        mVolSetOne = settings.getInt("volSetOne",5);
        mVolSetTwo = settings.getInt("volSetTwo",10);
        mSpeedSetOne = settings.getInt("speedSetOne",50);
        mSpeedSetTwo = settings.getInt("speedSetTwo", 100);
        mSpeedSetTwoTotal = mSpeedSetOne + mSpeedSetTwo;
        mUpdateInterval = settings.getInt("updateInterval",1000);
        mUpdateIntervalProg = settings.getInt("updateIntervalProg",1);


    }

    private void savePreferences() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("slowGainMode", mSlowGainMode);
        editor.putInt("volSetOne", mVolSetOne);
        editor.putInt("volSetTwo", mVolSetTwo);
        editor.putInt("speedSetOne", mSpeedSetOne);
        editor.putInt("speedSetTwo", mSpeedSetTwo);
        editor.putInt("updateInterval",mUpdateInterval);
        editor.putInt("updateIntervalProg",mUpdateIntervalProg);

        editor.apply();
    }

    private void initializeCruiseService() {
        Intent intent = new Intent(this, CruiseService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void initializeUI() {
        speedBarOne = (SeekBar) findViewById(R.id.seekSpeedThrOne);
        speedTextOne = (TextView) findViewById(R.id.textSpeedThrOne);
        volBarOne = (SeekBar) findViewById(R.id.seekVolThrOne);
        volTextOne = (TextView) findViewById(R.id.textVolThrOne);
        speedBarTwo = (SeekBar) findViewById(R.id.seekSpeedThrTwo);
        speedTextTwo = (TextView) findViewById(R.id.textSpeedThrTwo);
        volBarTwo = (SeekBar) findViewById(R.id.seekVolThrTwo);
        volTextTwo = (TextView) findViewById(R.id.textVolThrTwo);
        slowGainModeSwitch = (Switch) findViewById(R.id.switchSlowGainMode);
        serviceSwitch = (Switch) findViewById(R.id.switchService);
        updateIntervalBar = (SeekBar) findViewById(R.id.seekUpdateInterval);
        updateIntervalText = (TextView) findViewById(R.id.textUpdateInterval);
        speedBarOne.setOnSeekBarChangeListener(this);
        speedBarTwo.setOnSeekBarChangeListener(this);
        volBarOne.setOnSeekBarChangeListener(this);
        volBarTwo.setOnSeekBarChangeListener(this);
        updateIntervalBar.setOnSeekBarChangeListener(this);
        slowGainModeSwitch.setOnCheckedChangeListener(this);
        slowGainModeSwitch.setChecked(mSlowGainMode);
        serviceSwitch.setOnCheckedChangeListener(this);
        serviceSwitch.setChecked(false);
        speedBarOne.setMax(100);
        speedBarTwo.setMax(200);
        updateIntervalBar.setProgress(mUpdateIntervalProg);
        speedBarOne.setProgress(mSpeedSetOne);
        speedBarTwo.setProgress(mSpeedSetTwo);
        volBarOne.setProgress(mVolSetOne);
        volBarTwo.setProgress(mVolSetTwo);

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
            mBinder = (CruiseService.myBinder) service;
            mCruiseService = mBinder.getService();
            mBound = true;
            commitValues();

            Log.d("MY","Connection established");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCruiseService = null;
            mBound = false;
            Log.d("MY","Connection lost");

        }
    };

    public boolean commitValues(){
        if(mBound){
            mCruiseService.startSpeed = mSpeedSetOne;
            mCruiseService.endSpeed = mSpeedSetTwoTotal;
            mCruiseService.startVol = mVolSetOne;
            mCruiseService.endVol = mVolSetTwo;
            mCruiseService.setSlowGainMode(mSlowGainMode);
            mCruiseService.createBoundaries();

            if(mCruiseService.mUpdateInterval!=mUpdateInterval){
                mCruiseService.setUpdateInterval(mUpdateInterval);
            }

            return true;
        }else{
            return false;
        }
    }

    public void updateUI(){


        speedTextOne.setText(mSpeedSetOne +" km/h");
        speedTextTwo.setText(mSpeedSetTwoTotal +" km/h");
        updateIntervalText.setText(mUpdateInterval+ " ms");
        volTextOne.setText(mVolSetOne+"");
        volTextTwo.setText(mVolSetTwo+"");


    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch(seekBar.getId()){
            case    R.id.seekSpeedThrOne :
                mSpeedSetOne = progress;
                break;
            case    R.id.seekSpeedThrTwo :
                mSpeedSetTwo = progress;
                break;
            case    R.id.seekVolThrOne :
                if(progress<mVolSetTwo){
                    mVolSetOne = progress;
                }else{
                    seekBar.setProgress(mVolSetTwo - 1);
                }
                break;
            case    R.id.seekVolThrTwo :
                if(progress>mVolSetOne){
                    mVolSetTwo = progress;
                }else{
                    seekBar.setProgress(mVolSetOne + 1);
                }
                break;
            case    R.id.seekUpdateInterval :
                mUpdateIntervalProg = progress;
                break;
        }
        mSpeedSetTwoTotal = mSpeedSetOne + mSpeedSetTwo;
        if(mUpdateIntervalProg==0){
            mUpdateInterval = 1;
        }else{
            mUpdateInterval = mUpdateIntervalProg*1000;
        }
        updateUI();
        commitValues();


    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.switchSlowGainMode:
                mSlowGainMode = isChecked;
                break;
            case R.id.switchService:
                if(mBound){
                    if(isChecked){
                        mCruiseService.startLocationUpdates();
                    }else{
                        mCruiseService.stopLocationUpdates();
                    }
                }
                break;
        }
    }
}
