package com.example.jg.mobilecmps121;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;

public class shop_cart extends AppCompatActivity {
    private DBHelper _db = DBHelper.getInstance(this);
    CustomAdapter customAdapter;
    private ArrayList<Products> results;
    private double totalPrice;
    private String amount;
    private double origPrice = 0;
    private String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_cart);

        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("Shopping Cart");

        openDatabase();
        origPrice = totalPrice;
        final TextView subTotal = (TextView) findViewById(R.id.subtotal);
        Log.i("size", results.size() + "");
        subTotal.setText("Cart subtotal (" + results.size() + ") : $" + totalPrice);
        Log.d("count1", totalPrice + "");
        //TextView item = (TextView) customView.findViewById(R.id.shopItem);
        //ImageView image = (ImageView) customView.findViewById(R.id.shopImage);
        customAdapter = new CustomAdapter(this,R.layout.shop_cart_listitem,results) {
            public View getView(final int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View customView = inflater.inflate(R.layout.shop_cart_listitem, parent, false);
                final Products item = getItem(position);
                TextView itemText = (TextView) customView.findViewById(R.id.shopItem);

                final TextView priceItem = (TextView) customView.findViewById(R.id.priceItem);
                ImageView image = (ImageView) customView.findViewById(R.id.shopImage);
                Button deleteItem = (Button) customView.findViewById(R.id.removeItem);
                itemText.setText(item.get_productName());
                new ImageLoadTask(item.getImage(), image).execute();

                if(origPrice == 0) {
                    origPrice = item.getPrice();
                }

                priceItem.setText("$" + item.getPrice());
                Log.i("size", results.size() + "");
                //subTotal.setText("Cart subtotal (" + (item.getCount() - 1 + results.size())  + ") : $" + totalPrice);
                Log.i("size2", ((item.getCount() - 1) + results.size()) + "");

                MaterialSpinner dropdown = (MaterialSpinner) customView.findViewById(R.id.spinner1);
                dropdown.setBackgroundColor(Color.parseColor("#E0E0E0"));

                //Reference from https://stackoverflow.com/questions/4743116/get-screen-width-and-height
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int height = size.y;
                dropdown.setDropdownMaxHeight(height/4);
                dropdown.setItems("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

                dropdown.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(MaterialSpinner view, int position, long id, Object mItem) {
                        amount = mItem.toString();
                        int amount2 = Integer.parseInt(amount);
                        if(amount != null) {
                            //Log.d("test","123");
                            if(!amount.equals("10+")) {

                                int oldCount = item.getCount();
                                if(oldCount == 0) {
                                    oldCount = 1;
                                }
                                item.setCount(amount2);
                                Log.d("count", oldCount + " Old. " + item.getCount() + " new count");
                                //if user selected a number less than their previous choice in drop down
                                if(oldCount > item.getCount()) {
                                    //Log.d("test", oldCount + " Old. " + item.getCount() + " new count");
                                    //Log.d("test", totalPrice + " before3213");
                                    priceItem.setText("$" + item.getPrice() * amount2);
                                    totalPrice -= item.getPrice() * (oldCount - item.getCount());
                                    Log.d("test", totalPrice + " after123123");
                                    subTotal.setText("Cart subtotal (" + (item.getCount() - 1 + results.size())  + ") : $" + totalPrice);
                                } else {
                                    Log.d("test2", totalPrice + " totalPrice");
                                    Log.d("test2", oldCount + " Old. " + item.getCount() + " new count");
                                    priceItem.setText("$" + item.getPrice() * item.getCount());
                                    if(amount2 != 1) {
                                        totalPrice += item.getPrice() * (amount2 - oldCount);
                                    }
                                    subTotal.setText("Cart subtotal (" + (item.getCount() - 1 + results.size())  + ") : $" + totalPrice);
                                    Log.d("test2", totalPrice + " after");
                                }
                            }
                        }
                    }
                });

                //image.setImageResource(item.getImage());
                deleteItem.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View view) {
                        _db.deleteProduct(item.get_productName());
                        results.remove(item);
                        totalPrice = totalPrice - (item.getPrice() * item.getCount());
                        subTotal.setText("Cart subtotal (" + results.size() + ") : $" + totalPrice);
                        customAdapter.notifyDataSetChanged();
                    }
                });

                return customView;
            }
        };
        ListView listView = (ListView) findViewById(R.id.shop_cart);
        listView.setAdapter(customAdapter);
        customAdapter.notifyDataSetChanged();

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
                totalPrice += c.getInt(c.getColumnIndex(_db.COLUMN_PRICE));
            }
            c.moveToNext();
        }
        c.close();
        db.close();
    }

    public void continueShop(View view) {
        super.onBackPressed();
    }

    public void checkOut(View view) {
        if (totalPrice == 0) {
            Toast.makeText(this, "Your shopping cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("total_cost", totalPrice);
        startActivity(intent);
    }

}

