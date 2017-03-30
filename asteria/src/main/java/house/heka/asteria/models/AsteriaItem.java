package house.heka.asteria.models;

import android.util.Base64;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import house.heka.asteria.AsteriaActivity;
import house.heka.asteria.api.Asteria;
import house.heka.themislib.api.Themis;
import house.heka.themislib.model.secure.RemoteEncryptedContent;

/**
 * Created by aron2 on 3/7/2017.
 */

@IgnoreExtraProperties
public class AsteriaItem {

    private static final String TAG = "AsteriaItem";
    private AsteriaActivity mActive;
    public String nonce;
    public String remoteKey;
    public String content;
    public int length;
    public String tag;
    @Exclude
    private DatabaseReference ref;

    @Exclude
    public String key;
    private ChildEventListener listen;

    public AsteriaItem() {

    }

    public AsteriaItem(RemoteEncryptedContent rec, AsteriaActivity active) {
        nonce = Base64.encodeToString(rec.nonce,Themis.BASE64_FLAGS);
        remoteKey = Base64.encodeToString(rec.remote,Themis.BASE64_FLAGS);
        content = Base64.encodeToString(rec.content,Themis.BASE64_FLAGS);
        length = rec.length;
        tag = rec.tag;
        mActive = active;
    }

    public void setRef(DatabaseReference ref) {
        this.ref = ref;
        this.ref.setValue(this);
        this.key = this.ref.getKey();
        this.listen = getChildEventListener();
        this.ref.addChildEventListener(listen);
    }

    private ChildEventListener getChildEventListener() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG,"purging "+dataSnapshot.getValue());
                mActive.purgeRemoteItem(String.valueOf(dataSnapshot.getValue()));
                AsteriaItem.this.ref.removeEventListener(AsteriaItem.this.listen);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    public String decryptContent(AsteriaActivity active) {
        RemoteEncryptedContent rec = new RemoteEncryptedContent(
                Base64.decode(nonce,Themis.BASE64_FLAGS),
                Base64.decode(content,Themis.BASE64_FLAGS),
                length,
                Base64.decode(remoteKey,Themis.BASE64_FLAGS),
                tag);
        return active.decryptRemote(rec);
    }
}
