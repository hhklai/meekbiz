package view.models;

import api.entity.MizContent;
import utils.PlayConfigUtil;

public class MizViewModel {

    public MizViewModel(boolean isOwner, String userName, String mizTitle) {
        this.isOwner = isOwner;
        this.userName = userName;
        this.mizTitle = mizTitle;
    }

    public MizContent getMizContent() {
        return mizContent;
    }

    public void setMizContent(MizContent mizContent) {
        this.mizContent = mizContent;

        if (mizContent.bannerPic ==  null) {
            mizBannerLink = "/assets/img/miz/ninbg.jpg";
        } else {
            mizBannerLink = PlayConfigUtil.getConfig("aws.s3.host") + mizContent.bannerPic + ".jpg";
        }

        if (mizContent.thumbnail ==  null) {
            mizThumbnailLink = "/assets/img/miz/round_question_mark.jpg";
        } else {
            mizThumbnailLink = PlayConfigUtil.getConfig("aws.s3.host") + mizContent.thumbnail + ".jpg";
        }
    }

    public boolean isOwner() {
        return isOwner;
    }

    public String getUserName() {
        return userName;
    }

    public String getMizTitle() {
        return mizTitle;
    }

    public String getMizThumbnailLink() {
        return mizThumbnailLink;
    }

    public String getMizBannerLink() {
        return mizBannerLink;
    }

    private MizContent mizContent;
    private boolean isOwner;
    private String userName;
    private String mizTitle;

    private String mizThumbnailLink;
    private String mizBannerLink;
}
