package integration;

import api.entity.Miz;
import api.entity.MizContent;
import api.entity.User;
import api.entity.UserSession;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.libs.F;
import play.mvc.Result;
import play.test.FakeApplication;
import utils.SearchUtil;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.*;

public class Story20150830SearchIndexManagementSpec {
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
    public void regularUserCannotAccessAdminArea() {
        //set up, create a user in the database first
        final User user = new User();
        final String[] userId = new String[1];
        JPA.withTransaction(new F.Callback0() {
            @Override
            public void invoke() throws Throwable {
                //create a user
                user.setEmail("test1@example.com");
                user.setName(("test1"));
                user.setPassword("1235");
                userId[0] = user.save();
            }
        });

        //when calling a secure route with the correct credentials stored in the session
        Cache.set("session_" + userId[0], new UserSession(user, "127.0.0.1", 0L));
        Result result = callAction(api.controllers.routes.ref.SearchController.reInitSearch(), fakeRequest().withHeader("X-XSRF-TOKEN", userId[0]));
        //it should return unauthorized response
        assertThat(status(result)).isEqualTo(UNAUTHORIZED);
    }

    @Test
    public void managementEndpointsToAddUnsyncedDataAndToRemoveJunkData() {
        //set up, create a user in the database first
        final User admin = new User();
        final String[] userId = new String[1];
        final String[] mizId = new String[1];
        JPA.withTransaction(new F.Callback0() {
            @Override
            public void invoke() throws Throwable {
                //create a user
                admin.setEmail("admim@example.com");
                admin.setName(("admim"));
                admin.setPassword("1235");
                admin.isAdmin = true;
                userId[0] = admin.save();

                //create an unsynched miz
                Miz miz = new Miz();
                assertFalse(miz.searchSynched);
                MizContent mizContent = new MizContent();
                miz.urlTitle = "A new Miz";
                miz.ownerId = userId[0];
                miz.locationRegion = "Calgary";

                mizId[0] = miz.save();
                mizContent.mizId = mizId[0];
                mizContent.title = "A new Miz";
                mizContent.save();
            }
        });

        SearchUtil.resetIndex().get(5000);
        //miz in db should not show up in search yet
        List<Pair<String, MizContent>> pairs = api.controllers.MizController.getMizByQuery("title:\"A new Miz\"").get(5000);
        assertTrue(pairs.isEmpty());

        //when admin calls sync search
        Cache.set("session_" + userId[0], new UserSession(admin, "127.0.0.1", 0L));
        Result result = callAction(api.controllers.routes.ref.SearchController.synchSearchIndexWithNewMizes(), fakeRequest().withHeader("X-XSRF-TOKEN", userId[0]));
            //it should return an ok result without hitting the database
            assertThat(status(result)).isEqualTo(OK);

            wait2seconds();

            //it should show up in search
            pairs = api.controllers.MizController.getMizByQuery("title:\"A new Miz\"").get(5000);
            assertEquals("A new Miz", pairs.get(0).getRight().title);

            //it should change the miz in db to be search synched, so it won't try to sync it again
            JPA.withTransaction(new F.Callback0() {
                @Override
                public void invoke() throws Throwable {
                    Miz theMiz = Miz.get(mizId[0]);
                    assertTrue(theMiz.searchSynched);  //should changed to synched = true
                }
            });

        //when admin calls reset index
        result = callAction(api.controllers.routes.ref.SearchController.rebuildSearchIndex(), fakeRequest().withHeader("X-XSRF-TOKEN", userId[0]));

            wait2seconds();

            //it should delete all items from search, the miz should not be getting back a result
            pairs = api.controllers.MizController.getMizByQuery("title:\"A new Miz\"").get(5000);
            assertTrue(pairs.isEmpty());

            //it should change the miz in db to be search synched = false, so that next search sync will push the miz in db to search
            JPA.withTransaction(new F.Callback0() {
                @Override
                public void invoke() throws Throwable {
                    Miz theMiz = Miz.get(mizId[0]);
                    assertFalse(theMiz.searchSynched);  //should changed to synched = false
                }
            });

    }


    private void wait2seconds() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            //ignore
        }
    }
}
