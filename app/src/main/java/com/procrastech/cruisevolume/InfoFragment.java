package com.procrastech.cruisevolume;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by IEnteramine on 04.04.2017.
 */

public class InfoFragment extends Fragment {

    EditText editFeedbackContent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, null);
        getAllWidgets(view);

        return view;
    }

    private void getAllWidgets(View view) {
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
