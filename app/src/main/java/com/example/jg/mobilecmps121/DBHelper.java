package com.example.jg.mobilecmps121;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "products.db";
    public static final String TABLE_PRODUCTS = "products";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PRODUCTNAME = "productname";
    public static final String COLUMN_IMAGE = "productimage";
    public static final String COLUMN_PRICE = "productprice";
    public static final String COLUMN_COUNT = "productcount";
    public static final String COLUMN_FEATURE = "productfeature";
    private static DBHelper mDBHelper;
    private int count = 1;
    private DBHelper(Context context, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }


    public static DBHelper getInstance(Context context) {
        if(mDBHelper == null) {
            mDBHelper = new DBHelper(context.getApplicationContext(),null,1);
        }
        return mDBHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_PRODUCTS + "(" +
                COLUMN_PRODUCTNAME + " TEXT UNIQUE, " +
                COLUMN_IMAGE + " TEXT, " + COLUMN_PRICE + " INTEGER, "+ COLUMN_COUNT + " INTEGER" + ");";
        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }
    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
    public void addProduct(Products products) {
        //Log.i("product", productName);

        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCTNAME, products.get_productName());
        values.put(COLUMN_IMAGE, products.getImage());
        values.put(COLUMN_PRICE, products.getPrice());

        SQLiteDatabase db = getWritableDatabase();

        try {
            db.insertOrThrow(TABLE_PRODUCTS, null, values);
            values.put(COLUMN_COUNT, 1);
            products.setCount(1);
        } catch(SQLiteConstraintException e) {
            count++;
            Log.d("count", count + "");
            values.put(COLUMN_PRODUCTNAME, products.get_productName());
            products.setCount(count);
            values.put(COLUMN_COUNT, count);
            db.insert(TABLE_PRODUCTS, null, values);
        }

        db.close();
    }
    public void deleteProduct(String productName) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_PRODUCTNAME + "=\"" + productName + "\"");
        db.close();
    }
    public void deleteTable() {
        Log.i("stop", "delete table");
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PRODUCTS);
        db.close();
    }
   /* public String printDatabase(){
        String dbString = "";
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_PRODUCTS + " WHERE 1";

        //Cursor points to a location in your results
        Cursor c = db.rawQuery(query, null);
        //Move to the first row in your results
        c.moveToFirst();

        //Position after the last row means the end of the results
        while (!c.isAfterLast()) {
            if (c.getString(c.getColumnIndex("productname")) != null) {
                dbString += c.getString(c.getColumnIndex("productname"));
                dbString += "\n";
                Log.i("db", dbString);
            }
            c.moveToNext();

        }
        db.close();
        c.close();
        return dbString;
    }*/
   /* public ArrayList<Integer> getImage() {
        ArrayList<Integer> dbImage = new ArrayList<Integer>();
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_PRODUCTS + " WHERE 1";

        //Cursor points to a location in your results
        Cursor c = db.rawQuery(query, null);
        //Move to the first row in your results
        c.moveToFirst();

        //Position after the last row means the end of the results
        while (!c.isAfterLast()) {
            if (c.getString(c.getColumnIndex(COLUMN_IMAGE)) != null) {
                dbImage.add(c.getInt(c.getColumnIndex("productimage")));
            }
            c.moveToNext();

        }
        db.close();
        c.close();
        return dbString;
    }*/
}
