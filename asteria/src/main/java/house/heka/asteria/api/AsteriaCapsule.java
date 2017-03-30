package house.heka.asteria.api;

import com.google.firebase.database.DatabaseReference;

import house.heka.asteria.AsteriaActivity;
import house.heka.asteria.models.AsteriaItem;

/**
 * Created by aron2 on 3/11/2017.
 */

public interface AsteriaCapsule {
    public AsteriaItem generateAsteria(AsteriaActivity active);
    public AsteriaCapsule extractAsteria(AsteriaItem ai);
    public void destroy(AsteriaActivity active);
    public DatabaseReference expose(AsteriaActivity active);
}
