package integration.models;

import api.entity.User;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.db.jpa.JPA;
import play.libs.F;
import play.test.FakeApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static play.test.Helpers.*;

public class UserSpec {
    static FakeApplication fakeApp;
    User underTest;

    @BeforeClass
    public static void init() {
        fakeApp = fakeApplication(inMemoryDatabase());
        start(fakeApp);
    }

    @AfterClass
    public static void tearDown() {
        stop(fakeApp);
    }

    @Before
    public void setup() {
        underTest = new User();
    }
    
    @Test
    public void checkUserCreateAndRetrieval() {
        final String email = "test@example.com";
        final String name = "name";

        JPA.withTransaction(new F.Callback0() {
            @Override
            public void invoke() throws Throwable {
                //when saving a user model
                underTest.setEmail(email);
                underTest.setName(name);
                underTest.setPassword("something");
                underTest.save();

                // it should populate the user id
                assertNotNull(underTest.getId());

                //it should be findable by get email
                User retrieved = User.getUserByEmail(email);
                assertReflectionEquals("find by email not working", underTest, retrieved);

                //it should be findable by get name
                retrieved = User.getUserByName(name);
                assertReflectionEquals("find by name not working", underTest, retrieved);
                //

                //when trying to hack the application with SQL INJECTION
                //Note: trying to exploit that the resulting sql syntax is
                // SELECT e FROM User e WHERE e.email = e.email which returns all users
                User retrieved1 = User.getUserByEmail("e.email");

                //it should return nothing
                assertNull(retrieved1);
                //


            }
        });

        //when saving a user without a name
        JPA.withTransaction(new F.Callback0() {
            @Override
            public void invoke() throws Throwable {
                underTest =  new User();
                //when saving a user model
                underTest.setEmail("anotherEmail@example.com");
                underTest.setPassword("something");
                underTest.save();

                // it should populate the user name with the email
                assertEquals(underTest.getEmail(), underTest.getName());
            }
        });

        //when user somehow bypasses front end validation and sends a user without an email
        try {
            JPA.withTransaction(new F.Callback0() {
                @Override
                public void invoke() throws Throwable {
                    User userWithoutEmail = new User();
                    userWithoutEmail.setPassword("something");
                    userWithoutEmail.save();
                }
            });
        } catch (RuntimeException e) {
            //it should be rejected by the hibernate validator
            assertNotNull(e);
        }
    }
}
