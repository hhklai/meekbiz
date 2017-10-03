package integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import api.entity.UserSession;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.cache.Cache;
import play.libs.Json;
import play.mvc.Result;
import play.test.FakeApplication;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class Story20141202UpdateUserProfileSpec {
    static FakeApplication fakeApp;

    @BeforeClass
    public static void init() {
        fakeApp = fakeApplication(inMemoryDatabase());
        start(fakeApp);
    }

    @AfterClass
    public static void tearDown() {
        stop(fakeApp);
    }

    @Test
    public void updateUserSuccess() {
        //setup - add user into database
        ObjectNode requestObj = Json.newObject()
                .put("email", "hello@example.com")
                .put("password", "test1234");
        Result result = callAction(api.controllers.routes.ref.UserController.registerUser(), fakeRequest().withJsonBody(requestObj));
        JsonNode jsonResult = Json.parse(contentAsString(result));
        String token = jsonResult.get("token").asText();
            //cache should have user name of hello@example.com
            UserSession userSession = (UserSession) Cache.get("session_" + token);
            assertEquals("hello@example.com", userSession.getUserName());

        //when updating the name and email on the profile
        requestObj = Json.newObject()
                .put("email", "what@example.com")
                .put("name", "hello");
        result = callAction(api.controllers.routes.ref.UserController.updatePrivateProfile(), fakeRequest().withJsonBody(requestObj).withHeader("X-XSRF-TOKEN", token));
            //it should return update the name in the cache
            userSession = (UserSession) Cache.get("session_" + token);
            assertEquals("hello", userSession.getUserName());

            //it should return the updated user when retrieving the privateInfo
            result = callAction(api.controllers.routes.ref.UserController.viewPrivateProfile("all"), fakeRequest().withHeader("X-XSRF-TOKEN", token));
            jsonResult = Json.parse(contentAsString(result)).get("user");
            assertEquals("what@example.com", jsonResult.get("email").asText());
            assertEquals("hello", jsonResult.get("name").asText());

    }
}
