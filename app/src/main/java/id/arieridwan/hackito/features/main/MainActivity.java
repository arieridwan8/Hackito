package id.arieridwan.hackito.features.main;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.arieridwan.hackito.App;
import id.arieridwan.hackito.R;
import id.arieridwan.hackito.adapter.StoriesAdapter;
import id.arieridwan.hackito.models.ItemStories;

/**
 * Created by arieridwan on 27/06/2017.
 */

public class MainActivity extends AppCompatActivity
        implements MainContract.View, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_stories)
    RecyclerView rvStories;
    @BindView(R.id.swipeLayout)
    SwipeRefreshLayout swipeLayout;
    @BindView(R.id.main_content)
    CoordinatorLayout mainContent;

    private int startIndex = 0;
    // primary
    private List<Integer> mTopStories = new ArrayList<>();
    private List<ItemStories> mItem = new ArrayList<>();
    // temporary
    private List mTempItem = new ArrayList();
    private StoriesAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private boolean loading = true;
    private int lastVisiblesItem, visibleItemCount, totalItemCount;
    private boolean doubleBackToExitPressedOnce = false;

    @Inject
    MainPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        initInjector();
        mAdapter = new StoriesAdapter(mItem);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvStories.setHasFixedSize(true);
        rvStories.setAdapter(mAdapter);
        rvStories.setLayoutManager(mLayoutManager);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.post(() -> onRefresh());
        rvStories.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    lastVisiblesItem = mLayoutManager.findFirstVisibleItemPosition();
                    if (loading) {
                        if ((visibleItemCount + lastVisiblesItem) >= totalItemCount) {
                            loading = false;
                            callData();
                        }
                    }
                }
            }
        });
    }

    private void initInjector() {
        DaggerMainComponent.builder()
                .netComponent(((App) getApplicationContext()).getNetComponent())
                .mainModule(new MainModule(this))
                .build().inject(this);
    }

    // mTempItem = 0
    // mItem = 0

    @Override
    public void getTopStories(List<Integer> list) {
        startIndex = 0;
        mTopStories.clear();
        // temp item
        mTempItem.clear(); // mItem = 0
        Log.d("getTopStories: ", mItem.size()+""); // mItem = 10
        mTempItem = mItem; // mTempItem = 10
        Log.d("getTopStories: ", mTempItem.size()+"");
        mItem.clear(); // mItem = 0
        Log.d("getTopStories: ", mItem.size()+"");
        mTopStories.addAll(list);
        mAdapter.notifyDataSetChanged();
        callData();
    }

    @Override
    public void getItem(List<ItemStories> list) {
        mTempItem.addAll(list); // mTempItem = 10
        Log.d("getItem: ", mTempItem.size()+"");
        mItem = mTempItem;
        Log.d("getItem: ", mItem.size()+""); // mItem = 10
        Log.d("getItem: ", mTempItem.size()+""); // mTempItem = 10
        mAdapter.notifyDataSetChanged();
        startIndex = mItem.size();
    }


    private void callData() {
        int endIndex = startIndex + 10;
        if (mTopStories.size() > startIndex && mTopStories.size() > endIndex) {
            List<Integer> id = new ArrayList<>();
            for (int i = startIndex; i < endIndex; i++) {
                id.add(mTopStories.get(i));
            }
            mPresenter.loadItem(id);
        }
    }

    @Override
    public void startLoading() {
        swipeLayout.setRefreshing(true);
    }

    @Override
    public void stopLoadingError(Throwable e) {
        swipeLayout.setRefreshing(false);
        rvStories.setVisibility(View.VISIBLE);
        loading = true;
        Snackbar.make(mainContent, R.string.error_main, Snackbar.LENGTH_SHORT)
                .setAction(R.string.try_again, view -> onRefresh())
                .show();
    }

    @Override
    public void stopLoading() {
        swipeLayout.setRefreshing(false);
        loading = true;
    }

    @Override
    public void onRefresh() {
        mPresenter.loadTopStories();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Snackbar.make(mainContent, R.string.back_notice, Snackbar.LENGTH_SHORT)
                .setAction(R.string.exit, view -> super.onBackPressed())
                .show();
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
    }

}
