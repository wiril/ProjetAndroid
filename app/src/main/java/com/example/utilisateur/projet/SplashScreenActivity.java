package com.example.utilisateur.projet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.ProgressBar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class SplashScreenActivity extends AppCompatActivity {

    private int source_name;
    private String[] source_list;
    private ArrayList<HashMap<String, String>> listItem;
    private ArrayList<Bitmap> pictures = new ArrayList<Bitmap>();
    private ProgressBar progressBar;
    private double progressStatus = 0.;
    private int IMAGE_MAX_SIZE = 600;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        listItem = new ArrayList<HashMap<String, String>>();
        source_name = 1;

        if(networkAvailable()){
            new SplashScreenActivity.JSONAsyncTask("https://newsapi.org/v2/sources?apiKey=d31f5fa5f03443dd8a1b9e3fde92ec34&language=fr").execute();
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this);
            builder.setTitle("Pas de connexion")
                    .setMessage("Aucune connexion internet n'a été détectée.")
                    .setPositiveButton("Réessayer", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SplashScreenActivity.this.recreate();
                        }
                    })
                    .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {

        String target_URL;
        private ProgressDialog pDialog;

        public JSONAsyncTask(String url) {
            this.target_URL = url;
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            try {

                //------------------>>
                HttpGet firsthttppost = new HttpGet(target_URL);
                HttpClient firsthttpclient = new DefaultHttpClient();
                HttpResponse firstresponse = firsthttpclient.execute(firsthttppost);

                // StatusLine stat = response.getStatusLine();
                int firststatus = firstresponse.getStatusLine().getStatusCode();

                if (firststatus == 200) {
                    HttpEntity entity = firstresponse.getEntity();
                    String data = EntityUtils.toString(entity);

                    JSONObject firstjsono = new JSONObject(data);
                    JSONArray firsttheObject =firstjsono.getJSONArray("sources");
                    int sources_number = firsttheObject.length();
                    source_list = new String[sources_number];
                    for (int i = 0; i < sources_number; i++) {
                        source_list[i] = firsttheObject.getJSONObject(i).getString("id");
                    }
                }
                Log.e("source",source_list[source_name]);
                HttpGet httppost = new HttpGet("https://newsapi.org/v2/everything?apiKey=d31f5fa5f03443dd8a1b9e3fde92ec34&language=fr&sources="+source_list[source_name]);
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response = httpclient.execute(httppost);

                // StatusLine stat = response.getStatusLine();
                int status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);

                    JSONObject jsono = new JSONObject(data);
                    JSONArray theObject = jsono.getJSONArray("articles");

                    int articles_number = theObject.length();
                    for(int i=0; i<articles_number; i++){
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("title", theObject.getJSONObject(i).getString("title"));
                        if(theObject.getJSONObject(i).getString("author")!="null"){
                            map.put("author", theObject.getJSONObject(i).getString("author"));
                        }
                        String date = theObject.getJSONObject(i).getString("publishedAt");
                        char[] date_char = date.toCharArray();
                        char[] resultdate = new char[date_char.length];
                        resultdate[0]=date_char[8];
                        resultdate[1]=date_char[9];
                        resultdate[2]='-';
                        resultdate[3]=date_char[5];
                        resultdate[4]=date_char[6];
                        resultdate[5]='-';
                        for(int j=6;j<=9;j++){resultdate[j]=date_char[j-6];}
                        resultdate[10]=' ';
                        for(int j=11;j<=18;j++){resultdate[j]=date_char[j];}
                        date = new String(resultdate);
                        map.put("date", date);
                        map.put("description", theObject.getJSONObject(i).getString("description"));
                        map.put("url", theObject.getJSONObject(i).getString("url"));

                        switch (source_list[source_name]) {
                            case "le-monde":
                                map.put("source", "Le Monde");
                                break;
                            case "google-news-fr":
                                map.put("source", "Google News");
                                break;
                            case "lequipe":
                                map.put("source", "L'Équipe");
                                break;
                            case "les-echos":
                                map.put("source", "Les Échos");
                                break;
                            case "liberation":
                                map.put("source", "Libération");
                                break;
                        }

                        String image_url = theObject.getJSONObject(i).getString("urlToImage");
                        //Log.e("image_url",image_url);
                        char[] image_url_chars = image_url.toCharArray();
                        Bitmap mIcon11 = null;
                        int imageStatus = 200;
                        if(URLUtil.isValidUrl(image_url)) {
                            HttpGet imageHttppost = new HttpGet(image_url);
                            HttpClient imageHttpclient = new DefaultHttpClient();
                            HttpResponse imageResponse = imageHttpclient.execute(imageHttppost);
                            imageStatus = imageResponse.getStatusLine().getStatusCode();
                        }
                        if (imageStatus!=200 || !URLUtil.isValidUrl(image_url)
                                ||(image_url_chars[image_url_chars.length-4]!='.'&&image_url_chars[image_url_chars.length-5]!='.')
                                ||(image_url_chars[image_url_chars.length-3]=='s')&&image_url_chars[image_url_chars.length-2]=='v'&&image_url_chars[image_url_chars.length-1]=='g'){
                            //Log.e(TAG, "image url is 'null'");
                            switch(source_list[source_name]){
                                case "google-news-fr":
                                    mIcon11 = BitmapFactory.decodeResource(SplashScreenActivity.this.getResources(), R.drawable.google_news_logo);
                                    break;
                                case "le-monde":
                                    mIcon11 = BitmapFactory.decodeResource(SplashScreenActivity.this.getResources(), R.drawable.le_monde);
                                    break;
                                case "lequipe":
                                    mIcon11 = BitmapFactory.decodeResource(SplashScreenActivity.this.getResources(), R.drawable.lequipe);
                                    break;
                                case "les-echos":
                                    mIcon11 = BitmapFactory.decodeResource(SplashScreenActivity.this.getResources(), R.drawable.les_echos);
                                    break;
                                case "liberation":
                                    mIcon11 = BitmapFactory.decodeResource(SplashScreenActivity.this.getResources(), R.drawable.liberation);
                                    break;
                            }
                            pictures.add(mIcon11);
                        }
                        else{
                            try {
                                InputStream in = new java.net.URL(image_url).openStream();
                                //Decode image size
                                BitmapFactory.Options o = new BitmapFactory.Options();
                                o.inJustDecodeBounds = true;
                                BitmapFactory.decodeStream(in, null, o);
                                int scale = 1;
                                if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                                    scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                                            (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
                                }
                                //Decode with inSampleSize
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inSampleSize = scale;
                                in = new java.net.URL(image_url).openStream();
                                mIcon11 = BitmapFactory.decodeStream(in,null,options);
                                pictures.add(mIcon11);
                                in.close();
                            } catch (Exception e) {
                                Log.e("Image dwnlding error : ", e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        final HashMap<String, String> finalMap = map;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listItem.add(finalMap);
                            }
                        });
                        progressStatus+=100./articles_number;
                        if (progressStatus<=100){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {progressBar.setProgress((int)progressStatus);
                                }
                            });
                        }

                    }

                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            DataHolder.getInstance().setData(listItem);
            DataHolder.getInstance().setPictures(pictures);
            Intent nonIntent = new Intent(SplashScreenActivity.this, NewsListActivity.class);
            nonIntent.putExtra("source_name",source_name);
            nonIntent.putExtra("source_list",source_list);
            startActivity(nonIntent);
            finish();
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

    }

    private boolean networkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
