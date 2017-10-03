package api.entity;

import api.exceptions.ValidationException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.groupon.uuid.UUID;
import play.db.jpa.JPA;
import play.libs.Json;
import utils.HtmlCleaner;
import utils.PlayConfigUtil;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@NamedQueries({
        @NamedQuery(name = "findNewestMizContent", query = "SELECT e FROM MizContent e WHERE e.mizId = :mizId ORDER BY e.createdDate DESC LIMIT 1"),
        @NamedQuery(name = "findMizContent", query = "SELECT e FROM MizContent e WHERE e.mizId = :mizId ORDER BY e.createdDate")
})
public class MizContent {

    public static final int MAX_SUMMARY_LENGTH = 1000;

    public static MizContent create(String title, String summary, String mizId) {
        MizContent content = new MizContent();
        content.title = title;
        content.summary = summary;
        content.mizId = mizId;
        content.save();
        return content;
    }

    public enum ServiceDuration {  //how long each session takes
        LESS_THAN_FIFTEEN_MINUTES,
        LESS_THAN_HALF_AN_HOUR,
        HOUR,
        HOUR_AND_A_HALF,
        TWO_HOURS,
        THREE_HOURS,
        HALF_A_DAY,
        WORK_DAY
    }

    @Id
    public String id;

    public String mizId;

    public String moderatedBy;

    public Date createdDate;  //for tracking history

    public String title;

    public String summary;

    public ServiceDuration approxServiceDurationEnum;

    public String bannerPic;

    public String thumbnail;

    public String contentBody;

    public MizContent() {
        createdDate = new Date();
        approxServiceDurationEnum = ServiceDuration.LESS_THAN_FIFTEEN_MINUTES;
    }

    //////////// JPA methods ////////////////
    public String save() {
        this.id = new UUID().toString();
        JPA.em().persist(this);
        return this.id;
    }

    public static MizContent get(String id) {
        EntityManager em = JPA.em();
        try {
            return em.find(MizContent.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    public static void delete(MizContent mizContent) {
        EntityManager em = JPA.em();
        em.remove(mizContent);
    }

    public static MizContent getByMizId(String mizId) {
        try {
            return JPA.em().createNamedQuery("findNewestMizContent", MizContent.class).setParameter("mizId", mizId).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public static List<MizContent> getMizHistory(String mizId) {
        try {
            return JPA.em().createNamedQuery("findMizContent", MizContent.class).setParameter("mizId", mizId).getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }

    public boolean patch(MizContent patch) throws ValidationException {
        return patch(patch, null);
    }
    public boolean patch(MizContent patch, String mizOwnerId) throws ValidationException {
        boolean changed = false;
        if (patch.title != null && !patch.title.equals(this.title)) {
            if (patch.title.length() > Miz.MAX_TITLE_LENGTH) {
                throw new ValidationException("title cannot be greater than " +  Miz.MAX_TITLE_LENGTH + " characters");
            }
            this.title = patch.title;
            changed = true;
        }

        if (patch.summary != null && !patch.summary.equals(this.summary)) {
            if (patch.summary.length() > MAX_SUMMARY_LENGTH) {
                throw new ValidationException("summary cannot be greater than " + MAX_SUMMARY_LENGTH + " characters");
            }
            this.summary = patch.summary;
            changed = true;
        }

        if (patch.contentBody != null) {
            patch.contentBody = HtmlCleaner.clean(patch.contentBody);
            if (!patch.contentBody.equals(this.contentBody)) {
                this.contentBody = patch.contentBody;
                detectIdChangeState(patch.contentBody);
                changed = true;
            }
        }

        if (patch.thumbnail != null && !patch.thumbnail.equals(this.thumbnail)) {
            S3File file = S3File.getByPublicId(patch.thumbnail);
            if (file != null && file.getOwnerId().equals(mizOwnerId)) {
                this.thumbnail = patch.thumbnail;
                changed = true;
            } else {
                throw new ValidationException("Invalid profilePic id");
            }
        }

        if (patch.bannerPic != null && !patch.bannerPic.equals(this.bannerPic)) {
            S3File file = S3File.getByPublicId(patch.bannerPic);
            if (file != null && file.getOwnerId().equals(mizOwnerId)) {
                this.bannerPic = patch.bannerPic;
                changed = true;
            } else {
                throw new ValidationException("Invalid profilePic id");
            }
        }

        if (changed) {
            JPA.em().persist(this);
        }

        return changed;
    }

    private void detectIdChangeState(String contentBody) {
        Matcher m = Pattern.compile("<img[^>]+src=\"" + PlayConfigUtil.getConfig("aws.s3.host") + "([\\d-\\w]+)\\.jpg\"").matcher(contentBody);
        List<String> inDescription = new ArrayList<>();
        while (m.find()) {
            inDescription.add(m.group(1));
        }
        
        List<String> used = MizToS3File.getAllS3IdsByMizId(this.mizId, MizToS3File.State.USED);
        used.removeAll(inDescription);
        for (String item : used) {
            MizToS3File.updateS3File(this.mizId, item, MizToS3File.State.UNUSED);
        }
    }
    //////////////////////////////////////////

    public ObjectNode asJSONObject() {
        return Json.newObject()
                .put("title", title)
                .put("summary", summary)
                .put("approxServiceDurationEnum", approxServiceDurationEnum.toString())
                .put("bannerPic", bannerPic)
                .put("thumbnail", thumbnail)
                .put("contentBody", contentBody);
    }
}