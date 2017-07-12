package com.example.f1285.loginpink;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by f1285 on 2017/5/20.
 */

public class About extends Fragment {

    private Button buttonRate;
    private Button buttonFeedback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        buttonRate = (Button) rootView.findViewById(R.id.btn_rate);
        buttonFeedback = (Button) rootView.findViewById(R.id.btn_feedback);

        buttonRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+"com.google.android.apps.plus"));
                    startActivity(intent);
                }catch (android.content.ActivityNotFoundException anfe){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id="+"com.google.android.apps.plus"));
                    startActivity(intent);
                }
            }
        });

        buttonFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/DodgersApp/"));
                    startActivity(intent);
                }catch (android.content.ActivityNotFoundException anfe){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/DodgersApp/"));
                    startActivity(intent);
                }

            }
        });


        return rootView;
    }
}
