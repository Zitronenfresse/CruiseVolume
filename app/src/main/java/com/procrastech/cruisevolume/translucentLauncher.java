package com.procrastech.cruisevolume;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


public class translucentLauncher extends AppCompatActivity {

    boolean isFirstTime;
    protected static final String IS_FIRST_TIME = "IS_FIRST_TIME";
    protected static final String START_WITH_WAVE = "START_WITH_WAVE";
    protected static final String AUTO_START = "AUTO_START";
    private final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 1;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 2;
    private static boolean requestCalledBack = false;
    private boolean startWithWave = false;
    private boolean autoStart = false;
    SharedPreferences settings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadPreferences();
        checkForPermissions();
        checkPlayServices();
        if(isFirstTime){
            showFirstStartScreen();
        }else{
            showWaveAnimation();
        }


    }

    private void showWaveAnimation() {
        if(startWithWave){
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int ui = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(ui);

            final Window win= getWindow();
            win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            setContentView(R.layout.translucentlauncherminimal_layout);
            final ImageView v = (ImageView) findViewById(R.id.wave);

            Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fadein);
            final Animation fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fadeout);
            fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    v.setVisibility(View.INVISIBLE);
                    initializeCruiseService();
                    finish();

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            v.startAnimation(fadeInAnimation);
            fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                    v.startAnimation(fadeOutAnimation);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }else{
            initializeCruiseService();
            finish();
        }
    }
    private void showFirstStartScreen(){

        setContentView(R.layout.translucentlauncher_layout);
        final ImageView v = (ImageView) findViewById(R.id.wave);

        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fadeinslow);
        final Animation fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fadeoutslow);
        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.INVISIBLE);
                initializeCruiseService();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(fadeInAnimation);
        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.startAnimation(fadeOutAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        LinearLayout l = (LinearLayout) findViewById(R.id.translucentLayout);
        l.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent startSettingsIntent = new Intent(translucentLauncher.this,tabSettingsActivity.class);
                startActivity(startSettingsIntent);
                settings.edit().putBoolean(IS_FIRST_TIME, false).apply();
                finish();
                return false;
            }
        });

    }

    private void loadPreferences(){
        settings = getSharedPreferences(tabSettingsActivity.KEY_MODE_PREFS, 0);
        isFirstTime = settings.getBoolean(IS_FIRST_TIME,true);
        startWithWave = settings.getBoolean(START_WITH_WAVE,false);
        autoStart = settings.getBoolean(AUTO_START,false);
    }

    private void initializeCruiseService() {
        Intent intent = new Intent(this, CruiseService.class);

        if(autoStart){
            intent.setAction(CruiseService.ACTION_START_UPDATES);
        }else{
            intent.setAction(CruiseService.ACTION_START_PAUSED);

        }
        startService(intent);
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

    protected boolean checkForPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(translucentLauncher.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH}, MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
            return false;
        } else {
            Log.d("MY", "Permissions already granted");
            return true;
        }
    }
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                requestCalledBack = true;

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MY", "Permissions granted");


                } else {
                    finish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


}
