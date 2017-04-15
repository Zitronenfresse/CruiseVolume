package com.procrastech.cruisevolume;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RunnableScheduledFuture;

import static com.procrastech.cruisevolume.tabSettingsActivity.KEY_ACTIVE_PROFILE_NUMBER;
import static com.procrastech.cruisevolume.tabSettingsActivity.KEY_MODE_PREFS;
import static com.procrastech.cruisevolume.tabSettingsActivity.KEY_PROFILE;

/**
 * Created by IEnteramine on 04.04.2017.
 */

public class ProfilesFragment extends Fragment {

    SharedPreferences mode_prefs;
    private static int active_profile_number;

    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> s;
    Set<BluetoothDevice> pairedDevices;
    private BluetoothAdapter mBluetoothAdapter;
    private ListView bluetoothListView;

    private Handler handler;
    private int bluetoothUpdateDelay = 5000;
    AdapterView.OnItemLongClickListener btListener;
    View.OnLongClickListener btdeviceListener;

    String btdeviceaddress;

    AlertDialog renameDialog;

    RadioGroup.OnCheckedChangeListener profileGroupListener;
    RadioGroup profileGroup;
    RadioGroup.OnLongClickListener renameListener;
    private TextView bluetoothDeviceTextView;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        handler = new Handler();
        handler.postDelayed(runnable,bluetoothUpdateDelay);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profiles, null);
        getAllWidgets(view);
        initSharedPreferences();
        bluetoothDeviceTextView.setText(btdeviceaddress);

        ((RadioButton)profileGroup.getChildAt(active_profile_number-1)).setChecked(true);
        profileGroupListener = (new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int index = profileGroup.indexOfChild(getActivity().findViewById(profileGroup.getCheckedRadioButtonId()));
                setActive_profile_number(index+1);
            }
        });

        btdeviceListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TextView t = (TextView) v;
                String ad = t.getText().toString();
                mode_prefs.edit().remove("KEY_BT_DEVICE").apply();
                btdeviceaddress = "";
                t.setText(btdeviceaddress);
                return false;
            }
        };

        btListener = new AdapterView.OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView t = (TextView) view;
                btdeviceaddress = t.getText().toString();
                mode_prefs.edit().putString("KEY_BT_DEVICE",btdeviceaddress).apply();
                Log.d("BLUETOOTH","ADDED BT_DEVICE TO PREFS "+btdeviceaddress);
                bluetoothDeviceTextView.setText(btdeviceaddress);
                return false;
            }

        };

        renameListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                final EditText edittext = new EditText(getContext());
                final RadioButton r = (RadioButton)v;

                alert.setTitle(getString(R.string.profiles_renameDialog_text)+ r.getText());
                alert.setView(edittext);

                alert.setPositiveButton(R.string.profiles_renameDialogPositiveButton_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newName = edittext.getText().toString();
                        r.setText(newName);
                        int count = profileGroup.getChildCount();
                        for (int i=0;i<count;i++) {
                            View o = profileGroup.getChildAt(i);
                            if (o instanceof RadioButton) {
                                SharedPreferences.Editor editor = mode_prefs.edit();
                                editor.putString(KEY_PROFILE+(i+1)+"_TEXT",((RadioButton) o).getText().toString());
                                editor.apply();
                            }
                        }
                    }
                });

                alert.setNegativeButton(R.string.profiles_renameDialogNegativeButton_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                renameDialog = alert.show();
                return true;
            }
        };
        int count = profileGroup.getChildCount();
        for (int i=0;i<count;i++) {
            View o = profileGroup.getChildAt(i);
            if (o instanceof RadioButton) {
                o.setOnLongClickListener(renameListener);
            }
        }

        bluetoothDeviceTextView.setOnLongClickListener(btdeviceListener);

        profileGroup.setOnCheckedChangeListener(profileGroupListener);
        profileGroup.setOnLongClickListener(renameListener);


        return view;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {


            Log.d("BLUETOOTH","Getting devices, repopulating arrayadapter");

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(mBluetoothAdapter==null){
                Log.d("BLUETOOTH","Bluetooth adapter is null");
            }
            pairedDevices = mBluetoothAdapter.getBondedDevices();

            ArrayList<String> oldBTSCAN = new ArrayList<String>(s);
            ArrayList<String> newBTSCAN = new ArrayList<String>();
            for(BluetoothDevice bt : pairedDevices){
                Log.d("BLUETOOTH","Bluetooth device found " + bt.getAddress());
                newBTSCAN.add(bt.getAddress());

            }

            oldBTSCAN.removeAll(newBTSCAN);
            s.removeAll(oldBTSCAN);
            newBTSCAN.removeAll(s);

            for(String item : newBTSCAN){

                    Log.d("BLUETOOTH","Adding Bt Device " + item + " to adapter");
                    s.add(item);

            }



            arrayAdapter.notifyDataSetChanged();
            justifyListViewHeightBasedOnChildren(bluetoothListView);

            handler.postDelayed(this, bluetoothUpdateDelay);

        }
    };


    private void initBluetoothList(){

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter==null){
            Log.d("BLUETOOTH","Bluetooth adapter is null");
        }
        pairedDevices = mBluetoothAdapter.getBondedDevices();

        s = new ArrayList<String>();
        for(BluetoothDevice bt : pairedDevices)
            s.add(bt.getAddress());

        if(arrayAdapter==null)arrayAdapter = new ArrayAdapter<String>(getActivity(),R.layout.list,s);
        if(bluetoothListView!=null){

            bluetoothListView.setAdapter(arrayAdapter);
            bluetoothListView.setOnItemLongClickListener(btListener);
            bluetoothListView.setItemsCanFocus(true);
            justifyListViewHeightBasedOnChildren(bluetoothListView);
        }


    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        initBluetoothList();


    }

    @Override
    public void onPause(){
        super.onPause();
        handler.removeCallbacks(runnable);
        profileGroup.setOnCheckedChangeListener(null);
        profileGroup.setOnLongClickListener(null);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(renameDialog!=null){
            renameDialog.dismiss();
        }

    }

    @Override
    public void onResume(){
        super.onResume();
        handler.postDelayed(runnable,bluetoothUpdateDelay);
    }

    private void initSharedPreferences(){

        mode_prefs = getActivity().getSharedPreferences(KEY_MODE_PREFS,0);
        btdeviceaddress = mode_prefs.getString("KEY_BT_DEVICE","");
        active_profile_number = mode_prefs.getInt(KEY_ACTIVE_PROFILE_NUMBER,1);
        int count = profileGroup.getChildCount();
        for (int i=0;i<count;i++) {
            View o = profileGroup.getChildAt(i);
            if (o instanceof RadioButton) {
                ((RadioButton) o).setText(mode_prefs.getString(KEY_PROFILE+(i+1)+"_TEXT","Profile "+(i+1)));
            }
        }

    }

    private void setActive_profile_number(int active_profile_number){
        if(ProfilesFragment.active_profile_number !=active_profile_number){
            ProfilesFragment.active_profile_number = active_profile_number;
            SharedPreferences.Editor editor = mode_prefs.edit();
            editor.putInt(KEY_ACTIVE_PROFILE_NUMBER,active_profile_number);
            editor.apply();

        }

    }



    private void getAllWidgets(View view) {
        bluetoothListView = (ListView) view.findViewById(R.id.bluetoothListView);
        bluetoothDeviceTextView = (TextView) view.findViewById(R.id.bluetoothDeviceTextView);
        profileGroup = (RadioGroup) view.findViewById(R.id.selectProfileGroup);
    }


    public static void justifyListViewHeightBasedOnChildren (ListView listView) {

        ListAdapter adapter = listView.getAdapter();

        if (adapter == null) {
            return;
        }
        ViewGroup vg = listView;
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, vg);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams par = listView.getLayoutParams();
        par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(par);
        listView.requestLayout();
    }


}
