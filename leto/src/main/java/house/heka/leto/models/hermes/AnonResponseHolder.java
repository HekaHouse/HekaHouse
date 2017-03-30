package house.heka.leto.models.hermes;

import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.fivehundredpx.android.blur.BlurringView;

import house.heka.leto.R;
import house.heka.leto.ui.LetoMarkdownViewClient;
import house.heka.leto.ui.fragment.ReasoningListDialogFragment;
import house.heka.leto.ui.fragment.ReplyFragment;
import us.feras.mdv.MarkdownView;

/**
 * Created by aron2 on 3/18/2017.
 */

public class AnonResponseHolder extends RecyclerView.ViewHolder {
    private final MarkdownView response;
    public final ImageView avatar;
    private final TextView name;
    private final ImageButton collapse;
    private final ImageButton reply;
//    private final BlurringView blur;
    private boolean blurred = true;
    private boolean collapsed = false;
    public AnonResponseHolder(View itemView) {
        super(itemView);
        response = (MarkdownView) itemView.findViewById(R.id.response);
        response.getSettings().setAllowContentAccess(false);
        response.getSettings().setAllowFileAccess(false);
        response.getSettings().setAllowFileAccessFromFileURLs(false);
        response.getSettings().setAllowUniversalAccessFromFileURLs(false);
        response.getSettings().setJavaScriptEnabled(false);
//        blur = (BlurringView) itemView.findViewById(R.id.blurring_view);
        name = (TextView) itemView.findViewById(R.id.name);
        avatar = (ImageView) itemView.findViewById(R.id.avatar);
        collapse = (ImageButton)itemView.findViewById(R.id.collapse_button);
        reply = (ImageButton)itemView.findViewById(R.id.reply);
    }

    public void setContent(AnonResponse content, final ReasoningListDialogFragment frag) {
//        blur.setBlurredView(response);
        name.setText(content.getProfile().name);

        if (content.isDirty())
            response.loadMarkdown(content.getResponse(),"file:///android_asset/blur.css");
        else
            response.loadMarkdown(content.getResponse(),"file:///android_asset/blur-image.css");
        response.setWebViewClient(new LetoMarkdownViewClient(frag.getLeto()));
        response.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (frag != null) {
                    frag.onReasoningClicked(getAdapterPosition());
                    frag.dismiss();
                }
            }
        });
        collapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (collapsed) {
                    avatar.setVisibility(View.VISIBLE);
                    response.setVisibility(View.VISIBLE);
                    collapsed = false;
                    collapse.setImageDrawable(frag.getLeto().getDrawable(R.drawable.ico_close));
                } else {
                    avatar.setVisibility(View.INVISIBLE);
                    response.setVisibility(View.GONE);
                    collapsed = true;
                    collapse.setImageDrawable(frag.getLeto().getDrawable(R.drawable.ico_expand));
                }
            }
        });

        reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = frag.getLeto().getSupportFragmentManager();
                ReplyFragment replyFrag = ReplyFragment.newInstance("Some Title");
                replyFrag.show(fm, "fragment_edit_name");
            }
        });
    }
}
