package house.heka.leto.models.raw;

import house.heka.leto.LetoActivity;

/**
 * Created by aron2 on 3/12/2017.
 */

public class UnsecuredLeto extends LetoUnsecuredDisplayable {
    private final UnencryptedContent mContent;

    public UnsecuredLeto(UnencryptedContent content) {
        super();
        mContent = content;
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
