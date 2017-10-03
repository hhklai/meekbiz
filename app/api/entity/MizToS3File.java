package api.entity;

import play.db.jpa.JPA;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Entity
@NamedQueries({
        @NamedQuery(name = "findS3filesByMizId", query  = "SELECT e.pk.s3Id FROM MizToS3File e WHERE e.pk.mizId = :mizId AND e.flag = :state"),
        @NamedQuery(name = "updateS3fileToMizFlag", query = "UPDATE MizToS3File e SET e.flag = :flag WHERE e.pk.mizId = :mizId AND e.pk.s3Id = :s3Id"),
        @NamedQuery(name = "deleteS3fileToMiz", query = "DELETE FROM  MizToS3File e WHERE e.pk.mizId = :mizId")
})
public class MizToS3File {

    @Embeddable
    public static class MizToS3Id implements Serializable {

        static final long serialVersionUID = 18359376841038L;

        public String mizId;
        public String s3Id;
    }

    @EmbeddedId
    private MizToS3Id pk;
    private State flag;

    public MizToS3File() {
        pk = new MizToS3Id();
    }

    public enum State {
        USED,
        UNUSED,
        HISTORY
    }

    public static List<String> getAllS3IdsByMizId(String mizId, State state) {
        try {
            return JPA.em().createNamedQuery("findS3filesByMizId", String.class).setParameter("mizId", mizId).setParameter("state", state).getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }

    public static void linkS3File(String mizId, String s3FileId) {
        MizToS3File item = new MizToS3File();
        item.pk.mizId = mizId;
        item.pk.s3Id = s3FileId;
        item.flag = State.USED;
        JPA.em().persist(item);
    }

    public static void updateS3File(String mizId, String s3FileId, State flag) {
        MizToS3Id id = new MizToS3Id();
        id.mizId = mizId;
        id.s3Id = s3FileId;
        MizToS3File relation = JPA.em().find(MizToS3File.class, id);
        relation.flag = flag;
    }

    public static void deleteS3FileLinkForMiz(String mizId) {
        JPA.em().createNamedQuery("deleteS3fileToMiz").setParameter("mizId", mizId).executeUpdate();
    }
}
