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
    TextView volTextOne;
    TextView volTextTwo;
    Switch slowGainModeSwitch;
    protected int mSpeedSetOne;
    protected int mSpeedSetTwo;
    protected int mSpeedSetTwoTotal;
    protected int mVolSetOne;
    protected int mVolSetTwo;
    protected boolean mSlowGainMode;
    CruiseService.myBinder mBinder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        checkForPermissions();
        checkPlayServices();

        initializePreferences();
        initializeCruiseService();
        initializeUI();

    }

    private void initializePreferences() {
        //TODO: Import from local file
        mSpeedSetOne = 50;
        mSpeedSetTwo = 100;
        mSpeedSetTwoTotal = mSpeedSetOne + mSpeedSetTwo;
        mVolSetOne = 5;
        mVolSetTwo = 10;
    }

    private void initializeCruiseService() {
        if(!mBound) {
            Intent intent = new Intent(this, CruiseService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
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
        speedBarOne.setOnSeekBarChangeListener(this);
        speedBarTwo.setOnSeekBarChangeListener(this);
        volBarOne.setOnSeekBarChangeListener(this);
        volBarTwo.setOnSeekBarChangeListener(this);
        slowGainModeSwitch.setOnCheckedChangeListener(this);

        speedBarOne.setMax(100);
        speedBarTwo.setMax(200);

        //speedBarOne.setProgress(mSpeedSetOne);
        speedBarTwo.setProgress(100);


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
            mBinder = (CruiseService.myBinder) service;
            mCruiseService = mBinder.getService();
            mBound = true;

            Log.d("MY","Connection established");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCruiseService = null;
            mBound = false;
            Log.d("MY","Connection lost");

        }
    };

    public void onLauncherServiceButtonPressed(View view){
        if(mBound){
            mCruiseService.startLocationUpdates();
            if(mCruiseService.volarr==null||mCruiseService.boundarr==null){
                commitValues();
            }
        }

    }

    public void onSuspendServiceButtonPressed(View view){
        if(mBound){
            mCruiseService.stopLocationUpdates();
        }
    }

    public void onApplyServiceButtonPressed(View view){
        commitValues();
    }

    @Override
    public void onPause(){
        super.onPause();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public boolean commitValues(){
        if(mBound){
            mCruiseService.startSpeed = mSpeedSetOne;
            mCruiseService.endSpeed = mSpeedSetTwoTotal;
            mCruiseService.startVol = mVolSetOne;
            mCruiseService.endVol = mVolSetTwo;
            mCruiseService.setSlowGainMode(mSlowGainMode);
            mCruiseService.createBoundaries();

            return true;
        }else{
            return false;
        }
    }

    public void updateUI(){


        speedTextOne.setText(mSpeedSetOne +" km/h");
        speedTextTwo.setText(mSpeedSetTwoTotal +" km/h");
        speedBarOne.setProgress(mSpeedSetOne);
        speedBarTwo.setProgress(mSpeedSetTwo);
        volTextOne.setText(mVolSetOne+"");
        volTextTwo.setText(mVolSetTwo+"");
        volBarOne.setProgress(mVolSetOne);
        volBarTwo.setProgress(mVolSetTwo);

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
        }
        mSpeedSetTwoTotal = mSpeedSetOne + mSpeedSetTwo;
        updateUI();

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBinder("CruiseServiceBinder",mBinder);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        mBinder = (CruiseService.myBinder) savedInstanceState.getBinder("CruiseServiceBinder");
        mCruiseService = mBinder.getService();
        Intent intent = new Intent(this, CruiseService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        if(mCruiseService!=null){
            mBound = true;
        }
        // Restore state members from saved instance
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mSlowGainMode = isChecked;
    }
}
