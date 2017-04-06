package com.procrastech.cruisevolume;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import static com.procrastech.cruisevolume.tabSettingsActivity.KEY_ACTIVE_PROFILE_NUMBER;
import static com.procrastech.cruisevolume.tabSettingsActivity.KEY_MODE_PREFS;
import static com.procrastech.cruisevolume.tabSettingsActivity.KEY_PROFILE;
import static com.procrastech.cruisevolume.tabSettingsActivity.KEY_PROFILE_PREFS;

/**
 * Created by IEnteramine on 04.04.2017.
 */

public class ProfilesFragment extends Fragment {

    SharedPreferences mode_prefs;
    private int active_profile_number;

    RadioGroup.OnCheckedChangeListener profileGroupListener;
    RadioGroup profileGroup;
    RadioGroup.OnLongClickListener renameListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profiles, null);
        getAllWidgets(view);
        initSharedPreferences();

        ((RadioButton)profileGroup.getChildAt(active_profile_number-1)).setChecked(true);
        profileGroupListener = (new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int index = profileGroup.indexOfChild(getActivity().findViewById(profileGroup.getCheckedRadioButtonId()));
                setActive_profile_number(index+1);
            }
        });


        renameListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                final EditText edittext = new EditText(getContext());
                final RadioButton r = (RadioButton)v;

                alert.setTitle("Rename "+ r.getText());
                alert.setView(edittext);

                alert.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
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

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                alert.show();
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

        profileGroup.setOnCheckedChangeListener(profileGroupListener);
        profileGroup.setOnLongClickListener(renameListener);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //profileGroup.setOnCheckedChangeListener(null);

    }

    private void initSharedPreferences(){

        mode_prefs = getActivity().getSharedPreferences(KEY_MODE_PREFS,0);
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
        if(this.active_profile_number!=active_profile_number){
            this.active_profile_number = active_profile_number;
            SharedPreferences.Editor editor = mode_prefs.edit();
            editor.putInt(KEY_ACTIVE_PROFILE_NUMBER,active_profile_number);
            editor.apply();

        }

    }

    private void getAllWidgets(View view) {

        profileGroup = (RadioGroup) view.findViewById(R.id.selectProfileGroup);
    }
}
