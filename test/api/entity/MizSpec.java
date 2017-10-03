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

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JPA.class)
public class MizSpec {

    Miz underTest;
    EntityManager em;


    @Before
    public void setup() {
        underTest = new Miz();
        em = mock(EntityManager.class);
    }

    @Test
    public void checkPatchingNoChange() {
        PowerMockito.mockStatic(JPA.class);
        when(JPA.em()).thenReturn(em);

        underTest.urlTitle = "something title";
        underTest.ownerId = "a-userid";

        Miz patch = new Miz();
        patch.urlTitle = "something title";

        try {
            underTest.patch(patch);
        } catch (ValidationException e) {
            fail();
        }

        assertThat(underTest.urlTitle).isEqualTo("something title");
        assertThat(underTest.ownerId).isEqualTo("a-userid");

        verify(em, never()).persist(any(Miz.class));
    }

    @Test
    public void checkPatchingChangeAll() throws ValidationException {
        PowerMockito.mockStatic(JPA.class);
        when(JPA.em()).thenReturn(em);

        //for returning null when getMizByTitle gets calls
        TypedQuery<Miz> typedQuery =  mock(TypedQuery.class);
        when(em.createNamedQuery(anyString(), eq(Miz.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);

        underTest.urlTitle = "something title";
        underTest.ownerId = "a-userid";

        Miz patch = new Miz();
        patch.urlTitle = "something else title";

        underTest.patch(patch);

        ArgumentCaptor<Miz> captor = ArgumentCaptor.forClass(Miz.class);
        verify(em, times(1)).persist(captor.capture());
        List<Miz> mizCaptures =  captor.getAllValues();

        assertThat(mizCaptures.get(0).urlTitle).isEqualTo("something else title");
    }

    @Test
    public void checkPatchingTitleAlreadyExists() {
        PowerMockito.mockStatic(JPA.class);
        when(JPA.em()).thenReturn(em);

        //for returning a miz when getMizByTitle gets calls
        TypedQuery<Miz> typedQuery =  mock(TypedQuery.class);
        when(em.createNamedQuery(anyString(), eq(Miz.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(new Miz());

        Miz patch1 = new Miz();
        patch1.urlTitle = ("something else");
        try {
            underTest.patch(patch1);
            fail();
        } catch (ValidationException e) {
            //pass
        }

        verify(em, never()).persist(any(Miz.class));
    }
}


