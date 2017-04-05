package com.procrastech.cruisevolume;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by IEnteramine on 04.04.2017.
 */

public class ProfilesFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profiles, null);
        getAllWidgets(view);

        return view;
    }

    private void getAllWidgets(View view) {

    }
}
