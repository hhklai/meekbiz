package integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import api.entity.Miz;
import api.entity.MizToS3File;
import org.apache.commons.collections.CollectionUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.db.jpa.JPA;
import play.libs.F;
import play.libs.Json;
import play.mvc.Result;
import play.test.FakeApplication;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.*;

public class Story20150311CreateAndUpdateS3IdsForMizDescription {
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
    public void createMizCreateS3FileIdsSuccess() {
        //setup - add user into database
        ObjectNode requestObj = Json.newObject()
                .put("email", "hello@example.com")
                .put("password", "test1234");
        Result result = callAction(api.controllers.routes.ref.UserController.registerUser(), fakeRequest().withJsonBody(requestObj));
        JsonNode jsonResult = Json.parse(contentAsString(result));
        String token = jsonResult.get("token").asText();

        //setup - create a miz
        requestObj = Json.newObject()
                .put("title", "A new Miz")
                .put("summary", "hello")
                .put("locationRegion", "Calgary");
        result = callAction(api.controllers.routes.ref.MizController.createMiz(), fakeRequest().withJsonBody(requestObj).withHeader("X-XSRF-TOKEN", token));

        //request 3 ids
        requestObj = Json.newObject()
                .put("numIds", 3);
        result = callAction(api.controllers.routes.ref.MizController.requestS3Ids("A new Miz"), fakeRequest().withJsonBody(requestObj).withHeader("X-XSRF-TOKEN", token));
        jsonResult = Json.parse(contentAsString(result));
        Iterator<JsonNode> elems = jsonResult.elements();
        final List<String> returnedIds = new ArrayList<>();
        while (elems.hasNext()) {
            returnedIds.add(elems.next().get("fileId").asText());
        }

        //update miz with description with the 3 ids
        requestObj = Json.newObject()
                .put("contentBody", String.format("<img src=\"http://img-dev.meekbiz.com/%s.jpg\"/>" +
                                "<img src=\"http://img-dev.meekbiz.com/%s.jpg\"/>" +
                                "<img src=\"http://img-dev.meekbiz.com/%s.jpg\"/>",
                        returnedIds.get(0), returnedIds.get(1), returnedIds.get(2)));
        result = callAction(api.controllers.routes.ref.MizController.updateMiz("A new Miz"), fakeRequest().withJsonBody(requestObj).withHeader("X-XSRF-TOKEN", token));

        JPA.withTransaction(new F.Callback0() {
            @Override
            public void invoke() throws Throwable {
                Miz mizInDb = Miz.getByUserNameAndTitle("hello@example.com", "A new Miz");
                List<String> s3Ids = MizToS3File.getAllS3IdsByMizId(mizInDb.id, MizToS3File.State.USED);
                assertTrue(s3Ids.containsAll(returnedIds));  //assert returned values equals everything in the used column
                assertTrue(returnedIds.containsAll(s3Ids));
            }
        });

        //remove two s3ids from  miz description
        requestObj = Json.newObject()
                .put("contentBody", String.format("<img src=\"http://img-dev.meekbiz.com/%s.jpg\"/>",
                                    returnedIds.get(0)));
        result = callAction(api.controllers.routes.ref.MizController.updateMiz("A new Miz"), fakeRequest().withJsonBody(requestObj).withHeader("X-XSRF-TOKEN", token));

        JPA.withTransaction(new F.Callback0() {
            @Override
            public void invoke() throws Throwable {
                Miz mizInDb = Miz.getByUserNameAndTitle("hello@example.com", "A new Miz");
                List<String> s3Ids = MizToS3File.getAllS3IdsByMizId(mizInDb.id, MizToS3File.State.USED);
                assertEquals(1, s3Ids.size());
                assertTrue(s3Ids.contains(returnedIds.get(0)));  //assert undeleted id is still used
                s3Ids = MizToS3File.getAllS3IdsByMizId(mizInDb.id, MizToS3File.State.UNUSED);
                assertEquals(2, s3Ids.size());
                assertTrue(s3Ids.contains(returnedIds.get(1)));  //assert deleted ids are unused
                assertTrue(s3Ids.contains(returnedIds.get(2)));
            }
        });

        //request for 3 ids
        requestObj = Json.newObject()
                .put("numIds", 3);
        result = callAction(api.controllers.routes.ref.MizController.requestS3Ids("A new Miz"), fakeRequest().withJsonBody(requestObj).withHeader("X-XSRF-TOKEN", token));
        jsonResult = Json.parse(contentAsString(result));
        elems = jsonResult.elements();
        final List<String> nextReturnedIds = new ArrayList<>();
        while (elems.hasNext()) {
            nextReturnedIds.add(elems.next().get("fileId").asText());
        }

        //should have the 2 previously removed ids + a new one
        assertTrue(nextReturnedIds.contains(returnedIds.get(1)));
        assertTrue(nextReturnedIds.contains(returnedIds.get(2)));
        assertEquals(3, nextReturnedIds.size());
        assertEquals(1, CollectionUtils.subtract(returnedIds, nextReturnedIds).size());


        JPA.withTransaction(new F.Callback0() {
            @Override
            public void invoke() throws Throwable {
                Miz mizInDb = Miz.getByUserNameAndTitle("hello@example.com", "A new Miz");
                List<String> s3Ids = MizToS3File.getAllS3IdsByMizId(mizInDb.id, MizToS3File.State.UNUSED);
                assertEquals(0, s3Ids.size());  //check the two unused ids are now used
                s3Ids = MizToS3File.getAllS3IdsByMizId(mizInDb.id, MizToS3File.State.USED);
                assertEquals(4, s3Ids.size());
                assertTrue(s3Ids.containsAll(returnedIds));
                assertTrue(s3Ids.containsAll(nextReturnedIds));  //check all 4 ids are used
            }
        });
    }
}
