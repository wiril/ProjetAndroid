package com.example.utilisateur.projet;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Utilisateur on 28/12/2017.
 */

public class DataHolder {
    private ArrayList<HashMap<String, String>> data;
    public ArrayList<HashMap<String, String>> getData() {return data;}
    public void setData(ArrayList<HashMap<String, String>> data) {this.data = data;}

    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() {return holder;}
}
