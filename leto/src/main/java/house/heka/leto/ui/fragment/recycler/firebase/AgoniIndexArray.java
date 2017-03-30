package house.heka.leto.ui.fragment.recycler.firebase;

import android.util.Log;

import com.firebase.ui.database.ChangeEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class AgoniIndexArray extends AgoniArray {
    private static final String TAG = AgoniIndexArray.class.getSimpleName();

    private ArrayList<Query> mQueries;
    private ChangeEventListener mListener;
    private Map<Query, ValueEventListener> mRefs = new HashMap<>();
    private List<DataSnapshot> mDataSnapshots = new ArrayList<>();
    HashMap<String, Query> mKeys = new HashMap<>();

    public AgoniIndexArray(ArrayList<Query> keyRef, ArrayList<Query> dataRef) {
        super("", null);
        mQueries = dataRef;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        Set<Query> refs = new HashSet<>(mRefs.keySet());
        for (Query ref : refs) {
            ref.removeEventListener(mRefs.remove(ref));
        }
    }

    @Override
    public int getCount() {
        return mDataSnapshots.size();
    }

    @Override
    public DataSnapshot getItem(int index) {
        return mSnapshots.get(getIndexForKey(mDataSnapshots.get(index).getKey()));
    }

    private int getIndexForKey(String key) {
        int dataCount = getCount();
        int index = 0;
        for (int keyIndex = 0; index < dataCount; keyIndex++) {
            String superKey = super.getItem(keyIndex).getKey();
            if (key.equals(superKey)) {
                break;
            } else if (mDataSnapshots.get(index).getKey().equals(superKey)) {
                index++;
            }
        }
        return index;
    }

    private boolean isKeyAtIndex(String key, int index) {
        return index >= 0 && index < getCount() && mDataSnapshots.get(index).getKey().equals(key);
    }

    @Override
    public void onChildAdded(DataSnapshot keySnapshot, String previousChildKey) {
        super.setOnChangedListener(null);
        super.onChildAdded(keySnapshot, previousChildKey);
        super.setOnChangedListener(mListener);
        Query ref = keySnapshot.getRef();
        mKeys.put(keySnapshot.getKey(),ref);
        mRefs.put(ref, ref.addValueEventListener(new AgoniIndexArray.DataRefListener()));
    }

    private Query findChildRef(String key) {
        return mKeys.get(key);
    }

    @Override
    public void onChildChanged(DataSnapshot snapshot, String previousChildKey) {
        super.setOnChangedListener(null);
        super.onChildChanged(snapshot, previousChildKey);
        super.setOnChangedListener(mListener);
    }

    @Override
    public void onChildRemoved(DataSnapshot keySnapshot) {
        String key = keySnapshot.getKey();
        int index = getIndexForKey(key);
        findAndRemove(key);


        super.setOnChangedListener(null);
        super.onChildRemoved(keySnapshot);
        super.setOnChangedListener(mListener);

        if (isKeyAtIndex(key, index)) {
            mDataSnapshots.remove(index);
            notifyChangedListeners(ChangeEventListener.EventType.REMOVED, index);
        }
    }

    private void findAndRemove(final String key) {
        mKeys.remove(key);
        for (final Query mQuery: mQueries)
            mQuery.getRef().child(key).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mQuery.getRef().child(key).removeEventListener(mRefs.remove(mQuery.getRef().child(key)));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

    }

    @Override
    public void onChildMoved(DataSnapshot keySnapshot, String previousChildKey) {
        String key = keySnapshot.getKey();
        int oldIndex = getIndexForKey(key);

        super.setOnChangedListener(null);
        super.onChildMoved(keySnapshot, previousChildKey);
        super.setOnChangedListener(mListener);

        if (isKeyAtIndex(key, oldIndex)) {
            DataSnapshot snapshot = mDataSnapshots.remove(oldIndex);
            int newIndex = getIndexForKey(key);
            mDataSnapshots.add(newIndex, snapshot);
            notifyChangedListeners(ChangeEventListener.EventType.MOVED, newIndex, oldIndex);
        }
    }

    @Override
    public void onCancelled(DatabaseError error) {
        Log.e(TAG, "A fatal error occurred retrieving the necessary keys to populate your adapter.");
        super.onCancelled(error);
    }

    @Override
    public void setOnChangedListener(ChangeEventListener listener) {
        super.setOnChangedListener(listener);
        mListener = listener;
    }

    private class DataRefListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            String key = snapshot.getKey();
            int index = getIndexForKey(key);

            if (snapshot.getValue() != null) {
                if (!isKeyAtIndex(key, index)) {
                    mDataSnapshots.add(index, snapshot);
                    notifyChangedListeners(ChangeEventListener.EventType.ADDED, index);
                } else {
                    mDataSnapshots.set(index, snapshot);
                    notifyChangedListeners(ChangeEventListener.EventType.CHANGED, index);
                }
            } else {
                if (isKeyAtIndex(key, index)) {
                    mDataSnapshots.remove(index);
                    notifyChangedListeners(ChangeEventListener.EventType.REMOVED, index);
                } else {
                    Log.w(TAG, "Key not found at ref: " + snapshot.getRef());
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            notifyCancelledListeners(error);
        }
    }
}
