package integration;

import api.entity.MizContent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Result;
import play.test.FakeApplication;
import utils.SearchUtil;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.*;

public class Story20151204DeleteMizThatIsNotActivated {
    static FakeApplication fakeApp;

    @BeforeClass
    public static void init() {
        fakeApp = fakeApplication(inMemoryDatabase());
        start(fakeApp);
        SearchUtil.resetIndex().get(5000);
    }

    @AfterClass
    public static void tearDown() {
        stop(fakeApp);
    }

    @Test
    public void createMizAndDeleteSuccess() {
        //setup - add user into database
        ObjectNode requestObj = Json.newObject()
                .put("email", "hello@example.com")
                .put("password", "test1234");
        Result result = callAction(api.controllers.routes.ref.UserController.registerUser(), fakeRequest().withJsonBody(requestObj));
        JsonNode jsonResult = Json.parse(contentAsString(result));
        String token = jsonResult.get("token").asText();

        //when create blaMiz1
        requestObj = Json.newObject()
                .put("title", "blaMiz1")
                .put("summary", "hello")
                .put("locationRegion", "Calgary");
        result = callAction(api.controllers.routes.ref.MizController.createMiz(), fakeRequest().withJsonBody(requestObj).withHeader("X-XSRF-TOKEN", token));

            //it should return the miz when retrieving it
            result = callAction(api.controllers.routes.ref.MizController.getPrivateMiz("hello@example.com","blaMiz1"), fakeRequest().withHeader("X-XSRF-TOKEN", token));
            jsonResult = Json.parse(contentAsString(result));
            assertEquals("blaMiz1", jsonResult.get("title").asText());
            assertEquals("hello", jsonResult.get("summary").asText());

            wait2seconds();

            //it should show up in search
            List<Pair<String,MizContent>> pairs = api.controllers.MizController.getMizByQuery("title:\"blaMiz1\"").get(5000);
            assertEquals("blaMiz1", pairs.get(0).getRight().title);
            assertEquals("hello", pairs.get(0).getRight().summary);

            //request s3ids, to link miz description with s3File.  This is to ensure a bug identified does not regress.  Previously a service cannot be deleted because there was a linkage to a s3 file.
            requestObj = Json.newObject()
                    .put("numIds", 3);
            result = callAction(api.controllers.routes.ref.MizController.requestS3Ids("blaMiz1"), fakeRequest().withJsonBody(requestObj).withHeader("X-XSRF-TOKEN", token));

        //when deleting the miz
        result = callAction(api.controllers.routes.ref.MizController.deleteMizThatHasNeverBeenActivated("hello@example.com", "blaMiz1"), fakeRequest().withHeader("X-XSRF-TOKEN", token));

            //it should no longer return the miz when retrieving it
            result = callAction(api.controllers.routes.ref.MizController.getPrivateMiz("hello@example.com","blaMiz1"), fakeRequest().withHeader("X-XSRF-TOKEN", token));
            assertEquals(404, status(result));

            wait2seconds();

            //it should not show up in search
            pairs = api.controllers.MizController.getMizByQuery("title:\"blaMiz1\"").get(5000);
            assertTrue(pairs.isEmpty());

    }

    private void wait2seconds() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            //do nothing
        }
    }
}
