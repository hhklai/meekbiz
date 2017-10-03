package integration.models;

import api.entity.S3File;
import api.entity.User;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.db.jpa.JPA;
import play.libs.F;
import play.test.FakeApplication;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static play.test.Helpers.*;
import static play.test.Helpers.start;

public class S3FileSpec {
    static FakeApplication fakeApp;
    S3File underTest;
    String ownerId;

    @BeforeClass
    public static void init() {
        fakeApp = fakeApplication(inMemoryDatabase());
        start(fakeApp);
    }
    @Before
    public void setup() {
        underTest = new S3File();
        //setup db
        JPA.withTransaction(new F.Callback0() {
            @Override
            public void invoke() throws Throwable {
                //create a user
                User user = new User();
                user.setEmail("anExample@example.com");
                ownerId = user.save();
            }
        });
    }

    @AfterClass
    public static void tearDown() {
        stop(fakeApp);
    }
    
    @Test
    public void checkS3DatabasePersistenceAndRetrieval() {

        JPA.withTransaction(new F.Callback0() {
            @Override
            public void invoke() throws Throwable {
                //when saving a S3File
                String internalId = underTest.save(ownerId);

                // it should populate the public id
                assertNotNull(underTest.getPublicId());

                //when retrieving the S3File
                S3File file = S3File.get(internalId);
                String itemId = file.getPublicId();

                //it should retrieve with ownerId specified correctly
                assertNotNull(file);
                assertEquals(ownerId, file.getOwnerId());


                //when deleting this file without user permission
                S3File.delete(itemId, "someOtherId");

                //it should not have deleted the file
                file = S3File.getByPublicId(itemId);
                assertNotNull(file);

                //when deleting this file with permission
                S3File.delete(itemId, ownerId);

                //it should have deleted the file
                file = S3File.getByPublicId(itemId);
                assertNull(file);
            }
        });
    }
}
