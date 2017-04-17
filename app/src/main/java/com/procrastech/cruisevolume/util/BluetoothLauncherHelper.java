package com.procrastech.cruisevolume.util;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.procrastech.cruisevolume.CruiseService;
import com.procrastech.cruisevolume.translucentLauncher;

/**
 * Created by Test on 15.04.2017.
 */

public class BluetoothLauncherHelper extends BroadcastReceiver {


    public BluetoothLauncherHelper(){
    }
    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences mode_prefs = context.getSharedPreferences("KEY_MODE_PREFS",0);
        String address = mode_prefs.getString("KEY_BT_DEVICE","");

        Log.d("BLUETOOTH","Boradcast received");

        if(intent.getAction().equals("android.bluetooth.device.action.ACL_CONNECTED")){
            Log.d("BLUETOOTH","Bluetooth connect");

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if(device.getName().equals(address)){
                Log.d("BLUETOOTH","my Bluetooth device connect");
                Intent i = new Intent(context,translucentLauncher.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);

            }
        }
    }
}
