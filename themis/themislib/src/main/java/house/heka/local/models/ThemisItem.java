package house.heka.local.models;

import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.orm.SugarRecord;

import java.util.List;

import house.heka.themislib.api.Themis;
import house.heka.themislib.api.ThemisAPI;
import house.heka.themislib.model.secure.LocalEncryptedContent;

import static android.content.ContentValues.TAG;


public class ThemisItem extends SugarRecord implements ThemisAPI {
    String tag;
    String iv;
    String encryptedContent;
    String nonce;
    String pwnonce;

    public ThemisItem() {

    }

    public ThemisItem(LocalEncryptedContent lec, String tag) {
        this();
        this.tag = tag;
        iv = Base64.encodeToString(lec.iv, Themis.BASE64_FLAGS);
        this.encryptedContent = Base64.encodeToString(lec.content, Themis.BASE64_FLAGS);
        this.nonce = Base64.encodeToString(lec.nonce, Themis.BASE64_FLAGS);
        this.pwnonce = Base64.encodeToString(lec.pwnonce, Themis.BASE64_FLAGS);
    }


    public String getContent(Themis themis) {
        String failed = "decrypt attempt failed for local content";
        try {
            LocalEncryptedContent lec = generateLocalEncryptedContent();
            return themis.decryptLocal(lec);
        } catch (Exception e) {
            Log.e(TAG,failed);
        }
        return failed;
    }


    public static List<ThemisItem> findItemForTag(String tag) {
        return ThemisItem.find(ThemisItem.class, "tag = ?",tag);
    }

    public static List<ThemisKey> findKeyForTag(String tag) {
        return ThemisKey.find(ThemisKey.class, "tag = ?",tag);
    }
    @Override
    public LocalEncryptedContent generateLocalEncryptedContent() {
        return new LocalEncryptedContent(
                Base64.decode(nonce, Themis.BASE64_FLAGS),
                Base64.decode(pwnonce, Themis.BASE64_FLAGS),
                Base64.decode(iv, Themis.BASE64_FLAGS),
                Base64.decode(encryptedContent, Themis.BASE64_FLAGS));
    }
    @Override
    public byte[] getBytes(Themis themis) {
        LocalEncryptedContent lec = generateLocalEncryptedContent();
        return themis.decryptLocalBytes(lec);
    }

    @Override
    public String getTag() {
        return tag;
    }

    public String getEnc() {
        return encryptedContent;
    }

    @Override
    public int compareTo(@NonNull ThemisItem o) {
        return this.tag.compareTo(o.tag);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ThemisItem && (this.tag.equals(((ThemisItem) o).tag));
    }


    public void replaceContent(LocalEncryptedContent lec) {
        iv = Base64.encodeToString(lec.iv, Themis.BASE64_FLAGS);
        this.encryptedContent = Base64.encodeToString(lec.content, Themis.BASE64_FLAGS);
        this.nonce = Base64.encodeToString(lec.nonce, Themis.BASE64_FLAGS);
        this.pwnonce = Base64.encodeToString(lec.pwnonce, Themis.BASE64_FLAGS);
        this.save();
    }
}
