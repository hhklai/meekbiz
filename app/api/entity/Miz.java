package api.entity;

import api.exceptions.ValidationException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.groupon.uuid.UUID;
import play.db.jpa.JPA;
import play.libs.F;
import play.libs.Json;
import play.libs.WS;
import utils.SearchUtil;

import javax.persistence.*;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Entity
@NamedQueries({
        @NamedQuery(name = "findPublicMizesByOwnerId", query = "SELECT e FROM Miz e WHERE e.ownerId = :ownerId AND e.isPublic = true"),
        @NamedQuery(name = "findPendingMizesByOwnerId", query = "SELECT e FROM Miz e WHERE e.ownerId = :ownerId AND e.isPublic = false AND e.editable = true AND e.initialModeratedBy IS NULL"),
        @NamedQuery(name = "findByUserNameAndMizTitle", query =  "SELECT e FROM Miz e WHERE e.urlTitle = :title AND e.ownerId = (SELECT u.id FROM User u WHERE u.name = :name)"),
        @NamedQuery(name = "findByUserIdAndMizTitle", query =  "SELECT e FROM Miz e WHERE e.urlTitle = :title AND e.ownerId = :userId"),
        @NamedQuery(name = "findNonSynchedMizes", query =  "SELECT e FROM Miz e WHERE e.searchSynched = false"),
        @NamedQuery(name = "updateAllMizesToUnSynched", query = "UPDATE Miz e SET e.searchSynched = false")
})
public class Miz {

    public static final int MAX_TITLE_LENGTH = 255;

    public static Miz create(String title, String region, String ownerId) {
        Miz miz = new Miz();
        miz.urlTitle = title;
        miz.locationRegion = region;
        miz.ownerId = ownerId;
        miz.save();
        return miz;
    }

    ;
    public enum LocationPreference {
        AT_SPECIFIED_LOCATION,
        AT_CLIENTS_LOCATION,
        CONTACT_SELLER
    }

    public enum ValueProposition {
        VALUE,
        HIGH_SKILL
    }

    @Id
    public String id;

    public String ownerId;

    public Boolean editable;

    public Boolean isPublic;

    public String urlTitle;

    public String initialModeratedBy;

    public Date initialModerationDate;

    public Boolean highQuality;

    public Date bidStartDate;

    public Date bidEndDate;

    public Date serviceValidStartDate;

    public Date serviceExpiryDate;

    public LocationPreference locationEnum;

    public Float locationRadius;

    public Double locationLat;

    public Double locationLng;

    public String locationRegion;

    public Integer minCustomers;

    public Integer maxCustomers;

    public Float minPricePerCustomer;

    public ValueProposition valuePropositionEnum;

    public Boolean searchSynched;

    public Miz() {
        editable = true;
        isPublic = false;
        highQuality = false;
        minCustomers = 0;
        maxCustomers = 20;
        minPricePerCustomer = 0f;
        valuePropositionEnum = ValueProposition.VALUE;
        locationEnum = LocationPreference.AT_CLIENTS_LOCATION;
        searchSynched = false;
    }

    //////////// JPA methods ////////////////
    public String save() {
        this.id = new UUID().toString();
        JPA.em().persist(this);

        return this.id;
    }

    public static void delete(Miz miz) {
        EntityManager em = JPA.em();
        em.remove(miz);
    }

    public static Miz get(String id) {
        EntityManager em = JPA.em();
        try {
            return em.find(Miz.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    public static Miz getByUserNameAndTitle(String username, String title) {
        try {
            //SQL injection will not happen because we are using setParameter
            return JPA.em().createNamedQuery("findByUserNameAndMizTitle", Miz.class)
                    .setParameter("name", username)
                    .setParameter("title", title).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public static Miz getByUserIdAndTitle(String userId, String title) {
        try {
            //SQL injection will not happen because we are using setParameter
            return JPA.em().createNamedQuery("findByUserIdAndMizTitle", Miz.class)
                    .setParameter("userId", userId)
                    .setParameter("title", title).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public static List<Miz> getPublicMizesByOwnerId(String ownerId) {
        try {
            return JPA.em().createNamedQuery("findPublicMizesByOwnerId", Miz.class).setParameter("ownerId", ownerId).getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }

    public static List<Miz> getPendingMizesByOwnerId(String ownerId) {
        try {
            return JPA.em().createNamedQuery("findPendingMizesByOwnerId", Miz.class).setParameter("ownerId", ownerId).getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }

    public static void clearAllSearchSync() {
        //TODO , do this in batches?  This might take a while when there are many mizes.
        JPA.em().createNamedQuery("updateAllMizesToUnSynched").executeUpdate();
    }


    private void syncMizWithSearch() {
        final String theId = id;

        SearchUtil.httpMiz(this.id).put(asJSONObject()).onRedeem(new F.Callback<WS.Response>() {
            @Override
            public void invoke(WS.Response response) throws Throwable {
                if (response.getStatus() == 200) {
                    JPA.withTransaction(new F.Callback0() {
                        @Override
                        public void invoke() throws Throwable {
                            Miz miz = Miz.get(theId);
                            miz.searchSynched = true;
                        }
                    });
                }
            }
        });
    }

    public static List<Miz> getNonSynchedMizes(final int offset) {
        return JPA.em().createNamedQuery("findNonSynchedMizes", Miz.class).setFirstResult(offset).setMaxResults(1000).getResultList();
    }

    public boolean patch(Miz patch) throws ValidationException {
        boolean changed = false;
        if (patch.urlTitle != null && !patch.urlTitle.equals(this.urlTitle)) {
            if (getByUserIdAndTitle(this.ownerId, patch.urlTitle) == null) {
                if (patch.urlTitle.length() > MAX_TITLE_LENGTH) {
                    throw new ValidationException("title cannot be greater than " +  MAX_TITLE_LENGTH + " characters");
                }
                this.urlTitle = patch.urlTitle;
                changed = true;
            } else {
                throw new ValidationException("title already taken");
            }
        }

        if (patch.searchSynched != this.searchSynched) {
            this.searchSynched = patch.searchSynched;
            changed = true;
        }

        if (changed) {
            JPA.em().persist(this);
        }

        return changed;
    }
    /////////////////////////////////////////

    public ObjectNode asJSONObject() {
        ObjectNode node = Json.newObject()
                .put("editable", editable)
                .put("isPublic", isPublic)
                .put("locationRadius", locationRadius)
                .put("locationLat", locationLat)
                .put("locationLng", locationLng)
                .put("locationLng", locationRegion)
                .put("minCustomers", minCustomers)
                .put("maxCustomers", maxCustomers)
                .put("minPricePerCustomer", minPricePerCustomer);

        if (bidStartDate != null) node.put("bidStartDate", bidStartDate.getTime());
        if (bidEndDate != null) node.put("bidEndDate", bidEndDate.getTime());
        if (serviceValidStartDate != null) node.put("serviceValidStartDate", serviceValidStartDate.getTime());
        if (serviceExpiryDate != null) node.put("serviceExpiryDate", serviceExpiryDate.getTime());
        if (locationEnum != null) node.put("locationEnum", locationEnum.toString());
        if (valuePropositionEnum != null) node.put("valueProposition", valuePropositionEnum.toString());

        return node;
    }

}