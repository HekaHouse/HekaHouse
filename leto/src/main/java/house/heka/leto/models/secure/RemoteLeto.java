package house.heka.leto.models.secure;

import house.heka.leto.LetoActivity;
import house.heka.themislib.model.secure.RemoteEncryptedContent;

/**
 * Created by aron2 on 3/11/2017.
 */

public abstract class RemoteLeto extends LetoSecuredDisplayable {
    private final RemoteEncryptedContent mContent;

    public RemoteLeto( RemoteEncryptedContent rec) {
        super();
        mContent = rec;
    }

}
