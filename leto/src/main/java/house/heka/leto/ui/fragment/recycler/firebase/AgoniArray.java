package house.heka.leto.ui.fragment.recycler.firebase;


import android.support.v4.view.ViewPager;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.firebase.ui.database.ChangeEventListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import house.heka.leto.HermesActivity;
import house.heka.leto.R;
import house.heka.leto.models.hermes.HermesIndex;
import house.heka.leto.models.hermes.HermesRaw;

/**
 * This class implements an array-like collection on top of a Firebase location.
 */
public class AgoniArray implements ChildEventListener, ValueEventListener {
    private static final String TAG = "AgoniArray";
    private ArrayList<Query> mQueries = new ArrayList<>();
    private ChangeEventListener mListener;
    List<DataSnapshot> mSnapshots = new ArrayList<>();
    private ArrayList<String> queuedPaths;
    private String p1;
    private String p2;
    private int queryfails = 0;
    public AgoniArray(String category, HermesActivity active) {
        getPathQueue(category,active);

    }

    private void getPathQueue(final String category, final HermesActivity active) {
        active.rootDatabaseRef().child("categories")
                .child(category)
                .child("most_recent")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String most_recent = dataSnapshot.getValue().toString();
                    if (most_recent.matches("^\\d+$")) {
                        String[] parts = active.getPathParts(most_recent);
                        p1 = parts[0];
                        p2 = parts[1];
                    } else {
                        p1 = active.p1;
                        p2 = active.p2;
                    }
                }
                queuedPaths = new ArrayList<String>();


                final String baseUrl = "https://foodiebot-7d50c.firebaseio.com/content/categories/"+category+"/";

                final Response.ErrorListener error = new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                };

                Response.Listener<String> resp = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String strResponse) {
                        JSONObject response = null;
                        if (strResponse.equals("null")) {
                            foundEmpty(category, p1, p2, active);
                            if (Integer.valueOf(p2) < 1) {
                                p1 = String.valueOf(Integer.valueOf(p1) - 1);
                                p2 = "900";
                            } else {
                                p2 = String.valueOf(Integer.valueOf(p2) - 1);
                            }
                            Log.d(TAG,p1+"/"+p2);
                            while(hasBeenFoundEmpty(category, p1, p2,active) &&
                                    Integer.valueOf(p1) >= active.minP1 &&
                                    Integer.valueOf(p2) >-1) {
                                if (Integer.valueOf(p2) < 1) {
                                    p1 = String.valueOf(Integer.valueOf(p1) - 1);
                                    p2 = "900";
                                } else {
                                    p2 = String.valueOf(Integer.valueOf(p2) - 1);
                                }
                                Log.d(TAG,p1+"/"+p2);
                            }
                            if (queuedPaths.size() < 50 && Integer.valueOf(p1) >= active.minP1) {
                                queryfails = queryfails + 1;
                                active.stringRequest(baseUrl + p1 + "/" + p2 + ".json?shallow=true",
                                        this, error);
                            }
                        } else {

                            try {
                                response = new JSONObject(strResponse);


                                for (Iterator<String> keyIt = response.keys(); keyIt.hasNext();){
                                    String key = keyIt.next();
                                    //Log.d(TAG+":"+category,"adding "+p2+"/"+key);
                                    addPath(p2+"/"+key,category,active);
                                    queuedPaths.add(p2+"/"+key);
                                }
                                if (Integer.valueOf(p2) < 1) {
                                    p1 = String.valueOf(Integer.valueOf(p1) - 1);
                                    p2 = "900";
                                } else {
                                    p2 = String.valueOf(Integer.valueOf(p2) - 1);
                                }
                                Log.d(TAG,p1+"/"+p2);
                                while(hasBeenFoundEmpty(category, p1, p2,active) &&
                                        Integer.valueOf(p1) >=640 &&
                                        Integer.valueOf(p2) >-1) {
                                    if (Integer.valueOf(p2) < 1) {
                                        p1 = String.valueOf(Integer.valueOf(p1) - 1);
                                        p2 = "900";
                                    } else {
                                        p2 = String.valueOf(Integer.valueOf(p2) - 1);
                                    }
                                    Log.d(TAG,p1+"/"+p2);
                                }
                                if (queuedPaths.size() < 50  && Integer.valueOf(p1) >= active.minP1) {
                                    //Log.d(TAG,"requesting "+baseUrl + p1 + "/" + p2 + ".json?shallow=true");
                                    active.stringRequest(baseUrl+ p1 + "/" + p2 + ".json?shallow=true",
                                            this, error);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }


                    }
                };

                active.stringRequest(baseUrl+ p1 + "/" + p2 + ".json?shallow=true",
                        resp, error);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    private void foundEmpty(String category, String p1, String p2, HermesActivity active) {
        active.persistStringPreference(category+":"+p1+":"+p2,"foundEmpty");
    }

    private boolean hasBeenFoundEmpty(String category, String p1, String p2, HermesActivity active) {
        return active.retrieveStringPreference(category+":"+p1+":"+p2).length() > 0;
    }

    private void addPath(String path, String category, HermesActivity active) {
        String[] keys = path.split("/");
        Query q = active.getCategoryQuery(p1,keys[0],keys[1],category);
        mQueries.add(q);
        q.addChildEventListener(AgoniArray.this);
        q.addValueEventListener(AgoniArray.this);
    }

    public void cleanup() {
        for (Query mQuery: mQueries) {
            mQuery.removeEventListener((ValueEventListener) this);
            mQuery.removeEventListener((ChildEventListener) this);
        }
    }

    public int getCount() {
        return mSnapshots.size();
    }

    public DataSnapshot getItem(int index) {
        return mSnapshots.get(index);
    }

    private int getIndexForKey(String key) {
        int index = 0;
        for (DataSnapshot snapshot : mSnapshots) {
            if (snapshot.getValue(HermesRaw.class).getGdelt() != null) {
                if (snapshot.getValue(HermesRaw.class).getGdelt().equals(key)) {
                    return index;
                } else {
                    index++;
                }
            } else {
                if (snapshot.getValue(HermesIndex.class).getDockey().equals(key)) {
                    return index;
                } else {
                    index++;
                }
            }
        }

        return -1;
    }

    @Override
    public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
//        int index = 0;
//        if (getIndexForKey(snapshot.getKey()) > -1)
//            return;
//        if (previousChildKey != null) {
//            index = getIndexForKey(previousChildKey) + 1;
//        }
        mSnapshots.add(snapshot);

        notifyChangedListeners(ChangeEventListener.EventType.ADDED, mSnapshots.indexOf(snapshot));
    }

    @Override
    public void onChildChanged(DataSnapshot snapshot, String previousChildKey) {
        int index = getIndexForKey(snapshot.getKey());
        mSnapshots.set(index, snapshot);
        notifyChangedListeners(ChangeEventListener.EventType.CHANGED, index);
    }

    @Override
    public void onChildRemoved(DataSnapshot snapshot) {
        int index = getIndexForKey(snapshot.getKey());
        mSnapshots.remove(index);
        notifyChangedListeners(ChangeEventListener.EventType.REMOVED, index);
    }

    @Override
    public void onChildMoved(DataSnapshot snapshot, String previousChildKey) {
        int oldIndex = getIndexForKey(snapshot.getKey());
        mSnapshots.remove(oldIndex);
        int newIndex = previousChildKey == null ? 0 : (getIndexForKey(previousChildKey) + 1);
        mSnapshots.add(newIndex, snapshot);
        notifyChangedListeners(ChangeEventListener.EventType.MOVED, newIndex, oldIndex);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        mListener.onDataChanged();
    }

    @Override
    public void onCancelled(DatabaseError error) {
        notifyCancelledListeners(error);
    }

    public void setOnChangedListener(ChangeEventListener listener) {
        mListener = listener;
    }

    protected void notifyChangedListeners(ChangeEventListener.EventType type, int index) {
        notifyChangedListeners(type, index, -1);
    }

    protected void notifyChangedListeners(ChangeEventListener.EventType type, int index, int oldIndex) {
        if (mListener != null) {
            mListener.onChildChanged(type, index, oldIndex);
        }
    }

    protected void notifyCancelledListeners(DatabaseError error) {
        if (mListener != null) {
            mListener.onCancelled(error);
        }
    }
}
