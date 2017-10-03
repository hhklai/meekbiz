package api.entity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.codec.binary.Base64;
import play.Logger;
import play.db.jpa.JPA;
import play.libs.Json;
import utils.PlayConfigUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

@Entity
@NamedQuery(name = "findByPublicId", query =  "SELECT e FROM S3File e WHERE e.publicId = :publicId")
public class S3File {

    @Id
    protected String id;

    protected String publicId;

    protected String ownerId;

    public String getPublicId() {
        return publicId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    @Transient
    public File file;

    public String save(String ownerId) {
        this.id = new com.groupon.uuid.UUID().toString();
        this.publicId = UUID.randomUUID().toString();
        this.ownerId = ownerId;
        JPA.em().persist(this);
        return this.id;
    }

    public static void delete(String publicId, String ownerId) {
        S3File s3file = getByPublicId(publicId);
        if (s3file != null && s3file.ownerId.equals(ownerId)) {
            JPA.em().remove(s3file);
        }
    }

    public static S3File get(String id) {
        try {
            return JPA.em().find(S3File.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    public static S3File getByPublicId(String publicId) {
        try {
            return JPA.em().createNamedQuery("findByPublicId", S3File.class).setParameter("publicId", publicId).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public ObjectNode createPolicyResponseNode() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, +10);
        Date expiry = cal.getTime();
        return createPolicyResponseNode(publicId, expiry);
    }

    protected static ObjectNode createPolicyResponseNode(String fileId, Date expiry) {
        ObjectNode fileInfo = Json.newObject();
        String policy = getPolicy(fileId, expiry);
        fileInfo.put("fileId", fileId);
        fileInfo.put("policy", policy);
        fileInfo.put("accessKey", PlayConfigUtil.getConfig("aws.access.key"));
        fileInfo.put("signature", getSignature(policy));
        fileInfo.put("bucket", PlayConfigUtil.getConfig("aws.s3.bucket"));
        return fileInfo;
    }

    protected static String getPolicy(String fileId, Date expiryTime) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));

        String policyDoc = "{" +
                "'expiration': '" + df.format(expiryTime) + "'," +
                "'conditions': [" +
                "{'bucket': '" + PlayConfigUtil.getConfig("aws.s3.bucket") + "'}," +
                "['eq', '$key', '" + fileId + ".jpg']," +
                "{'acl': 'public-read'}," +
                "{'success_action_status': '201'}," +
                "['starts-with', '$Content-Type', '']," +
                "['content-length-range', 0, 1048576]" +
                "]" +
                "}";

        String policy = null;
        try {
            policy = Base64.encodeBase64String(policyDoc.getBytes("UTF-8")).trim();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return policy;
    }

    protected static String getSignature(String policy) {
        String aws_secret_key = PlayConfigUtil.getConfig("aws.secret.key");
        Mac hmac;
        try {
            hmac = Mac.getInstance("HmacSHA1");
            hmac.init(new SecretKeySpec(
                    aws_secret_key.getBytes("UTF-8"), "HmacSHA1"));
            return Base64.encodeBase64String(hmac.doFinal(policy.getBytes("UTF-8"))).trim();
        } catch (Exception e) {
            Logger.warn("Encoding problem", e);
        }
        return null;
    }

}