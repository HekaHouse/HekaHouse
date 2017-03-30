package house.heka.themislib.api;

import house.heka.local.models.ThemisItem;
import house.heka.themislib.model.secure.LocalEncryptedContent;

/**
 * Created by aron2 on 3/9/2017.
 */

public interface ThemisAPI extends Comparable<ThemisItem> {
    public byte[] getBytes(Themis themis);
    public String getTag();
    public String getEnc();
    public void replaceContent(LocalEncryptedContent lec);
    public LocalEncryptedContent generateLocalEncryptedContent();
}
