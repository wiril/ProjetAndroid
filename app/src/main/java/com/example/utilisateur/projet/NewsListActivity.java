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
import android.provider.ContactsContract;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ThemedSpinnerAdapter;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class NewsListActivity extends ListActivity implements AbsListView.OnScrollListener {

    private static ArrayList<HashMap<String, String>> listItem;
    private static ArrayList<Bitmap> pictures;
    private View mFooterView;
    private boolean mIsLoading = false;
    private boolean mWasLoading = false;
    private Handler mHandler;
    private MirrorAdapter mSchedule;
    private static int numberOfPages = 1;
    private String[] source_list;
    private static int source_name;

    private Runnable mAddItemsRunnable = new Runnable() {
        @Override
        public void run() {
            mSchedule.addMoreItems();
            mIsLoading = false;
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

        mSchedule = new MirrorAdapter(this.getBaseContext(), listItem,
                R.layout.activity_main,
                new String[]{"title", "author", "date", "imageView_left", "imageView_right"}, new int[]{R.id.title,
                R.id.author, R.id.date, R.id.imageView_left, R.id.imageView_right});
        mHandler = new Handler();
        mFooterView = LayoutInflater.from(this).inflate(R.layout.loading_view, null);
        getListView().addFooterView(mFooterView);
        setListAdapter(mSchedule);
        getListView().setOnScrollListener(this);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (!mIsLoading) {
            if (totalItemCount <= firstVisibleItem + visibleItemCount) {
                mIsLoading = true;
                mHandler.postDelayed(mAddItemsRunnable, 1000);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Ignore
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mWasLoading) {
            mWasLoading = false;
            mIsLoading = true;
            mHandler.postDelayed(mAddItemsRunnable, 1000);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mAddItemsRunnable);
        mWasLoading = mIsLoading;
        mIsLoading = false;
    }

    //similar structure with SimpleAdapter :
    public class MirrorAdapter extends BaseAdapter implements Filterable, ThemedSpinnerAdapter {
        private final LayoutInflater mInflater;

        private int[] mTo;
        private String[] mFrom;
        private android.widget.SimpleAdapter.ViewBinder mViewBinder;

        private List<? extends Map<String, ?>> mData;

        private int mResource;
        private int mDropDownResource;

        /** Layout inflater used for {@link #getDropDownView(int, View, ViewGroup)}. */
        private LayoutInflater mDropDownInflater;

        private SimpleFilter mFilter;
        private ArrayList<Map<String, ?>> mUnfilteredData;

        /**
         * Constructor
         *
         * @param context The context where the View associated with this SimpleAdapter is running
         * @param data A List of Maps. Each entry in the List corresponds to one row in the list. The
         *        Maps contain the data for each row, and should include all the entries specified in
         *        "from"
         * @param resource Resource identifier of a view layout that defines the views for this list
         *        item. The layout file should include at least those named views defined in "to"
         * @param from A list of column names that will be added to the Map associated with each
         *        item.
         * @param to The views that should display column in the "from" parameter. These should all be
         *        TextViews. The first N views in this list are given the values of the first N columns
         *        in the from parameter.
         */
        public MirrorAdapter(Context context, List<? extends Map<String, ?>> data,
                             @LayoutRes int resource, String[] from, @IdRes int[] to) {
            mData = data;
            mResource = mDropDownResource = resource;
            mFrom = from;
            mTo = to;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        /**
         * @see android.widget.Adapter#getCount()
         */
        public int getCount() {
            return mData.size();
        }

        /**
         * @see android.widget.Adapter#getItem(int)
         */
        public Object getItem(int position) {
            return mData.get(position);
        }

        /**
         * @see android.widget.Adapter#getItemId(int)
         */
        public long getItemId(int position) {
            return position;
        }

        /**
         * @see android.widget.Adapter#getView(int, View, ViewGroup)
         */
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

        /**
         * <p>Sets the layout resource to create the drop down views.</p>
         *
         * @param resource the layout resource defining the drop down views
         * @see #getDropDownView(int, android.view.View, android.view.ViewGroup)
         */
        public void setDropDownViewResource(int resource) {
            mDropDownResource = resource;
        }

        /**
         * Sets the {@link android.content.res.Resources.Theme} against which drop-down views are
         * inflated.
         * <p>
         * By default, drop-down views are inflated against the theme of the
         * {@link Context} passed to the adapter's constructor.
         *
         * @param theme the theme against which to inflate drop-down views or
         *              {@code null} to use the theme from the adapter's context
         * @see #getDropDownView(int, View, ViewGroup)
         */
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
                if (!pictures.isEmpty()){
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
                }
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

        /**
         * Returns the {@link android.widget.SimpleAdapter.ViewBinder} used to bind data to views.
         *
         * @return a ViewBinder or null if the binder does not exist
         *
         * @see #setViewBinder(android.widget.SimpleAdapter.ViewBinder)
         */
        public android.widget.SimpleAdapter.ViewBinder getViewBinder() {
            return mViewBinder;
        }

        /**
         * Sets the binder used to bind data to views.
         *
         * @param viewBinder the binder used to bind data to views, can be null to
         *        remove the existing binder
         *
         * @see #getViewBinder()
         */
        public void setViewBinder(android.widget.SimpleAdapter.ViewBinder viewBinder) {
            mViewBinder = viewBinder;
        }

        /**
         * Called by bindView() to set the image for an ImageView but only if
         * there is no existing ViewBinder or if the existing ViewBinder cannot
         * handle binding to an ImageView.
         *
         * This method is called instead of {@link #setViewImage(ImageView, String)}
         * if the supplied data is an int or Integer.
         *
         * @param v ImageView to receive an image
         * @param value the value retrieved from the data set
         *
         * @see #setViewImage(ImageView, String)
         */
        public void setViewImage(ImageView v, int value) {
            v.setImageResource(value);
        }

        /**
         * Called by bindView() to set the image for an ImageView but only if
         * there is no existing ViewBinder or if the existing ViewBinder cannot
         * handle binding to an ImageView.
         *
         * By default, the value will be treated as an image resource. If the
         * value cannot be used as an image resource, the value is used as an
         * image Uri.
         *
         * This method is called instead of {@link #setViewImage(ImageView, int)}
         * if the supplied data is not an int or Integer.
         *
         * @param v ImageView to receive an image
         * @param value the value retrieved from the data set
         *
         * @see #setViewImage(ImageView, int)
         */
        public void setViewImage(ImageView v, String value) {
            try {
                v.setImageResource(Integer.parseInt(value));
            } catch (NumberFormatException nfe) {
                Log.e(TAG,"setViewImage failed"+value);
                v.setImageURI(Uri.parse(value));
            }
        }

        /**
         * Called by bindView() to set the text for a TextView but only if
         * there is no existing ViewBinder or if the existing ViewBinder cannot
         * handle binding to a TextView.
         *
         * @param v TextView to receive text
         * @param text the text to be set for the TextView
         */
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

        /**
         * This class can be used by external clients of SimpleAdapter to bind
         * values to views.
         *
         * You should use this class to bind values to views that are not
         * directly supported by SimpleAdapter or to change the way binding
         * occurs for views supported by SimpleAdapter.
         *
         * @see android.widget.SimpleAdapter#setViewImage(ImageView, int)
         * @see android.widget.SimpleAdapter#setViewImage(ImageView, String)
         * @see android.widget.SimpleAdapter#setViewText(TextView, String)
         */
        //public interface ViewBinder {
            /**
             * Binds the specified data to the specified view.
             *
             * When binding is handled by this ViewBinder, this method must return true.
             * If this method returns false, SimpleAdapter will attempts to handle
             * the binding on its own.
             *
             * @param view the view to bind the data to
             * @param data the data to bind to the view
             * @param textRepresentation a safe String representation of the supplied data:
             *        it is either the result of data.toString() or an empty String but it
             *        is never null
             *
             * @return true if the data was bound to the view, false otherwise
             */
        //    boolean setViewValue(View view, Object data, String textRepresentation);
        //}

        /**
         * <p>An array filters constrains the content of the array adapter with
         * a prefix. Each item that does not start with the supplied prefix
         * is removed from the list.</p>
         */
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
        //faudra aussi gérer la transmission de l'image. 2 solutions : la retélécharger, ou réussir à la carry d'une manière ou d'une autre.
        HashMap<String, String> map = listItem.get(position);
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
                        if (image_url.equals("null")){
                            //Log.e(TAG, "image url is 'null'");
                            if(source_list[source_name].equals("google-news-fr")){
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
                            if(source_list[source_name].equals("le-monde")){
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
                        listItem.add(map);
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
            super.onPostExecute(result);
            mSchedule.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

    }
}
