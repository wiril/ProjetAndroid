package com.example.utilisateur.projet;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Utilisateur on 28/12/2017.
 */

public class DataHolder {

    private ArrayList<HashMap<String, String>> data;
    private ArrayList<Bitmap> pictures;

    public ArrayList<HashMap<String, String>> getListItem() {return data;}
    public void setData(ArrayList<HashMap<String, String>> data) {this.data = data;}

    public ArrayList<Bitmap> getPictures() {return pictures;}
    public void setPictures(ArrayList<Bitmap> pictures) {this.pictures = pictures;}

    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() {return holder;}
}
