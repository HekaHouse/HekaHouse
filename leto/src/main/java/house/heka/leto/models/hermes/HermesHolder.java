package house.heka.leto.models.hermes;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import house.heka.leto.HermesActivity;
import house.heka.leto.R;
import house.heka.leto.models.raw.LetoRawHolder;

/**
 * Created by aron2 on 3/12/2017.
 */

public class HermesHolder extends LetoRawHolder {
    private final TextView title;
    private final TextView description;
    private final ImageView full_image;
    private final TextView source_text;
    private final ImageButton concur;
    private final ImageButton contest;
    private final CardView root;
    private String source;
    private String key;

    public HermesHolder(View itemView) {
        super(itemView);
        root = (CardView) itemView.findViewById(R.id.root_view);
        title = (TextView) itemView.findViewById(R.id.title_text);
        description = (TextView) itemView.findViewById(R.id.desc_text);
        full_image = (ImageView) itemView.findViewById(R.id.main_image);
        source_text = (TextView) itemView.findViewById(R.id.source_text);
        concur = (ImageButton) itemView.findViewById(R.id.concurButton);
        contest = (ImageButton) itemView.findViewById(R.id.contestButton);
    }

    @Override
    public ImageView getFullImage() {
        return full_image;
    }

    @Override
    public ImageView getThumb() {
        return null;
    }

    public void setTitle(String name) {
        title.setText(name);
    }

    public void setDescription(String description_text) {
        description.setText(description_text);
    }


    public void constructOnClickListener(final HermesActivity hermes) {
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hermes.setQueryAt(getAdapterPosition());
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(source));
                hermes.startActivity(browserIntent);
            }
        });
    }

    public void setUrl(String source) {
        this.source = source;
    }

    public void setKey(String gdelt) {
        key = gdelt;
        concur.setTag("concur:"+key);
        contest.setTag("contest:"+key);
    }

    public void setSource(String displaySource) {
        source_text.setText(displaySource);
        root.setVisibility(View.VISIBLE);
    }
}
