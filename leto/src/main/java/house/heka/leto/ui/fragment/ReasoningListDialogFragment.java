package house.heka.leto.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;

import house.heka.leto.HermesActivity;
import house.heka.leto.LetoActivity;
import house.heka.leto.R;
import house.heka.leto.models.hermes.AnonResponse;
import house.heka.leto.models.hermes.AnonResponseHolder;
import house.heka.leto.ui.LetoMarkdownViewClient;
import us.feras.mdv.MarkdownView;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     ReasoningListDialogFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 *
 */
public class ReasoningListDialogFragment extends BottomSheetDialogFragment  {

    // TODO: Customize parameter argument names
    private static final String ARG_ITEM_COUNT = "item_count";
    private static final String CHOICE = "choice";
    private static final String KEY = "key";
    private static final String REACTION = "reaction";
    private FirebaseRecyclerAdapter<AnonResponse, AnonResponseHolder> adapter;
    private String choice;
    private String key;
    private String reaction;

    // TODO: Customize parameters
    public static ReasoningListDialogFragment newInstance(String choice, String key, @Nullable String reaction) {
        final ReasoningListDialogFragment fragment = new ReasoningListDialogFragment();
        final Bundle args = new Bundle();
        args.putString(CHOICE, choice);
        args.putString(KEY, key);
        if (reaction != null) {
            args.putString(REACTION, reaction);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reasoning_list_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.choices);
        final Button send = (Button) view.findViewById(R.id.send);
        final MarkdownView preview = (MarkdownView) view.findViewById(R.id.preview);
        final EditText reason = (EditText) view.findViewById(R.id.reason);

        choice = getArguments().getString(CHOICE);
        key = getArguments().getString(KEY);
        reaction = getArguments().getString(REACTION);



        if (reaction != null) {
            view.findViewById(R.id.intro_text).setVisibility(View.GONE);
            view.findViewById(R.id.reasonform).setVisibility(View.GONE);
            view.findViewById(R.id.choices).setVisibility(View.GONE);
            view.findViewById(R.id.buttons).setVisibility(View.GONE);
            view.findViewById(R.id.responses).setVisibility(View.VISIBLE);
            recyclerView = (RecyclerView) view.findViewById(R.id.responses);
        } else {
            preview.setWebViewClient(new LetoMarkdownViewClient(getLeto()));
            reason.addTextChangedListener(new TextWatcher() {
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

            final TextView intro = (TextView) view.findViewById(R.id.intro_text);

            if (choice.equals("concur"))
                intro.setText(R.string.concur_intro);
            else
                intro.setText(R.string.contest_intro);

            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((HermesActivity)getActivity()).postResponse(choice, key, reason.getText().toString());
                }
            });

        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new FirebaseRecyclerAdapter<AnonResponse, AnonResponseHolder>(AnonResponse.class, R.layout.anon_response, AnonResponseHolder.class, ((HermesActivity)getActivity()).getResponseRef(choice,key)) {
            @Override
            public void populateViewHolder(AnonResponseHolder holder, AnonResponse content, int position) {
                holder.setContent(content, ReasoningListDialogFragment.this);
                getLeto().loadFirebaseImage(content.getProfile().avatar,holder.avatar);
            }
        };

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }



    public void onReasoningClicked(int position) {
        if (reaction == null)
            ((HermesActivity)getActivity()).postResponseAgreement(choice, key, adapter.getRef(position).getKey());
    };

    public LetoActivity getLeto() {
        return (LetoActivity)getActivity();
    }


}
