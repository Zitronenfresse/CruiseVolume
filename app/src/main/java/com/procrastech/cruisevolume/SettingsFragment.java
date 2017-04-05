package com.procrastech.cruisevolume;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;


/**
 * Created by IEnteramine on 04.04.2017.
 */

public class SettingsFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, Switch.OnCheckedChangeListener{



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
    Switch accModeSwitch;
    TextView thrText;
    SeekBar thrBar;
    protected int mSpeedSetOne;
    protected int mSpeedSetTwo;
    protected int mSpeedSetTwoTotal;
    protected int mVolSetOne;
    protected int mVolSetTwo;
    protected boolean mSlowGainMode;
    protected boolean accMode;
    protected int mUpdateInterval;
    protected int mUpdateIntervalProg;
    protected int accelerationThreshold;
    protected int accelerationThresholdProg;
    ImageView rotatingLogoView;
    AnimationDrawable rotatingLogoAnimation;

    public static final String PREFS_NAME = "CruisePrefsFile";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, null);
        getAllWidgets(view);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restorePreferences();

    }

    private void getAllWidgets(View view) {
        rotatingLogoView = (ImageView) view.findViewById(R.id.rotatingLogoView);
        rotatingLogoAnimation = (AnimationDrawable) rotatingLogoView.getDrawable();
        rotatingLogoAnimation.start();
        thrBar = (SeekBar) view.findViewById(R.id.seekThreshold);
        thrText = (TextView) view.findViewById(R.id.textThreshold);
        accModeSwitch = (Switch) view.findViewById(R.id.switchAccMode);
        speedBarOne = (SeekBar) view.findViewById(R.id.seekSpeedThrOne);
        speedTextOne = (TextView) view.findViewById(R.id.textSpeedThrOne);
        volBarOne = (SeekBar) view.findViewById(R.id.seekVolThrOne);
        volTextOne = (TextView) view.findViewById(R.id.textVolThrOne);
        speedBarTwo = (SeekBar) view.findViewById(R.id.seekSpeedThrTwo);
        speedTextTwo = (TextView) view.findViewById(R.id.textSpeedThrTwo);
        volBarTwo = (SeekBar) view.findViewById(R.id.seekVolThrTwo);
        volTextTwo = (TextView) view.findViewById(R.id.textVolThrTwo);
        slowGainModeSwitch = (Switch) view.findViewById(R.id.switchSlowGainMode);
        updateIntervalBar = (SeekBar) view.findViewById(R.id.seekUpdateInterval);
        updateIntervalText = (TextView) view.findViewById(R.id.textUpdateInterval);
        speedBarOne.setOnSeekBarChangeListener(this);
        speedBarTwo.setOnSeekBarChangeListener(this);
        volBarOne.setOnSeekBarChangeListener(this);
        volBarTwo.setOnSeekBarChangeListener(this);
        updateIntervalBar.setOnSeekBarChangeListener(this);
        thrBar.setOnSeekBarChangeListener(this);
        slowGainModeSwitch.setOnCheckedChangeListener(this);
        slowGainModeSwitch.setChecked(mSlowGainMode);
        accModeSwitch.setOnCheckedChangeListener(this);
        accModeSwitch.setChecked(accMode);
        speedBarOne.setMax(100);
        speedBarTwo.setMax(100);

        thrBar.setProgress(accelerationThresholdProg);
        updateIntervalBar.setProgress(mUpdateIntervalProg);
        speedBarOne.setProgress(mSpeedSetOne);
        speedBarTwo.setProgress(mSpeedSetTwo);
        volBarOne.setProgress(mVolSetOne);
        volBarTwo.setProgress(mVolSetTwo);
    }

    private void restorePreferences() {
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        mSlowGainMode = settings.getBoolean("slowGainMode", true);
        mVolSetOne = settings.getInt("volSetOne",5);
        mVolSetTwo = settings.getInt("volSetTwo",10);
        mSpeedSetOne = settings.getInt("speedSetOne",50);
        mSpeedSetTwo = settings.getInt("speedSetTwo", 99);
        mSpeedSetTwoTotal = mSpeedSetOne + mSpeedSetTwo;
        mUpdateInterval = settings.getInt("updateInterval",1000);
        mUpdateIntervalProg = settings.getInt("updateIntervalProg",1);
        accelerationThreshold = settings.getInt("accelerationThreshold",5);
        accelerationThresholdProg = settings.getInt("accelerationThresholdProg",10);
        accMode = settings.getBoolean("accMode",false);


    }
    private void savePreferences() {
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("slowGainMode", mSlowGainMode);
        editor.putInt("volSetOne", mVolSetOne);
        editor.putInt("volSetTwo", mVolSetTwo);
        editor.putInt("speedSetOne", mSpeedSetOne);
        editor.putInt("speedSetTwo", mSpeedSetTwo);
        editor.putInt("updateInterval",mUpdateInterval);
        editor.putInt("updateIntervalProg",mUpdateIntervalProg);
        editor.putInt("accelerationThreshold",accelerationThreshold);
        editor.putInt("accelerationThresholdProg",accelerationThresholdProg);
        editor.putBoolean("accMode",accMode);
        editor.apply();
    }

    public void updateUI(){


        speedTextOne.setText(mSpeedSetOne +" km/h");
        speedTextTwo.setText(mSpeedSetTwoTotal +" km/h");
        updateIntervalText.setText(mUpdateInterval+ " ms");
        volTextOne.setText(mVolSetOne+"");
        volTextTwo.setText(mVolSetTwo+"");
        thrText.setText(accelerationThreshold+"");


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
            case    R.id.seekThreshold :
                accelerationThresholdProg = progress;
                accelerationThreshold = accelerationThresholdProg + 5;
                break;
        }
        mSpeedSetTwoTotal = mSpeedSetOne + mSpeedSetTwo;
        if(mUpdateIntervalProg==0){
            mUpdateInterval = 1;
        }else{
            mUpdateInterval = mUpdateIntervalProg*1000;
        }
        updateUI();
        updateParams();


    }
    private void updateParams(){
        Intent startServiceIntent = new Intent(this.getContext(),CruiseService.class);
        startServiceIntent.setAction(CruiseService.ACTION_UPDATE_PREFS);
        startServiceIntent.putExtra("accMode",accMode);
        startServiceIntent.putExtra("slowGainMode",mSlowGainMode);
        startServiceIntent.putExtra("startSpeed",mSpeedSetOne);
        startServiceIntent.putExtra("endSpeed",mSpeedSetTwoTotal);
        startServiceIntent.putExtra("startVol",mVolSetOne);
        startServiceIntent.putExtra("endVol",mVolSetTwo);
        startServiceIntent.putExtra("mUpdateInterval",mUpdateInterval);
        startServiceIntent.putExtra("accelerationThreshold",accelerationThreshold);
        getActivity().startService(startServiceIntent);

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
            case R.id.switchAccMode :
                accMode = isChecked;
                break;
        }
        updateParams();
    }
    @Override
    public void onPause() {

        savePreferences();
        super.onPause();
    }




}
