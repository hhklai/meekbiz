package api.entity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.cache.Cache;
import play.libs.Json;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UserSession {
    private String userId;
    private String userName;
    private Boolean isAdmin;
    private Boolean isModerator;
    private String webIp;
    private long expiryTime;

    public UserSession(User user, String webIp, long expiryTime) {
        this.userId = user.id;
        this.userName = user.name;
        this.isAdmin = user.isAdmin;
        this.isModerator = user.isModerator;
        this.webIp = webIp;
        this.expiryTime = expiryTime;
    }

    public static ObjectNode createInst(User user, String webIp) {
        return createInst(user, webIp, false);
    }

    public static ObjectNode createInst(User user, String webIp, boolean rememberMe) {
        long expirationTime = System.currentTimeMillis() +
                (rememberMe ? TimeUnit.DAYS.toMillis(30) : TimeUnit.DAYS.toMillis(2));
        UserSession inst = new UserSession(user, webIp, expirationTime);

        String sessionId = UUID.randomUUID().toString();
        int expiryDays = rememberMe ? 30 : 2;

        Cache.set("session_" + sessionId, inst, (int) TimeUnit.DAYS.toSeconds(expiryDays));

        return Json.newObject()
                .put("token", sessionId)
                .put("expiryDays", expiryDays);
    }

    public static UserSession retrieveInst(String token) {
        return (UserSession) Cache.get("session_" + token);
    }

    public static UserSession retrieveFromRequestHeaders(Map<String, String[]> headers) {
        String[] tokens = headers.get("X-XSRF-TOKEN");
        if (tokens != null && tokens.length != 0) {
            return retrieveInst(tokens[0]);
        } else {
            return null;
        }
    }

    public static void updateUserName(String token, String newUserName) {
        UserSession session = retrieveInst(token);
        session.userName = newUserName;
        Cache.set("session_" + token, session, (int) TimeUnit.DAYS.toSeconds(2));
    }

    public static void deleteInstance(String token) {
        Cache.remove("session_" + token);
    }

    public String getUserId() {
        return userId;
    }

    public String getWebIp() {
        return webIp;
    }

    public String getUserName() {
        return userName;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public Boolean getIsModerator() {
        return isModerator;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UserSession) {
            UserSession u = (UserSession) o;
            return (
                this.userName.equals(u.userName) &&
                this.webIp.equals(u.webIp) &&
                this.userId.equals(u.userId)
            );
        }
        return false;
    }

    public static String validateRequest(String token, String remoteAddress) {
        UserSession userSession = UserSession.retrieveInst(token);
        if (userSession == null) {
            return null;
        }
        if (!remoteAddress.equals(userSession.getWebIp())) {
            return null;
        }
        return userSession.getUserId();
    }

    public static String validateAdmin(String token, String remoteAddress) {
        UserSession userSession = UserSession.retrieveInst(token);
        if (userSession == null) {
            return null;
        }
        if (!remoteAddress.equals(userSession.getWebIp())) {
            return null;
        }
        if (!userSession.getIsAdmin()) {
            return null;
        }
        return userSession.getUserId();
    }

    public static String validateModerator(String token, String remoteAddress) {
        UserSession userSession = UserSession.retrieveInst(token);
        if (userSession == null) {
            return null;
        }
        if (!remoteAddress.equals(userSession.getWebIp())) {
            return null;
        }
        if (!userSession.getIsModerator()) {
            return null;
        }
        return userSession.getUserId();
    }
}
