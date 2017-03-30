package house.heka.asteria.api;

import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import house.heka.asteria.AsteriaActivity;
import house.heka.asteria.models.AsteriaItem;
import house.heka.asteria.models.FireToken;
import house.heka.local.models.AuthKey;
import house.heka.local.models.FireKey;
import house.heka.themislib.model.secure.RemoteEncryptedContent;


public class Asteria {
    private final static String TAG = "Asteria";
    private FirebaseUser user;
    private static FirebaseDatabase fire;

    public Asteria(FirebaseUser user) {
        this.user = user;
        if (fire == null) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(false);
            fire = FirebaseDatabase.getInstance();
        }
    }

    public AsteriaItem createAsteriaItem(RemoteEncryptedContent rec, AsteriaActivity active, DatabaseReference ref) {
        AsteriaItem ai = new AsteriaItem(rec, active);
        ai.setRef(ref);
        return ai;
    }

    public void queueNewAccount(String novel, final AsteriaActivity active, final Authentic auth) {
        getData().child("new-account-queue").child(user.getUid()).child("token").setValue(novel);
        getData().child("new-account-assigned").child(user.getUid()).child("token").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FireToken ft = new FireToken();
                String token = (String) dataSnapshot.getValue();
                if (token != null) {
                    ft.save(active, token);
                    getData().child("new-account-assigned").child(user.getUid()).child("token").setValue(null);
                    auth.authenticateDeviceKey(active, FireToken.getFireKey());
                    active.onDataPrepared();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private DatabaseReference getData() {
        return fire.getReference();
    }

    public void setPersistance(boolean persist) {
        fire.setPersistenceEnabled(persist);
    }

    public void populateRemoteContent(final AsteriaActivity active) {
        getData().child("asteria_items").child(user.getUid()).child("rec").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                AsteriaItem ai = dataSnapshot.getValue(AsteriaItem.class);
                active.addRemoteItem(ai);
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
    }

    public static Authentic initAuth(final AsteriaActivity active) {

        return new Authentic(active);
    }

    public void setUser(FirebaseUser fbuser) {
        user = fbuser;
    }

    public DatabaseReference getFreshRef() {
        return getData().child("asteria_items").child(user.getUid()).child("rec").push();
    }

    public DatabaseReference getRootRef() {
        return getData();
    }

    public static class Authentic {
        private FirebaseAuth mAuth;
        private FirebaseAuth.AuthStateListener mAuthListener;
        Authentic(final AsteriaActivity active) {
            mAuth = FirebaseAuth.getInstance();
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    if (user != null && active.needsAsteria()) {
                        active.setAsteria(new Asteria(user));

                        for (String provider: user.getProviders())
                            Log.d(TAG, "onAuthStateChanged:signed_in:"+ provider + ":" + user.getUid());
                    } else if (user == null){
                        // User is signed out
                        Log.d(TAG, "onAuthStateChanged:signed_out");
                    }
                    // ...
                }
            };
        }

        public void authenticateAnonymously(final AsteriaActivity active) {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(active, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                            if (!task.isSuccessful()) {
                                Log.w(TAG, "signInAnonymously", task.getException());
                                Toast.makeText(active, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }

                            // ...
                        }
                    });
        }

        public void authenticateDeviceKey(final AsteriaActivity active, final FireKey keyed) {
            mAuth.signInWithCustomToken(active.decryptForLocalUse(keyed.generateLocalEncryptedContent()))
                    .addOnCompleteListener(active, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "signInWithCustomToken:onComplete:" + task.isSuccessful());
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "signInWithCustomTokenFailed", task.getException());
                                active.updateToken(active.generateDeviceKey());
                            } else {
                                FirebaseUser fbuser = task.getResult().getUser();
                                if (fbuser != null)
                                    active.updateUser(fbuser);
                                else {
                                    Log.w(TAG, "signInWithCustomTokenFailed null user");
                                    //authenticateAnonymously(active);
                                }
                            }
                        }
                    });
        }



        public void startAuthenticListener() {
            mAuth.addAuthStateListener(mAuthListener);
        }

        public void stopAuthenticListener() {
            if (mAuthListener != null) {
                mAuth.removeAuthStateListener(mAuthListener);
            }
        }
    }
}
