package house.heka.leto;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.ArrayList;

import house.heka.asteria.AsteriaActivity;
import house.heka.asteria.models.AsteriaItem;
import house.heka.leto.api.Leto;
import house.heka.leto.models.hermes.HermesHolder;
import house.heka.leto.models.hermes.HermesRaw;
import house.heka.leto.models.raw.LetoRaw;
import house.heka.leto.models.raw.LetoRawHolder;
import house.heka.leto.ui.fragment.ReasoningListDialogFragment;
import house.heka.leto.ui.fragment.recycler.firebase.AgoniAdapter;
import house.heka.local.models.ThemisItem;

public abstract class LetoActivity extends AsteriaActivity  {
    public static final String EXTERNAL_LINK = "external_link";
    private TextView remote_encrypt, remote_decrypt, remote_encrypt_two, remote_decrypt_two, local_encrypt, local_decrypt;
    private static final String TAG = "LetoActivity";
    private Button generateKeys;
    private Leto leto;
    public int height;
    public int width;

    public boolean queryFixed = false;

    //protected RecyclerView mRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leto);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;
        leto = new Leto(this);
    }
    @Override
    public void populateRemoteContent() {
        super.populateRemoteContent();
        collectRequiredViews();
    }


    protected void collectRequiredViews() {
        generateKeys = (Button) findViewById(R.id.generate);
        generateKeys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generate();
            }
        });
        remote_encrypt = (TextView) findViewById(R.id.remote_encrypt);
        remote_decrypt = (TextView) findViewById(R.id.remote_decrypt);

        remote_encrypt_two = (TextView) findViewById(R.id.remote_encrypt_two);
        remote_decrypt_two = (TextView) findViewById(R.id.remote_decrypt_two);

        local_encrypt = (TextView) findViewById(R.id.local_encrypt);
        local_decrypt = (TextView) findViewById(R.id.local_decrypt);
    }

    protected void secureScreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
    }

    protected FirebaseListAdapter getRawListAdapter(@LayoutRes int layout, Query ref) {
        return new FirebaseListAdapter<LetoRaw>(this, LetoRaw.class, layout, ref) {
            @Override
            protected void populateView(View view, LetoRaw rawItem, int position) {

            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanupAdapters();
    }


    protected void processRawLetoImages(LetoRawHolder holder, LetoRaw content) {
        if (holder.getFullImage() != null)
            loadLetoImage(content,holder.getFullImage());
        loadRawLetoContent(holder,content);
    }


    protected void loadLetoImage(LetoRaw raw, ImageView iv) {
        leto.loadFirebaseImage(raw.getLocalImage(),iv);
    }

    public void loadFirebaseImage(String path, ImageView iv) {
        leto.loadFirebaseImage(path,iv);
    }

    @Override
    public void generate()  {
        String message = "howdy";
        changePassword();
        try {
            byte[] sig = sign(message);

            if (verifyMySignature(message,sig)) {
                Toast.makeText(this, "signature verified", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "signature verification failed", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }



        remote_encrypt.setText("");
        remote_decrypt.setText("");
        remote_encrypt_two.setText("");
        remote_decrypt_two.setText("");
        local_encrypt.setText("");
        local_decrypt.setText("");

        String to_test = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi et tempor est, ac rutrum nibh. Suspendisse quis viverra tellus. Nullam quam felis, lobortis ac neque sit amet, pharetra cursus ligula. Proin tincidunt purus ex, non placerat eros ullamcorper porta. Sed consequat luctus dapibus. Cras ac rhoncus turpis. Maecenas consequat felis purus, ac posuere dolor aliquam vitae. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Praesent turpis nunc, maximus et eros nec, fringilla finibus sapien.\n" +
                "\n" +
                "Vestibulum orci arcu, facilisis quis eleifend ut, placerat ac ligula. Mauris justo enim, sodales a cursus vel, maximus ut neque. Morbi.";


        if (content.size() < 10) {
            DatabaseReference ref = getNewAsteriaRef();
            ThemisItem ti = new ThemisItem(encryptForLocalUse(to_test), ref.getKey());
            ti.save();
            createAsteriaItem(encryptForRemoteUse(to_test),ref);
        }



        for (ThemisItem c: collectLocalStoredContent()) {
            local_encrypt.setText(c.getEnc());
            local_decrypt.setText(decryptForLocalUse(c.generateLocalEncryptedContent()));
            break;
        }

    }

    protected DatabaseReference getNewAsteriaRef() {
        return freshDatabaseRef();
    }


    boolean odd = true;
    ArrayList<String> seen = new ArrayList<>();
    ArrayList<String> one = new ArrayList<>();
    ArrayList<String> two = new ArrayList<>();
    @Override
    public void addRemoteItem(AsteriaItem ai) {
        if (seen.contains(ai.tag))
            return;
        if (odd || remote_encrypt.getText().length() < 1) {
            one.add(ai.tag);
            remote_encrypt.setText(ai.content);
            remote_decrypt.setText(ai.decryptContent(this));
            odd = false;
        } else {
            two.add(ai.tag);
            remote_encrypt_two.setText(ai.content);
            remote_decrypt_two.setText(ai.decryptContent(this));
            odd = true;
        }
        seen.add(ai.tag);
    }

    @Override
    protected void removeRemoteContent(String tag) {
        seen.remove(tag);
        if (one.contains(tag)) {
            one.remove(tag);
            remote_encrypt.setText("");
            remote_decrypt.setText("");
        } else if (two.contains(tag)) {
            two.remove(tag);
            remote_encrypt.setText("");
            remote_decrypt.setText("");
            odd = true;
        }
    }

    //public abstract void setRecyclerView(RecyclerView recyclerView);
    public abstract AgoniAdapter getCategoryAdapter(FloatingActionButton fab, String category);
    public abstract AgoniAdapter getRawFeedAdapter(FloatingActionButton fab);
    public abstract AgoniAdapter getFrontPageAdapter(FloatingActionButton fab);
    public abstract void restoreFixed();
    protected abstract ArrayList<Query> getFrontPageQuery();
    protected abstract ArrayList<Query> getKeyQuery(String category);
    protected abstract ArrayList<Query> getBaseQuery(FloatingActionButton fab, String type);
    protected abstract void setupViewPager(ViewPager viewPager);
    protected abstract void cleanupAdapters();

    protected abstract AgoniAdapter getRawRecyclerAdapter(@LayoutRes int layout,String category) ;
    protected abstract void loadRawLetoContent(LetoRawHolder holder, LetoRaw content);
}
