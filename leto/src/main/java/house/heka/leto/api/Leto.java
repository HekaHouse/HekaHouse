package house.heka.leto.api;

import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import house.heka.leto.LetoActivity;

/**
 * Created by aron2 on 3/12/2017.
 */

public class Leto {
    private static final String TAG = "Leto";
    private final LetoActivity mActive;

    public Leto(LetoActivity active) {
        mActive = active;
    }

    private FirebaseStorage getStorage() {
        return FirebaseStorage.getInstance();
    }

    private StorageReference getStorageReference(String path) {
        return getStorage().getReference().child(path);
    }

    public void loadFirebaseImage(String path, ImageView iv) {
        if (path != null && path.length() > 0)
            try {
                StorageReference ref = getStorageReference(path);

                Glide.with(mActive)
                        .using(new FirebaseImageLoader())
                        .load(getStorageReference(path))
                        .into(iv);
            } catch (Exception e) {
                Log.d(TAG,e.getMessage());
            }

    }


}
