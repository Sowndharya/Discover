package com.orange.discover;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.buddy.sdk.Buddy;
import com.buddy.sdk.models.User;

import java.util.ArrayList;
import java.util.List;

public class Loading extends AppCompatActivity {

    private static final String TAG = "Loading Activity";


    String appId = "bbbbbc.GscDDtlPmDsnc";
    String appKey = "62675660-1cb4-7cb4-add0-027504b34f0e";


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);


        Context myContext = getApplicationContext();
        Buddy.init(myContext, appId, appKey);


        Log.w(TAG, "INSIDE LOADING ACTIVITY");


        // If user is detected start the main activity
        // Else start the login activity

        BuddyApplication.instance.getCurrentUser(false, new GetCurrentUserCallback() {

            @Override
            public void complete(User user) {

                Log.w(TAG, "INSIDE GET CURRENT USER METHOD");

                if (user != null) {

                    Log.w(TAG, "STARTING THE MAIN ACTIVITY");

                    Intent i = new Intent(getBaseContext(), LocationTracker.class);

                    // The main activity should not return to the loading activity

                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();

                } else {

                    Log.w(TAG, "STARTING THE LOGIN ACTIVITY");

                    Intent i = new Intent(getBaseContext(), LogIn.class);

                    // The login activity should not return to the loading activity
                    startActivity(i);
                    finish();
                }
            }
        });
    }
}


