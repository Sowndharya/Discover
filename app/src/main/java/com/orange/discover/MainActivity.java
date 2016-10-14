package com.orange.discover;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.buddy.sdk.Buddy;
import com.buddy.sdk.BuddyCallback;
import com.buddy.sdk.BuddyResult;

public class MainActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener {


    private static final String TAG = "Main activity";

    private SeekBar bar;

    private TextView textProgress;

    private int radius;

    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.w(TAG, "INSIDE ON CREATE");

        mToolbar = (Toolbar) findViewById(R.id.tool_bar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        // display the first navigation drawer view on app launch
        displayView(0);
    }
/*
        bar = (SeekBar)findViewById(R.id.seekBar); // make seekbar object
        bar.setOnSeekBarChangeListener(this);
        textProgress = (TextView)findViewById(R.id.text_radius);

        Button b1 = (Button) findViewById(R.id.button1);
        Button b2 = (Button) findViewById(R.id.button2);


        b1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                Log.w(TAG, "SEARCH TUTORS BUTTON CLICKED");

                if (radius == 0) { radius = 1; }

                Intent intent = new Intent(getApplicationContext(), TutorList.class);

                intent.putExtra("radius", radius);

                startActivity(intent);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Log.w(TAG, "SEEKER LIST BUTTON CLICKED");

                Intent intent = new Intent(getApplicationContext(), SeekerList.class);
                startActivity(intent);

            }
        });


    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        // change progress text label with current seekbar value
        Log.w(TAG, "PROGRESS CHANGING");

        radius = progress;
        textProgress.setText(progress + " km");

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        seekBar.setSecondaryProgress(seekBar.getProgress());

    }
*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_logout) {
            logout();

        }

        return super.onOptionsItemSelected(item);
    }

    public void logout() {

        Log.w(TAG, "Logging out");

        Buddy.logoutUser(new BuddyCallback<Boolean>(Boolean.class) {

            @Override
            public void completed(BuddyResult<Boolean> result) {

                Log.w(TAG, "Starting the login activity");

                Intent i = new Intent(getBaseContext(), LogIn.class);
                BuddyApplication.instance.setCurrentUser(null);

                // Trigger the login dialog. If the current user is not set, the
                // Buddy.setUserAuthenticationRequriedCallback fires.
                BuddyApplication.instance.getCurrentUser(true, null);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    private void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                title = getString(R.string.title_home);
                break;
            case 1:
                logout();
            /*
            case 1:
                fragment = new FriendsFragment();
                title = getString(R.string.title_friends);
                break;
            case 2:
                fragment = new MessagesFragment();
                title = getString(R.string.title_messages);
                break;
                */
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

            // set the toolbar title
            getSupportActionBar().setTitle(title);
        }
    }
}
