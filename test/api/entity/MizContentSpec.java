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
import utils.PlayConfigUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JPA.class, MizToS3File.class, PlayConfigUtil.class})
public class MizContentSpec {

    MizContent underTest;
    EntityManager em;


    @Before
    public void setup() {
        PowerMockito.mockStatic(PlayConfigUtil.class);
        when(PlayConfigUtil.getConfig(eq("aws.s3.host"))).thenReturn("http://img-dev.meekbiz.com/");

        PowerMockito.mockStatic(MizToS3File.class);

        underTest = new MizContent();
        em = mock(EntityManager.class);

        PowerMockito.mockStatic(JPA.class);
        when(JPA.em()).thenReturn(em);
    }

    @Test
    public void checkPatchingNoChange() {
        underTest.title = "some title";
        underTest.mizId = "some id";
        underTest.summary = "some summary";
        underTest.thumbnail = "a-thumbnail-id";
        underTest.bannerPic = "a-banner-id";

        MizContent patch = new MizContent();
        patch.title = "some title";
        patch.summary = "some summary";
        patch.thumbnail = "a-thumbnail-id";
        patch.bannerPic = "a-banner-id";

        try {
            underTest.patch(patch);
        } catch (ValidationException e) {
            fail();
        }

        assertThat(underTest.title).isEqualTo("some title");
        assertThat(underTest.summary).isEqualTo("some summary");
        assertThat(underTest.thumbnail).isEqualTo("a-thumbnail-id");
        assertThat(underTest.bannerPic).isEqualTo("a-banner-id");

        verify(em, never()).persist(any(MizContent.class));
    }

    @Test
    public void checkPatchingChangeAll() {
        S3File s3fileInDB = new S3File();
        s3fileInDB.ownerId = "aOwnerId";

        //for return an S3File that is owned by the user
        String userId = "aOwnerId";
        S3File s3File = new S3File();
        s3File.ownerId = userId;
        setUpS3FileMock(s3File);

        underTest.title = "some title";
        underTest.mizId = "some id";
        underTest.summary = "some summary";
        underTest.thumbnail = "a-thumbnail-id";
        underTest.bannerPic = "a-banner-id";

        MizContent patch = new MizContent();
        patch.title = "some other title";
        patch.summary = "some other summary";
        patch.thumbnail = "another-thumbnail-id";
        patch.bannerPic = "another-banner-id";

        try {
            underTest.patch(patch, userId);
        } catch (ValidationException e) {
            fail();
        }

        ArgumentCaptor<MizContent> captor = ArgumentCaptor.forClass(MizContent.class);
        verify(em, times(1)).persist(captor.capture());
        List<MizContent> mizContentCaptures = captor.getAllValues();

        assertThat(mizContentCaptures.get(0).title).isEqualTo("some other title");
        assertThat(mizContentCaptures.get(0).summary).isEqualTo("some other summary");
        assertThat(mizContentCaptures.get(0).thumbnail).isEqualTo("another-thumbnail-id");
        assertThat(mizContentCaptures.get(0).bannerPic).isEqualTo("another-banner-id");
    }

    @Test
    public void mizThumbnailHasWrongOwner() {
        underTest = new MizContent();

        S3File s3fileInDB = new S3File();
        s3fileInDB.ownerId = "someUserId";

        setUpS3FileMock(s3fileInDB);

        MizContent patch1 = new MizContent();
        patch1.thumbnail = "aFileId";
        try {
            underTest.patch(patch1, "myUserId");
            fail();
        } catch (ValidationException e) {
            //expected
        }

        verify(em, never()).persist(any(MizContent.class));
    }

    @Test
    public void invalidThumbnailPic() {
        setUpS3FileMock(null);

        MizContent patch1 = new MizContent();
        patch1.thumbnail = "aFileId";
        try {
            underTest.patch(patch1);
            fail();
        } catch (ValidationException e) {
            //expected
        }

        verify(em, never()).persist(any(MizContent.class));
    }

    @Test
    public void mizBannerHasWrongOwner() {
        underTest = new MizContent();

        S3File s3fileInDB = new S3File();
        s3fileInDB.ownerId = "someUserId";

        setUpS3FileMock(s3fileInDB);

        MizContent patch1 = new MizContent();
        patch1.bannerPic = "aFileId";
        try {
            underTest.patch(patch1, "myUserId");
            fail();
        } catch (ValidationException e) {
            //expected
        }

        verify(em, never()).persist(any(MizContent.class));
    }

    @Test
    public void invalidBannerPic() {
        setUpS3FileMock(null);

        MizContent patch1 = new MizContent();
        patch1.bannerPic = "aFileId";
        try {
            underTest.patch(patch1);
            fail();
        } catch (ValidationException e) {
            //expected
        }

        verify(em, never()).persist(any(MizContent.class));
    }

    @Test
    public void checkDescriptionGetCleaned() throws ValidationException {
        String userId = "aUserId";

        MizContent patch = new MizContent();
        patch.contentBody = "<script type=\"text/javascript\">alert(1);</script>";

        underTest.patch(patch, userId);

        ArgumentCaptor<MizContent> captor = ArgumentCaptor.forClass(MizContent.class);
        verify(em, times(1)).persist(captor.capture());
        List<MizContent> mizContentCaptures = captor.getAllValues();

        assertThat(mizContentCaptures.get(0).contentBody).isEqualTo("");
    }

    public void checkDescriptionGetsFiltered() throws ValidationException {
        String userId = "aUserId";

        MizContent patch = new MizContent();
        patch.contentBody = "<p>hello</p>";

        underTest.patch(patch, userId);

        ArgumentCaptor<MizContent> captor = ArgumentCaptor.forClass(MizContent.class);
        verify(em, times(1)).persist(captor.capture());
        List<MizContent> mizContentCaptures = captor.getAllValues();

        assertThat(mizContentCaptures.get(0).contentBody).isEqualTo("<p>hello</p>");
    }

    @Test
    public void checkDescriptionPictureIdsGetUpdatedToUnused() throws ValidationException {
        List<String> idsAttachedToMiz = new ArrayList<>();
        idsAttachedToMiz.add("1985-1983585-9815389-1532");
        idsAttachedToMiz.add("898191481-1849849");
        idsAttachedToMiz.add("1985-1983585-ad893-8315");
        idsAttachedToMiz.add("8134519-58398315-153153");
        when(MizToS3File.getAllS3IdsByMizId(anyString(), eq(MizToS3File.State.USED))).thenReturn(idsAttachedToMiz);

        String userId = "aUserId";
        MizContent patch = new MizContent();
        patch.contentBody = "<img " +
                            "src=\"http://img-dev.meekbiz.com/1985-1983585-9815389-1532.jpg\"></img>"+
                            "<p>hello</p>"+
                            "<img width=20 height=600 src=\"http://img-dev.meekbiz.com/1985-1983585-ad893-8315.jpg\"></img>";

        underTest.patch(patch, userId);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        PowerMockito.verifyStatic(times(2));
        MizToS3File.updateS3File(anyString(), captor.capture(), eq(MizToS3File.State.UNUSED));

        List<String> s3IdCaptures = captor.getAllValues();
        assertThat(s3IdCaptures).contains("898191481-1849849");
        assertThat(s3IdCaptures).contains("8134519-58398315-153153");

    }

    private void setUpS3FileMock(S3File s3FileInDb) {
        TypedQuery<S3File> fileTypedQuery =  mock(TypedQuery.class);
        when(em.createNamedQuery(anyString(), eq(S3File.class))).thenReturn(fileTypedQuery);
        when(fileTypedQuery.setParameter(anyString(), any())).thenReturn(fileTypedQuery);
        when(fileTypedQuery.getSingleResult()).thenReturn(s3FileInDb);
    }
}


