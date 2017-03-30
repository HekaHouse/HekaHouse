package house.heka.leto.ui.fragment.recycler;

import house.heka.leto.HermesActivity;
import house.heka.leto.ui.fragment.recycler.firebase.AgoniAdapter;

/**
 * Created by aron2 on 3/17/2017.
 */

public class RawFeedRecycler extends LetoRecycler {

    @Override
    public AgoniAdapter contructAdapter() {
        HermesActivity active = (HermesActivity) getActivity();
        return active.getRawFeedAdapter(fab);
    }


}
