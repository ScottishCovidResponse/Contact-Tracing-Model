package uk.co.ramp.policy;

import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.VirusStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.intThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContactIsolationPolicyTest {

    private SingleCaseIsolationPolicy singleCaseIsolationPolicy;
    private DiseaseProperties diseaseProperties;
    private Case caseA;
    private Case caseB;
    double contactWeight = 0.2;
    double actualInfectedProportion = 0.1;
    int currentTime = 0;

    @Before
    public void setUp() {
        this.singleCaseIsolationPolicy = mock(SingleCaseIsolationPolicy.class);

        this.diseaseProperties = mock(DiseaseProperties.class);
        when(diseaseProperties.exposureThreshold()).thenReturn(0.5);

        this.caseA = mock(Case.class);
        when(caseA.id()).thenReturn(0);
        when(caseA.status()).thenReturn(VirusStatus.INFECTED);
        when(caseA.alertStatus()).thenReturn(AlertStatus.NONE);
        when(caseA.compliance()).thenReturn(1.0);

        this.caseB = mock(Case.class);
        when(caseB.id()).thenReturn(1);
        when(caseB.status()).thenReturn(VirusStatus.SUSCEPTIBLE);
        when(caseB.alertStatus()).thenReturn(AlertStatus.NONE);
        when(caseB.compliance()).thenReturn(1.0);
    }

    @Test
    public void isContactIsolated_CaseAIsolate() {
        when(singleCaseIsolationPolicy.isIndividualInIsolation(intThat(a -> a == 0), any(VirusStatus.class), any(AlertStatus.class), anyDouble(), anyDouble(), anyInt())).thenReturn(true);
        when(singleCaseIsolationPolicy.isIndividualInIsolation(intThat(a -> a == 1), any(VirusStatus.class), any(AlertStatus.class), anyDouble(), anyDouble(), anyInt())).thenReturn(false);

        var isolationPolicy = new ContactIsolationPolicy(singleCaseIsolationPolicy, diseaseProperties);
        assertThat(isolationPolicy.isContactIsolated(caseA, caseB, contactWeight, actualInfectedProportion, currentTime)).isTrue();
    }

    @Test
    public void isContactIsolated_CaseBIsolate() {
        when(singleCaseIsolationPolicy.isIndividualInIsolation(intThat(a -> a == 0), any(VirusStatus.class), any(AlertStatus.class), anyDouble(), anyDouble(), anyInt())).thenReturn(false);
        when(singleCaseIsolationPolicy.isIndividualInIsolation(intThat(a -> a == 1), any(VirusStatus.class), any(AlertStatus.class), anyDouble(), anyDouble(), anyInt())).thenReturn(true);

        var isolationPolicy = new ContactIsolationPolicy(singleCaseIsolationPolicy, diseaseProperties);
        assertThat(isolationPolicy.isContactIsolated(caseA, caseB, contactWeight, actualInfectedProportion, currentTime)).isTrue();
    }
    @Test
    public void isContactIsolated_BothCaseABIsolate() {
        when(singleCaseIsolationPolicy.isIndividualInIsolation(intThat(a -> a == 0), any(VirusStatus.class), any(AlertStatus.class), anyDouble(), anyDouble(), anyInt())).thenReturn(true);
        when(singleCaseIsolationPolicy.isIndividualInIsolation(intThat(a -> a == 1), any(VirusStatus.class), any(AlertStatus.class), anyDouble(), anyDouble(), anyInt())).thenReturn(true);

        var isolationPolicy = new ContactIsolationPolicy(singleCaseIsolationPolicy, diseaseProperties);
        assertThat(isolationPolicy.isContactIsolated(caseA, caseB, contactWeight, actualInfectedProportion, currentTime)).isTrue();
    }

    @Test
    public void isContactIsolated_NoneIsolate() {
        when(singleCaseIsolationPolicy.isIndividualInIsolation(intThat(a -> a == 0), any(VirusStatus.class), any(AlertStatus.class), anyDouble(), anyDouble(), anyInt())).thenReturn(false);
        when(singleCaseIsolationPolicy.isIndividualInIsolation(intThat(a -> a == 1), any(VirusStatus.class), any(AlertStatus.class), anyDouble(), anyDouble(), anyInt())).thenReturn(false);

        var isolationPolicy = new ContactIsolationPolicy(singleCaseIsolationPolicy, diseaseProperties);
        assertThat(isolationPolicy.isContactIsolated(caseA, caseB, contactWeight, actualInfectedProportion, currentTime)).isFalse();
    }

    @Test
    public void isContactIsolated_BothABIsolate_StrongContact() {
        when(singleCaseIsolationPolicy.isIndividualInIsolation(intThat(a -> a == 0), any(VirusStatus.class), any(AlertStatus.class), anyDouble(), anyDouble(), anyInt())).thenReturn(true);
        when(singleCaseIsolationPolicy.isIndividualInIsolation(intThat(a -> a == 1), any(VirusStatus.class), any(AlertStatus.class), anyDouble(), anyDouble(), anyInt())).thenReturn(true);

        var contactWeight = 1.0;
        var isolationPolicy = new ContactIsolationPolicy(singleCaseIsolationPolicy, diseaseProperties);
        assertThat(isolationPolicy.isContactIsolated(caseA, caseB, contactWeight, actualInfectedProportion, currentTime)).isFalse();
    }
}