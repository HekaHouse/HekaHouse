package house.heka.leto.ui.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import house.heka.leto.HermesActivity;
import house.heka.leto.LetoActivity;
import house.heka.leto.R;
import house.heka.leto.ui.LetoMarkdownViewClient;
import us.feras.mdv.MarkdownView;

/**
 * Created by aron2 on 3/20/2017.
 */

public class ReplyFragment extends DialogFragment {
    private static final String KEY = "key";
    private EditText reply;
    private MarkdownView preview;
    private Button mSend;
    private Button mClear;
    private String key;
    private ImageButton mImage;

    public ReplyFragment() {

    }
    public static ReplyFragment newInstance(String key) {
        ReplyFragment frag = new ReplyFragment();
        Bundle args = new Bundle();
        args.putString(KEY, key);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.reply_dialog, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view

        key = getArguments().getString(KEY);
        reply = (EditText) view.findViewById(R.id.reply);
        preview = (MarkdownView) view.findViewById(R.id.preview);

        mImage = (ImageButton) view.findViewById(R.id.add_image);
        mSend = (Button) view.findViewById(R.id.send);
        mClear = (Button) view.findViewById(R.id.clear);
        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("CLear entry")
                        .setMessage("Are you sure you want to clear this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                reply.setText("");
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HermesActivity)getActivity()).postReply(key, reply.getText().toString());
            }
        });

        preview.setWebViewClient(new LetoMarkdownViewClient(getLeto()));
        reply.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                preview.loadMarkdown(s.toString());
            }
        });

    }

    private LetoActivity getLeto() {
        return (LetoActivity) getActivity();
    }

}
