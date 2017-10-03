package integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

public class Story20140628UserLoginLogoutSpec {
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
    public void registerAndLoginUser() {
        //when checking if that user hello@example exists on an empty database
            Result result = callAction(api.controllers.routes.ref.UserController.checkIfUserExists("hello@example.com", null), fakeRequest());
            //it should say it is not found
            JsonNode jsonResult = Json.parse(contentAsString(result));
            assertFalse(jsonResult.get("found").asBoolean());

        //when registering a user with email and password
            ObjectNode requestObj = Json.newObject()
                    .put("email", "hello@example.com")
                    .put("password", "test1234");
            result = callAction(api.controllers.routes.ref.UserController.registerUser(), fakeRequest().withJsonBody(requestObj));

            //it should return status created
            assertThat(status(result)).isEqualTo(CREATED);

            //it should return a Session Id with expiry
            jsonResult = Json.parse(contentAsString(result));
            String token = jsonResult.get("token").asText();
            assertNotNull(token);
            assertNotNull(jsonResult.get("expiryDays"));

            //it should have a cache entry
            assertNotNull(Cache.get("session_" + token));

        //when checking if that user has been created
            result = callAction(api.controllers.routes.ref.UserController.checkIfUserExists("hello@example.com", null), fakeRequest());
            //it should say it is found
            jsonResult = Json.parse(contentAsString(result));
            assertTrue(jsonResult.get("found").asBoolean());

        //when registering someone that already existed
            result = callAction(api.controllers.routes.ref.UserController.registerUser(), fakeRequest().withJsonBody(requestObj));

            //it should return status bad request
            assertThat(status(result)).isEqualTo(BAD_REQUEST);

        //when logged out
            result = callAction(api.controllers.routes.ref.UserController.logout(), fakeRequest().withHeader("X-XSRF-TOKEN", token));
            //it should have removed cache key
            assertThat(status(result)).isEqualTo(OK);
            assertNull(Cache.get("session_" + token));

        //when logging in with that user after session expires
            ObjectNode loginRequestObject = Json.newObject()
                    .put("emailplus", "hello@example.com")
                    .put("password", "test1234");
            result = callAction(api.controllers.routes.ref.UserController.login(), fakeRequest().withJsonBody(loginRequestObject));

            //it should return a Session Id with expiry
            jsonResult = Json.parse(contentAsString(result));
            String newToken = jsonResult.get("token").asText();
            assertNotNull(newToken);
            assertNotNull(jsonResult.get("expiryDays"));

            //it should have created a session
            assertNotNull(Cache.get("session_" + newToken));

        //when getting user name while logged in
            result = callAction(api.controllers.routes.ref.UserController.viewPrivateProfile("name"), fakeRequest().withHeader("X-XSRF-TOKEN", newToken));
            //it should return the user name
            jsonResult = Json.parse(contentAsString(result));
            String name = jsonResult.get("user").get("name").asText();
            assertEquals("hello@example.com", name);
    }

    @Test
    public void rejectSomeoneTryingToTakeSomeoneElseEmailForUsername() {
        //when registering with a user name with @ in the characters
        ObjectNode requestObj = Json.newObject()
                .put("email", "hello@example.com")
                .put("name", "someoneElseEmail@example.com")
                .put("password", "test1234");
        Result result = callAction(api.controllers.routes.ref.UserController.registerUser(), fakeRequest().withJsonBody(requestObj));
        //it should return status bad request
        assertThat(status(result)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void rejectSomeoneTryingToRegisterSameUserNameOrEmail() {
        //when registering with an unused user
        ObjectNode requestObj = Json.newObject()
                .put("email", "whyhello@example.com")
                .put("name", "hello")
                .put("password", "test1234");
        Result result = callAction(api.controllers.routes.ref.UserController.registerUser(), fakeRequest().withJsonBody(requestObj));
        //it should return status ok
        assertThat(status(result)).isEqualTo(CREATED);

        //when registering with a taken email
        requestObj = Json.newObject()
                .put("email", "whyhello@example.com")
                .put("name", "woot")
                .put("password", "test1234");
        result = callAction(api.controllers.routes.ref.UserController.registerUser(), fakeRequest().withJsonBody(requestObj));
        //it should return status bad request
        assertThat(status(result)).isEqualTo(BAD_REQUEST);

        //when registering with a taken name
        requestObj = Json.newObject()
                .put("email", "me@example.com")
                .put("name", "hello")
                .put("password", "test1234");
        result = callAction(api.controllers.routes.ref.UserController.registerUser(), fakeRequest().withJsonBody(requestObj));
        //it should return status bad request
        assertThat(status(result)).isEqualTo(BAD_REQUEST);
    }

}
