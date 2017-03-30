package house.heka.asteria;

import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

import house.heka.asteria.api.Asteria;
import house.heka.asteria.models.AsteriaItem;
import house.heka.asteria.models.FireToken;
import house.heka.local.models.AuthKey;
import house.heka.local.models.FireKey;
import house.heka.local.models.ThemisItem;
import house.heka.local.models.ThemisKey;
import house.heka.themislib.ThemisActivity;
import house.heka.themislib.model.secure.RemoteEncryptedContent;


public abstract class AsteriaActivity extends ThemisActivity {

    public static final int BASE64_SAFE_URL_FLAGS = Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP;
    private static final String TAG = "MainActivity";


    private Button generateKeys;
    private Asteria asteria;
    private Asteria.Authentic auth;
    private FireKey fireKey;
    protected List<ThemisItem> content;
    private RequestQueue volleyQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asteria);

    }

    public void onDataPrepared() {
        content = collectLocalStoredContent();
        populateRemoteContent();
    }


    protected void createAsteriaItem(RemoteEncryptedContent remoteEncryptedContent, DatabaseReference ref) {
        asteria.createAsteriaItem(remoteEncryptedContent,this,ref);
    }
    protected DatabaseReference freshDatabaseRef() {
        return asteria.getFreshRef();
    }
    public DatabaseReference rootDatabaseRef() {
        return asteria.getRootRef();
    }
    protected void populateRemoteContent() {
        asteria.populateRemoteContent(this);
    }

    @Override
    public List<ThemisItem> collectLocalStoredContent() {
        return ThemisItem.listAll(ThemisItem.class);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth = Asteria.initAuth(this);
        auth.startAuthenticListener();
        fireKey = FireToken.getFireKey();
        if (fireKey == null)
            auth.authenticateAnonymously(this);
        else
            auth.authenticateDeviceKey(this,fireKey);
    }

    @Override
    public void onStop() {
        super.onStop();
        auth.stopAuthenticListener();
    }

    public void setPersistance(boolean persist) {
        asteria.setPersistance(persist);
    }

    public abstract void generate();


    public abstract void addRemoteItem(AsteriaItem ai);

    public void setAsteria(Asteria asteria) {
        this.asteria = asteria;
        fireKey = FireToken.getFireKey();
        if (fireKey == null)
            this.asteria.queueNewAccount(
                    decryptForLocalUse(generateDeviceKey().generateLocalEncryptedContent()),
                    this,
                    auth);
        else
            onDataPrepared();
    }

    public void updateUser(FirebaseUser fbuser) {
        asteria.setUser(fbuser);
    }


    public void updateToken(AuthKey authKey) {
        asteria.queueNewAccount(decryptForLocalUse(authKey.generateLocalEncryptedContent()),this,auth);
    }

    public void purgeRemoteItem(String tag) {
        List<ThemisKey> tks = ThemisKey.findKeyForTag(tag);
        if (tks.size() > 0)
            for (ThemisKey tk: tks)
                tk.delete();
        removeRemoteContent(tag);
    }

    protected abstract void removeRemoteContent(String tag);

    public boolean needsAsteria() {
        return asteria==null;
    }

    public void stringRequest(String url, Response.Listener<String> response, Response.ErrorListener error) {
        if (volleyQueue == null)
            volleyQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response, error);
        volleyQueue.add(stringRequest);
    }
}

