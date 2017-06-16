package com.example.jg.mobilecmps121;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.loginLayout).requestFocus();

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
    }

    //Reference from
    //https://sourcey.com/beautiful-android-login-and-signup-screens-with-material-design/
    public void loginUser(View view) {

        EditText emailText = (EditText) findViewById(R.id.input_email);
        EditText passwordText = (EditText) findViewById(R.id.input_password);

        if(!isValidEmail(emailText.getText().toString())) {
            Toast.makeText(this, emailText.getText().toString()
                    + " is not a valid email address", Toast.LENGTH_SHORT).show();
            emailText.requestFocus();
            return;
        }

        if (passwordText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter a valid password!", Toast.LENGTH_SHORT).show();
            passwordText.requestFocus();
            return;
        }

        final Button loginButton = (Button) findViewById(R.id.btn_login);

        loginButton.setEnabled(false);
        hideSoftKeyboard(this);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.Theme_AppCompat_DayNight_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(emailText.getText().toString()
                , passwordText.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                progressDialog.dismiss();

                                //Shows profile activity if user hasnt setup a profile yet
                                setUserProfile(user);

                                Toast.makeText(LoginActivity.this, "Login Success!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // If sign in fails, display a message to the user.

                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Incorrect email or password",
                                    Toast.LENGTH_SHORT).show();
                        }

                        loginButton.setEnabled(true);
                    }
                });

    }

    public void signupUser(View view) {
        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(intent);
    }

    // Reference from
    // https://stackoverflow.com/questions/1819142/how-should-i-validate-an-e-mail-address
    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    //Shows profile activity if user hasnt setup a profile yet
    public void setUserProfile(FirebaseUser currentUser) {
        DatabaseReference ref = mDatabase.getReference("users/userId/" + currentUser.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("profile").getValue().toString().equals("false")) {

                    //Start profile activity
                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {

                    //Start categories activity
                    Intent intent = new Intent(getApplicationContext(), Categories.class);
                    intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    //reference from
    //https://stackoverflow.com/questions/
    // 4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

}
