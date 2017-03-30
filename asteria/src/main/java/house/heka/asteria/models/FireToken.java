package house.heka.asteria.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

import house.heka.asteria.AsteriaActivity;
import house.heka.local.models.FireKey;
import house.heka.local.models.ThemisItem;

@IgnoreExtraProperties
public class FireToken {
    public String token;


    public FireToken() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public static FireKey getFireKey() {
        List<FireKey> keys = FireKey.listAll(FireKey.class);
        if (keys.size() > 0)
            return keys.get(0);
        else
            return null;
    }

    public void save(AsteriaActivity active, String token) {
        FireKey.deleteAll(FireKey.class);
        this.token = token;
        FireKey fk = new FireKey(active.encryptForLocalUse(this.token));
        fk.save();
    }
}
