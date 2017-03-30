package house.heka.leto;

import android.support.annotation.LayoutRes;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import house.heka.leto.models.hermes.AnonResponse;
import house.heka.leto.models.hermes.HermesHolder;
import house.heka.leto.models.hermes.HermesIndex;
import house.heka.leto.models.hermes.HermesRaw;
import house.heka.leto.models.raw.LetoRaw;
import house.heka.leto.models.raw.LetoRawHolder;
import house.heka.leto.ui.fragment.LetoFragmentPager;
import house.heka.leto.ui.fragment.ReasoningListDialogFragment;
import house.heka.leto.ui.fragment.recycler.firebase.AgoniAdapter;
import house.heka.leto.ui.fragment.recycler.firebase.AgoniArray;
import house.heka.leto.ui.fragment.recycler.firebase.AgoniIndexAdapter;
import house.heka.local.models.ThemisItem;
import house.heka.themislib.model.secure.LocalEncryptedContent;

public class HermesActivity extends LetoActivity {


    private static final String TAG = "HermesActivity";
    private static final String ACTIVE_PAGE = "active_page";
    private ArrayList<String> queuedPaths;
    private ViewPager mPager;
    private LetoFragmentPager mPagerAdapter;
    private RecyclerView.OnScrollListener mScrollListener;
    private View.OnClickListener fabClick;
    private int pagerIndex;
    private boolean frontPageLoading=false;
    private HashMap<String,AgoniAdapter> activeAdapters = new HashMap<String,AgoniAdapter>();
    private String mostRecent;
    //private ArrayList<String> queuedPaths = new ArrayList<>();
    public String p1;
    public String p2;
    private String p3;
    public int minP1;

    public HermesActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hermes);
        FirebaseApp.initializeApp(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (queryFixed) {
            restoreFixed();
        }
    }



    @Override
    public void onPause() {
        super.onPause();
        persistIntPreference(ACTIVE_PAGE,mPager.getCurrentItem());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void collectRequiredViews() {
        rootDatabaseRef().child("items").child("most_recent").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (mostRecent == null) {
                            mostRecent = dataSnapshot.getValue().toString();
                            // Instantiate the RequestQueue.
                            String[] parts = getPathParts(mostRecent);
                            p3 = parts[2].substring(0, 1) + "00";
                            p2 = parts[1];
                            p1 = parts[0];

                            minP1 = Integer.valueOf(p1)-1;

                            ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
                            if (viewPager != null) {
                                setupViewPager(viewPager);
                            }


                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



    }


    @Override
    protected void setupViewPager(ViewPager viewPager) {
        mPager = viewPager;

        //Query topCats =  rootDatabaseRef().child("featured");
        Query topCats =  rootDatabaseRef().child("categories").orderByChild("article_count").limitToLast(13);
        topCats.keepSynced(true);
        topCats.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mPagerAdapter = new LetoFragmentPager(HermesActivity.this.getSupportFragmentManager(), HermesActivity.this,dataSnapshot);
                mPager.setAdapter(mPagerAdapter);

                TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
                tabLayout.setupWithViewPager(mPager);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }


    @Override
    protected void cleanupAdapters() {
        for (AgoniAdapter adapt: activeAdapters.values()) {
            adapt.cleanup();
        }
    }




    @Override
    protected void loadRawLetoContent(LetoRawHolder holder, LetoRaw content) {
        HermesHolder held = (HermesHolder)holder;
        HermesRaw contented = (HermesRaw)content;
        held.setTitle(contented.getName());
        held.setUrl(contented.getSource());
        held.setKey(contented.getGdelt());
        held.constructOnClickListener(this);

        Log.d(TAG,contented.getGdelt() + String.valueOf(contented.getRank()));

        held.setDescription(contented.getDescription());
        held.setSource(contented.getDisplaySource());
    }

    @Override
    public void restoreFixed() {
        mPager.setCurrentItem(retrieveIntPreference(ACTIVE_PAGE));
    }



//    @Override
//    public void setRecyclerView(final RecyclerView recyclerView) {
//        mRecycler = recyclerView;
//    }


    @Override
    public AgoniAdapter getRawFeedAdapter(FloatingActionButton fab) {
        if (activeAdapters.containsKey("raw")) {
            return activeAdapters.get("raw");
        } else {
//            Log.d(TAG, "methodCall:getRawFeedAdapter");
//            AgoniAdapter fired = getRawRecyclerAdapter(new HermesRaw().getDefaultLayout(), getBaseQuery(fab, "raw"));
//            activeAdapters.put("raw", fired);
            return null;
        }
    }

    @Override
    public AgoniAdapter getFrontPageAdapter(FloatingActionButton fab) {
//        if (activeAdapters.containsKey("front")) {
//            return activeAdapters.get("front");
//        } else {
//            Log.d(TAG, "methodCall:getFrontPageAdapter");
//            frontPageLoading = true;
//
//            AgoniAdapter fired = getRawRecyclerAdapter(new HermesRaw().getDefaultLayout(), getFrontPageQuery(), getBaseQuery(fab, "front"));
//            activeAdapters.put("front",fired);
//            return fired;
//        }
        return null;
    }

    @Override
    public AgoniAdapter getCategoryAdapter(FloatingActionButton fab, String category) {
        if (activeAdapters.containsKey(category)) {
            return activeAdapters.get(category);
        } else {
            Log.d(TAG, "methodCall:getRawFeedAdapter");
            AgoniAdapter fired = getRawRecyclerAdapter(new HermesRaw().getDefaultLayout(), category);
            activeAdapters.put(category, fired);
            return fired;
        }
    }

    public void setQueryAt(int article) {
        queryFixed = true;
        persistIntPreference(EXTERNAL_LINK,article);
    }

    @Override
    protected AgoniAdapter getRawRecyclerAdapter(@LayoutRes int layout, String category) {

        return new AgoniAdapter<HermesIndex, HermesHolder>(HermesIndex.class, layout, HermesHolder.class, new AgoniArray(category,this)) {
            @Override
            public void populateViewHolder(final HermesHolder holder, final HermesIndex content, int position) {
                Log.d(TAG,"child view added:category");
                if (content.getDockey() == null)
                    return;
                String path = getPathForKey(content.getDockey());
                rootDatabaseRef().child(path).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue(HermesRaw.class) == null)
                            return;
                        processRawLetoImages(holder,dataSnapshot.getValue(HermesRaw.class));
                        loadRawLetoContent(holder,dataSnapshot.getValue(HermesRaw.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                findViewById(R.id.recyclerLoading).setVisibility(View.GONE);
            }
        };
    }

    private String getPathForKey(String dockey) {
        String[] parts = getPathParts(dockey);
        String new2 = parts[2].substring(0,1) + "00";
        return "/content/items/"+parts[0]+"/"+parts[1]+"/"+new2+"/"+dockey;
    }

//    @Override
//    protected AgoniAdapter getRawRecyclerAdapter(@LayoutRes int layout, ArrayList<Query> ref) {
//
//        return new AgoniAdapter<HermesRaw, HermesHolder>(HermesRaw.class, layout, HermesHolder.class, ref) {
//            @Override
//            public void populateViewHolder(HermesHolder holder, HermesRaw content, int position) {
//                Log.d(TAG,"child view added:front page");
//                findViewById(R.id.recyclerLoading).setVisibility(View.GONE);
//                frontPageLoading = false;
//                processRawLetoImages(holder,content);
//                loadRawLetoContent(holder,content);
//            }
//        };
//    }

    @Override
    protected ArrayList<Query> getKeyQuery(String category) {
        ArrayList<Query> qs = new ArrayList<>();
//        queuedPaths = getPathQueue(category);
//        for (String queue: queuedPaths) {
//            Log.d(TAG,"looking for "+queue);
//            String[] keys = queue.split("/");
//            qs.add(getCategoryQuery(p1,keys[0],keys[1],category));
//        }
        return qs;
    }



    public Query getCategoryQuery(String p1, String p2, String p3, final String category) {
        Query q = rootDatabaseRef()
                .child("content")
                .child("categories")
                .child(category)
                .child(p1)
                .child(p2)
                .child(p3);
        q.keepSynced(true);
        q.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG,"child key added:"+category);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return q;
    }


    @Override
    protected ArrayList<Query> getFrontPageQuery() {
        ArrayList<Query> qs = new ArrayList<>();
        Query q = rootDatabaseRef().child("response").limitToFirst(50).orderByPriority();
//        if (queryFixed)
//            q = q.startAt(retrieveStringPreference(EXTERNAL_LINK));


        q.keepSynced(true);
        qs.add(q);
        return qs;
    }

    @Override
    protected ArrayList<Query> getBaseQuery(final FloatingActionButton fab, final String type) {
        ArrayList<Query> qs = new ArrayList<>();
        String[] pathWalker = getPathParts(mostRecent);
        int p3begin = Integer.valueOf(pathWalker[2].substring(0,1));
        if (Integer.valueOf(pathWalker[2].substring(1)) > 50) {
            qs.add(getItemQuery(pathWalker[0],pathWalker[1],p3begin+"00", type, fab));
        }

        while (p3begin > 0) {
            p3begin = p3begin - 1;
            qs.add(getItemQuery(pathWalker[0],pathWalker[1],p3begin+"00", type, fab));
        }

        String p2prior = String.valueOf(Integer.valueOf(pathWalker[1]) - 1);
        String startPriorAt = "900";
        while (qs.size() < 7) {
            qs.add(getItemQuery(pathWalker[0],p2prior,startPriorAt, type, fab));
            startPriorAt = String.valueOf(Integer.valueOf(startPriorAt) - 100);
        }
        return qs;
    }

    private Query getItemQuery(String p1, String p2, String p3, final String type, final FloatingActionButton fab) {
        Query q = rootDatabaseRef()
                .child("content")
                .child("items")
                .child(p1)
                .child(p2)
                .child(p3);
        q.keepSynced(true);
        q.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG,"child added:"+type);
                fab.setVisibility(View.VISIBLE);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return q;
    }

    public String[] getPathParts(String recent) {
        String part3 = recent.substring(recent.length()-3);
        String part2 = recent.substring(recent.length()-6,recent.length()-3);
        String part1 = recent.substring(0,recent.length()-6);
        return new String[]{part1,part2,part3};
    }

    public void contest(View view) {
        String[] tagged = view.getTag().toString().split(":");
        if (tagged.length < 2)
            return;
        String hash = createHash(tagged[1]);
        String reaction = null;
        List<ThemisItem> tis = ThemisItem.findItemForTag(hash);
        if (tis.size() > 0) {
            ThemisItem ti = tis.get(0);
            reaction = decryptForLocalUse(ti.generateLocalEncryptedContent());
        }
        ReasoningListDialogFragment.newInstance(tagged[0],tagged[1],reaction).show(getSupportFragmentManager(), tagged[0]);
    }
    public void concur(View view) {
        String[] tagged = view.getTag().toString().split(":");
        if (tagged.length < 2)
            return;
        String hash = createHash(tagged[1]);
        String reaction = null;
        List<ThemisItem> tis = ThemisItem.findItemForTag(hash);
        if (tis.size() > 0) {
            ThemisItem ti = tis.get(0);
            reaction = decryptForLocalUse(ti.generateLocalEncryptedContent());
        }
        ReasoningListDialogFragment.newInstance(tagged[0],tagged[1],reaction).show(getSupportFragmentManager(), tagged[0]);
    }

    public void postResponse(String qualifier, String key, String response) {
        DatabaseReference db = rootDatabaseRef().child("response").child(key).child(qualifier).push();
        db.setValue(new AnonResponse(response));
        db.setPriority(0-new Date().getTime());
    }

    public DatabaseReference getResponseRef(String qualifier, String key) {
        String hash = createHash(key);
        LocalEncryptedContent lec = encryptForLocalUse(key);
        ThemisItem responsible = new ThemisItem(lec,hash);
        responsible.save();
        return rootDatabaseRef().child("response").child(key).child(qualifier);
    }

    public void postResponseAgreement(String qualifier, String key, String responseKey) {
        String hash = createHash(key);
        LocalEncryptedContent lec = encryptForLocalUse(key+":"+responseKey);
        ThemisItem responsible = new ThemisItem(lec,hash);
        responsible.save();
        rootDatabaseRef().child("response").child(key).child(qualifier).child(responseKey).child("agree").push().setValue(true);
    }

    public void postReply(String key, String reply) {
        DatabaseReference db = rootDatabaseRef().child("discussion").child(key).push();
        db.setValue(new AnonResponse(reply));
        db.setPriority(0-new Date().getTime());
    }
}
