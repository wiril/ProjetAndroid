package com.example.utilisateur.projet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class SplashScreenActivity extends AppCompatActivity {

    private String url_string = "le-monde";
    private ArrayList<HashMap<String, String>> listItem;
    private ArrayList<Bitmap> pictures = new ArrayList<Bitmap>();
    private ProgressBar progressBar;
    private double progressStatus = 0.;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        listItem = new ArrayList<HashMap<String, String>>();

        new SplashScreenActivity.JSONAsyncTask("https://newsapi.org/v2/everything?apiKey=d31f5fa5f03443dd8a1b9e3fde92ec34&language=fr&sources="+url_string).execute();

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
                HttpGet httppost = new HttpGet(target_URL);
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
                        map.put("date", theObject.getJSONObject(i).getString("publishedAt"));

                        String image_url = theObject.getJSONObject(i).getString("urlToImage");
                        //Log.e("image_url",image_url);
                        if (image_url =="null"){
                            //Log.e(TAG, "image url is 'null'");
                            if(url_string=="google-news-fr"){
                                //Log.e(TAG, "source is google news");
                                if(i%2==0){
                                    map.put("imageView_left", String.valueOf(R.drawable.google_news_logo));
                                    map.put("imageView_right", "");
                                }
                                else{
                                    map.put("imageView_right", String.valueOf(R.drawable.google_news_logo));
                                    map.put("imageView_left", "");
                                }
                            }
                            if(url_string=="le-monde"){
                                //Log.e(TAG, "source is le monde");
                                if(i%2==0){
                                    map.put("imageView_left", String.valueOf(R.drawable.le_monde));
                                    map.put("imageView_right", "");
                                }
                                else{
                                    map.put("imageView_left", String.valueOf(R.drawable.le_monde));
                                    map.put("imageView_right", "");
                                }
                            }
                        }
                        else{
                            Bitmap mIcon11;
                            try {
                                InputStream in = new java.net.URL(image_url).openStream();
                                mIcon11 = BitmapFactory.decodeStream(in);
                                pictures.add(mIcon11);
                            } catch (Exception e) {
                                Log.e("Image dwnlding error : ", e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        //if(url_string=="google-new-fr"){
                        //    if (theObject.getJSONObject(i).getString("urlToImage")!= null){
                        //        map.put("imageView", theObject.getJSONObject(i).getString("urlToImage"));
                        //    }
                        //    else{
                        //        map.put("imageView", theObject.getJSONObject(i).getString("urlToImage"));
                        //    }
                        //}
                        listItem.add(map);
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
            startActivity(nonIntent);
            finish();
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

    }
}
