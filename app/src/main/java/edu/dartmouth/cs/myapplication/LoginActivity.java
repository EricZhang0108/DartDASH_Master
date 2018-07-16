package edu.dartmouth.cs.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import edu.dartmouth.cs.myapplication.Model.Preferences;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    // UI references
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_login);

        // Get references to EditTexts
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);

        // If coming from SettingsActivity (just logged out), update preferences
        if (getIntent().hasExtra("ACTIVITY_NAME")) {
            if (getIntent().getStringExtra("ACTIVITY_NAME").equals("SETTINGS_ACTIVITY")) {
                Preferences preferences = new Preferences(LoginActivity.this);
                preferences.setLoggedIn(false);
                preferences.setEmail(null);
                preferences.setPassword(null);
            }
        }
    }

    // Logs in previously created user account
    public void login(View view) {

        final String inputEmail = mEmailView.getText().toString();
        final String inputPassword = mPasswordView.getText().toString();

        final Preferences preferences = new Preferences(LoginActivity.this);

        //check to make sure the fields are not empty
        if(inputEmail.equals("") || inputPassword.equals("")){
            Toast.makeText(getApplicationContext(), "Please fill out the email and password to sign in!", Toast.LENGTH_LONG).show();
        }
        else {
            mAuth.signInWithEmailAndPassword(inputEmail, inputPassword)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Set logged in to true
                                preferences.setLoggedIn(true);
                                // Save the email and password!
                                preferences.setEmail(inputEmail);
                                preferences.setPassword(inputPassword);
                                // Go to MainActivity
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                            else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(LoginActivity.this, "Login information incorrect.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    // Registers new user account
    public void register(View view) {

        final String inputEmail = mEmailView.getText().toString();
        final String inputPassword = mPasswordView.getText().toString();

        final Preferences preferences = new Preferences(LoginActivity.this);

        //check to make sure the fields are not empty
        if(inputEmail.equals("") || inputPassword.equals("")){
            Toast.makeText(getApplicationContext(), "Please fill out the email and password to register!", Toast.LENGTH_LONG).show();
        }
        else {
            mAuth.createUserWithEmailAndPassword(inputEmail,
                    inputPassword)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Profile created!",
                                        Toast.LENGTH_SHORT).show();
                                // Set logged in to true
                                preferences.setLoggedIn(true);
                                // Save the email and password!
                                preferences.setEmail(inputEmail);
                                preferences.setPassword(inputPassword);
                                // Go to MainActivity
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                            else {
                                try {
                                    throw task.getException();
                                }
                                catch (FirebaseAuthWeakPasswordException e) {
                                    mPasswordView.setError("Password not strong enough!");
                                    mPasswordView.requestFocus();
                                }
                                catch (FirebaseAuthInvalidCredentialsException e) {
                                    mEmailView.setError("Email invalid.");
                                    mEmailView.requestFocus();
                                }
                                catch (FirebaseAuthUserCollisionException e) {
                                    mEmailView.setError("User already exists!");
                                    mEmailView.requestFocus();
                                }
                                catch(Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            }
                        }
                    });
        }
    }

}

