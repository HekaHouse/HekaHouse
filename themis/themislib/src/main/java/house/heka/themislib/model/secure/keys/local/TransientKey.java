package house.heka.themislib.model.secure.keys.local;

import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.security.KeyStore;

import house.heka.themislib.api.Themis;

/**
 * Created by aron2 on 3/8/2017.
 */

public class TransientKey extends LocalEncryptionKey {
    private static final String TAG = "TransientKey";
    private final byte[] iv;
    private final byte[] secret;

    public TransientKey(byte[] iv, byte[] secretKey, KeyStore androidKeys, SharedPreferences pref) {
        super(iv, secretKey, androidKeys, pref);
        this.iv = iv;
        this.secret = secretKey;
    }
    @Override
    protected void storeKeys(String pub, String priv) {
    }

    @Override
    public String retrievePubKey() {
        return Base64.encodeToString(iv,Themis.BASE64_FLAGS);
    }

    @Override
    protected String retrievePrivKey() {
        return Base64.encodeToString(Themis.androidDecrypt(secret,iv,mAndroidKeys),Themis.BASE64_FLAGS);
    }

}
