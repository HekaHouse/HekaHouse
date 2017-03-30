package house.heka.leto.models.impl;

import com.google.firebase.database.DatabaseReference;

import house.heka.asteria.AsteriaActivity;
import house.heka.asteria.api.AsteriaCapsule;
import house.heka.asteria.models.AsteriaItem;
import house.heka.leto.LetoActivity;
import house.heka.leto.models.secure.RemoteLeto;
import house.heka.themislib.model.secure.RemoteEncryptedContent;

/**
 * Created by aron2 on 3/11/2017.
 */

public class LetoRemote extends RemoteLeto {

    public LetoRemote(RemoteEncryptedContent rec) {
        super(rec);
    }

    @Override
    public AsteriaItem generateAsteria(AsteriaActivity active) {
        return null;
    }

    @Override
    public AsteriaCapsule extractAsteria(AsteriaItem ai) {
        return null;
    }

    @Override
    public void destroy(AsteriaActivity active) {

    }

    @Override
    public DatabaseReference expose(AsteriaActivity active) {
        return null;
    }

    @Override
    protected int getDefaultLayout() {
        return 0;
    }

    @Override
    protected int getPhoneCardLayout() {
        return 0;
    }

    @Override
    protected int getTabletCardLayout() {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getLocalThumbPath() {
        return null;
    }

    @Override
    public String getLocalImage() {
        return null;
    }
}
