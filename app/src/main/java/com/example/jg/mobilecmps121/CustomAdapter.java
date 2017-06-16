package com.example.jg.mobilecmps121;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Random;

public class CustomAdapter extends ArrayAdapter<Products> {

        /*SharedPreferences settings = getSharedPreferences("", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("1231", "");*/

    private DBHelper _db;
    int resource;
    Context context;
    public CustomAdapter(Context context, int _resource, List products) {
        super(context, _resource, products);
        this.resource = _resource;
        this.context = context;
        _db = _db.getInstance(context);
    }
    // show The Image in a ImageView



    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }

    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.list_item, parent, false);
        final Products item = getItem(position);
        //final String productName = item.get_productName();
        //final int image = item.getImage();
        TextView itemText = (TextView) customView.findViewById(R.id.itemText);
        TextView priceText = (TextView) customView.findViewById(R.id.priceText);
        ImageView imageItem = (ImageView) customView.findViewById(R.id.imageShirt);
        final Button addItemButton = (Button) customView.findViewById(R.id.addCart);
        itemText.setText(item.get_productName());
        Random r = new Random(System.currentTimeMillis());
        int rand = r.nextInt(400-200) + 200;
        if(item.getPrice() == 0) {
            item.setPrice(rand);
            double price = item.getPrice();
            priceText.setText("$" + price);
        } else {
            priceText.setText("$" + item.getPrice());
        }
        if(item.getFeatures() == null) {
            item.setFeatures(" ");
        }
        new ImageLoadTask(item.getImage(), imageItem).execute();


        //Log.i("price", item.getPrice() + "");
        //Log.i("url", item.getImage());

        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _db.addProduct(item);

                Toast.makeText(context,item.get_productName() + " added!",Toast.LENGTH_SHORT).show();
                //Log.i("database", _db.printDatabase());
            }
        });

        return customView;
    }
}
