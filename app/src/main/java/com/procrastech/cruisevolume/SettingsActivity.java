package com.procrastech.cruisevolume;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, Switch.OnCheckedChangeListener  {

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
    protected int mSpeedSetOne;
    protected int mSpeedSetTwo;
    protected int mSpeedSetTwoTotal;
    protected int mVolSetOne;
    protected int mVolSetTwo;
    protected boolean mSlowGainMode;
    protected int mUpdateInterval;
    protected int mUpdateIntervalProg;
    ImageView rotatingLogoView;
    AnimationDrawable rotatingLogoAnimation;

    public static final String PREFS_NAME = "CruisePrefsFile";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        restorePreferences();
        initializeUI();


    }


    private void restorePreferences() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mSlowGainMode = settings.getBoolean("slowGainMode", true);
        mVolSetOne = settings.getInt("volSetOne",5);
        mVolSetTwo = settings.getInt("volSetTwo",10);
        mSpeedSetOne = settings.getInt("speedSetOne",50);
        mSpeedSetTwo = settings.getInt("speedSetTwo", 99);
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

    private void initializeUI() {
        rotatingLogoView = (ImageView) findViewById(R.id.rotatingLogoView);
        rotatingLogoAnimation = (AnimationDrawable) rotatingLogoView.getDrawable();
        rotatingLogoAnimation.start();


        speedBarOne = (SeekBar) findViewById(R.id.seekSpeedThrOne);
        speedTextOne = (TextView) findViewById(R.id.textSpeedThrOne);
        volBarOne = (SeekBar) findViewById(R.id.seekVolThrOne);
        volTextOne = (TextView) findViewById(R.id.textVolThrOne);
        speedBarTwo = (SeekBar) findViewById(R.id.seekSpeedThrTwo);
        speedTextTwo = (TextView) findViewById(R.id.textSpeedThrTwo);
        volBarTwo = (SeekBar) findViewById(R.id.seekVolThrTwo);
        volTextTwo = (TextView) findViewById(R.id.textVolThrTwo);
        slowGainModeSwitch = (Switch) findViewById(R.id.switchSlowGainMode);
        updateIntervalBar = (SeekBar) findViewById(R.id.seekUpdateInterval);
        updateIntervalText = (TextView) findViewById(R.id.textUpdateInterval);
        speedBarOne.setOnSeekBarChangeListener(this);
        speedBarTwo.setOnSeekBarChangeListener(this);
        volBarOne.setOnSeekBarChangeListener(this);
        volBarTwo.setOnSeekBarChangeListener(this);
        updateIntervalBar.setOnSeekBarChangeListener(this);
        slowGainModeSwitch.setOnCheckedChangeListener(this);
        slowGainModeSwitch.setChecked(mSlowGainMode);
        speedBarOne.setMax(100);
        speedBarTwo.setMax(100);



        updateIntervalBar.setProgress(mUpdateIntervalProg);
        speedBarOne.setProgress(mSpeedSetOne);
        speedBarTwo.setProgress(mSpeedSetTwo);
        volBarOne.setProgress(mVolSetOne);
        volBarTwo.setProgress(mVolSetTwo);

    }

    public void updateUI(){


        speedTextOne.setText(mSpeedSetOne +" km/h");
        speedTextTwo.setText(mSpeedSetTwoTotal +" km/h");
        updateIntervalText.setText(mUpdateInterval+ " ms");
        volTextOne.setText(mVolSetOne+"");
        volTextTwo.setText(mVolSetTwo+"");


    }



    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        rotatingLogoAnimation.start();


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
        savePreferences();
        Intent startServiceIntent = new Intent(this,CruiseService.class);
        startServiceIntent.setAction(CruiseService.ACTION_UPDATE_PREFS);
        startServiceIntent.putExtra("slowGainMode",mSlowGainMode);
        startServiceIntent.putExtra("startSpeed",mSpeedSetOne);
        startServiceIntent.putExtra("endSpeed",mSpeedSetTwoTotal);
        startServiceIntent.putExtra("startVol",mVolSetOne);
        startServiceIntent.putExtra("endVol",mVolSetTwo);
        startServiceIntent.putExtra("mUpdateInterval",mUpdateInterval);
        startService(startServiceIntent);


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

        }
    }


}
