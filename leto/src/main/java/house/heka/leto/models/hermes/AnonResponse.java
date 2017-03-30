package house.heka.leto.models.hermes;

/**
 * Created by aron2 on 3/18/2017.
 */

public class AnonResponse {

    private String response;

    private Boolean dirty = false;

    private Profile profile;
    public AnonResponse() {

    }

    public AnonResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public Profile getProfile() {
        if (profile == null)
            profile = new Profile();
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isDirty() {
        return dirty;
    }

    public static class Profile {
        public String name;
        public String avatar;
        public Profile() {

        }
    }
}
