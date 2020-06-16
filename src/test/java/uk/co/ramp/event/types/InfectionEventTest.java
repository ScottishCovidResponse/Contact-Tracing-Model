package uk.co.ramp.event.types;

import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.people.Case;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.ramp.people.VirusStatus.EXPOSED;
import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;

public class InfectionEventTest {

    private final InfectionEvent event = ImmutableInfectionEvent.builder()
            .exposedBy(10)
            .exposedTime(5)
            .id(2)
            .nextStatus(EXPOSED)
            .oldStatus(SUSCEPTIBLE)
            .time(3)
            .build();

    private Case thisCase;

    @Before
    public void setUp() {
        thisCase = mock(Case.class);
        when(thisCase.virusStatus()).thenReturn(SUSCEPTIBLE);
    }

    @Test
    public void testInfectionEventExposedBySetInCase() {
        event.applyEventToCase(thisCase);
        verify(thisCase).setExposedBy(10);
    }

    @Test
    public void testInfectionEventExposedTimeSetInCase() {
        event.applyEventToCase(thisCase);
        verify(thisCase).setExposedTime(5);
    }

    @Test
    public void testInfectionEventVirusStatusSetInCase() {
        event.applyEventToCase(thisCase);
        verify(thisCase).setVirusStatus(EXPOSED);
    }
}
