package com.example.utilisateur.projet;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ThemedSpinnerAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class NewsListActivity extends ListActivity {

    private ArrayList<HashMap<String, String>> listItem;
    private static ArrayList<Bitmap> pictures;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listItem = DataHolder.getInstance().getListItem();
        pictures = DataHolder.getInstance().getPictures();

        MirrorAdapter mSchedule = new MirrorAdapter(this.getBaseContext(), listItem,
                R.layout.activity_main,
                new String[]{"title", "author", "date", "imageView_left", "imageView_right"}, new int[]{R.id.title,
                R.id.author, R.id.date, R.id.imageView_left, R.id.imageView_right});
        //On attribut à notre listActivity l'adapter que l'on vient de créer
        setListAdapter(mSchedule);
    }

    public void showToast(final String toast){ //créer un toast depuis un thread autre que UIThread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NewsListActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //similar structure with SimpleAdapter :
    public static class MirrorAdapter extends BaseAdapter implements Filterable, ThemedSpinnerAdapter {
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
        public static interface ViewBinder {
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
            boolean setViewValue(View view, Object data, String textRepresentation);
        }

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
        Intent nonIntent = new Intent(NewsListActivity.this, NewsDetails.class);
        for (Map.Entry<String, String> entry : map.entrySet()){
            nonIntent.putExtra(entry.getKey(),entry.getValue());
        }
        startActivity(nonIntent);
    }
}
