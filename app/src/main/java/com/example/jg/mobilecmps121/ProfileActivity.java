package com.example.jg.mobilecmps121;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    private int mYear = 1990;
    private int mMonth = 0;
    private int mDay = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        findViewById(R.id.profileLayout).requestFocus();

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView toolbar_skip = (TextView) findViewById(R.id.toolbar_skip);
        toolbar_skip.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            //Start login activity
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            DatabaseReference ref = mDatabase.getReference("users/userId/" + currentUser.getUid());
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
                    toolbar_title.setText("Hello "+dataSnapshot.child("name").getValue().toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void finishProfile(View view) {
        EditText phoneText = (EditText) findViewById(R.id.input_phone);

        if(!validatePhoneNum(phoneText.getText().toString()) ||
                phoneText.getText().toString().length() != 10) {
            Toast.makeText(this, phoneText.getText().toString()
                    + " is not a valid US phone number", Toast.LENGTH_SHORT).show();
            phoneText.requestFocus();
            return;
        }

        EditText addressText = (EditText) findViewById(R.id.input_address);

        if (addressText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter an address", Toast.LENGTH_SHORT).show();
            addressText.requestFocus();
            return;
        }

        EditText birthdayText = (EditText) findViewById(R.id.editText_birthday);

        if (birthdayText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please select your birthday", Toast.LENGTH_SHORT).show();
            birthdayText.performClick();
            return;
        }

        RadioGroup mRadioGroup = (RadioGroup) findViewById(R.id.radioSex);
        if (mRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            mRadioGroup.requestFocus();
            return;
        }

        hideSoftKeyboard(this);

        View radioButton = mRadioGroup.findViewById(mRadioGroup.getCheckedRadioButtonId());
        int radioId = mRadioGroup.indexOfChild(radioButton);
        RadioButton btn = (RadioButton) mRadioGroup.getChildAt(radioId);
        String genderText = btn.getText().toString();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        DatabaseReference ref = mDatabase.getReference("users/userId/" + currentUser.getUid());
        ref.child("phoneNumber").setValue(phoneText.getText().toString());
        ref.child("address").setValue(addressText.getText().toString());
        ref.child("birthday").setValue(birthdayText.getText().toString());
        ref.child("gender").setValue(genderText);

        ref.child("profile").setValue("true");

        //Start categories activity
        Intent intent = new Intent(getApplicationContext(), Categories.class);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void skipProfile(View view) {
        //Start categories activity
        Intent intent = new Intent(getApplicationContext(), Categories.class);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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

    public void selectBirthday(View view) {
        DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
                mYear = year;
                mMonth = month;
                mDay = dayOfMonth;

                String date = mMonth + 1 + "/" + mDay + "/" + mYear;

                SimpleDateFormat birthdayFormat = new SimpleDateFormat("dd/mm/yyyy");
                try {
                    Date mDate = birthdayFormat.parse(date);
                    date = birthdayFormat.format(mDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                EditText editText_birthday = (EditText) findViewById(R.id.editText_birthday);
                editText_birthday.setText(date);
            }
        };

        DatePickerDialog dialog = new DatePickerDialog(this, mDateListener, mYear, mMonth, mDay);
        dialog.show();
    }

    public boolean validatePhoneNum(String number) {
        return android.util.Patterns.PHONE.matcher(number).matches();
    }

    @Override
    public void onBackPressed() {

    }
}
