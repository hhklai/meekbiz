package integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import api.entity.S3File;
import api.entity.User;
import api.entity.UserSession;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Play;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.libs.F;
import play.libs.Json;
import play.mvc.Result;
import play.test.FakeApplication;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

public class Story20140923FileUploadToAmazonS3 {
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
    public void createUserProfilePicThenUpdateAndDelete() {
        //set up, create a user in the database first
        final User user = new User();
        final String[] ownerId = new String[1];
        JPA.withTransaction(new F.Callback0() {
            @Override
            public void invoke() throws Throwable {
                //create a user
                user.setEmail("anExample@example.com");
                user.setName(("aName"));
                ownerId[0] = user.save();
            }
        });


        //when calling a createS3File endpoint
        Cache.set("session_a-uuid", new UserSession(user, "127.0.0.1", 0L));
        Result result = callAction(api.controllers.routes.ref.S3Controller.createS3FileMetaData(), fakeRequest().withHeader("X-XSRF-TOKEN", "a-uuid"));
        //it should return a S3File id to use and the permission for that id
        JsonNode jsonResult = Json.parse(contentAsString(result));
        final String fileId = jsonResult.get("fileId").asText();
        assertThat(fileId).isNotNull();
        assertThat(jsonResult.get("policy").asText()).isNotNull();
        assertThat(jsonResult.get("accessKey").asText()).isEqualTo(Play.application().configuration().getString("aws.access.key"));
        assertThat(jsonResult.get("signature").asText()).isNotNull();
        assertThat(jsonResult.get("bucket").asText()).isEqualTo(Play.application().configuration().getString("aws.s3.bucket"));
        assertThat(status(result)).isEqualTo(OK);
        //it should created an S3 file with that id
        final S3File [] s3Files = new S3File[1];
        JPA.withTransaction(new F.Callback0() {
            @Override
            public void invoke() throws Throwable {
                s3Files[0] = S3File.getByPublicId(fileId);
            }
        });
        assertThat(s3Files[0]).isNotNull();

        //when frontend retrieves createS3File endpoint success and updates the user profile pic
        ObjectNode requestObj = Json.newObject()
                .put("profilePic", fileId);
        Result resultU = callAction(api.controllers.routes.ref.UserController.updatePrivateProfile(),
                fakeRequest().withHeader("X-XSRF-TOKEN", "a-uuid").withJsonBody(requestObj));
        assertThat(status(resultU)).isEqualTo(OK);
        //it should created an S3 file with that id
        final String[] profilePic = new String[1];
        JPA.withTransaction(new F.Callback0() {
            @Override
            public void invoke() throws Throwable {
                User user = User.getById(ownerId[0]);
                profilePic[0] = user.getProfilePic();
            }
        });
        assertThat(profilePic[0]).isEqualTo(fileId);


        //when calling a update s3 file endpoint with an existing
        Result result1 = callAction(api.controllers.routes.ref.S3Controller.updateS3FileMetaData(fileId), fakeRequest().withHeader("X-XSRF-TOKEN", "a-uuid"));
        JsonNode jsonResult1 = Json.parse(contentAsString(result1));
        assertThat(jsonResult1.get("fileId").asText()).isEqualTo(fileId);
        assertThat(jsonResult1.get("policy").asText()).isNotNull();
        assertThat(jsonResult1.get("accessKey").asText()).isEqualTo(Play.application().configuration().getString("aws.access.key"));
        assertThat(jsonResult1.get("signature").asText()).isNotNull();
        assertThat(jsonResult1.get("bucket").asText()).isEqualTo(Play.application().configuration().getString("aws.s3.bucket"));

        //it should return an ok result
        assertThat(status(result1)).isEqualTo(OK);


        //when calling delete s3 file
        Result result2 = callAction(api.controllers.routes.ref.S3Controller.deleteS3FileMetaData(fileId), fakeRequest().withHeader("X-XSRF-TOKEN", "a-uuid"));
        assertThat(status(result2)).isEqualTo(OK);

        //it should delete the s3 file entry from database and set profile pic to null
        JPA.withTransaction(new F.Callback0() {
            @Override
            public void invoke() throws Throwable {
                User user = User.getById(ownerId[0]);
                profilePic[0] = user.getProfilePic();
                s3Files[0] = S3File.get(fileId);
            }
        });
        assertThat(s3Files[0]).isNull();
        assertThat(profilePic[0]).isNull();
    }
}
