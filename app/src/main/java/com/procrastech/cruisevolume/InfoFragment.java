package com.procrastech.cruisevolume;

import com.procrastech.cruisevolume.BuildConfig;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by IEnteramine on 04.04.2017.
 */

public class InfoFragment extends Fragment {

    EditText editFeedbackContent;
    TextView buildInfo;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        if(tabSettingsActivity.proVersion){
            view = inflater.inflate(R.layout.fragment_info_pro, null);

        }else{
            view = inflater.inflate(R.layout.fragment_info, null);

        }
        getAllWidgets(view);
        String version = BuildConfig.VERSION_NAME;
        buildInfo.setText(getString(R.string.build_info_text,version));
        return view;
    }

    private void getAllWidgets(View view) {
        buildInfo = (TextView) view.findViewById(R.id.buildInfoTextView);
        editFeedbackContent = (EditText) view.findViewById(R.id.editFeedbackContent);

    }

    public String getFeedbackContent(){
        if(editFeedbackContent.getText()!=null){
            String feedbackContent = editFeedbackContent.getText().toString();
            return  feedbackContent;
        }
        return "";

    }

    public void clearFeedback(){
        editFeedbackContent.getText().clear();
        editFeedbackContent.clearComposingText();
    }



}
