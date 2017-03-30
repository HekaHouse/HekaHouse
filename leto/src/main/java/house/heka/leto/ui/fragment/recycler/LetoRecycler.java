package house.heka.leto.ui.fragment.recycler;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;

import house.heka.leto.HermesActivity;
import house.heka.leto.LetoActivity;
import house.heka.leto.R;
import house.heka.leto.models.hermes.HermesRaw;
import house.heka.leto.ui.GravitySnapHelper;
import house.heka.leto.ui.fragment.recycler.firebase.AgoniAdapter;

/**
 * Created by aron2 on 3/17/2017.
 */

public abstract class LetoRecycler extends Fragment {
    private static final String TAG = "LetoRecycler";
    private LetoActivity mActive;
    protected FloatingActionButton fab;
    private ViewGroup mRoot;
    private RecyclerView cycler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.recycler_page, container, false);
        fab = (FloatingActionButton) rootView.findViewById(R.id.up_button);

        mRoot = rootView;

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFragmentUIActive()) {

            mActive.findViewById(R.id.recyclerLoading).setVisibility(View.VISIBLE);
            cycler = getRecyclerView(mRoot);
            prepFloatingActionButton(fab, cycler);

            if (mActive.queryFixed ) {
                cycler.scrollToPosition(mActive.retrieveIntPreference(LetoActivity.EXTERNAL_LINK));
                mActive.queryFixed = false;
            }
            cycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    int firstVisibleItem = ((LinearLayoutManager)cycler.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                    if (firstVisibleItem > 1) {
                        fab.setVisibility(View.VISIBLE);
                        //Log.d(TAG,String.valueOf(((HermesRaw)((AgoniAdapter)cycler.getAdapter()).getItem(firstVisibleItem)).getRank()));
                    }
                    else {

                    }
                    //
                    if(firstVisibleItem == 0){
                        scrollToTop(recyclerView);
                        fab.setVisibility(View.GONE);
                    }
                }
            });
        }

    }

    public boolean isFragmentUIActive() {
        return isVisible();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void scrollToTop(RecyclerView cycle) {
        if (mActive.queryFixed)
            mActive.restoreFixed();

        cycle.scrollToPosition(0);
    }

    private void prepFloatingActionButton(FloatingActionButton fab, final RecyclerView rv) {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollToTop(rv);
            }
        });
    }

    public void setActive(LetoActivity active) {
        mActive = active;
    }


    public RecyclerView getRecyclerView(View v) {
        HermesActivity active = (HermesActivity) getActivity();
        RecyclerView rv = (RecyclerView) v.findViewById(R.id.hermes_content);
        rv.setHasFixedSize(true);

        rv.setOnFlingListener(null);

        SnapHelper snapHelper = new GravitySnapHelper(Gravity.TOP);
        snapHelper.attachToRecyclerView(rv);

        rv.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false));

        ViewGroup.LayoutParams params=rv.getLayoutParams();
        params.height=active.height;
        rv.setLayoutParams(params);

        AgoniAdapter adapter = contructAdapter();

        rv.setAdapter(adapter);


        return rv;
    }

    public abstract AgoniAdapter contructAdapter();
}
