package com.example.jg.mobilecmps121;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Products> aList;
    private ArrayList<Products> results;
    private DBHelper _db;
    private String category;

    //https://stackoverflow.com/questions/13814503/reading-a-json-file-in-android/13814551#13814551
    public String loadJSONFromAsset(String file) {
        String json = "";
        try {
            InputStream is = getAssets().open(category + "/" + file);
            int size = is.available();

            if(size>1000) {
                return null;
            }
            byte[] buffer = new byte[1000];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return json;

    }

    public void readJSON(String file) {
        try {
            String jFile = loadJSONFromAsset(file);
            if(jFile == null) {
                return;
            }
            JSONObject obj = new JSONObject(jFile);
            JSONArray reviews = obj.getJSONArray("Reviews");

            String name = obj.getJSONObject("ProductInfo").getString("Name");
            int price = 0;
            try {
                price = obj.getJSONObject("ProductInfo").getInt("Price");
            } catch(Exception e) {
                Log.i("error", "it was a string: " + price);
            }


            String features = obj.getJSONObject("ProductInfo").getString("Features");
            String imgurl = obj.getJSONObject("ProductInfo").getString("ImgURL");
            if(!imgurl.equals("null") && !name.equals("null")) {

                name = name.replaceAll("\"","");
                aList.add(new Products(name,imgurl,price));
            }

        } catch(JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.shopping_cart:
                        Intent intent = new Intent(getApplicationContext(), shop_cart.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        break;
                    case R.id.btn_log_out:
                        _db.deleteTable();
                        FirebaseAuth.getInstance().signOut();
                        Intent mIntent = new Intent(getApplicationContext(), LoginActivity.class);
                        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mIntent);
                        break;
                    case R.id.ordersList:
                        intent = new Intent(getApplicationContext(), OrdersList.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });

        _db = _db.getInstance(this);
        aList = new ArrayList<Products>();
        Bundle extra = getIntent().getExtras();
        category = extra.getString("category");

        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(category);

        String[] f = null;
        try {
            f = getAssets().list(category);
            for (String f1 : f) {
                readJSON(f1);
            }
        } catch (Exception e) {

        }
        CustomAdapter customAdapter = new CustomAdapter(this,R.layout.list_item, aList);
        ListView listView = (ListView) findViewById(R.id.listText);
        listView.setAdapter(customAdapter);
        customAdapter.notifyDataSetChanged();
    }

}
