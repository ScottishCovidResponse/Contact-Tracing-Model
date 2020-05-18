package uk.co.ramp;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.ProgressionDistribution;
import uk.co.ramp.people.Gender;
import uk.co.ramp.people.ImmutablePerson;
import uk.co.ramp.people.InvalidStatusTransitionException;
import uk.co.ramp.people.Person;
import uk.co.ramp.people.VirusStatus;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.ramp.people.VirusStatus.DEAD;
import static uk.co.ramp.people.VirusStatus.EXPOSED;
import static uk.co.ramp.people.VirusStatus.EXPOSED_2;
import static uk.co.ramp.people.VirusStatus.INFECTED;
import static uk.co.ramp.people.VirusStatus.INFECTED_SYMP;
import static uk.co.ramp.people.VirusStatus.RECOVERED;
import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;

public class EvaluateCaseTest {
    private Person person;
    private EvaluateCase evaluateCase;
    private Map<VirusStatus, Set<VirusStatus>> validTransitions;
    private Set<VirusStatus> virusStatusSet;

    @Before
    public void setUp() {
        person = ImmutablePerson.builder()
                .age(50)
                .compliance(0.5)
                .exposedBy(-1)
                .gender(Gender.FEMALE)
                .health(0.5)
                .id(1)
                .status(VirusStatus.SUSCEPTIBLE)
                .nextStatusChange(1)
                .build();

        DiseaseProperties diseaseProperties = mock(DiseaseProperties.class);
        when(diseaseProperties.progressionDistribution()).thenReturn(ProgressionDistribution.FLAT);
        RandomDataGenerator randomDataGenerator = mock(RandomDataGenerator.class);
        evaluateCase = new EvaluateCase(diseaseProperties, randomDataGenerator);

        validTransitions = ImmutableMap.<VirusStatus, Set<VirusStatus>>builder()
                .put(SUSCEPTIBLE, Set.of(EXPOSED))
                .put(EXPOSED, Set.of(EXPOSED_2))
                .put(EXPOSED_2, Set.of(INFECTED, INFECTED_SYMP))
                .put(INFECTED, Set.of(RECOVERED))
                .put(INFECTED_SYMP, Set.of(DEAD, RECOVERED))
                .build();
        virusStatusSet = Set.of(SUSCEPTIBLE, EXPOSED, EXPOSED_2, INFECTED, INFECTED_SYMP, RECOVERED, DEAD);
    }

    @Test
    public void testValidTransitions() {
        for (Map.Entry<VirusStatus, Set<VirusStatus>> transition : validTransitions.entrySet()) {
            VirusStatus from = transition.getKey();
            Set<VirusStatus> tos = transition.getValue();
            person = ImmutablePerson.copyOf(person).withStatus(from);
            for (VirusStatus to : tos) {
                Person updatedPerson = evaluateCase.updateStatus(person, to, 0);
                assertThat(updatedPerson.status()).isEqualTo(to);
            }
        }
    }

    @Test
    public void testInvalidTransitions() {
        for (VirusStatus from : virusStatusSet) {
            Set<VirusStatus> validTransitionsForStatus = validTransitions.getOrDefault(from, Set.of());
            Set<VirusStatus> invalidTransitionsForStatus = new HashSet<>(virusStatusSet);
            invalidTransitionsForStatus.removeAll(validTransitionsForStatus);
            person = ImmutablePerson.copyOf(person).withStatus(from);
            for (VirusStatus to : invalidTransitionsForStatus) {
                assertThatExceptionOfType(InvalidStatusTransitionException.class).isThrownBy(() -> evaluateCase.updateStatus(person, to, 0));
            }
        }
    }
}