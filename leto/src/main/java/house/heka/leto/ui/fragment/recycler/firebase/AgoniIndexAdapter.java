package house.heka.leto.ui.fragment.recycler.firebase;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

import java.util.ArrayList;

/**
 * This class is a generic way of backing an RecyclerView with a Firebase location.
 * It handles all of the child events at the given Firebase location. It marshals received data into the given
 * class type.
 * <p>
 * To use this class in your app, subclass it passing in all required parameters and implement the
 * populateViewHolder method.
 * <p>
 * <pre>
 *     private static class ChatMessageViewHolder extends RecyclerView.ViewHolder {
 *         TextView messageText;
 *         TextView nameText;
 *
 *         public ChatMessageViewHolder(View itemView) {
 *             super(itemView);
 *             nameText = (TextView)itemView.findViewById(android.R.id.text1);
 *             messageText = (TextView) itemView.findViewById(android.R.id.text2);
 *         }
 *     }
 *
 *     FirebaseIndexRecyclerAdapter<ChatMessage, ChatMessageViewHolder> adapter;
 *     DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
 *
 *     RecyclerView recycler = (RecyclerView) findViewById(R.id.messages_recycler);
 *     recycler.setHasFixedSize(true);
 *     recycler.setLayoutManager(new LinearLayoutManager(this));
 *
 *     adapter = new FirebaseIndexRecyclerAdapter<ChatMessage, ChatMessageViewHolder>(
 *          ChatMessage.class, android.R.layout.two_line_list_item, ChatMessageViewHolder.class, keyRef, dataRef) {
 *         public void populateViewHolder(ChatMessageViewHolder chatMessageViewHolder,
 *                                        ChatMessage chatMessage,
 *                                        int position) {
 *             chatMessageViewHolder.nameText.setText(chatMessage.getName());
 *             chatMessageViewHolder.messageText.setText(chatMessage.getMessage());
 *         }
 *     };
 *     recycler.setAdapter(mAdapter);
 * </pre>
 *
 * @param <T>  The Java class that maps to the type of objects stored in the Firebase location.
 * @param <VH> The ViewHolder class that contains the Views in the layout that is shown for each object.
 */
public abstract class AgoniIndexAdapter<T, VH extends RecyclerView.ViewHolder>
        extends AgoniAdapter<T, VH> {
    private static final String TAG = "AgoniIndexAdapter";

    /**
     * @param modelClass      Firebase will marshall the data at a location into an instance
     *                        of a class that you provide
     * @param modelLayout     This is the layout used to represent a single item in the list.
     *                        You will be responsible for populating an
     *                        instance of the corresponding view with the data from an instance of modelClass.
     * @param viewHolderClass The class that hold references to all sub-views in an instance modelLayout.
     * @param keyRef          The Firebase location containing the list of keys to be found in {@code dataRef}.
     *                        Can also be a slice of a location, using some
     *                        combination of {@code limit()}, {@code startAt()}, and {@code endAt()}.
     * @param dataRef         The Firebase location to watch for data changes.
     *                        Each key key found at {@code keyRef}'s location represents
     *                        a list item in the {@code RecyclerView}.
     */
    public AgoniIndexAdapter(Class<T> modelClass,
                                        @LayoutRes int modelLayout,
                                        Class<VH> viewHolderClass,
                             ArrayList<Query> keyRef,
                             ArrayList<Query> dataRef) {
        super(modelClass, modelLayout, viewHolderClass, new AgoniIndexArray(keyRef, dataRef));
    }

    @Override
    public void onBindViewHolder(VH viewHolder, int position) {
        T model = getItem(position);
        if (model == null)
            Log.d(TAG,"null model");
        else
            populateViewHolder(viewHolder, model, position);
    }

    @Override
    public T getItem(int position) {
        return mSnapshots.getItem(position).getValue(mModelClass);
    }
}