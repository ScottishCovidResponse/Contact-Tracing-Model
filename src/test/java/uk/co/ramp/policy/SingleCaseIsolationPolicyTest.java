package uk.co.ramp.policy;

import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.distribution.Distribution;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.distribution.ImmutableDistribution;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.utilities.MinMax;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.ramp.distribution.ProgressionDistribution.FLAT;
import static uk.co.ramp.people.AlertStatus.ALERTED;
import static uk.co.ramp.people.AlertStatus.AWAITING_RESULT;
import static uk.co.ramp.people.AlertStatus.NONE;
import static uk.co.ramp.people.AlertStatus.TESTED_POSITIVE;
import static uk.co.ramp.people.VirusStatus.EXPOSED;
import static uk.co.ramp.people.VirusStatus.EXPOSED_2;
import static uk.co.ramp.people.VirusStatus.INFECTED;
import static uk.co.ramp.people.VirusStatus.INFECTED_SYMP;
import static uk.co.ramp.people.VirusStatus.RECOVERED;
import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;

public class SingleCaseIsolationPolicyTest {
    private DistributionSampler distributionSampler;

    private final Distribution flatZeroPercent = ImmutableDistribution.builder()
            .mean(0)
            .max(0)
            .type(FLAT)
            .build();

    private final Distribution flatHundredPercent = ImmutableDistribution.builder()
            .mean(100)
            .max(100)
            .type(FLAT)
            .build();

    private final Distribution flatOneDay = ImmutableDistribution.builder()
            .mean(1)
            .max(1)
            .type(FLAT)
            .build();

    private final IsolationProperty zeroIsolationProperty = ImmutableIsolationProperty.builder()
            .isolationProbabilityDistribution(flatZeroPercent)
            .isolationTimeDistribution(flatOneDay)
            .priority(0)
            .build();

    private final VirusStatusIsolationProperty infectedSymptomaticHundredPercentIsolationProperty = ImmutableVirusStatusIsolationProperty.builder()
            .virusStatus(INFECTED_SYMP)
            .isolationProperty(ImmutableIsolationProperty.builder()
                    .priority(0)
                    .isolationProbabilityDistribution(flatHundredPercent)
                    .isolationTimeDistribution(flatOneDay)
                    .build())
            .build();

    private final AlertStatusIsolationProperty testedPositiveHundredPercentIsolationProperty = ImmutableAlertStatusIsolationProperty.builder()
            .alertStatus(TESTED_POSITIVE)
            .isolationProperty(ImmutableIsolationProperty.builder()
                    .priority(0)
                    .isolationProbabilityDistribution(flatHundredPercent)
                    .isolationTimeDistribution(flatOneDay)
                    .build())
            .build();

    private final AlertStatusIsolationProperty alertedHundredPercentIsolationProperty = ImmutableAlertStatusIsolationProperty.builder()
            .alertStatus(AlertStatus.ALERTED)
            .isolationProperty(ImmutableIsolationProperty.builder()
                    .priority(0)
                    .isolationProbabilityDistribution(flatHundredPercent)
                    .isolationTimeDistribution(flatOneDay)
                    .build())
            .build();

    private final ProportionInfectedIsolationProperty twentyPercentInfectedHundredPercentIsolationProperty = ImmutableProportionInfectedIsolationProperty.builder()
            .proportionInfected(MinMax.of(20, 100))
            .isolationProperty(ImmutableIsolationProperty.builder()
                    .priority(0)
                    .isolationProbabilityDistribution(flatHundredPercent)
                    .build())
            .build();

    private final ProportionInfectedIsolationProperty twentyPercentInfectedZeroPercentIsolationProperty = ImmutableProportionInfectedIsolationProperty.builder()
            .proportionInfected(MinMax.of(20, 100))
            .isolationProperty(ImmutableIsolationProperty.builder()
                    .priority(0)
                    .isolationProbabilityDistribution(flatZeroPercent)
                    .build())
            .build();

    private final int currentTime = 0;
    private final int id = 0;
    private final double compliance = 1.0;
    private final double proportionOfPopulationInfected = 0.0;

    private SingleCaseIsolationPolicy isolationPolicy;

    @Before
    public void setUp() {
        IsolationProperties isolationProperties = ImmutableIsolationProperties.builder()
                .defaultPolicy(zeroIsolationProperty)
                .individualIsolationPolicies(ImmutableIndividualIsolationPolicies.builder()
                        .putVirusStatusPolicies("virusPolicyName1", infectedSymptomaticHundredPercentIsolationProperty)
                        .putAlertStatusPolicies("alertPolicyName1", testedPositiveHundredPercentIsolationProperty)
                        .putAlertStatusPolicies("alertPolicyName2", alertedHundredPercentIsolationProperty)
                        .build())
                .globalIsolationPolicies(Map.of("globalPolicyName1", twentyPercentInfectedHundredPercentIsolationProperty))
                .isolationProbabilityDistributionThreshold(50)
                .build();

        this.distributionSampler = mock(DistributionSampler.class);
        when(this.distributionSampler.getDistributionValue(any(Distribution.class))).thenAnswer(invocation -> (int) invocation.getArgument(0, Distribution.class).mean());
        when(this.distributionSampler.uniformBetweenZeroAndOne()).thenReturn(0.5);
        when(this.distributionSampler.uniformInteger(anyInt())).thenAnswer(invocation -> invocation.getArgument(0, int.class) / 2);

        this.isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);
    }

    @Test
    public void testShouldIsolate_UsesIndividualPolicy_BothInfectedAndTestedPositive() {
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, TESTED_POSITIVE, compliance, proportionOfPopulationInfected, currentTime)).isTrue();
    }

    @Test
    public void testShouldIsolate_UsesIndividualPolicy_BothInfectedAndOneTestedPositive() {
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, TESTED_POSITIVE, compliance, proportionOfPopulationInfected, currentTime)).isTrue();
    }

    @Test
    public void testShouldIsolate_UsesIndividualPolicy_BothInfectedAndNoneTestedPositive() {
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED, NONE, compliance, proportionOfPopulationInfected, currentTime)).isFalse();
    }

    @Test
    public void testShouldIsolate_UsesIndividualPolicy_BothExposedInfectiousAndNoneAlerted() {
        assertThat(isolationPolicy.isIndividualInIsolation(id, EXPOSED_2, NONE, compliance, proportionOfPopulationInfected, currentTime)).isFalse();
    }

    @Test
    public void testShouldIsolate_UsesIndividualPolicy_BothExposedInfectiousAndBothAlerted() {
        assertThat(isolationPolicy.isIndividualInIsolation(id, EXPOSED_2, ALERTED, compliance, proportionOfPopulationInfected, currentTime)).isTrue();
    }

    @Test
    public void testShouldIsolate_UsesIndividualPolicy_BothExposedUninfectiousAndNoneAlerted() {
        assertThat(isolationPolicy.isIndividualInIsolation(id, EXPOSED, NONE, compliance, proportionOfPopulationInfected, currentTime)).isFalse();
    }

    @Test
    public void testShouldIsolate_UsesIndividualPolicy_BothExposedUninfectiousAndBothAlerted() {
        assertThat(isolationPolicy.isIndividualInIsolation(id, EXPOSED, ALERTED, compliance, proportionOfPopulationInfected, currentTime)).isTrue();
    }

    @Test
    public void testShouldIsolate_UsesIndividualPolicy_BothExposedSuceptibleAndBothAlerted() {
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, ALERTED, compliance, proportionOfPopulationInfected, currentTime)).isTrue();
    }

    @Test
    public void testShouldIsolate_UsesIndividualPolicy_BothExposedSuceptibleAndNoneAlerted() {
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, NONE, compliance, proportionOfPopulationInfected, currentTime)).isFalse();
    }

    @Test
    public void testShouldIsolate_UsesGlobalPolicy_ProportionInfectedAboveThreshold() {
        var proportionOfPopulationInfected = 0.25;
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, NONE, compliance, proportionOfPopulationInfected, currentTime)).isTrue();
    }

    @Test
    public void testShouldIsolate_UsesGlobalPolicy_ProportionInfectedBelowThreshold() {
        var proportionOfPopulationInfected = 0.15;
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, NONE, compliance, proportionOfPopulationInfected, currentTime)).isFalse();
    }

    @Test
    public void testShouldIsolate_ConflictingPolicies_UsesIndividualPolicyWithHigherPriority() {
        IsolationProperties isolationProperties = ImmutableIsolationProperties.builder()
                .individualIsolationPolicies(ImmutableIndividualIsolationPolicies.builder()
                        .putVirusStatusPolicies("virusPolicyName1", ImmutableVirusStatusIsolationProperty.builder()
                                .virusStatus(INFECTED_SYMP)
                                .isolationProperty(ImmutableIsolationProperty.builder()
                                        .priority(1)
                                        .isolationProbabilityDistribution(flatHundredPercent)
                                        .isolationTimeDistribution(flatOneDay)
                                        .build())
                                .build())
                        .build())
                .putGlobalIsolationPolicies("globalPolicyName1", twentyPercentInfectedZeroPercentIsolationProperty)
                .defaultPolicy(zeroIsolationProperty)
                .isolationProbabilityDistributionThreshold(50)
                .build();

        var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

        var proportionOfPopulationInfected = 0.25;
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime)).isTrue();
    }

    @Test
    public void testShouldIsolate_ConflictingPolicies_UsesGlobalPolicyWithHigherPriority() {
        IsolationProperties isolationProperties = ImmutableIsolationProperties.builder()
                .individualIsolationPolicies(ImmutableIndividualIsolationPolicies.builder()
                        .putVirusStatusPolicies("virusPolicyName1", infectedSymptomaticHundredPercentIsolationProperty)
                        .build())
                .putGlobalIsolationPolicies("globalPolicyName1", ImmutableProportionInfectedIsolationProperty.builder()
                        .proportionInfected(MinMax.of(20, 100))
                        .isolationProperty(ImmutableIsolationProperty.builder()
                                .priority(1)
                                .isolationProbabilityDistribution(flatZeroPercent)
                                .build())
                        .build())
                .defaultPolicy(zeroIsolationProperty)
                .isolationProbabilityDistributionThreshold(50)
                .build();

        var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

        var proportionOfPopulationInfected = 0.25;
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime)).isFalse();
    }

    @Test
    public void testShouldIsolate_Symptomatic_NotTested_StaysIsolated() {
        VirusStatusIsolationProperty virusStatusIsolationProperty = ImmutableVirusStatusIsolationProperty.builder()
                .virusStatus(INFECTED_SYMP)
                .isolationProperty(ImmutableIsolationProperty.builder()
                        .priority(0)
                        .isolationProbabilityDistribution(flatHundredPercent)
                        .isolationTimeDistribution(ImmutableDistribution.builder()
                                .type(FLAT)
                                .mean(3)
                                .max(3)
                                .build())
                        .build())
                .build();

        IsolationProperties isolationProperties = ImmutableIsolationProperties.builder()
                .individualIsolationPolicies(ImmutableIndividualIsolationPolicies.builder()
                        .putVirusStatusPolicies("virusPolicyName1", virusStatusIsolationProperty)
                        .build())
                .putGlobalIsolationPolicies("globalPolicyName1", twentyPercentInfectedZeroPercentIsolationProperty)
                .defaultPolicy(zeroIsolationProperty)
                .isolationProbabilityDistributionThreshold(50)
                .build();

        var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 1)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 2)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 3)).isFalse();
    }

    @Test
    public void testShouldIsolate_Symptomatic_TestedPositive_StaysIsolated() {
        VirusStatusIsolationProperty virusStatusIsolationProperty = ImmutableVirusStatusIsolationProperty.builder()
                .virusStatus(INFECTED_SYMP)
                .isolationProperty(ImmutableIsolationProperty.builder()
                        .priority(0)
                        .isolationProbabilityDistribution(flatHundredPercent)
                        .isolationTimeDistribution(ImmutableDistribution.builder()
                                .type(FLAT)
                                .mean(3)
                                .max(3)
                                .build())
                        .build())
                .build();

        IsolationProperties isolationProperties = ImmutableIsolationProperties.builder()
                .individualIsolationPolicies(ImmutableIndividualIsolationPolicies.builder()
                        .putVirusStatusPolicies("virusPolicyName1", virusStatusIsolationProperty)
                        .build())
                .putGlobalIsolationPolicies("globalPolicyName1", twentyPercentInfectedZeroPercentIsolationProperty)
                .defaultPolicy(zeroIsolationProperty)
                .isolationProbabilityDistributionThreshold(50)
                .build();

        var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, AWAITING_RESULT, compliance, proportionOfPopulationInfected, currentTime)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, AWAITING_RESULT, compliance, proportionOfPopulationInfected, currentTime + 1)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, TESTED_POSITIVE, compliance, proportionOfPopulationInfected, currentTime + 2)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, TESTED_POSITIVE, compliance, proportionOfPopulationInfected, currentTime + 3)).isFalse();
    }

    @Test
    public void testShouldIsolate_Symptomatic_TestedNegative_StopsIsolating() {
        VirusStatusIsolationProperty virusStatusIsolationProperty = ImmutableVirusStatusIsolationProperty.builder()
                .virusStatus(INFECTED_SYMP)
                .isolationProperty(ImmutableIsolationProperty.builder()
                        .priority(0)
                        .isolationProbabilityDistribution(flatHundredPercent)
                        .isolationTimeDistribution(ImmutableDistribution.builder()
                                .type(FLAT)
                                .mean(3)
                                .max(3)
                                .build())
                        .build())
                .build();

        IsolationProperties isolationProperties = ImmutableIsolationProperties.builder()
                .individualIsolationPolicies(ImmutableIndividualIsolationPolicies.builder()
                        .putVirusStatusPolicies("virusPolicyName1", virusStatusIsolationProperty)
                        .build())
                .putGlobalIsolationPolicies("globalPolicyName1", twentyPercentInfectedZeroPercentIsolationProperty)
                .defaultPolicy(zeroIsolationProperty)
                .isolationProbabilityDistributionThreshold(50)
                .build();

        var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, AWAITING_RESULT, compliance, proportionOfPopulationInfected, currentTime)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, AWAITING_RESULT, compliance, proportionOfPopulationInfected, currentTime + 1)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, RECOVERED, NONE, compliance, proportionOfPopulationInfected, currentTime + 2)).isFalse();
        assertThat(isolationPolicy.isIndividualInIsolation(id, RECOVERED, NONE, compliance, proportionOfPopulationInfected, currentTime + 3)).isFalse();
    }

    @Test
    public void testShouldIsolate_InfectedAgain_SecondIsolation() {
        VirusStatusIsolationProperty virusStatusIsolationProperty = ImmutableVirusStatusIsolationProperty.builder()
                .virusStatus(INFECTED_SYMP)
                .isolationProperty(ImmutableIsolationProperty.builder()
                        .priority(0)
                        .isolationProbabilityDistribution(flatHundredPercent)
                        .isolationTimeDistribution(ImmutableDistribution.builder()
                                .type(FLAT)
                                .mean(3)
                                .max(3)
                                .build())
                        .build())
                .build();

        IsolationProperties isolationProperties = ImmutableIsolationProperties.builder()
                .individualIsolationPolicies(ImmutableIndividualIsolationPolicies.builder()
                        .putVirusStatusPolicies("virusPolicyName1", virusStatusIsolationProperty)
                        .build())
                .putGlobalIsolationPolicies("globalPolicyName1", twentyPercentInfectedZeroPercentIsolationProperty)
                .defaultPolicy(zeroIsolationProperty)
                .isolationProbabilityDistributionThreshold(50)
                .build();

        var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, AWAITING_RESULT, compliance, proportionOfPopulationInfected, currentTime)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, AWAITING_RESULT, compliance, proportionOfPopulationInfected, currentTime + 1)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, RECOVERED, NONE, compliance, proportionOfPopulationInfected, currentTime + 2)).isFalse();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 3)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 4)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 5)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 6)).isFalse();
    }

    @Test
    public void testShouldIsolate_AlertTestNegativeThenInfected_SecondIsolation() {
        VirusStatusIsolationProperty virusStatusIsolationProperty = ImmutableVirusStatusIsolationProperty.builder()
                .virusStatus(INFECTED_SYMP)
                .isolationProperty(ImmutableIsolationProperty.builder()
                        .priority(0)
                        .isolationProbabilityDistribution(flatHundredPercent)
                        .isolationTimeDistribution(ImmutableDistribution.builder()
                                .type(FLAT)
                                .mean(3)
                                .max(3)
                                .build())
                        .build())
                .build();

        AlertStatusIsolationProperty alertStatusIsolationProperty = ImmutableAlertStatusIsolationProperty.builder()
                .alertStatus(AlertStatus.ALERTED)
                .isolationProperty(ImmutableIsolationProperty.builder()
                        .priority(0)
                        .isolationProbabilityDistribution(flatHundredPercent)
                        .isolationTimeDistribution(ImmutableDistribution.builder()
                                .type(FLAT)
                                .mean(3)
                                .max(3)
                                .build())
                        .build())
                .build();

        IsolationProperties isolationProperties = ImmutableIsolationProperties.builder()
                .individualIsolationPolicies(ImmutableIndividualIsolationPolicies.builder()
                        .putVirusStatusPolicies("virusPolicyName1", virusStatusIsolationProperty)
                        .putAlertStatusPolicies("alertPolicyName1", alertStatusIsolationProperty)
                        .build())
                .putGlobalIsolationPolicies("globalPolicyName1", twentyPercentInfectedZeroPercentIsolationProperty)
                .defaultPolicy(zeroIsolationProperty)
                .isolationProbabilityDistributionThreshold(50)
                .build();

        var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, ALERTED, compliance, proportionOfPopulationInfected, currentTime)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, ALERTED, compliance, proportionOfPopulationInfected, currentTime + 1)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, NONE, compliance, proportionOfPopulationInfected, currentTime + 2)).isFalse();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 3)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 4)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 5)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 6)).isFalse();
    }

    @Test
    public void testShouldIsolate_IsNotCompliant() {
        var compliance = 0.25;
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, ALERTED, compliance, proportionOfPopulationInfected, currentTime)).isFalse();
    }

    @Test
    public void testShouldIsolate_UseGlobalPolicy_IndividualPolicyExpired() {
        IsolationProperties isolationProperties = ImmutableIsolationProperties.builder()
                .individualIsolationPolicies(ImmutableIndividualIsolationPolicies.builder()
                        .putVirusStatusPolicies("virusPolicyName1", infectedSymptomaticHundredPercentIsolationProperty)
                        .build())
                .putGlobalIsolationPolicies("globalPolicyName1", ImmutableProportionInfectedIsolationProperty.builder()
                        .proportionInfected(MinMax.of(20, 100))
                        .isolationProperty(ImmutableIsolationProperty.builder()
                                .isolationProbabilityDistribution(flatHundredPercent)
                                .priority(0)
                                .build())
                        .build())
                .defaultPolicy(zeroIsolationProperty)
                .isolationProbabilityDistributionThreshold(50)
                .build();

        var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);
        var proportionOfPopulationInfectious = 0.0;
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfectious, currentTime)).isTrue();
        proportionOfPopulationInfectious = 0.3;
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfectious, currentTime + 1)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfectious, currentTime + 2)).isTrue();
        proportionOfPopulationInfectious = 0.0;
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfectious, currentTime + 3)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfectious, currentTime + 4)).isFalse();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfectious, currentTime + 5)).isFalse();
    }

    @Test
    public void testShouldIsolate_UseGlobalPolicyWithNoTimeConstraintAfterFewTimeSteps() {
        var proportionOfPopulationInfectious = 0.0;
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, NONE, compliance, proportionOfPopulationInfectious, currentTime)).isFalse();
        proportionOfPopulationInfectious = 0.3;
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, NONE, compliance, proportionOfPopulationInfectious, currentTime + 1)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, NONE, compliance, proportionOfPopulationInfectious, currentTime + 2)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, NONE, compliance, proportionOfPopulationInfectious, currentTime + 3)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, NONE, compliance, proportionOfPopulationInfectious, currentTime + 4)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, NONE, compliance, proportionOfPopulationInfectious, currentTime + 5)).isTrue();
    }
}