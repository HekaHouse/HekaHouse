package house.heka.leto.models.secure;

import house.heka.leto.LetoActivity;
import house.heka.themislib.model.secure.LocalEncryptedContent;

/**
 * Created by aron2 on 3/11/2017.
 */

public abstract class LocalLeto extends LetoSecuredDisplayable {
    private final LocalEncryptedContent mContent;

    public LocalLeto(LocalEncryptedContent lec) {
        super();
        mContent = lec;
    }

}
