package com.orange.discover;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.buddy.sdk.*;
import com.buddy.sdk.models.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.orange.discover.SimpleAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.*;
import java.util.concurrent.TimeUnit;


// The main screen lists the apps users, so we can pick who to chat with
// and allows the current user to logout.
public class TutorList extends AppCompatActivity {


    private static final String TAG = "Tutor List";
    private Toolbar mToolbar;

    ListView buddyUsers;

    UsersSimpleAdapter chatAdapter;

    ArrayList<String> topicsToLearn;

    User currentBuddyUser;

    ParseUser currentParseUser;

    List<User> buddyUserList;

    public static boolean checkPlayServices(Context context, Activity activity) {

        final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();

        int resultCode = googleAPI.isGooglePlayServicesAvailable(context);

        if (resultCode != ConnectionResult.SUCCESS) {

            if (googleAPI.isUserResolvableError(resultCode)) {

                googleAPI.getErrorDialog(activity, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();

                Log.i(TAG, "This device is not supported for push");
            }
            else {

                final Activity alertActivity = activity;

                new AlertDialog.Builder(activity)
                        .setMessage("This sample requires the Google APK.")
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {

                                alertActivity.finish();
                                System.exit(0);
                            }
                        })
                        .show();
            }

            return false;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (!checkPlayServices(getApplicationContext(), this)) {

            return; // TODO: verify this is the right thing to do for onCreate
        }

        setContentView(R.layout.activity_tutor_list);
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        final Button logoutButton = (Button) findViewById(R.id.btnLogout);

        logoutButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Buddy.logoutUser(new BuddyCallback<Boolean>(Boolean.class) {

                    @Override
                    public void completed(BuddyResult<Boolean> result) {

                        Intent i = new Intent(getBaseContext(), LogIn.class);
                        BuddyApplication.instance.setCurrentUser(null);

                        // Trigger the login dialog. If the current user is not set, the
                        // Buddy.setUserAuthenticationRequriedCallback fires.

                        BuddyApplication.instance.getCurrentUser(true, null);
                        startActivity(i);
                    }
                });
            }
        });

        final TextView labelHello = (TextView) findViewById(R.id.lblHello);

        BuddyApplication.instance.getCurrentUser(false, new GetCurrentUserCallback() {

            @Override
            public void complete(User user) {
                if (user != null) {
                    labelHello.setText(String.format("Hello %s!", user.userName));
                    currentBuddyUser = user;
                }
            }
        });

        // set up our users list

        buddyUsers = (ListView) findViewById(R.id.lvUsers);

        buddyUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // on click of an item, fire up a chat

                User u = (User) buddyUsers.getItemAtPosition(i);

                startChat(u);
            }
        });



        chatAdapter = new UsersSimpleAdapter(getBaseContext());

        buddyUsers.setAdapter(chatAdapter);

        refreshList();
    }

    private void startChat(User u) {

        Intent ci = new Intent(getBaseContext(), Chat.class);

        ci.putExtra("userName", u.userName);
        ci.putExtra("name", u.firstName + " " + u.lastName);
        ci.putExtra("userId", u.id);

        startActivity(ci);
    }

    private void refreshList() {

        final ProgressDialog progressDialog = new ProgressDialog(TutorList.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Retrieving Tutors...");
        progressDialog.show();

        Log.w(TAG, "Searching users who teach the subject");

        currentParseUser = ParseUser.getCurrentUser();

        topicsToLearn = new ArrayList<String>();
        topicsToLearn = (ArrayList<String>)currentParseUser.get("topicsToLearn");

        // Get location of current user
        ParseGeoPoint userLocation = (ParseGeoPoint) currentParseUser.get("userLocation");

        // Query for users within 10 miles of the users location
        ParseQuery<ParseUser> parseUserQuery = ParseUser.getQuery();
        parseUserQuery.whereNear("userLocation", userLocation);
        parseUserQuery.setLimit(10);
        parseUserQuery.whereContainedIn("topicsToTeach", topicsToLearn);

        // Execute the query
        parseUserQuery.findInBackground(new FindCallback<ParseUser>() {

            public void done(List<ParseUser> queriedParseUserList, ParseException e) {

                if (e == null) {

                    Log.w(TAG, "USER RETRIEVAL SUCCESSFUL");

                    Log.w(TAG, String.valueOf(queriedParseUserList.size()));

                    buddyUserList = new ArrayList<User>();

                    for (Iterator<ParseUser> parseUserListIterator = queriedParseUserList.iterator(); parseUserListIterator.hasNext(); ) {

                        Log.w(TAG, "Iteration " + String.valueOf(queriedParseUserList.size()));

                        ParseUser p = parseUserListIterator.next();
                        String buddyUserId = p.get("userId").toString();
                        Log.w(TAG, buddyUserId);

                        // Remove current user from user list

                        if (p.getUsername().equals(currentParseUser.getUsername())) {

                            Log.w(TAG, "Removing current user from user list");
                            parseUserListIterator.remove();
                            continue;
                        }

                        Buddy.get("/users/"+buddyUserId, null, new BuddyCallback<User>(User.class) {

                            @Override
                            public void completed(BuddyResult<User> result) {
                                Log.w(TAG, "INSIDE BUDDY USER QUERY");
                                buddyUserList.add(result.getResult());
                                chatAdapter.setItemList(buddyUserList);
                                chatAdapter.notifyDataSetChanged();

                            }
                        });
                    }

                    Log.w(TAG, "outside Loop");


                }
                else {
                    Log.w(TAG, "USER RETRIEVAL UNSUCCESSFUL");

                    e.printStackTrace();
                }
                progressDialog.dismiss();

            }
        });
    }

    private BroadcastReceiver onEvent = new BroadcastReceiver() {

        public void onReceive(Context ctx, Intent i) {
            Log.d(TAG, "on Receive");

            String payload = i.getStringExtra("payload");

            if (payload == null) {
                return;
            }

            // if we get a chat message, then go ahead and
            // fire up the chat activity.

            String[] parts = payload.split("\t");

            String msg = parts[0];

            String id = parts[1];

            // find the user, then start the chat.
            if (chatAdapter.getItemList() != null) {
                for (User u : chatAdapter.getItemList()) {
                    if (u.id.equals((id))) {
                        startChat(u);
                        break;
                    }
                }
            }
        }
    };

    @Override
    public void onPause() {

        Log.w(TAG, "On pause");

        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(onEvent);

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.w(TAG, "On resume");


        if (!checkPlayServices(getApplicationContext(), this)) {
            return; // TODO: verify this is the right thing to do for onCreate
        }

        IntentFilter f = new IntentFilter(GcmListenerService.ACTION_MESSAGE_RECEIVED);

        LocalBroadcastManager.getInstance(this).registerReceiver(onEvent, f);

        // make sure we have a current user.  Doing this will fire up the login dialog
        // if we don't have one for some reason
        BuddyApplication.instance.getCurrentUser(false, new GetCurrentUserCallback() {

            public void complete(User u) {
                currentBuddyUser = u;
                if (u != null) {
                    refreshList();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class UsersSimpleAdapter extends SimpleAdapter<User> {

        public UsersSimpleAdapter(Context c) {
            super(null, c);
        }

        protected <T> void populateView(View v, T u) {
            User user = (User) u;
            TextView text1 = (TextView) v.findViewById(android.R.id.text1);
            text1.setText(String.format("%s (Teaches %s)", user.userName, topicsToLearn));

            TextView text2 = (TextView) v.findViewById(android.R.id.text2);

            Date now = new Date();
            long millis = now.getTime() - user.lastLogin.getTime();
            String hms = String.format("%02d hrs :%02d mins :%02d secs", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
            text2.setText(String.format("Last Seen: %s ago", hms));

        }
    }
}
