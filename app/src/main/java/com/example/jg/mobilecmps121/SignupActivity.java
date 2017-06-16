package com.example.jg.mobilecmps121;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        findViewById(R.id.signupLayout).requestFocus();

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    //Reference from
    //https://sourcey.com/beautiful-android-login-and-signup-screens-with-material-design/
    public void signupUser(View view) {
        EditText nameText = (EditText) findViewById(R.id.input_name);

        if (nameText.getText().toString().isEmpty()) {
            Toast.makeText(this, "please enter a name", Toast.LENGTH_SHORT).show();
            nameText.requestFocus();
            return;
        }

        EditText emailText = (EditText) findViewById(R.id.input_email);

        if(!isValidEmail(emailText.getText().toString())) {
            Toast.makeText(this, emailText.getText().toString()
                    + " is not a valid email address", Toast.LENGTH_SHORT).show();
            emailText.requestFocus();
            return;
        }

        EditText passwordText = (EditText) findViewById(R.id.input_password);

        if (passwordText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter a valid password", Toast.LENGTH_SHORT).show();
            passwordText.requestFocus();
            return;
        }

        if (passwordText.getText().toString().length() < 6) {
            Toast.makeText(this, "password must be longer than 6 characters",
                    Toast.LENGTH_SHORT).show();
            passwordText.requestFocus();
            return;
        }

        final Button signupButton = (Button) findViewById(R.id.btn_signup);

        signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.Theme_AppCompat_DayNight_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating account...");
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(emailText.getText().toString(),
                passwordText.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            mDatabase.child("users").child("userId").child(user.getUid());
                            mDatabase.child("users").child("userId")
                                    .child(user.getUid()).setValue(user);

                            EditText nameText = (EditText) findViewById(R.id.input_name);
                            mDatabase.child("users").child("userId")
                                    .child(user.getUid()).child("name")
                                    .setValue(nameText.getText().toString());

                            mDatabase.child("users").child("userId")
                                    .child(user.getUid()).child("profile")
                                    .setValue("false");

                            //Start profile activity
                            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "User already exists.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        progressDialog.dismiss();
                        signupButton.setEnabled(true);
                    }
                });
    }

    public void loginUser(View view) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // Reference from
    // https://stackoverflow.com/questions/1819142/how-should-i-validate-an-e-mail-address
    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

}
