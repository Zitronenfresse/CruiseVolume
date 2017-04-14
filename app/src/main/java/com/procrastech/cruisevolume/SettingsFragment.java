package com.procrastech.cruisevolume;

import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import static com.procrastech.cruisevolume.tabSettingsActivity.KEY_ACTIVE_PROFILE_NUMBER;
import static com.procrastech.cruisevolume.tabSettingsActivity.KEY_MODE_PREFS;
import static com.procrastech.cruisevolume.tabSettingsActivity.KEY_PROFILE_PREFS;


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
    Switch startWithWaveSwitch;
    Switch autoStartSwitch;
    TextView thrText;
    SeekBar thrBar;
    protected int mSpeedSetOne;
    protected int mSpeedSetTwo;
    protected int mSpeedSetTwoTotal;
    protected int mVolSetOne;
    protected int mVolSetTwo;
    protected boolean mSlowGainMode;
    protected boolean accMode;
    protected boolean startwithwave;
    protected int mUpdateInterval;
    protected int mUpdateIntervalProg;
    protected int accelerationThreshold;
    protected int accelerationThresholdProg;
    ImageView rotatingLogoView;
    AnimationDrawable rotatingLogoAnimation;

    SharedPreferences mode_prefs;
    SharedPreferences profile_prefs;

    SharedPreferences profile_prefs1;
    SharedPreferences profile_prefs2;
    SharedPreferences profile_prefs3;
    SharedPreferences profile_prefs4;
    SharedPreferences profile_prefs5;


    SharedPreferences.OnSharedPreferenceChangeListener mode_changed_listener;

    private static int active_profile_number;
    private boolean autoStart;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, null);

        getAllWidgets(view);
        initSharedPreferences();
        startWithWaveSwitch.setChecked(startwithwave);
        accModeSwitch.setChecked(accMode);
        autoStartSwitch.setChecked(autoStart);
        slowGainModeSwitch.setChecked(mSlowGainMode);
        thrBar.setProgress(accelerationThresholdProg);
        updateIntervalBar.setProgress(mUpdateIntervalProg);
        speedBarOne.setProgress(mSpeedSetOne);
        speedBarTwo.setProgress(mSpeedSetTwo);
        volBarOne.setProgress(mVolSetOne);
        volBarTwo.setProgress(mVolSetTwo);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void getAllWidgets(View view) {
        rotatingLogoView = (ImageView) view.findViewById(R.id.rotatingLogoView);
        rotatingLogoAnimation = (AnimationDrawable) rotatingLogoView.getDrawable();
        rotatingLogoAnimation.start();
        thrBar = (SeekBar) view.findViewById(R.id.seekThreshold);
        thrText = (TextView) view.findViewById(R.id.textThreshold);
        accModeSwitch = (Switch) view.findViewById(R.id.switchAccMode);
        startWithWaveSwitch = (Switch) view.findViewById(R.id.startWithWaveSwitch);
        speedBarOne = (SeekBar) view.findViewById(R.id.seekSpeedThrOne);
        speedTextOne = (TextView) view.findViewById(R.id.textSpeedThrOne);
        volBarOne = (SeekBar) view.findViewById(R.id.seekVolThrOne);
        volTextOne = (TextView) view.findViewById(R.id.textVolThrOne);
        speedBarTwo = (SeekBar) view.findViewById(R.id.seekSpeedThrTwo);
        speedTextTwo = (TextView) view.findViewById(R.id.textSpeedThrTwo);
        volBarTwo = (SeekBar) view.findViewById(R.id.seekVolThrTwo);
        volTextTwo = (TextView) view.findViewById(R.id.textVolThrTwo);
        slowGainModeSwitch = (Switch) view.findViewById(R.id.switchSlowGainMode);
        autoStartSwitch = (Switch) view.findViewById(R.id.autoStartSwitch);
        updateIntervalBar = (SeekBar) view.findViewById(R.id.seekUpdateInterval);
        updateIntervalText = (TextView) view.findViewById(R.id.textUpdateInterval);
        speedBarOne.setOnSeekBarChangeListener(this);
        speedBarTwo.setOnSeekBarChangeListener(this);
        volBarOne.setOnSeekBarChangeListener(this);
        volBarTwo.setOnSeekBarChangeListener(this);
        updateIntervalBar.setOnSeekBarChangeListener(this);
        thrBar.setOnSeekBarChangeListener(this);
        slowGainModeSwitch.setOnCheckedChangeListener(this);
        accModeSwitch.setOnCheckedChangeListener(this);
        startWithWaveSwitch.setOnCheckedChangeListener(this);
        autoStartSwitch.setOnCheckedChangeListener(this);

        speedBarOne.setMax(100);
        speedBarTwo.setMax(100);


    }

    private void initSharedPreferences(){

        profile_prefs1 = getActivity().getSharedPreferences(KEY_PROFILE_PREFS+1,0);
        profile_prefs2 = getActivity().getSharedPreferences(KEY_PROFILE_PREFS+2,0);
        profile_prefs3 = getActivity().getSharedPreferences(KEY_PROFILE_PREFS+3,0);
        profile_prefs4 = getActivity().getSharedPreferences(KEY_PROFILE_PREFS+4,0);
        profile_prefs5 = getActivity().getSharedPreferences(KEY_PROFILE_PREFS+5,0);


        mode_changed_listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                active_profile_number = sharedPreferences.getInt(KEY_ACTIVE_PROFILE_NUMBER,1);
                profile_prefs = getActivity().getSharedPreferences(KEY_PROFILE_PREFS+active_profile_number,0);
                Log.d("PREF",KEY_PROFILE_PREFS+active_profile_number+" loaded");
                loadProfilePreferences();
            }
        };

        mode_prefs = getActivity().getSharedPreferences(KEY_MODE_PREFS,0);
        active_profile_number = mode_prefs.getInt(KEY_ACTIVE_PROFILE_NUMBER,1);
        mode_prefs.registerOnSharedPreferenceChangeListener(mode_changed_listener);

        profile_prefs = getActivity().getSharedPreferences(KEY_PROFILE_PREFS+active_profile_number,0);

        autoStart = mode_prefs.getBoolean(translucentLauncher.AUTO_START,false);
        startwithwave = mode_prefs.getBoolean(translucentLauncher.START_WITH_WAVE,false);
        loadProfilePreferences();
    }

    private void loadProfilePreferences() {
        mSlowGainMode = profile_prefs.getBoolean("slowGainMode", true);
        mVolSetOne = profile_prefs.getInt("volSetOne",5);
        mVolSetTwo = profile_prefs.getInt("volSetTwo",10);
        mSpeedSetOne = profile_prefs.getInt("speedSetOne",50);
        mSpeedSetTwo = profile_prefs.getInt("speedSetTwo", 99);
        mSpeedSetTwoTotal = mSpeedSetOne + mSpeedSetTwo;
        mUpdateInterval = profile_prefs.getInt("updateInterval",1000);
        mUpdateIntervalProg = profile_prefs.getInt("updateIntervalProg",1);
        accelerationThreshold = profile_prefs.getInt("accelerationThreshold",5);
        accelerationThresholdProg = profile_prefs.getInt("accelerationThresholdProg",10);
        accMode = profile_prefs.getBoolean("accMode",false);
        updateUIAfterProfileChange();
    }

    private void saveProfilePreferences(){
        if(profile_prefs!=null){
            SharedPreferences.Editor editor = profile_prefs.edit();
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
        }else{
            Log.d("PREF","Prefs not saved, because profile_prefs = null");

        }

    }

    public void updateUI(){
        speedTextOne.setText(getString(R.string.onset_speed,mSpeedSetOne));
        speedTextTwo.setText(getString(R.string.final_speed,mSpeedSetTwoTotal));
        updateIntervalText.setText(getString(R.string.updateInterval,mUpdateInterval));
        volTextOne.setText(getString(R.string.onset_volume,mVolSetOne));
        volTextTwo.setText(getString(R.string.final_volume,mVolSetTwo));
        thrText.setText(getString(R.string.accelerationThreshold,accelerationThreshold));

    }

    private void updateUIAfterProfileChange(){
        updateUI();
        thrBar.setProgress(accelerationThresholdProg);
        updateIntervalBar.setProgress(mUpdateIntervalProg);
        speedBarOne.setProgress(mSpeedSetOne);
        speedBarTwo.setProgress(mSpeedSetTwo);
        volBarOne.setProgress(mVolSetOne);
        volBarTwo.setProgress(mVolSetTwo);
        accModeSwitch.setChecked(accMode);
        slowGainModeSwitch.setChecked(mSlowGainMode);

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


    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        saveProfilePreferences();

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
            case R.id.startWithWaveSwitch :
                startwithwave = isChecked;
                if(mode_prefs!=null){
                    mode_prefs.edit().putBoolean(translucentLauncher.START_WITH_WAVE,isChecked).apply();
                }
                break;
            case R.id.autoStartSwitch :
                if(mode_prefs!=null){
                    mode_prefs.edit().putBoolean(translucentLauncher.AUTO_START,isChecked).apply();
                }
        }
        saveProfilePreferences();
    }
    @Override
    public void onPause() {
        super.onPause();
        mode_prefs.unregisterOnSharedPreferenceChangeListener(mode_changed_listener);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }




}
