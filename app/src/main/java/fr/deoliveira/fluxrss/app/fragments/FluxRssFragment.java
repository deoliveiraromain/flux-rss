package fr.deoliveira.fluxrss.app.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import at.theengine.android.simple_rss2_android.RSSItem;

import com.einmalfel.earl.EarlParser;
import com.einmalfel.earl.Feed;
import com.einmalfel.earl.Item;

import fr.deoliveira.fluxrss.app.R;
import fr.deoliveira.fluxrss.app.adapters.ItemRssAdapter;
import fr.deoliveira.fluxrss.app.base.RSSContentProvider;
import fr.deoliveira.fluxrss.app.base.RSSTable;
import fr.deoliveira.fluxrss.app.model.FluxRss;
import fr.deoliveira.fluxrss.app.model.ItemRss;
import fr.deoliveira.fluxrss.app.utils.SimpleRssParserExt;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

public class FluxRssFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SimpleRssParserExt.SimpleRssParserExtCallBack {

    private static final String ARG_PARAM_TYPE = "typeInfo";
    private String typeStr;

    private RecyclerView recyclerViewItemRss;
    // private ProgressBar progressBar;
    private LinearLayout layout_loading_event;
    private ItemRssAdapter itemRssAdapter;
    private List<FluxRss> listeFlux;

    private OnFluxRssInteractionListener mListener;

    public static FluxRssFragment newInstance(String typeInfoStr) {
        FluxRssFragment fragment = new FluxRssFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_TYPE, typeInfoStr);
        fragment.setArguments(args);
        return fragment;
    }

    public FluxRssFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String typeStr = getArguments().getString(ARG_PARAM_TYPE);
            this.typeStr = typeStr;
        }
        this.loadDataFromDb();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_flux_rss, container, false);
        this.recyclerViewItemRss = (RecyclerView) rootView.findViewById(R.id.recyclerViewItemRss);
        // this.layout_loading_event = (LinearLayout) rootView.findViewById(R.id.layout_loading_event);

        this.itemRssAdapter = new ItemRssAdapter(new ArrayList<ItemRss>());
        recyclerViewItemRss.setAdapter(itemRssAdapter);
        recyclerViewItemRss.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewItemRss.setHasFixedSize(true);

        recyclerViewItemRss.setItemAnimator(new DefaultItemAnimator());

        return rootView;
    }


    private void loadDataFromDb() {
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = "";
        String[] projection = null;
        selection = RSSTable.COLUMN_TYPE + " = ?";
        String[] selectionArgs = new String[]{this.typeStr};
        Log.i(this.getClass().getName(), "Creation Loader DB RSS Content provider");
        Log.i(this.getClass().getName(), "Type Str" + typeStr);
        CursorLoader cursorLoader = new CursorLoader(this.getActivity(),
                RSSContentProvider.CONTENT_URI, projection, selection, selectionArgs, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        List<FluxRss> rsses = RSSTable.mapFromCursor(data);
        Log.i(this.getClass().getName(), "CallBack RSS Load Data DB : " + rsses.size());
        this.listeFlux = rsses;
        // loadFeedsNew();
        new DllAsyncTask().execute();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(this.getClass().getName(), "CallBack RSS Load Reset DB : ");
    }

    public void onButtonPressed(String url) {
        if (mListener != null) {
            mListener.onFluxRssInteraction(url);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFluxRssInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private List<Item> loadFeedsNew() {
        List<Item> listItem = new ArrayList<>();
        for (FluxRss flux : this.listeFlux) {
            InputStream inputStream = null;
            try {
                inputStream = new URL(flux.getUrl()).openConnection().getInputStream();
                Feed feed = null;
                feed = EarlParser.parseOrThrow(inputStream, 0);
                Log.i("FLUXRSS", "Processing feed: " + feed.getTitle() + flux.getUrl());
                for (Item item : feed.getItems()) {
                    String title = item.getTitle();
                    Log.i("FLUXRSS", "Item title: " + (title == null ? "N/A" : title));
                }
                listItem.addAll(feed.getItems());
                // bindNew(listItem,flux.getAuteur());
            } catch (XmlPullParserException e) {
                Log.e("FLUXRSS", e.getMessage(), e);
            } catch (DataFormatException e) {
                Log.e("FLUXRSS", e.getMessage(), e);
            } catch (IOException e) {
                Log.e("FLUXRSS", e.getMessage(), e);
            }

        }
        return listItem;
    }

    private void loadFeeds() {
        final Map<String, String> mapNametUrl = new HashMap<>();
        for (FluxRss flux : this.listeFlux) {
            mapNametUrl.put(flux.getAuteur(), flux.getUrl());
        }
        displayLoadingLayout();
        final int size = this.listeFlux.size();
        int i = 0;
        boolean endList = false;
        for (final String key : mapNametUrl.keySet()) {
            ++i;
            if (i == size) {
                endList = true;
            }
            SimpleRssParserExt parser = new SimpleRssParserExt(mapNametUrl.get(key), this, endList);
            parser.parseAsync();

        }
    }

    private void displayLoadingLayout() {
        //this.layout_loading_event.setVisibility(View.VISIBLE);
        this.recyclerViewItemRss.setVisibility(View.GONE);
    }

    private void displayRSSList() {
        // this.layout_loading_event.setVisibility(View.GONE);
        this.recyclerViewItemRss.setVisibility(View.VISIBLE);
    }

    private void bind(List<RSSItem> list, String source) {
        List<ItemRss> listeConv = new ArrayList<>();
        for (RSSItem item : list) {
            ItemRss itemRss = new ItemRss();
            itemRss.setDescription(item.getDescription());
            itemRss.setLien(item.getLink().toString());
            itemRss.setTitre(item.getTitle());
            itemRss.setDate(item.getDate());
            itemRss.setSource(source);
            listeConv.add(itemRss);
        }
        this.itemRssAdapter.add(listeConv);
    }

    private void bindNew(List<Item> list, String source) {
        List<ItemRss> listeConv = new ArrayList<>();
        for (Item item : list) {
            ItemRss itemRss = new ItemRss();
            itemRss.setDescription(item.getDescription());
            itemRss.setLien(item.getLink().toString());
            itemRss.setTitre(item.getTitle());
            if (item.getPublicationDate() != null)
                itemRss.setDate(item.getPublicationDate().toString());
            itemRss.setSource(source);
            listeConv.add(itemRss);
        }
        this.itemRssAdapter.add(listeConv);
    }

    @Override
    public void onFeedParsed(List<RSSItem> rssItemList, String url, boolean endList) {
        Log.d(this.getClass().getName(), "FEED PARSED " + url + "end ?" + endList);
        bind(rssItemList, url);
        if (endList) {
            displayRSSList();
        }
    }

    @Override
    public void onError(Exception exception) {
        Log.e(this.getClass().getName(), exception.getMessage());
        displayRSSList();
    }

    public interface OnFluxRssInteractionListener {
        void onFluxRssInteraction(String url);

    }

    private class DllAsyncTask extends AsyncTask<Object, Void, List<Item>> {


        @Override
        protected List<Item> doInBackground(Object[] params) {
            return loadFeedsNew();
        }

        @Override
        protected void onPostExecute(List<Item> list) {
            super.onPostExecute(list);
            bindNew(list, "test");
        }
    }

}
