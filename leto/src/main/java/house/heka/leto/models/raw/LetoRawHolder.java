package house.heka.leto.models.raw;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import house.heka.leto.R;

/**
 * Created by aron2 on 3/12/2017.
 */

public abstract class LetoRawHolder extends RecyclerView.ViewHolder {
    private final ImageView mThumb;
    private final ImageView mFullImage;

    public LetoRawHolder(View itemView) {
        super(itemView);
        mThumb = getThumb();
        mFullImage = getFullImage();
    }

    public abstract ImageView getFullImage();

    public abstract ImageView getThumb();
}
