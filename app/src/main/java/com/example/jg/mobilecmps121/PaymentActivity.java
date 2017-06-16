package com.example.jg.mobilecmps121;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

// Credit Card Form reference from:
//   https://github.com/yekmer/credit_card_lib

//Edit Text format reference from:
//   https://stackoverflow.com/questions/11790102/format-credit-card-in-edit-text-in-android
public class PaymentActivity extends AppCompatActivity {

    final private String stripeApiSecretKey = "sk_test_aQsljbndMd15tGO2f3nxQm18";
    private int totalCost;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private SweetAlertDialog mSweetAlertDialog;
    private DBHelper _db = DBHelper.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        Bundle extra = getIntent().getExtras();
        totalCost = (int)extra.getDouble("total_cost");

        final EditText shippingAddress = (EditText) findViewById(R.id.input_shipping_address);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        DatabaseReference ref = mDatabase.getReference("users/userId/" + currentUser.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                shippingAddress.setText(dataSnapshot.child("address").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        EditText cardNumberField = (EditText) findViewById(R.id.credit_card_edit_text);
        cardNumberField.addTextChangedListener(
                new TextWatcher() {

                    private static final int TOTAL_SYMBOLS = 19; // size of pattern 0000-0000-0000-0000
                    private static final int TOTAL_DIGITS = 16; // max numbers of digits in pattern: 0000 x 4
                    private static final int DIVIDER_MODULO = 5; // means divider position is every 5th symbol beginning with 1
                    private static final int DIVIDER_POSITION = DIVIDER_MODULO - 1; // means divider position is every 4th symbol beginning with 0
                    private static final char DIVIDER = '-';

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!isInputCorrect(s, TOTAL_SYMBOLS, DIVIDER_MODULO, DIVIDER)) {
                            s.replace(0, s.length(), buildCorrecntString(getDigitArray(s, TOTAL_DIGITS), DIVIDER_POSITION, DIVIDER));
                        }
                    }


                    private boolean isInputCorrect(Editable s, int totalSymbols, int dividerModulo, char divider) {
                        boolean isCorrect = s.length() <= totalSymbols; // check size of entered string
                        for (int i = 0; i < s.length(); i++) { // chech that every element is right
                            if (i > 0 && (i + 1) % dividerModulo == 0) {
                                isCorrect &= divider == s.charAt(i);
                            } else {
                                isCorrect &= Character.isDigit(s.charAt(i));
                            }
                        }
                        return isCorrect;
                    }

                    private String buildCorrecntString(char[] digits, int dividerPosition, char divider) {
                        final StringBuilder formatted = new StringBuilder();

                        for (int i = 0; i < digits.length; i++) {
                            if (digits[i] != 0) {
                                formatted.append(digits[i]);
                                if ((i > 0) && (i < (digits.length - 1)) && (((i + 1) % dividerPosition) == 0)) {
                                    formatted.append(divider);
                                }
                            }
                        }

                        return formatted.toString();
                    }

                    private char[] getDigitArray(final Editable s, final int size) {
                        char[] digits = new char[size];
                        int index = 0;
                        for (int i = 0; i < s.length() && index < size; i++) {
                            char current = s.charAt(i);
                            if (Character.isDigit(current)) {
                                digits[index] = current;
                                index++;
                            }
                        }
                        return digits;
                    }
                }
        );


        EditText cardExpirationField = (EditText) findViewById(R.id.credit_card_expiration_date_editText);
        cardExpirationField.addTextChangedListener(
                new TextWatcher() {

                    private static final int TOTAL_SYMBOLS = 5; // size of pattern 01/19
                    private static final int TOTAL_DIGITS = 4; // max numbers of digits in pattern: 0000 x 4
                    private static final int DIVIDER_MODULO = 3; // means divider position is every 5th symbol beginning with 1
                    private static final int DIVIDER_POSITION = DIVIDER_MODULO - 1; // means divider position is every 4th symbol beginning with 0
                    private static final char DIVIDER = '/';

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!isInputCorrect(s, TOTAL_SYMBOLS, DIVIDER_MODULO, DIVIDER)) {
                            s.replace(0, s.length(), buildCorrecntString(getDigitArray(s, TOTAL_DIGITS), DIVIDER_POSITION, DIVIDER));
                        }
                    }

                    private boolean isInputCorrect(Editable s, int totalSymbols, int dividerModulo, char divider) {
                        boolean isCorrect = s.length() <= totalSymbols; // check size of entered string
                        for (int i = 0; i < s.length(); i++) { // chech that every element is right
                            if (i > 0 && (i + 1) % dividerModulo == 0) {
                                isCorrect &= divider == s.charAt(i);
                            } else {
                                isCorrect &= Character.isDigit(s.charAt(i));
                            }
                        }
                        return isCorrect;
                    }

                    private String buildCorrecntString(char[] digits, int dividerPosition, char divider) {
                        final StringBuilder formatted = new StringBuilder();

                        for (int i = 0; i < digits.length; i++) {
                            if (digits[i] != 0) {
                                formatted.append(digits[i]);
                                if ((i > 0) && (i < (digits.length - 1)) && (((i + 1) % dividerPosition) == 0)) {
                                    formatted.append(divider);
                                }
                            }
                        }

                        return formatted.toString();
                    }

                    private char[] getDigitArray(final Editable s, final int size) {
                        char[] digits = new char[size];
                        int index = 0;
                        for (int i = 0; i < s.length() && index < size; i++) {
                            char current = s.charAt(i);
                            if (Character.isDigit(current)) {
                                digits[index] = current;
                                index++;
                            }
                        }
                        return digits;
                    }
                }
        );
    }

    public void submitCard(View view) {
        final String publishableApiKey = getString(R.string.com_stripe_publishable_key);

        EditText cardNumberField = (EditText) findViewById(R.id.credit_card_edit_text);
        EditText expirationDate = (EditText) findViewById(R.id.credit_card_expiration_date_editText);
        EditText cvcField = (EditText) findViewById(R.id.credit_card_cvc);

        Card card = new Card(cardNumberField.getText().toString(),
                Integer.valueOf(expirationDate.getText().toString().substring(0,2)),
                Integer.valueOf(expirationDate.getText().toString().substring(3)),
                cvcField.getText().toString());

        if (!card.validateNumber()) {
            cardNumberField.getBackground()
                    .setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC);
            return;
        } else {
            cardNumberField.getBackground().clearColorFilter();
        }

        if (!card.validateExpiryDate()) {
            expirationDate.getBackground()
                    .setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC);
            return;
        } else {
            expirationDate.getBackground().clearColorFilter();
        }

        if (!card.validateCVC()) {
            cvcField.getBackground()
                    .setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC);
            return;
        } else {
            cvcField.getBackground().clearColorFilter();
        }

        Stripe stripe = new Stripe();
        stripe.createToken(card, publishableApiKey, new TokenCallback() {
            public void onSuccess(Token token) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("amount", totalCost);
                params.put("currency", "usd");
                params.put("description", "Green Electronics");
                params.put("card", token.getId());

                Button payButton = (Button) findViewById(R.id.pay_button);
                payButton.setEnabled(false);

                new makePayment().execute(params);
            }

            public void onError(Exception error) {
                new SweetAlertDialog(PaymentActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Payment Failed!")
                        .setContentText("Try again later!")
                        .show();
                Log.d("Stripe", error.getLocalizedMessage());
            }
        });
    }

    //Reference from
    //  http://www.vogella.com/tutorials/AndroidBackgroundProcessing/article.html
    private class makePayment extends AsyncTask<Map<String, Object>, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Map<String, Object>... params) {
            try {
                com.stripe.Stripe.apiKey = stripeApiSecretKey;
                for (Map<String, Object> i : params) {
                    Charge charge = Charge.create(i);
                }

                return true;
            } catch (AuthenticationException e) {
                e.printStackTrace();
            } catch (InvalidRequestException e) {
                e.printStackTrace();
            } catch (APIConnectionException e) {
                e.printStackTrace();
            } catch (CardException e) {
                e.printStackTrace();
            } catch (APIException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            //Show a alert dialog of success or failure
            if(result) {
                 mSweetAlertDialog =
                        new SweetAlertDialog(PaymentActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Payment Success!")
                        .setContentText("Your order is on the way! " +
                                "\n\nThank you for shopping with Green Electronics")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                saveProducts();
                                Intent intent = new Intent(getApplicationContext(), Categories.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        });

            } else {
                mSweetAlertDialog =
                new SweetAlertDialog(PaymentActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Payment Failed!")
                        .setContentText("Try again later!");

            }

            mSweetAlertDialog.show();

            Button payButton = (Button) findViewById(R.id.pay_button);
            payButton.setEnabled(true);
        }
    }

    public void saveProducts() {
        SQLiteDatabase db = _db.getReadableDatabase();
        String query = "SELECT * FROM " + _db.TABLE_PRODUCTS + " WHERE 1";
        Cursor c = db.rawQuery(query, null);
        //Move to the first row in your results
        c.moveToFirst();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        DatabaseReference ref = mDatabase.getReference("users/userId/" + currentUser.getUid() + "/purchase_products");

        //Position after the last row means the end of the results
        while (!c.isAfterLast()) {
            String key = ref.push().getKey();
            if (c.getString(c.getColumnIndex(_db.COLUMN_PRODUCTNAME)) != null) {
                ref.child(key).child("product_name").setValue(c.getString(c.getColumnIndex(_db.COLUMN_PRODUCTNAME)));
                ref.child(key).child("product_image").setValue(c.getString(c.getColumnIndex(_db.COLUMN_IMAGE)));
                ref.child(key).child("product_price").setValue(c.getInt(c.getColumnIndex(_db.COLUMN_PRICE)));
            }
            c.moveToNext();
        }
        c.close();
        db.close();

        _db.deleteTable();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSweetAlertDialog != null) {
            mSweetAlertDialog.dismiss();
            mSweetAlertDialog = null;
        }
    }

}