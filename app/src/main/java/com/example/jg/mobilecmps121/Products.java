package com.example.jg.mobilecmps121;

import java.util.ArrayList;

public class Products {
    private int _id;
    private String _productName;
    private String _image;
    private double _price;
    private String features;
    private ArrayList<Products> r_array;
    private int count = 1;

    public Products(String productName, String image, double price) {
        _productName = productName;
        _price = price;
        //this.features = features;
        _image = image;

    }

    public void set_id(int _id) {
        this._id = _id;
    }
    public void setImage(String image) {
        this._image = image;
    }
    public void set_productName(String _productName) {
        this._productName = _productName;
    }
    public void setPrice(double price) {
        this._price = price;
    }
    public void setFeatures(String feat) {
        features = feat;
    }
    public void setCount(int c) {
        count = c;
    }
    public int getCount() {
        return count;
    }
    public ArrayList<Products> getArray() {
        return r_array;
    }
    public int get_id() {
        return _id;
    }
    public double getPrice() {
        return _price;
    }
    public String get_productName() {
        return _productName;
    }

    public String getImage() {
        return _image;
    }
    public String getFeatures() {return features;}
}
