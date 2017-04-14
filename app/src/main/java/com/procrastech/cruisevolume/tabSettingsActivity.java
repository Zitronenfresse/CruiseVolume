package com.procrastech.cruisevolume;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.procrastech.cruisevolume.util.IabHelper;
import com.procrastech.cruisevolume.util.IabResult;
import com.procrastech.cruisevolume.util.Inventory;
import com.procrastech.cruisevolume.util.Purchase;

import static com.procrastech.cruisevolume.CruiseService.REQUEST_CHECK_SETTINGS;


public class tabSettingsActivity extends AppCompatActivity {

    private ViewPagerAdapter adapter;
    private SettingsFragment mSettingsFragment;
    private ProfilesFragment mProfilesFragment;
    private InfoFragment mInfoFragment;
    private ViewPager viewPager;
    private TabLayout allTabs;

    protected static boolean proVersion;


    protected static final String KEY_PROFILE = "KEY_PROFILE";


    protected static final String KEY_ACTIVE_PROFILE_NUMBER = "KEY_ACTIVE_PROFILE_NUMBER";
    protected static final String KEY_MODE_PREFS = "KEY_MODE_PREFS";
    protected static final String KEY_PROFILE_PREFS = "KEY_PROFILE_PREFS";
    public static final String ACTION_STOP_SETTINGS = "com.procrastech.cruisevolume.ACTION_STOP_SETTINGS";

    private AdView mAdView;

    private String POSITION;

    private static final String KEY_PREF_PURCHASE_PROVERSION_AVAILABLE = "KEY_PREF_PURCHASE_PROVERSION_AVAILABLE";
    private static final String TAG = "tabSettings";
    static final String ITEM_SKU = "proversion";

    IabHelper mHelper;


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };



    @Override
    protected void onNewIntent(Intent intent){
        PendingIntent pI = (getIntent().getParcelableExtra("resolution"));

        if(pI!=null){
            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(it);

            try {
                startIntentSenderForResult(pI.getIntentSender(),1,null,0,0,0);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PendingIntent pI = (getIntent().getParcelableExtra("resolution"));

        if(pI!=null){


            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(it);

            try {
                startIntentSenderForResult(pI.getIntentSender(),1,null,0,0,0);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }



        Intent intent = new Intent(this, CruiseService.class);
        startService(intent);

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,new IntentFilter(ACTION_STOP_SETTINGS));


        SharedPreferences settings = getSharedPreferences(KEY_MODE_PREFS, 0);

        proVersion = !settings.getBoolean(KEY_PREF_PURCHASE_PROVERSION_AVAILABLE,true);

        if(proVersion){
            setContentView(R.layout.activity_tab_settings_pro);

        }else{
            setContentView(R.layout.activity_tab_settings);
            initAds();

        }


        viewPager = (ViewPager) findViewById(R.id.viewpager);


        allTabs = (TabLayout) findViewById(R.id.tabs);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        mSettingsFragment = new SettingsFragment();
        mProfilesFragment = new ProfilesFragment();
        mInfoFragment = new InfoFragment();
        adapter.addFragment(mSettingsFragment, getString(R.string.tabSettings_settingsTab_text));
        adapter.addFragment(mProfilesFragment, getString(R.string.tabSetting_profilesTab_text));
        adapter.addFragment(mInfoFragment, getString(R.string.tabSettings_infoTab_text));
        viewPager.setAdapter(adapter);
        allTabs.setupWithViewPager(viewPager);


        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArXjuny14c2ZLblnB7cPO0zCG2xr0SVIOwkDEH28lnangmNNtP3/+q0ecDnoiGQITp9XKtTwSn69G/Dz+U4MPBE2g25a3JFeADc0H3yd30meUmWSHOv2Pmtg09iTwc9Ct3elpIQilCv1ker67Dei4l5Buy5mNT4y2xUSwy0mJQpecZWGaXmDX5Zc3wE3MCBgQbYhD6hCw3GZYzKlEdfbbJSzvZh28+uHnag6YdRaS3OqZmFfj4Cc5bmKD7kgbIZ7G0AUfETy4d2/5bdpcQ1Nk6j4stHN7eYqChItWTTH7ccJjOlp5zaQQMBIzwe4+8pptcOaR6Misd60cLQEs2Hdp6wIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result)
            {
                if (!result.isSuccess()) {
                    Log.d(TAG, "In-app Billing setup failed: " +
                            result);
                } else {
                    mHelper.queryInventoryAsync(mReceivedInventoryListener);
                    Log.d(TAG, "In-app Billing is set up OK");
                }
            }
        });



    }


    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result,
                                          Purchase purchase)
        {
            if (result.isFailure()) {
                // Handle error
                return;
            }
            else if (purchase.getSku().equals(ITEM_SKU)) {
                consumeItem();
            }

        }
    };



    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            if (result.isFailure()) {
                // Handle failure
            } else {
                boolean hasPurchased_PROVERSION = inventory.hasPurchase(ITEM_SKU);
                SharedPreferences sharedPref = getSharedPreferences(KEY_MODE_PREFS, 0);
                SharedPreferences.Editor editor = sharedPref.edit();
                boolean toRecreate = false;
                if(sharedPref.getBoolean(KEY_PREF_PURCHASE_PROVERSION_AVAILABLE,true) && hasPurchased_PROVERSION){
                    setContentView(R.layout.activity_tab_settings_pro);
                    toRecreate = true;
                }
                Log.d("IAB","InventoryQueryfinsihed, user has pro: " + hasPurchased_PROVERSION);
                editor.putBoolean(KEY_PREF_PURCHASE_PROVERSION_AVAILABLE, !hasPurchased_PROVERSION);
                editor.commit();

                if(toRecreate){
                    recreate();
                }

            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {

                    if (result.isSuccess()) {
                        proVersion = true;
                    } else {
                        // handle error
                    }
                }
            };

    public void buyClick(View view) {
        mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001,
                mPurchaseFinishedListener, "mypurchasetoken");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (!mHelper.handleActivityResult(requestCode,
                resultCode, data)) {
            switch (requestCode) {
                // Check for the integer request code originally supplied to startResolutionForResult().
                case REQUEST_CHECK_SETTINGS:
                    switch (resultCode) {
                        case Activity.RESULT_OK:
                            Log.i("LSR", "User agreed to make required location settings changes.");
                            break;
                        case Activity.RESULT_CANCELED:
                            Log.i("LSR", "User chose not to make required location settings changes.");
                            break;
                    }
                    break;
            }
            super.onActivityResult(requestCode, resultCode, data);
        }

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
            startActivity(Intent.createChooser(emailIntent, getString(R.string.sendfeedback_chooser_text)));

            Log.d("EMAIL","Finished sending email...");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.sendfeedback_noClientInstalledToast_text, Toast.LENGTH_SHORT).show();
        }
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
        super.onPause();
        if (mAdView != null) {
            mAdView.pause();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

    }

    @Override
    public void onResume() {
        super.onResume();
        startService(new Intent(this,CruiseService.class));
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
        if (mHelper != null){
             mHelper.dispose();
        }
        mHelper = null;

    }

}