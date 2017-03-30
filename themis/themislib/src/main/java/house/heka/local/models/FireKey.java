package house.heka.local.models;

import android.support.annotation.NonNull;
import android.util.Base64;

import com.orm.SugarRecord;

import java.util.List;

import house.heka.themislib.api.Themis;
import house.heka.themislib.api.ThemisAPI;
import house.heka.themislib.model.secure.LocalEncryptedContent;

/**
 * Created by aron2 on 3/7/2017.
 */

public class FireKey extends SugarRecord implements ThemisAPI {
    String tag="FireKey";
    public String iv;
    String encryptedKey;
    String nonce;
    String pwnonce;

    public FireKey() {

    }

    public FireKey(LocalEncryptedContent lec) {
        this();
        iv = Base64.encodeToString(lec.iv, Themis.BASE64_FLAGS);
        this.encryptedKey = Base64.encodeToString(lec.content, Themis.BASE64_FLAGS);
        this.nonce = Base64.encodeToString(lec.nonce, Themis.BASE64_FLAGS);
        this.pwnonce = Base64.encodeToString(lec.pwnonce, Themis.BASE64_FLAGS);
    }
    @Override
    public String getTag() {
        return tag;
    }

    public byte[] getKey(Themis themis) {
        return getBytes(themis);
    }

    public static List<FireKey> findKeyForTag(String tag) {
        return FireKey.find(FireKey.class, "tag = ?",tag);
    }
    @Override
    public byte[] getBytes(Themis themis) {
        LocalEncryptedContent lec = new LocalEncryptedContent(
                Base64.decode(nonce,Themis.BASE64_FLAGS),
                Base64.decode(pwnonce,Themis.BASE64_FLAGS),
                Base64.decode(iv,Themis.BASE64_FLAGS),
                Base64.decode(encryptedKey,Themis.BASE64_FLAGS));
        return themis.decryptLocalBytes(lec);
    }
    @Override
    public String getEnc() {
        return encryptedKey;
    }

    @Override
    public int compareTo(@NonNull ThemisItem o) {
        return this.tag.compareTo(o.tag);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ThemisItem && (this.tag.equals(((ThemisItem) o).tag));
    }
    @Override
    public void replaceContent(LocalEncryptedContent lec) {
        iv = Base64.encodeToString(lec.iv, Themis.BASE64_FLAGS);
        this.encryptedKey = Base64.encodeToString(lec.content, Themis.BASE64_FLAGS);
        this.nonce = Base64.encodeToString(lec.nonce, Themis.BASE64_FLAGS);
        this.pwnonce = Base64.encodeToString(lec.pwnonce, Themis.BASE64_FLAGS);
        this.save();
    }

    @Override
    public LocalEncryptedContent generateLocalEncryptedContent() {
        return new LocalEncryptedContent(
                Base64.decode(nonce, Themis.BASE64_FLAGS),
                Base64.decode(pwnonce, Themis.BASE64_FLAGS),
                Base64.decode(iv, Themis.BASE64_FLAGS),
                Base64.decode(encryptedKey, Themis.BASE64_FLAGS));
    }

}
