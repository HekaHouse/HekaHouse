package house.heka.leto.models.raw;

import android.support.annotation.LayoutRes;
import android.util.DisplayMetrics;

import house.heka.leto.LetoActivity;
import house.heka.leto.R;

/**
 * Created by aron2 on 3/12/2017.
 */

public abstract class LetoRaw {
    protected String remoteImagePath;
    protected String localImagePath;

    protected abstract @LayoutRes int getDefaultLayout();
    protected abstract @LayoutRes int getPhoneCardLayout();
    protected abstract @LayoutRes int getTabletCardLayout();

    public LetoRaw() {

    }


    public abstract String getName();

    public abstract String getDescription();

    public abstract String getLocalThumbPath();

    public abstract String getLocalImage();
}
