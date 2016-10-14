package com.orange.discover;

//Created by a on 23/8/16.

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.buddy.sdk.Buddy;
import com.buddy.sdk.BuddyCallback;
import com.buddy.sdk.BuddyResult;
import com.buddy.sdk.ConnectivityLevel;
import com.buddy.sdk.ConnectivityLevelChangedCallback;
import com.buddy.sdk.UserAuthenticationRequiredCallback;
import com.buddy.sdk.models.User;
import com.parse.Parse;


public class BuddyApplication extends Application {

    private static final String TAG = "Buddy Application Class";

    public static final String SENDER_ID = "728801908856";

    public static final String parseID = "LFKD9fJKixEi4T7BVPXBzr0j6wfzCcJs3odf7Z4Z";
    public static final String appId = "bbbbbc.GscDDtlPmDsnc";
    public static final String appKey = "62675660-1cb4-7cb4-add0-027504b34f0e";


    public static BuddyApplication instance;
    public User currentUser;
    public static Chat activeChat;

    public boolean loginVisible;

    public BuddyApplication() {

        Log.w(TAG, "Constructor");
        instance = this;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {

        super.onCreate();

        Log.w(TAG, "INSIDE ON CREATE");


        Context myContext = getApplicationContext();
        Buddy.init(myContext, appId, appKey);

        Parse.initialize(new Parse.Configuration.Builder(myContext)
                        .applicationId(parseID)
                        .clientKey("h0Pd7YclblFtiWO5ITWgJyjmVuuOrzBIZkkPXsIl")
                        .server("https://parseapi.back4app.com/")
                        .build()
        );



        // Automatically calls the Login Activity whenever
        // authentication fails for a user-level API call

        Buddy.setUserAuthenticationRequiredCallback(new UserAuthenticationRequiredCallback() {

            @Override
            public void authenticate() {

                if (loginVisible) {

                    return;

                } else {

                    loginVisible = true;

                    Log.w(TAG, "STARTING THE LOGIN ACTIVITY");

                    Intent loginIntent = new Intent(BuddyApplication.this, LogIn.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(loginIntent);
                }
            }
        });

        Buddy.setConnectivityLevelChangedCallback(new ConnectivityLevelChangedCallback() {

            @Override
            public void connectivityLevelChanged(ConnectivityLevel level) {

                String message = getResources().getString((level == ConnectivityLevel.None) ?
                        R.string.connection_lost :
                        R.string.reconnected);

                Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
            }
        });
    }

    public void setCurrentUser(User currentUser) {

        Log.w(TAG, "SETTING CURRENT USER");


        this.currentUser = currentUser;
    }

    public void getCurrentUser(final boolean refresh, final GetCurrentUserCallback callback) {

        if (currentUser != null && !refresh) {

            if (callback != null) {

                Log.w(TAG, "GETTING CURRENT USER");


                callback.complete(currentUser);
            }

        } else {

            Buddy.getCurrentUser(new BuddyCallback<User>(User.class) {

                @Override
                public void completed(BuddyResult<User> result) {

                    if (result.getIsSuccess() && result.getResult() != null) {

                        currentUser = result.getResult();
                    }

                    if (callback != null) {

                        callback.complete(currentUser);
                    }
                }
            });
        }
    }
}
