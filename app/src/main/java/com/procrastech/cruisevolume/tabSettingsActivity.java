package com.procrastech.cruisevolume;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class tabSettingsActivity extends AppCompatActivity {

    public static tabSettingsActivity instance;
    private ViewPagerAdapter adapter;
    private SettingsFragment mSettingsFragment;
    private ProfilesFragment mProfilesFragment;
    private InfoFragment mInfoFragment;
    private ViewPager viewPager;
    private TabLayout allTabs;

    private boolean proVersion = false;

    private AdView mAdView;

    private String POSITION;
    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String encodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArXjuny14c2ZLblnB7cPO0zCG2xr0SVIOwkDEH28lnangmNNtP3/+q0ecDnoiGQITp9XKtTwSn69G/Dz+U4MPBE2g25a3JFeADc0H3yd30meUmWSHOv2Pmtg09iTwc9Ct3elpIQilCv1ker67Dei4l5Buy5mNT4y2xUSwy0mJQpecZWGaXmDX5Zc3wE3MCBgQbYhD6hCw3GZYzKlEdfbbJSzvZh28+uHnag6YdRaS3OqZmFfj4Cc5bmKD7kgbIZ7G0AUfETy4d2/5bdpcQ1Nk6j4stHN7eYqChItWTTH7ccJjOlp5zaQQMBIzwe4+8pptcOaR6Misd60cLQEs2Hdp6wIDAQAB";
        if(proVersion){
            setContentView(R.layout.activity_tab_settings_pro);

        }else{
            setContentView(R.layout.activity_tab_settings);
            initAds();

        }
        instance=this;

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        allTabs = (TabLayout) findViewById(R.id.tabs);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        mSettingsFragment = new SettingsFragment();
        mProfilesFragment = new ProfilesFragment();
        mInfoFragment = new InfoFragment();
        adapter.addFragment(mSettingsFragment, "Settings");
        adapter.addFragment(mProfilesFragment, "Profiles");
        adapter.addFragment(mInfoFragment, "Info");
        viewPager.setAdapter(adapter);
        allTabs.setupWithViewPager(viewPager);




    }

    public void onSendFeebackButton(View view){

        String content = mInfoFragment.getFeedbackContent();
        if(!content.isEmpty()){
            sendEmail(content);
            mInfoFragment.clearFeedback();

        }
    }

    protected void sendEmail(String content) {
        Log.d("EMAIL","Send email");

        //String[] TO = {"procrastech+feedback@gmail.com"};


        Intent emailIntent = new Intent(Intent.ACTION_SENDTO,Uri.fromParts("mailto","procrastech+support@gmail.com",null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "CruiseVolume Feedback");
        emailIntent.putExtra(Intent.EXTRA_TEXT, content);
        //emailIntent.setType("message/rfc822");


        //emailIntent.setData(Uri.parse("mailto:"));
        //emailIntent.setType("text/plain");


        //emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send Feedback"));

            Log.d("EMAIL","Finished sending email...");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this,"There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public static tabSettingsActivity getInstance() {
        return instance;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putInt(POSITION, allTabs.getSelectedTabPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //viewPager.setCurrentItem(savedInstanceState.getInt(POSITION));
    }

    private void initAds() {
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-4077367878822895~3749568563");

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice("B285B4A48DCD75D67F18B862A24B5AFD").build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public void onPause(){
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

}