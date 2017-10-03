package integration;

import api.entity.User;
import api.entity.UserSession;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.libs.F;
import play.mvc.Result;
import play.test.FakeApplication;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

public class Story20140710AuthenticatedSpec {
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
    public void callActionWithSecuritNotLoggedIny() {
        //when calling a secure route with a default request
        Result result = callAction(api.controllers.routes.ref.UserController.viewPrivateProfile("name"), fakeRequest());
        //it should return an unauthorized status code
        assertThat(status(result)).isEqualTo(UNAUTHORIZED);
    }


    @Test
    public void callActionWithSecuritySuccess() {
        //set up, create a user in the database first
        final User user = new User();
        final String[] userId = new String[1];
        JPA.withTransaction(new F.Callback0() {
            @Override
            public void invoke() throws Throwable {
                //create a user
                user.setEmail("test1@example.com");
                user.setName(("test1"));
                userId[0] = user.save();
            }
        });

        //when calling a secure route with the correct credentials stored in the session
        Cache.set("session_" + userId[0], new UserSession(user, "127.0.0.1", 0L));
        Result result = callAction(api.controllers.routes.ref.UserController.viewPrivateProfile("name"), fakeRequest().withHeader("X-XSRF-TOKEN", userId[0]));
        //it should return an ok result without hitting the database
        assertThat(status(result)).isEqualTo(OK);
    }

    @Test
    public void callActionWithSecurityBadIp() {
        //set up, create a user in the database first
        final User user = new User();
        final String[] userId = new String[1];
        JPA.withTransaction(new F.Callback0() {
            @Override
            public void invoke() throws Throwable {
                //create a user
                user.setEmail("test2@example.com");
                user.setName(("test2"));
                userId[0] = user.save();
            }
        });

        //when calling a secure route with wrong ip stored in the session
        Cache.set("session_" + userId[0], new UserSession(user, "127.0.0.5", 0L));
        Result result = callAction(api.controllers.routes.ref.UserController.viewPrivateProfile("name"), fakeRequest().withHeader("X-XSRF-TOKEN", userId[0]));
        //it should return an ok result
        assertThat(status(result)).isEqualTo(UNAUTHORIZED);
    }
}
