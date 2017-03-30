package house.heka.leto.models.hermes;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;

import com.google.common.collect.ObjectArrays;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import house.heka.leto.R;
import house.heka.leto.models.raw.LetoRaw;

/**
 * Created by aron2 on 3/12/2017.
 */

@IgnoreExtraProperties
public class HermesRaw extends LetoRaw implements Comparable<HermesRaw> {


    private String date;
    private String image;
    private String source;
    private String text;
    private String title;
    private double rank;



    private String gdelt;


    public HermesRaw() {

    }



    @Exclude
    @Override
    public @LayoutRes int getDefaultLayout() {
        return R.layout.item_hermes;
    }

    @Exclude
    @Override
    public @LayoutRes int getPhoneCardLayout() {
        return R.layout.item_hermes;
    }

    @Exclude
    @Override
    public @LayoutRes int getTabletCardLayout() {
        return R.layout.item_hermes;
    }

    @Exclude
    @Override
    public String getName() {
        return getTitle();
    }

    @Exclude
    @Override
    public String getDescription() {
        return getShortText();
    }

    @Exclude
    @Override
    public String getLocalThumbPath() {
        return getImage();
    }

    @Override
    public String getLocalImage() {
        return getImage();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSource() {
        return source;
    }

    public String getDisplaySource() {
        return source.replaceAll("https?:\\/\\/(.+?)\\/.*","$1");
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getShortText() {
        if (getText().replaceAll("\\n", " ").replaceAll("\\s+"," ").length() > 125)
            return getText().replaceAll("\\n", " ").replaceAll("\\s+"," ").substring(0,125)+"...";
        else
            return getText().replaceAll("\\n", " ").replaceAll("\\s+"," ");
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        if (title.replaceAll("\\n", " ").replaceAll("\\s+"," ").length() > 80)
            return title.replaceAll("\\n", " ").replaceAll("\\s+"," ").substring(0,73)+"...";
        else
            return title.replaceAll("\\n", " ").replaceAll("\\s+"," ");
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGdelt() {
        return gdelt;
    }

    public void setGdelt(String gdelt) {
        this.gdelt = gdelt;
    }

    @Override
    public int compareTo(@NonNull HermesRaw h) {
            if (this.rank < h.rank) {
                return 1;
            } else if (this.rank > h.rank) {
                return -1;
            } else {
                return 0;
            }
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }
}
