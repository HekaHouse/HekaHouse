package house.heka.leto.ui.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import house.heka.leto.LetoActivity;
import house.heka.leto.ui.fragment.recycler.CategoryPageRecycler;
import house.heka.leto.ui.fragment.recycler.FrontPageRecycler;
import house.heka.leto.ui.fragment.recycler.RawFeedRecycler;
import house.heka.leto.ui.fragment.recycler.LetoRecycler;

/**
 * Created by aron2 on 3/17/2017.
 */

public class LetoFragmentPager extends FragmentStatePagerAdapter {
    private final LetoActivity mActive;
    private final RawFeedRecycler baseRecycle;
    private final DataSnapshot mSnapshot;
    private ArrayList<String> items = new ArrayList<>();
    public LetoFragmentPager(FragmentManager fm, LetoActivity active, DataSnapshot titles) {
        super(fm);
        mActive = active;
        baseRecycle = getRawFeedRecycler();
        mSnapshot = titles;
        Iterable<DataSnapshot> kids = mSnapshot.getChildren();
        for (DataSnapshot snapshot : kids) {
            items.add(snapshot.getKey());
        }
        Collections.reverse(items);
    }


    public String getPageTitle(int position) {
        return items.get(position);
    }


    @Override
    public Fragment getItem(int position) {
        LetoRecycler recycle;

        recycle = getCategoryRecycler(items.get(position));


        return recycle;
    }

    private LetoRecycler getCategoryRecycler(String category) {
        CategoryPageRecycler cpr = new CategoryPageRecycler();
        cpr.setCategory(category);
        cpr.setActive(mActive);
        return cpr;
    }

    private LetoRecycler getFrontPageRecycler() {
        FrontPageRecycler fpr = new FrontPageRecycler();
        fpr.setActive(mActive);
        return fpr;
    }

    private RawFeedRecycler getRawFeedRecycler() {
        RawFeedRecycler rpr = new RawFeedRecycler();
        rpr.setActive(mActive);
        return rpr;
    }

    @Override
    public int getCount() {
        return (int)mSnapshot.getChildrenCount();
    }
}
