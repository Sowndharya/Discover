package com.orange.discover;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.buddy.sdk.Buddy;
import com.buddy.sdk.BuddyCallback;
import com.buddy.sdk.BuddyResult;
import com.buddy.sdk.models.User;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.Arrays;

public class SignUp extends AppCompatActivity {


    private static final String TAG = "SignupActivity";


    private EditText nameText;
    private EditText emailText;
    private EditText passwordText;
    private Button   signupButton;
    private TextView loginLink;

    public String userID;
    private Toolbar mToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Log.w(TAG, "INSIDE THE SIGNUP ACTIVITY");

        mToolbar = (Toolbar) findViewById(R.id.tool_bar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        nameText     = (EditText) findViewById(R.id.input_name);
        emailText    = (EditText) findViewById(R.id.input_email);
        passwordText = (EditText) findViewById(R.id.input_password);
        signupButton = (Button)   findViewById(R.id.btn_signup);
        loginLink    = (TextView) findViewById(R.id.link_login);

        signupButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.w(TAG, "SIGNUP BUTTON CLICKED");


                signup();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Finish the registration screen and return to the Login activity
                Log.w(TAG, "LOGIN LINK CLICKED");

                finish();
            }
        });
    }

    public void signup() {

        Log.w(TAG, "SIGNING UP");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        //signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignUp.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        final String name     = nameText.getText().toString();
        final String email    = emailText.getText().toString();
        final String password = passwordText.getText().toString();




        Buddy.createUser(name, password, "", "", email, null, null, TAG, new BuddyCallback<User>(User.class) {

            @Override
            public void completed(BuddyResult<User> result) {

                if (result.getIsSuccess()) {

                    Log.w(TAG, "USER CREATED: " + result.getResult().userName);

                    userID = result.getResult().id;

                    BuddyApplication.instance.setCurrentUser(result.getResult());

                    ParseUser user = new ParseUser();

                    user.setUsername(name);
                    user.setPassword(password);
                    user.setEmail(email);

                    user.put("userId", userID);

                    user.signUpInBackground(new SignUpCallback() {

                        @Override
                        public void done(ParseException e) {

                            if (e == null) {

                                Log.w(TAG, "PARSE REGISTRATION SUCCESSFUL");
                                onSignupSuccess();
                            }
                            else {

                                Log.w(TAG, e.getMessage());
                            }
                        }
                    });
                }
            }
        });
    }


    public void onSignupSuccess() {

        signupButton.setEnabled(true);
        setResult(RESULT_OK, null);

        Log.w(TAG, "EXITING THE SIGNUP ACTIVITY");

        finish();
    }

    public void onSignupFailed() {

        Toast.makeText(getBaseContext(), "Sign Up failed", Toast.LENGTH_LONG).show();

        signupButton.setEnabled(true);
    }

    public boolean validate() {

        boolean valid = true;

        String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {

            nameText.setError("at least 3 characters");
            valid = false;

        } else {

            nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            emailText.setError("Enter a valid email address");
            valid = false;

        } else {

            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {

            passwordText.setError("Between 4 and 10 alphanumeric characters");
            valid = false;

        } else {

            passwordText.setError(null);
        }

        return valid;
    }

}
