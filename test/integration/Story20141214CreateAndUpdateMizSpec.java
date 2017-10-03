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

public class Story20141214CreateAndUpdateMizSpec {
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
    public void createMizAndUpdateSuccess() {
        //setup - add user into database
        ObjectNode requestObj = Json.newObject()
                .put("email", "hello@example.com")
                .put("password", "test1234");
        Result result = callAction(api.controllers.routes.ref.UserController.registerUser(), fakeRequest().withJsonBody(requestObj));
        JsonNode jsonResult = Json.parse(contentAsString(result));
        String token = jsonResult.get("token").asText();

        //when create a new miz
        requestObj = Json.newObject()
                .put("title", "A new Miz")
                .put("summary", "hello")
                .put("locationRegion", "Calgary");
        result = callAction(api.controllers.routes.ref.MizController.createMiz(), fakeRequest().withJsonBody(requestObj).withHeader("X-XSRF-TOKEN", token));

            //it should return the miz when retrieving it
            result = callAction(api.controllers.routes.ref.MizController.getPrivateMiz("hello@example.com","A new Miz"), fakeRequest().withHeader("X-XSRF-TOKEN", token));
            jsonResult = Json.parse(contentAsString(result));
            assertEquals("A new Miz", jsonResult.get("title").asText());
            assertEquals("hello", jsonResult.get("summary").asText());

            wait2seconds();

            //it should show up in search
            List<Pair<String,MizContent>> pairs = api.controllers.MizController.getMizByQuery("title:\"A new Miz\"").get(5000);
            assertEquals("A new Miz", pairs.get(0).getRight().title);
            assertEquals("hello", pairs.get(0).getRight().summary);


        //when updating the title, summary, description on the miz
        requestObj = Json.newObject()
                .put("title", "blaMiz")
                .put("summary", "what's new?")
                .put("contentBody", "<p>hello</p>");
        result = callAction(api.controllers.routes.ref.MizController.updateMiz("A new Miz"), fakeRequest().withJsonBody(requestObj).withHeader("X-XSRF-TOKEN", token));

            //it should return the miz when retrieving it
            result = callAction(api.controllers.routes.ref.MizController.getPrivateMiz("hello@example.com","blaMiz"), fakeRequest().withHeader("X-XSRF-TOKEN", token));
            jsonResult = Json.parse(contentAsString(result));
            assertEquals("blaMiz", jsonResult.get("title").asText());
            assertEquals("what's new?", jsonResult.get("summary").asText());
            assertTrue(jsonResult.get("contentBody").asText().contains("<p>hello</p>"));

            wait2seconds();

            //it should show up in search
            pairs = api.controllers.MizController.getMizByQuery("title:blaMiz").get(5000);
            assertEquals("blaMiz", pairs.get(0).getRight().title);
            assertEquals("what's new?", pairs.get(0).getRight().summary);

            //old title should not show up in search
            pairs = api.controllers.MizController.getMizByQuery("title:\"A new Miz\"").get(5000);
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
