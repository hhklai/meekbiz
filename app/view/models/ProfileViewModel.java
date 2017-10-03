package view.models;

import api.entity.MizContent;
import api.entity.User;
import utils.PlayConfigUtil;

import java.util.List;

public class ProfileViewModel {

    public ProfileViewModel(boolean isOwner, User user, List<MizContent> publicMizes) {
        this.isOwner = isOwner;
        this.user = user;
        this.publicMizes = publicMizes;

        if (user != null) {
            if (user.getProfilePic() ==  null) {
                profileImageLink =  "/assets/img/profile/anon_person.png";
            } else {
                profileImageLink = PlayConfigUtil.getConfig("aws.s3.host") + user.getProfilePic() + ".jpg";
            }
        }
    }

    public boolean getIsOwner() {
        return isOwner;
    }

    public User getUser() {
        return user;
    }

    public List<MizContent> getPublicMizes() {
        return publicMizes;
    }

    public String getProfileImageLink() {
        return profileImageLink;
    }

    public String getMizImageLink(MizContent miz) {
        if (miz.thumbnail ==  null) {
            return "/assets/img/miz/round_question_mark.jpg";
        } else {
            return PlayConfigUtil.getConfig("aws.s3.host") + miz.thumbnail + ".jpg";
        }
    }

    private boolean isOwner;
    private User user;
    private List<MizContent> publicMizes;
    private String profileImageLink;
}
