package api.entity;

import api.entity.UserSession;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import play.cache.Cache;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.powermock.api.mockito.PowerMockito.when;


@RunWith(PowerMockRunner.class)
@PrepareForTest(Cache.class)
public class UserSessionSpec {

    @Test
    public void checkCreateInst() {
        User user = new User();
        user.id = "aUserId";
        user.setName("aUserName");

        //when registering or logging in
        PowerMockito.mockStatic(Cache.class);
        ObjectNode result = UserSession.createInst(user, "127.0.0.1");

        //it should create a cache entry
        UserSession expected = new UserSession(user, "127.0.0.1", 0L);
        PowerMockito.verifyStatic();
        Cache.set(startsWith("session_"), eq(expected), eq((int) TimeUnit.DAYS.toSeconds(2)));

        //it should return a JSON result
        assertNotNull(result.get("token").asText());
        assertEquals(2, result.get("expiryDays").asInt());
    }

    @Test
    public void checkCreateInstRememberMe() {
        User user = new User();
        user.id = "aUserId";
        user.setName("aUserName");

        //when registering or logging in with remember me
        PowerMockito.mockStatic(Cache.class);
        ObjectNode result = UserSession.createInst(user, "127.0.0.1", true);

        //it should create a cache entry
        UserSession expected = new UserSession(user, "127.0.0.1", 0L);
        PowerMockito.verifyStatic();
        Cache.set(startsWith("session_"), eq(expected), eq((int) TimeUnit.DAYS.toSeconds(30)));

        //it should return a JSON result
        assertNotNull(result.get("token").asText());
        assertEquals(30, result.get("expiryDays").asInt());
    }

    @Test
    public void checkRetrieveInstance() {
        //when getting security access checks for user session
        PowerMockito.mockStatic(Cache.class);
        UserSession.retrieveInst("a-token");

        PowerMockito.verifyStatic();
        Cache.get(eq("session_a-token"));
    }

    @Test
    public void checkValidateRequest () {
        User user = new User();
        user.id = "aUserId";
        user.setName("aUserName");

        //when checking secured endpoint and valid session is stored
        UserSession userSession = new UserSession(user, "127.0.0.1", 0L);
        PowerMockito.mockStatic(Cache.class);
        when(Cache.get(anyString())).thenReturn(userSession);
        String result = UserSession.validateRequest("a-token","127.0.0.1");
        //it should return a username
        assertEquals("aUserId", result);

        //when checking secured endpoint and ip does not match
        result = UserSession.validateRequest("a-token","127.0.0.2");
        //it should fail validation
        assertNull(result);

        //when checking secured endpoint and token is invalid
        PowerMockito.mockStatic(Cache.class);
        when(Cache.get(anyString())).thenReturn(null);
        result = UserSession.validateRequest("a-token","127.0.0.1");
        //it should fail validation
        assertNull(result);
    }
}
