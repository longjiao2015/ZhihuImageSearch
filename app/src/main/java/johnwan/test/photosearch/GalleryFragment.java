package johnwan.test.photosearch;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.reginald.swiperefresh.CustomSwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import andrew.cmu.edu.photosearch.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class GalleryFragment extends Fragment {
    private static final String TAG = GalleryFragment.class.getSimpleName();

    private static final int COLUMN_NUM = 3;
    private static final int ITEM_PER_PAGE = 100;

    private RequestQueue mRq;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private CustomSwipeRefreshLayout mCustomSwipeRefreshLayout;

    private GalleryAdapter mAdapter;

    private boolean mLoading = false;
    private boolean mHasMore = true;

    private SearchView mSearchView;
    private int MY_SOCKET_TIMEOUT_MS = 10000;

    public GalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        mRq = Volley.newRequestQueue(getActivity());

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
//        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                int totalItem = mLayoutManager.getItemCount();
//                int lastItemPos = mLayoutManager.findLastVisibleItemPosition();
//                if (mHasMore && !mLoading && totalItem - 1 != lastItemPos) {
//                    startLoading();
//                }
//            }
//        });

        mLayoutManager = new GridLayoutManager(getActivity(), COLUMN_NUM);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new GalleryAdapter(getActivity(), new ArrayList<ZhihuGalleryItem>());
        mRecyclerView.setAdapter(mAdapter);

        mCustomSwipeRefreshLayout = (CustomSwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        mCustomSwipeRefreshLayout.setOnRefreshListener(
                new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        startLoading();
        return view;
    }

    public void refresh() {
        mAdapter.clear();
        startLoading();
    }

    private void startLoading() {
        Log.d(TAG, "startLoading");
        mLoading = true;
        int totalItem = mLayoutManager.getItemCount();
        final int page = totalItem / ITEM_PER_PAGE + 1;

        String query = PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getString(UrlManager.PREF_SEARCH_QUERY, null);

        String url = UrlManager.getInstance().getItemUrl(query, page);
        final ProgressDialog dialog = ProgressDialog.show(getContext(), "Loading", "Please wait...", true);
        JsonObjectRequest request = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse " + response);
                        List<ZhihuGalleryItem> result = new ArrayList<>();
                        try {
//                            JSONObject photos = response.getJSONObject("photos");
//                            if (photos.getInt("pages") == page) {
//                                mHasMore = false;
//                            }
                            JSONArray photoArr = response.getJSONArray("imageList");
                            if(photoArr.length() == 0){
                                Toast.makeText(getContext(),"No Image found.", Toast.LENGTH_SHORT).show();
                            }
                            for (int i = 0; i < photoArr.length(); i++) {
                                String url = photoArr.getString(i);
                                ZhihuGalleryItem item = new ZhihuGalleryItem(url);
                                result.add(item);
                            }
                        } catch (JSONException e) {

                        }
                        mAdapter.addAll(result);
                        mAdapter.notifyDataSetChanged();
                        mLoading = false;
                        mCustomSwipeRefreshLayout.refreshComplete();
                        dialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error.toString().equalsIgnoreCase("com.android.volley.TimeoutError"))
                            Toast.makeText(getContext(),"Request Timeout", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setTag(TAG);
        mRq.add(request);
    }

    private void stopLoading() {
        if (mRq != null) {
            mRq.cancelAll(TAG);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLoading();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        mSearchView = (SearchView) searchItem.getActionView();
        if (mSearchView != null) {

        }
        SearchManager searchManager = (SearchManager) getActivity()
                .getSystemService(Context.SEARCH_SERVICE);
        ComponentName name = getActivity().getComponentName();
        SearchableInfo searchInfo = searchManager.getSearchableInfo(name);
        mSearchView.setSearchableInfo(searchInfo);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean selectionHandled = false;
        switch (item.getItemId()) {
            case R.id.menu_item_search:
                getActivity().onSearchRequested();
                selectionHandled = true;
                break;
            case R.id.menu_item_move:
                if(mRecyclerView != null) {
                    mRecyclerView.smoothScrollToPosition(0);
                }
                selectionHandled = true;
                break;
            case R.id.menu_item_clear:
                if(mSearchView != null) {
                    mSearchView.setQuery("", false);
                    mSearchView.setIconified(false);
                }

                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(UrlManager.PREF_SEARCH_QUERY, null)
                        .commit();
                refresh();
                selectionHandled = true;
                break;
            default:
                selectionHandled = super.onOptionsItemSelected(item);
                break;
        }
        return selectionHandled;
    }
}
