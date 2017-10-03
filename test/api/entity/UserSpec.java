package api.entity;

import api.exceptions.ValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import play.db.jpa.JPA;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.fest.assertions.Assertions.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JPA.class)
public class UserSpec {

    User underTest;
    EntityManager em;


    @Before
    public void setup() {
        underTest = new User();
        em = mock(EntityManager.class);
    }

    @Test
    public void checkPassword() {
        //when setting a password
        String testPassword = "a super long password ";
        underTest.setPassword(testPassword);

        //it should not store unencrypted string in the entity
        assertNotEquals(underTest.getPassword(), testPassword);

        //it should pass the checkPassword function if the same password is used
        assertTrue(underTest.checkPassword(testPassword));

        //it should not pass the checkPassword method if some other password is used
        assertFalse(underTest.checkPassword("some other password"));
    }

    @Test
    public void checkPatchingNoChange() {
        PowerMockito.mockStatic(JPA.class);
        when(JPA.em()).thenReturn(em);

        underTest.setEmail("example@example.com");
        underTest.setName("example@example.com");
        underTest.setProfilePic("anId");
        underTest.setPassword("aPassword");

        User patch = new User();
        patch.setEmail("example@example.com");
        patch.setName("example@example.com");
        patch.setProfilePic("anId");

        try {
            underTest.patch(patch);
        } catch (ValidationException e) {
            fail();
        }

        assertThat(underTest.getEmail()).isEqualTo("example@example.com");
        assertThat(underTest.getName()).isEqualTo("example@example.com");
        assertThat(underTest.getProfilePic()).isEqualTo("anId");

        verify(em, never()).persist(any(User.class));
    }

    @Test
    public void checkPatchingChangeAll() {
        PowerMockito.mockStatic(JPA.class);
        when(JPA.em()).thenReturn(em);

        //for returning null when getUserByEmail and getUserByName gets calls
        TypedQuery<User> typedQuery =  mock(TypedQuery.class);
        when(em.createNamedQuery(anyString(), eq(User.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        S3File s3fileInDB = new S3File();
        s3fileInDB.ownerId = "aUserId";

        //for return an S3File that is owned by the user
        String userId = "aUserId";
        S3File s3File = new S3File();
        s3File.ownerId = userId;
        setUpS3FileMock(s3File);

        underTest.id = userId;
        underTest.setEmail("example@example.com");
        underTest.setName("example@example.com");
        underTest.setProfilePic("anId");
        underTest.setPassword("aPassword");

        User patch1 = new User();
        patch1.setEmail("new@example.com");
        try {
            underTest.patch(patch1);
        } catch (ValidationException e) {
            fail();
        }
        underTest = new User(underTest);  //release reference and make copy

        User patch2 = new User();
        patch2.setName("newbie");
        try {
            underTest.patch(patch2);
        } catch (ValidationException e) {
            fail();
        }
        assertThat(underTest.getName()).isEqualTo("newbie");
        underTest = new User(underTest); //release reference and make copy

        User patch3 = new User();
        patch3.setProfilePic("aNewId");
        patch3.setPassword("aNewPassword");
        try {
            underTest.patch(patch3);
        } catch (ValidationException e) {
            fail();
        }

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(em, times(3)).persist(captor.capture());
        List<User> userCaptures =  captor.getAllValues();

        assertThat(userCaptures.get(0).getEmail()).isEqualTo("new@example.com");
        assertThat(userCaptures.get(0).getName()).isEqualTo("new@example.com");

        assertThat(userCaptures.get(1).getName()).isEqualTo("newbie");

        assertThat(userCaptures.get(2).getEmail()).isEqualTo("new@example.com");
        assertThat(userCaptures.get(2).getName()).isEqualTo("newbie");
        assertThat(userCaptures.get(2).getProfilePic()).isEqualTo("aNewId");
        assertTrue(userCaptures.get(2).checkPassword("aNewPassword"));

    }

    @Test
    public void checkPatchingEmailAlreadyExists() {
        PowerMockito.mockStatic(JPA.class);
        when(JPA.em()).thenReturn(em);

        //for returning a found user when getUserByEmail and getUserByName gets calls
        TypedQuery<User> typedQuery =  mock(TypedQuery.class);
        when(em.createNamedQuery(anyString(), eq(User.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(new User());

        User patch1 = new User();
        patch1.setEmail("new@example.com");
        try {
            underTest.patch(patch1);
            fail();
        } catch (ValidationException e) {
            //expected
        }

        verify(em, never()).persist(any(User.class));
    }

    @Test
    public void checkPatchingNameAlreadyExists() {
        PowerMockito.mockStatic(JPA.class);
        when(JPA.em()).thenReturn(em);

        //for returning a found user when getUserByEmail and getUserByName gets calls
        TypedQuery<User> userTypedQuery =  mock(TypedQuery.class);
        when(em.createNamedQuery(anyString(), eq(User.class))).thenReturn(userTypedQuery);
        when(userTypedQuery.setParameter(anyString(), any())).thenReturn(userTypedQuery);
        when(userTypedQuery.getSingleResult()).thenReturn(new User());

        User patch1 = new User();
        patch1.setName("newbie");
        try {
            underTest.patch(patch1);
            fail();
        } catch (ValidationException e) {
            //expected
        }

        verify(em, never()).persist(any(User.class));
    }

    @Test
    public void profilePicHasWrongOwner() {
        PowerMockito.mockStatic(JPA.class);
        when(JPA.em()).thenReturn(em);

        underTest = new User(){{
            id = "anId";
        }};

        S3File s3fileInDB = new S3File();
        s3fileInDB.ownerId = "someOtherId";

        setUpS3FileMock(s3fileInDB);

        User patch1 = new User();
        patch1.setProfilePic("aFileId");
        try {
            underTest.patch(patch1);
            fail();
        } catch (ValidationException e) {
            //expected
        }

        verify(em, never()).persist(any(User.class));
    }

    @Test
    public void invalidProfilePic() {
        PowerMockito.mockStatic(JPA.class);
        when(JPA.em()).thenReturn(em);
        setUpS3FileMock(null);

        User patch1 = new User();
        patch1.setProfilePic("aFileId");
        try {
            underTest.patch(patch1);
            fail();
        } catch (ValidationException e) {
            //expected
        }

        verify(em, never()).persist(any(User.class));
    }

    private void setUpS3FileMock(S3File s3FileInDb) {
        TypedQuery<S3File> fileTypedQuery =  mock(TypedQuery.class);
        when(em.createNamedQuery(anyString(), eq(S3File.class))).thenReturn(fileTypedQuery);
        when(fileTypedQuery.setParameter(anyString(), any())).thenReturn(fileTypedQuery);
        when(fileTypedQuery.getSingleResult()).thenReturn(s3FileInDb);
    }
}


