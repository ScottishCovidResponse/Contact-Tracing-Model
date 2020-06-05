package uk.co.ramp.policy;

import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.distribution.Distribution;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.distribution.ImmutableDistribution;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.utilities.MinMax;

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

    private final IsolationProperty defaultZeroIsolationProperty = ImmutableIsolationProperty.builder()
            .id("zeroIsolationPolicy")
            .isolationProbabilityDistribution(flatZeroPercent)
            .priority(-1)
            .build();

    private final VirusStatusIsolationProperty infectedSymptomaticHundredPercentIsolationProperty = ImmutableVirusStatusIsolationProperty.builder()
            .virusStatus(INFECTED_SYMP)
            .isolationProperty(ImmutableIsolationProperty.builder()
                    .id("virusPolicyName1")
                    .priority(1)
                    .isolationProbabilityDistribution(flatHundredPercent)
                    .isolationTimeDistribution(flatOneDay)
                    .build())
            .build();

    private final AlertStatusIsolationProperty testedPositiveHundredPercentIsolationProperty = ImmutableAlertStatusIsolationProperty.builder()
            .alertStatus(TESTED_POSITIVE)
            .isolationProperty(ImmutableIsolationProperty.builder()
                    .id("alertedPolicyName1")
                    .priority(0)
                    .isolationProbabilityDistribution(flatHundredPercent)
                    .isolationTimeDistribution(flatOneDay)
                    .build())
            .build();

    private final AlertStatusIsolationProperty alertedHundredPercentIsolationProperty = ImmutableAlertStatusIsolationProperty.builder()
            .alertStatus(AlertStatus.ALERTED)
            .isolationProperty(ImmutableIsolationProperty.builder()
                    .id("alertedPolicyName2")
                    .priority(0)
                    .isolationProbabilityDistribution(flatHundredPercent)
                    .isolationTimeDistribution(flatOneDay)
                    .build())
            .build();

    private final ProportionInfectedIsolationProperty twentyPercentInfectedHundredPercentIsolationProperty = ImmutableProportionInfectedIsolationProperty.builder()
            .proportionInfected(MinMax.of(20, 100))
            .isolationProperty(ImmutableIsolationProperty.builder()
                    .id("globalPolicyName1")
                    .priority(0)
                    .isolationProbabilityDistribution(flatHundredPercent)
                    .build())
            .build();

    private final ProportionInfectedIsolationProperty twentyPercentInfectedZeroPercentIsolationProperty = ImmutableProportionInfectedIsolationProperty.builder()
            .proportionInfected(MinMax.of(20, 100))
            .isolationProperty(ImmutableIsolationProperty.builder()
                    .id("globalPolicyName2")
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
                .defaultPolicy(defaultZeroIsolationProperty)
                .addVirusStatusPolicies(infectedSymptomaticHundredPercentIsolationProperty)
                .addAlertStatusPolicies(testedPositiveHundredPercentIsolationProperty)
                .addAlertStatusPolicies(alertedHundredPercentIsolationProperty)
                .addGlobalIsolationPolicies(twentyPercentInfectedHundredPercentIsolationProperty)
                .isolationProbabilityDistributionThreshold(50)
                .build();

        this.distributionSampler = mock(DistributionSampler.class);
        when(this.distributionSampler.getDistributionValue(any(Distribution.class))).thenAnswer(invocation -> (int) invocation.getArgument(0, Distribution.class).mean());
        when(this.distributionSampler.uniformBetweenZeroAndOne()).thenReturn(0.5);
        when(this.distributionSampler.uniformInteger(anyInt())).thenAnswer(invocation -> invocation.getArgument(0, int.class) / 2);

        this.isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);
    }

    @Test
    public void testShouldIsolate_UsesIndividualPolicy_InfectedAndTestedPositive() {
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, TESTED_POSITIVE, compliance, proportionOfPopulationInfected, currentTime)).isTrue();
    }

    @Test
    public void testShouldIsolate_UsesIndividualPolicy_InfectedAndNoneTestedPositive() {
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED, NONE, compliance, proportionOfPopulationInfected, currentTime)).isFalse();
    }

    @Test
    public void testShouldIsolate_UsesIndividualPolicy_ExposedInfectiousAndNoneAlerted() {
        assertThat(isolationPolicy.isIndividualInIsolation(id, EXPOSED_2, NONE, compliance, proportionOfPopulationInfected, currentTime)).isFalse();
    }

    @Test
    public void testShouldIsolate_UsesIndividualPolicy_ExposedInfectiousAndAlerted() {
        assertThat(isolationPolicy.isIndividualInIsolation(id, EXPOSED_2, ALERTED, compliance, proportionOfPopulationInfected, currentTime)).isTrue();
    }

    @Test
    public void testShouldIsolate_UsesIndividualPolicy_ExposedUninfectiousAndNoneAlerted() {
        assertThat(isolationPolicy.isIndividualInIsolation(id, EXPOSED, NONE, compliance, proportionOfPopulationInfected, currentTime)).isFalse();
    }

    @Test
    public void testShouldIsolate_UsesIndividualPolicy_ExposedUninfectiousAndBothAlerted() {
        assertThat(isolationPolicy.isIndividualInIsolation(id, EXPOSED, ALERTED, compliance, proportionOfPopulationInfected, currentTime)).isTrue();
    }

    @Test
    public void testShouldIsolate_UsesIndividualPolicy_ExposedSuceptibleAndBothAlerted() {
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, ALERTED, compliance, proportionOfPopulationInfected, currentTime)).isTrue();
    }

    @Test
    public void testShouldIsolate_UsesIndividualPolicy_ExposedSuceptibleAndNoneAlerted() {
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
                .addVirusStatusPolicies(ImmutableVirusStatusIsolationProperty.builder()
                        .virusStatus(INFECTED_SYMP)
                        .isolationProperty(ImmutableIsolationProperty.builder()
                                .id("virusPolicyName3")
                                .priority(1)
                                .isolationProbabilityDistribution(flatHundredPercent)
                                .isolationTimeDistribution(flatOneDay)
                                .build())
                        .build())
                .addGlobalIsolationPolicies(twentyPercentInfectedZeroPercentIsolationProperty)
                .defaultPolicy(defaultZeroIsolationProperty)
                .isolationProbabilityDistributionThreshold(50)
                .build();

        var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

        var proportionOfPopulationInfected = 0.25;
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime)).isTrue();
    }

    @Test
    public void testShouldIsolate_ConflictingPolicies_UsesGlobalPolicyWithHigherPriority() {
        IsolationProperties isolationProperties = ImmutableIsolationProperties.builder()
                .addVirusStatusPolicies(infectedSymptomaticHundredPercentIsolationProperty)
                .addGlobalIsolationPolicies(ImmutableProportionInfectedIsolationProperty.builder()
                        .proportionInfected(MinMax.of(20, 100))
                        .isolationProperty(ImmutableIsolationProperty.builder()
                                .id("globalPolicyName3")
                                .priority(5)
                                .isolationProbabilityDistribution(flatZeroPercent)
                                .build())
                        .build())
                .defaultPolicy(defaultZeroIsolationProperty)
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
                        .id("virusPolicyName4")
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
                        .addVirusStatusPolicies(virusStatusIsolationProperty)
                .addGlobalIsolationPolicies(twentyPercentInfectedZeroPercentIsolationProperty)
                .defaultPolicy(defaultZeroIsolationProperty)
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
                        .id("virusPolicyName4")
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
                .addVirusStatusPolicies(virusStatusIsolationProperty)
                .addGlobalIsolationPolicies(twentyPercentInfectedZeroPercentIsolationProperty)
                .defaultPolicy(defaultZeroIsolationProperty)
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
                        .id("virusPolicyName4")
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
                .addVirusStatusPolicies(virusStatusIsolationProperty)
                .addGlobalIsolationPolicies(twentyPercentInfectedZeroPercentIsolationProperty)
                .defaultPolicy(defaultZeroIsolationProperty)
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
                        .id("virusPolicyName4")
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
                .addVirusStatusPolicies(virusStatusIsolationProperty)
                .addGlobalIsolationPolicies(twentyPercentInfectedZeroPercentIsolationProperty)
                .defaultPolicy(defaultZeroIsolationProperty)
                .isolationProbabilityDistributionThreshold(50)
                .build();

        var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, AWAITING_RESULT, compliance, proportionOfPopulationInfected, currentTime)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, AWAITING_RESULT, compliance, proportionOfPopulationInfected, currentTime + 1)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, AWAITING_RESULT, compliance, proportionOfPopulationInfected, currentTime + 2)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, AWAITING_RESULT, compliance, proportionOfPopulationInfected, currentTime + 3)).isFalse();
        assertThat(isolationPolicy.isIndividualInIsolation(id, RECOVERED, NONE, compliance, proportionOfPopulationInfected, currentTime + 4)).isFalse();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 5)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 6)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 7)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 8)).isFalse();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 9)).isFalse();
    }

    @Test
    public void testShouldIsolate_InfectedAgain_WithinExpiry_SecondIsolation() {
        VirusStatusIsolationProperty virusStatusIsolationProperty = ImmutableVirusStatusIsolationProperty.builder()
                .virusStatus(INFECTED_SYMP)
                .isolationProperty(ImmutableIsolationProperty.builder()
                        .id("virusPolicyName4")
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
                .addVirusStatusPolicies(virusStatusIsolationProperty)
                .addGlobalIsolationPolicies(twentyPercentInfectedZeroPercentIsolationProperty)
                .defaultPolicy(defaultZeroIsolationProperty)
                .isolationProbabilityDistributionThreshold(50)
                .build();

        var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, AWAITING_RESULT, compliance, proportionOfPopulationInfected, currentTime)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, RECOVERED, NONE, compliance, proportionOfPopulationInfected, currentTime + 1)).isFalse();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 2)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 3)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 4)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 5)).isFalse();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 6)).isFalse();
    }

    @Test
    public void testShouldIsolate_AlertTestNegativeThenInfected_SecondIsolation() {
        VirusStatusIsolationProperty virusStatusIsolationProperty = ImmutableVirusStatusIsolationProperty.builder()
                .virusStatus(INFECTED_SYMP)
                .isolationProperty(ImmutableIsolationProperty.builder()
                        .id("virusPolicyName4")
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
                        .id("alertPolicyName4")
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
                .addVirusStatusPolicies(virusStatusIsolationProperty)
                .addAlertStatusPolicies(alertStatusIsolationProperty)
                .addGlobalIsolationPolicies(twentyPercentInfectedZeroPercentIsolationProperty)
                .defaultPolicy(defaultZeroIsolationProperty)
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
    public void testShouldIsolate_AlertTestNegativeThenInfected_SecondIsolation_Short_Cycle() {
        VirusStatusIsolationProperty virusStatusIsolationProperty = ImmutableVirusStatusIsolationProperty.builder()
                .virusStatus(INFECTED_SYMP)
                .isolationProperty(ImmutableIsolationProperty.builder()
                        .id("virusPolicyName4")
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
                        .id("alertPolicyName4")
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
                .addVirusStatusPolicies(virusStatusIsolationProperty)
                .addAlertStatusPolicies(alertStatusIsolationProperty)
                .addGlobalIsolationPolicies(twentyPercentInfectedZeroPercentIsolationProperty)
                .defaultPolicy(defaultZeroIsolationProperty)
                .isolationProbabilityDistributionThreshold(50)
                .build();

        var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, ALERTED, compliance, proportionOfPopulationInfected, currentTime)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, NONE, compliance, proportionOfPopulationInfected, currentTime + 1)).isFalse();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 2)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 3)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 4)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfected, currentTime + 5)).isFalse();
    }

    @Test
    public void testShouldIsolate_IsNotCompliant() {
        var compliance = 0.25;
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, ALERTED, compliance, proportionOfPopulationInfected, currentTime)).isFalse();
    }

    @Test
    public void testShouldIsolate_UseGlobalPolicy_IndividualPolicyExpired() {
        IsolationProperties isolationProperties = ImmutableIsolationProperties.builder()
                .addVirusStatusPolicies(infectedSymptomaticHundredPercentIsolationProperty)
                .addGlobalIsolationPolicies(ImmutableProportionInfectedIsolationProperty.builder()
                        .proportionInfected(MinMax.of(20, 100))
                        .isolationProperty(ImmutableIsolationProperty.builder()
                                .id("globalPolicyName1")
                                .isolationProbabilityDistribution(flatHundredPercent)
                                .priority(2)
                                .build())
                        .build())
                .defaultPolicy(defaultZeroIsolationProperty)
                .isolationProbabilityDistributionThreshold(50)
                .build();

        var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);
        var proportionOfPopulationInfectious = 0.0;
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfectious, currentTime)).isTrue();
        proportionOfPopulationInfectious = 0.3;
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfectious, currentTime + 1)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfectious, currentTime + 2)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfectious, currentTime + 3)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfectious, currentTime + 4)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfectious, currentTime + 5)).isTrue();
        proportionOfPopulationInfectious = 0.0;
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfectious, currentTime + 6)).isFalse();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfectious, currentTime + 7)).isFalse();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfectious, currentTime + 8)).isFalse();
    }

    @Test
    public void testShouldIsolate_UseGlobalPolicyWithNoTimeConstraintAfterFewTimeSteps() {
        var proportionOfPopulationInfectious = 0.0;
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, NONE, compliance, proportionOfPopulationInfectious, currentTime)).isFalse();
        proportionOfPopulationInfectious = 0.3;
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, NONE, compliance, proportionOfPopulationInfectious, currentTime + 1)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, NONE, compliance, proportionOfPopulationInfectious, currentTime + 2)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, INFECTED_SYMP, NONE, compliance, proportionOfPopulationInfectious, currentTime + 3)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, NONE, compliance, proportionOfPopulationInfectious, currentTime + 4)).isTrue();
        assertThat(isolationPolicy.isIndividualInIsolation(id, SUSCEPTIBLE, NONE, compliance, proportionOfPopulationInfectious, currentTime + 5)).isTrue();
    }
}