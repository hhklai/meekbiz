package integration;

import api.entity.MizContent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
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

public class Story20160216StringMaxLengthWhenCreatingAndUpdatingMizSpec {
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
    public void createMizAndUpdateShouldFailByMaxLength() {
        //setup - add user into database
        ObjectNode requestObj = Json.newObject()
                .put("email", "hello@example.com")
                .put("password", "test1234");
        Result result = callAction(api.controllers.routes.ref.UserController.registerUser(), fakeRequest().withJsonBody(requestObj));
        JsonNode jsonResult = Json.parse(contentAsString(result));
        String token = jsonResult.get("token").asText();

        wait2seconds();

        //when create a new miz with title that is more than 255 characters
        requestObj = Json.newObject()
                .put("title", StringUtils.repeat("*", 256))
                .put("summary", "hello")
                .put("locationRegion", "Calgary");
        result = callAction(api.controllers.routes.ref.MizController.createMiz(), fakeRequest().withJsonBody(requestObj).withHeader("X-XSRF-TOKEN", token));
            //it should reject the request
            assertEquals("Create: title is too long",400, status(result));
            assertEquals("title cannot be greater than 255 characters", contentAsString(result));

        //when create a new miz with summary that is more than 1000 characters
        requestObj = Json.newObject()
                .put("title", "blaMiz")
                .put("summary", StringUtils.repeat("*", 1001))
                .put("locationRegion", "Calgary");
        result = callAction(api.controllers.routes.ref.MizController.createMiz(), fakeRequest().withJsonBody(requestObj).withHeader("X-XSRF-TOKEN", token));
            //it should reject the request
            assertEquals("Create: summary is too long", 400, status(result));
            assertEquals("summary cannot be greater than 1000 characters", contentAsString(result));


        //persist a miz for updating
        requestObj = Json.newObject()
                .put("title", "blaMiz")
                .put("summary", "hello")
                .put("locationRegion", "Calgary");
        result = callAction(api.controllers.routes.ref.MizController.createMiz(), fakeRequest().withJsonBody(requestObj).withHeader("X-XSRF-TOKEN", token));

        wait2seconds();

        //when updating the title with more than 255 characters
        requestObj = Json.newObject()
                .put("title", StringUtils.repeat("*", 256))
                .put("summary", "what's new?")
                .put("contentBody", "<p>hello</p>");
        result = callAction(api.controllers.routes.ref.MizController.updateMiz("blaMiz"), fakeRequest().withJsonBody(requestObj).withHeader("X-XSRF-TOKEN", token));
            //it should reject the request
            assertEquals("Update: title is too long", 400, status(result));
            assertEquals("title cannot be greater than 255 characters", contentAsString(result));


        //when updating the summary with more than 1000 characters
        requestObj = Json.newObject()
                .put("title", "blaMiz")
                .put("summary", StringUtils.repeat("*", 1001))
                .put("contentBody", "<p>hello</p>");
        result = callAction(api.controllers.routes.ref.MizController.updateMiz("blaMiz"), fakeRequest().withJsonBody(requestObj).withHeader("X-XSRF-TOKEN", token));
            //it should reject the request
            assertEquals("Update: summary is too long", 400, status(result));
            assertEquals("summary cannot be greater than 1000 characters", contentAsString(result));


    }

    private void wait2seconds() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            //do nothing
        }
    }
}
