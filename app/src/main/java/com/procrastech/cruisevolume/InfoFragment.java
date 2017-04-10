package com.procrastech.cruisevolume;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by IEnteramine on 04.04.2017.
 */

public class InfoFragment extends Fragment {

    EditText editFeedbackContent;
    TextView buildInfo;
    Button developerWebsiteButton;
    Button privacyPolicyButton;
    Button disclaimerButton;

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

        developerWebsiteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.procrastech.de");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        privacyPolicyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.cruisevolume.procrastech.de/privacypolicy.html");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        disclaimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.cruisevolume.procrastech.de/disclaimer.html");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });


        return view;
    }

    private void getAllWidgets(View view) {
        buildInfo = (TextView) view.findViewById(R.id.buildInfoTextView);
        editFeedbackContent = (EditText) view.findViewById(R.id.editFeedbackContent);
        developerWebsiteButton = (Button) view.findViewById(R.id.developerWebsiteButton);
        privacyPolicyButton = (Button) view.findViewById(R.id.privacyPolicyButton);
        disclaimerButton = (Button) view.findViewById(R.id.disclaimerButton);

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
