package com.example.jg.mobilecmps121;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class OrdersList extends AppCompatActivity {
    private DBHelper _db = DBHelper.getInstance(this);
    CustomAdapter customAdapter;
    private ArrayList<Products> results;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_list);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        getOrderData();
    }

    private void getOrderData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        DatabaseReference ref = mDatabase.getReference("users/userId/" + currentUser.getUid() + "/purchase_products");

        results = new ArrayList<Products>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String price = snapshot.child("product_price").getValue().toString();

                    results.add(new Products(snapshot.child("product_name").getValue().toString(),
                            snapshot.child("product_image").getValue().toString(),
                            Integer.parseInt(price)));

                    Calendar c = Calendar.getInstance();
                    final String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    customAdapter = new CustomAdapter(getApplicationContext(), R.layout.activity_orders_lisitem, results) {
                        public View getView(final int position, View convertView, ViewGroup parent) {
                            LayoutInflater inflater = LayoutInflater.from(getContext());
                            View customView = inflater.inflate(R.layout.activity_orders_lisitem, parent, false);
                            final Products item = getItem(position);

                            TextView title = (TextView) customView.findViewById(R.id.itemTitle);
                            TextView itemDate = (TextView) customView.findViewById(R.id.itemDate);
                            ImageView image = (ImageView) customView.findViewById(R.id.ordersItem);
                            title.setText(item.get_productName());
                            itemDate.setText("Ordered on " + date);

                            new ImageLoadTask(item.getImage(), image).execute();
                            return customView;

                        }
                    };
                    ListView listView = (ListView) findViewById(R.id.ordersList);
                    listView.setAdapter(customAdapter);
                    customAdapter.notifyDataSetChanged();


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void openDatabase() {
        SQLiteDatabase db = _db.getReadableDatabase();
        String query = "SELECT * FROM " + _db.TABLE_PRODUCTS + " WHERE 1";
        Cursor c = db.rawQuery(query, null);
        //Move to the first row in your results
        c.moveToFirst();

        results = new ArrayList<Products>();

        //Position after the last row means the end of the results
        while (!c.isAfterLast()) {
            if (c.getString(c.getColumnIndex(_db.COLUMN_PRODUCTNAME)) != null) {
                results.add(new Products(c.getString(c.getColumnIndex(_db.COLUMN_PRODUCTNAME)),
                        c.getString(c.getColumnIndex(_db.COLUMN_IMAGE)),
                        c.getInt(c.getColumnIndex(_db.COLUMN_PRICE))));
            }
            c.moveToNext();
        }
        c.close();
        db.close();
    }

}

