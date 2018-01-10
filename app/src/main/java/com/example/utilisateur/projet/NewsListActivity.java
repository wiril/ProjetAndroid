package com.example.utilisateur.projet;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ThemedSpinnerAdapter;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class NewsListActivity extends ListActivity implements AbsListView.OnScrollListener {

    private static ArrayList<HashMap<String, String>> listItem;
    private static ArrayList<Bitmap> pictures;
    private View mFooterView;
    private View mHeaderView;
    private static boolean mIsLoading = false;
    private Handler mHandler;
    private MirrorAdapter mSchedule;
    private static int numberOfPages = 1;
    private String[] source_list;
    private static int source_name;
    private int IMAGE_MAX_SIZE = 600;
    private int max_simult_pages = 20;

    private Runnable mAddItemsRunnable = new Runnable() {
        @Override
        public void run() {
            mSchedule.addMoreItems();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listItem = DataHolder.getInstance().getListItem();
        pictures = DataHolder.getInstance().getPictures();
        Bundle b = getIntent().getExtras();
        source_list = (String[]) b.get("source_list");
        source_name = (int) b.get("source_name");
        numberOfPages = (int) b.get("numberOfPages");

        mSchedule = new MirrorAdapter(this.getBaseContext(), listItem,
                R.layout.activity_main,
                new String[]{"title", "author", "date", "imageView_left", "imageView_right"}, new int[]{R.id.title,
                R.id.author, R.id.date, R.id.imageView_left, R.id.imageView_right});
        mHandler = new Handler();
        mHeaderView = LayoutInflater.from(this).inflate(R.layout.spinner_view, null);
        TextView tv = (TextView) mHeaderView.findViewById(R.id.source_name);
        tv.setText("Liste des actualités");
        Spinner sv = (Spinner) mHeaderView.findViewById(R.id.spinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.clear();
        switch (source_list[source_name]) {
            case "le-monde":
                adapter.add("Le Monde");
                adapter.add("Google News");
                adapter.add("L' Équipe");
                adapter.add("Les Échos");
                adapter.add("Libération");
                break;
            case "google-news-fr":
                adapter.add("Google News");
                adapter.add("Le Monde");
                adapter.add("L' Équipe");
                adapter.add("Les Échos");
                adapter.add("Libération");
                break;
            case "lequipe":
                adapter.add("L' Équipe");
                adapter.add("Google News");
                adapter.add("Le Monde");
                adapter.add("Les Échos");
                adapter.add("Libération");
                break;
            case "les-echos":
                adapter.add("Les Échos");
                adapter.add("Google News");
                adapter.add("Le Monde");
                adapter.add("L' Équipe");
                adapter.add("Libération");
                break;
            case "liberation":
                adapter.add("Libération");
                adapter.add("Google News");
                adapter.add("Le Monde");
                adapter.add("L' Équipe");
                adapter.add("Les Échos");
                break;
        }
        sv.setAdapter(adapter);
        sv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position!=0){
                    String title = adapter.getItem(position);
                    switch(title){
                        case "Google News":
                            source_name = 0;
                            break;
                        case "Le Monde":
                            source_name = 1;
                            break;
                        case "L' Équipe":
                            source_name = 2;
                            break;
                        case "Les Échos":
                            source_name = 3;
                            break;
                        case "Libération":
                            source_name = 4;
                            break;
                    }
                    Intent nonIntent = new Intent(NewsListActivity.this, SplashScreenActivity.class);
                    nonIntent.putExtra("numberOfPages",0);
                    nonIntent.putExtra("sourceName",source_name);
                    startActivity(nonIntent);
                    finish();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        getListView().addHeaderView(mHeaderView);
        mFooterView = LayoutInflater.from(this).inflate(R.layout.loading_view, null);
        getListView().addFooterView(mFooterView);
        Button button = (Button) mFooterView.findViewById(R.id.reloadButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent nonIntent = new Intent(NewsListActivity.this, SplashScreenActivity.class);
                nonIntent.putExtra("numberOfPages",numberOfPages);
                startActivity(nonIntent);
                finish();
            }
        });
        setListAdapter(mSchedule);
        getListView().setOnScrollListener(this);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (!mIsLoading&&numberOfPages%max_simult_pages!=0) {
            if (totalItemCount-4<= firstVisibleItem + visibleItemCount) {
                mIsLoading = true;
                mHandler.postDelayed(mAddItemsRunnable, 1);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Ignore
    }

    //@Override
    //public void onStart() {
    //    super.onStart();
    //    Log.e("On start","...");
    //    if (mWasLoading) {
    //        mWasLoading = false;
    //        mIsLoading = true;
    //        mHandler.postDelayed(mAddItemsRunnable, 1000);
    //    }
    //}

    //@Override
    //public void onStop() {
    //    super.onStop();
    //    Log.e("On stop","...");
    //    mHandler.removeCallbacks(mAddItemsRunnable);
    //    mWasLoading = mIsLoading;
    //    mIsLoading = false;
    //}

    //similar structure with SimpleAdapter :
    public class MirrorAdapter extends BaseAdapter implements Filterable, ThemedSpinnerAdapter {
        private final LayoutInflater mInflater;

        private int[] mTo;
        private String[] mFrom;
        private android.widget.SimpleAdapter.ViewBinder mViewBinder;

        private List<? extends Map<String, ?>> mData;

        private int mResource;
        private int mDropDownResource;

        private LayoutInflater mDropDownInflater;

        private SimpleFilter mFilter;
        private ArrayList<Map<String, ?>> mUnfilteredData;

        public MirrorAdapter(Context context, List<? extends Map<String, ?>> data,
                             @LayoutRes int resource, String[] from, @IdRes int[] to) {
            mData = data;
            mResource = mDropDownResource = resource;
            mFrom = from;
            mTo = to;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return mData.size();
        }

        public Object getItem(int position) {
            return mData.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return createViewFromResource(mInflater, position, convertView, parent, mResource);
        }

        private View createViewFromResource(LayoutInflater inflater, int position, View convertView,
                                            ViewGroup parent, int resource) {
            View v;
            if (convertView == null) {
                v = inflater.inflate(resource, parent, false);
            } else {
                v = convertView;
            }
            bindView(position, v);
            return v;
        }

        public void setDropDownViewResource(int resource) {
            mDropDownResource = resource;
        }

        @Override
        public void setDropDownViewTheme(Resources.Theme theme) {
            if (theme == null) {
                mDropDownInflater = null;
            } else if (theme == mInflater.getContext().getTheme()) {
                mDropDownInflater = mInflater;
            } else {
                final Context context = new ContextThemeWrapper(mInflater.getContext(), theme);
                mDropDownInflater = LayoutInflater.from(context);
            }
        }

        @Override
        public Resources.Theme getDropDownViewTheme() {
            return mDropDownInflater == null ? null : mDropDownInflater.getContext().getTheme();
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            final LayoutInflater inflater = mDropDownInflater == null ? mInflater : mDropDownInflater;
            return createViewFromResource(inflater, position, convertView, parent, mResource);
        }

        private void bindView(int position, View view) {
            final Map dataSet = mData.get(position);
            if (dataSet == null) {
                return;
            }

            final android.widget.SimpleAdapter.ViewBinder binder = mViewBinder;
            final String[] from = mFrom;
            final int[] to = mTo;
            final int count = to.length;

            for (int i = 0; i < count; i++) {
                final ImageView imv;
                if(position%2==0){
                    imv = (ImageView) view.findViewById(R.id.imageView_left);
                    view.findViewById(R.id.imageView_right).setVisibility(View.GONE);
                }
                else{
                    imv = (ImageView) view.findViewById(R.id.imageView_right);
                    view.findViewById(R.id.imageView_left).setVisibility(View.GONE);
                }
                //Log.e("picture is null "," "+(pictures.get(position)==null));
                imv.setImageBitmap(pictures.get(position));
                imv.setVisibility(View.VISIBLE);
                final View v = view.findViewById(to[i]);
                if (v != null) {
                    final Object data = dataSet.get(from[i]);
                    String text = data == null ? "" : data.toString();
                    if (text == null) {
                        text = "";
                    }

                    boolean bound = false;
                    if (binder != null) {
                        bound = binder.setViewValue(v, data, text);
                    }

                    if (!bound) {
                        if (v instanceof Checkable) {
                            if (data instanceof Boolean) {
                                ((Checkable) v).setChecked((Boolean) data);
                            } else if (v instanceof TextView) {
                                // Note: keep the instanceof TextView check at the bottom of these
                                // ifs since a lot of views are TextViews (e.g. CheckBoxes).
                                setViewText((TextView) v, text);
                            } else {
                                throw new IllegalStateException(v.getClass().getName() +
                                        " should be bound to a Boolean, not a " +
                                        (data == null ? "<unknown type>" : data.getClass()));
                            }
                        } else if (v instanceof TextView) {
                            // Note: keep the instanceof TextView check at the bottom of these
                            // ifs since a lot of views are TextViews (e.g. CheckBoxes).
                            setViewText((TextView) v, text);
                        } else if (v instanceof ImageView) {
                            if (data instanceof Integer) {
                                setViewImage((ImageView) v, (Integer) data);
                            } else {
                                if (text != ""){
                                    if(position%2==0){
                                        view.findViewById(R.id.imageView_right).setVisibility(View.GONE);
                                    }
                                    else{
                                        view.findViewById(R.id.imageView_left).setVisibility(View.GONE);
                                    }
                                    v.setVisibility(View.VISIBLE);
                                    setViewImage((ImageView) v, text);
                                }
                            }
                        } else {
                            throw new IllegalStateException(v.getClass().getName() + " is not a " +
                                    " view that can be bounds by this SimpleAdapter");
                        }
                    }
                }
            }
        }

        public android.widget.SimpleAdapter.ViewBinder getViewBinder() {
            return mViewBinder;
        }

        public void setViewBinder(android.widget.SimpleAdapter.ViewBinder viewBinder) {
            mViewBinder = viewBinder;
        }

        public void setViewImage(ImageView v, int value) {
            v.setImageResource(value);
        }

        public void setViewImage(ImageView v, String value) {
            try {
                v.setImageResource(Integer.parseInt(value));
            } catch (NumberFormatException nfe) {
                Log.e(TAG,"setViewImage failed"+value);
                v.setImageURI(Uri.parse(value));
            }
        }

        public void setViewText(TextView v, String text) {
            v.setText(text);
        }

        public Filter getFilter() {
            if (mFilter == null) {
                mFilter = new SimpleFilter();
            }
            return mFilter;
        }

        public void addMoreItems() {
            numberOfPages++ ;
            new NewsListActivity.JSONAsyncTask("https://newsapi.org/v2/sources?apiKey=d31f5fa5f03443dd8a1b9e3fde92ec34&language=fr").execute();
        }

        private class SimpleFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence prefix) {
                FilterResults results = new FilterResults();

                if (mUnfilteredData == null) {
                    mUnfilteredData = new ArrayList<Map<String, ?>>(mData);
                }

                if (prefix == null || prefix.length() == 0) {
                    ArrayList<Map<String, ?>> list = mUnfilteredData;
                    results.values = list;
                    results.count = list.size();
                } else {
                    String prefixString = prefix.toString().toLowerCase();

                    ArrayList<Map<String, ?>> unfilteredValues = mUnfilteredData;
                    int count = unfilteredValues.size();

                    ArrayList<Map<String, ?>> newValues = new ArrayList<Map<String, ?>>(count);

                    for (int i = 0; i < count; i++) {
                        Map<String, ?> h = unfilteredValues.get(i);
                        if (h != null) {

                            int len = mTo.length;

                            for (int j=0; j<len; j++) {
                                String str =  (String)h.get(mFrom[j]);

                                String[] words = str.split(" ");
                                int wordCount = words.length;

                                for (int k = 0; k < wordCount; k++) {
                                    String word = words[k];

                                    if (word.toLowerCase().startsWith(prefixString)) {
                                        newValues.add(h);
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    results.values = newValues;
                    results.count = newValues.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //noinspection unchecked
                mData = (List<Map<String, ?>>) results.values;
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        HashMap<String, String> map = listItem.get(position-1);
        ImageView imv = (ImageView) v.findViewById(R.id.imageView_left);
        if(imv.getVisibility()== View.GONE){
            imv = (ImageView) v.findViewById(R.id.imageView_right);
        }
        DataHolder.getInstance().setDrawable(imv.getDrawable());

        Intent nonIntent = new Intent(NewsListActivity.this, NewsDetails.class);
        for (Map.Entry<String, String> entry : map.entrySet()){
            nonIntent.putExtra(entry.getKey(),entry.getValue());
        }
        startActivity(nonIntent);
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
                HttpGet httppost = new HttpGet("https://newsapi.org/v2/everything?apiKey=d31f5fa5f03443dd8a1b9e3fde92ec34&language=fr&sources="+source_list[source_name]+"&page="+numberOfPages);
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
                                ||(image_url_chars[image_url_chars.length-3]=='s')&&image_url_chars[image_url_chars.length-2]=='v'&&image_url_chars[image_url_chars.length-1]=='g') {
                            //Log.e(TAG, "image url is 'null'");
                            switch (source_list[source_name]) {
                                case "google-news-fr":
                                    mIcon11 = BitmapFactory.decodeResource(NewsListActivity.this.getResources(), R.drawable.google_news_logo);
                                    break;
                                case "le-monde":
                                    mIcon11 = BitmapFactory.decodeResource(NewsListActivity.this.getResources(), R.drawable.le_monde);
                                    break;
                                case "lequipe":
                                    mIcon11 = BitmapFactory.decodeResource(NewsListActivity.this.getResources(), R.drawable.lequipe);
                                    break;
                                case "les-echos":
                                    mIcon11 = BitmapFactory.decodeResource(NewsListActivity.this.getResources(), R.drawable.les_echos);
                                    break;
                                case "liberation":
                                    mIcon11 = BitmapFactory.decodeResource(NewsListActivity.this.getResources(), R.drawable.liberation);
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
                        //Solution a priori temporaire au bug de toucher l'écran alors que la liste est en cours de changement.
                        final HashMap<String, String> finalMap = map;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listItem.add(finalMap);
                                mSchedule.notifyDataSetChanged();
                            }
                        });
                        //_______________________________________
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
            mSchedule.notifyDataSetChanged();
            if(numberOfPages%max_simult_pages==0){
                mFooterView.findViewById(R.id.progressBar2).setVisibility(View.GONE);
                mFooterView.findViewById(R.id.reloadButton).setVisibility(View.VISIBLE);
            }
            mIsLoading = false;
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

    }
}
