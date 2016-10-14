package com.orange.discover;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.buddy.sdk.Buddy;
import com.buddy.sdk.BuddyCallback;
import com.buddy.sdk.BuddyResult;
import com.buddy.sdk.models.User;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LogIn extends AppCompatActivity {


    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private Toolbar mToolbar;
    private Button loginButton;
    private EditText nameText;
    private EditText passwordText;
    private TextView signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_log_in);

        mToolbar = (Toolbar) findViewById(R.id.tool_bar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Log.w(TAG, "INSIDE THE LOGIN ACTIVITY");


        loginButton  = (Button)   findViewById(R.id.btn_login);

        nameText     = (EditText) findViewById(R.id.input_name);

        passwordText = (EditText) findViewById(R.id.input_password);

        signupLink   = (TextView) findViewById(R.id.link_signup);


        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Log.w(TAG, "LOGIN BUTTON CLICKED");

                login();
            }
        });

        signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Start the Signup activity
                Log.w(TAG, "SIGNUP BUTTON CLICKED");
                Log.w(TAG, "STARTING THE SIGNUP ACTIVITY");


                Intent intent = new Intent(getApplicationContext(), SignUp.class);
                startActivityForResult(intent, REQUEST_SIGNUP);//check this

            }
        });


    }


    public void login() {

        Log.w(TAG, "LOGIN");

        if (!validate()) {

            Log.w(TAG,"VALIDATING");
            onLoginFailed();
            return;
        }

        //loginButton.setEnabled(false);

        // Progress Dialog box indicating authentication

        final ProgressDialog progressDialog = new ProgressDialog(LogIn.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();


        // Obtain the values from the text boxes

        final String name     = nameText.getText().toString();
        final String password = passwordText.getText().toString();





        // Buddy login

        Buddy.loginUser(name, password, new BuddyCallback<User>(User.class) {

            @Override
            public void completed(BuddyResult<User> result) {

                if(result.getIsSuccess()) {
                    Log.w(TAG, "BUDDY LOGIN SUCCESSFUL");


                    BuddyApplication.instance.setCurrentUser(result.getResult());

                    // Parse login

                    ParseUser.logInInBackground(name, password, new LogInCallback() {

                        public void done(ParseUser user, ParseException e) {

                            if (user != null) {

                                Log.w(TAG, "PARSE LOGIN SUCCESSFUL");
                                Log.w(TAG, "STARTING THE LOCATION TRACKER ACTIVITY FROM LOGIN");
                                Intent intent = new Intent(getApplicationContext(), LocationTracker.class);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                // Signup failed. Look at the ParseException to see what happened.
                            }
                        }
                    });
                } else {

                    // Show error
                    String error = result.getError();

                    // Username or password false, display and an error
                    final AlertDialog.Builder dlgAlert = new AlertDialog.Builder(LogIn.this);

                    dlgAlert.setTitle("Error Logging In");
                    dlgAlert.setPositiveButton("OK", null);
                    dlgAlert.setCancelable(true);


                    if ("AuthBadUsernameOrPassword".equalsIgnoreCase(error)) {

                        dlgAlert.setMessage("Username or password incorrect, please try again.");

                    } else {

                        dlgAlert.setMessage(String.format("Error attempting login: %s.", result.getError()));
                    }
                    dlgAlert.create().show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_SIGNUP) {

            if (resultCode == RESULT_OK) {
                Log.w(TAG, "STARTING THE LEARNING TOPICS ACTIVITY FROM SIGNUP");

                Intent intent = new Intent(getApplicationContext(), LearningTopicsActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    public void onLoginSuccess() {

        loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {

        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        loginButton.setEnabled(true);
    }

    public boolean validate() {

        boolean valid = true;

        String name = nameText.getText().toString();
        String password = passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {

            nameText.setError("at least 3 characters");
            valid = false;

        } else {

            nameText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {

            passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;

        } else {

            passwordText.setError(null);
        }

        return valid;
    }
}
