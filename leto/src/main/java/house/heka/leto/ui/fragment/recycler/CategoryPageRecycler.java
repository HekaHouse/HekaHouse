package house.heka.leto.ui.fragment.recycler;

import house.heka.leto.HermesActivity;
import house.heka.leto.ui.fragment.recycler.firebase.AgoniAdapter;

/**
 * Created by aron2 on 3/17/2017.
 */

public class CategoryPageRecycler extends LetoRecycler {

    private String mCategory = "world";

    @Override
    public AgoniAdapter contructAdapter() {
        //DatabaseReference keys = rootDatabaseRef().child("content").child("categories").child("science").orderByPriority().getRef();
        HermesActivity active = (HermesActivity) getActivity();

        return active.getCategoryAdapter(fab,mCategory);
    }

    public void setCategory(String category) {
        mCategory = category;
    }
}
