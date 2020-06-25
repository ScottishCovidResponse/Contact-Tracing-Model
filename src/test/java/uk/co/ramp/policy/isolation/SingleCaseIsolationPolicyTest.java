package uk.co.ramp.policy.isolation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.AdditionalAnswers.returnsElementsOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.ramp.distribution.ProgressionDistribution.FLAT;
import static uk.co.ramp.distribution.ProgressionDistribution.LINEAR;
import static uk.co.ramp.people.AlertStatus.*;
import static uk.co.ramp.people.VirusStatus.*;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.distribution.Distribution;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.distribution.ImmutableDistribution;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.utilities.MinMax;

public class SingleCaseIsolationPolicyTest {
  private DistributionSampler distributionSampler;

  private final Distribution flatZeroPercent =
      ImmutableDistribution.builder().mean(0).max(0).type(FLAT).build();

  private final Distribution flatHundredPercent =
      ImmutableDistribution.builder().mean(100).max(100).type(FLAT).build();

  private final Distribution flatOneDay =
      ImmutableDistribution.builder().mean(1).max(1).type(FLAT).build();

  private final IsolationProperty defaultZeroIsolationProperty =
      ImmutableIsolationProperty.builder()
          .id("zeroIsolationPolicy")
          .isolationProbabilityDistribution(flatZeroPercent)
          .priority(-1)
          .build();

  private final VirusStatusIsolationProperty infectedSymptomaticHundredPercentIsolationProperty =
      ImmutableVirusStatusIsolationProperty.builder()
          .virusStatus(SYMPTOMATIC)
          .isolationProperty(
              ImmutableIsolationProperty.builder()
                  .id("virusPolicyName1")
                  .priority(1)
                  .isolationProbabilityDistribution(flatHundredPercent)
                  .isolationTimeDistribution(flatOneDay)
                  .build())
          .build();

  private final VirusStatusIsolationProperty recoveredZeroPercentIsolationProperty =
      ImmutableVirusStatusIsolationProperty.builder()
          .virusStatus(RECOVERED)
          .isolationProperty(
              ImmutableIsolationProperty.builder()
                  .id("Recovered Policy")
                  .priority(1)
                  .isolationProbabilityDistribution(flatZeroPercent)
                  .overrideComplianceAndForcePolicy(true)
                  .build())
          .build();

  private final VirusStatusIsolationProperty deadZeroPercentIsolationProperty =
      ImmutableVirusStatusIsolationProperty.builder()
          .virusStatus(DEAD)
          .isolationProperty(
              ImmutableIsolationProperty.builder()
                  .id("Dead Policy")
                  .priority(1)
                  .isolationProbabilityDistribution(flatHundredPercent)
                  .overrideComplianceAndForcePolicy(true)
                  .build())
          .build();

  private final AlertStatusIsolationProperty testedPositiveHundredPercentIsolationProperty =
      ImmutableAlertStatusIsolationProperty.builder()
          .alertStatus(TESTED_POSITIVE)
          .isolationProperty(
              ImmutableIsolationProperty.builder()
                  .id("alertedPolicyName1")
                  .priority(0)
                  .isolationProbabilityDistribution(flatHundredPercent)
                  .isolationTimeDistribution(flatOneDay)
                  .build())
          .build();

  private final AlertStatusIsolationProperty alertedHundredPercentIsolationProperty =
      ImmutableAlertStatusIsolationProperty.builder()
          .alertStatus(AlertStatus.ALERTED)
          .isolationProperty(
              ImmutableIsolationProperty.builder()
                  .id("alertedPolicyName2")
                  .priority(0)
                  .isolationProbabilityDistribution(flatHundredPercent)
                  .isolationTimeDistribution(flatOneDay)
                  .build())
          .build();

  private final ProportionInfectedIsolationProperty
      twentyPercentInfectedHundredPercentIsolationProperty =
          ImmutableProportionInfectedIsolationProperty.builder()
              .proportionInfected(MinMax.of(20, 100))
              .isolationProperty(
                  ImmutableIsolationProperty.builder()
                      .id("globalPolicyName1")
                      .priority(0)
                      .isolationProbabilityDistribution(flatHundredPercent)
                      .build())
              .build();

  private final ProportionInfectedIsolationProperty
      twentyPercentInfectedZeroPercentIsolationProperty =
          ImmutableProportionInfectedIsolationProperty.builder()
              .proportionInfected(MinMax.of(20, 100))
              .isolationProperty(
                  ImmutableIsolationProperty.builder()
                      .id("globalPolicyName2")
                      .priority(0)
                      .isolationProbabilityDistribution(flatZeroPercent)
                      .build())
              .build();

  private final Distribution flatThreeDays =
      ImmutableDistribution.builder().mean(3).max(3).type(FLAT).build();

  private final Distribution thresholdLinearDistribution =
      ImmutableDistribution.builder().mean(50).max(100).type(LINEAR).build();

  private final int currentTime = 0;
  private final int exposedTime = 0;
  private final int id = 0;
  private final double compliance = 1.0;
  private final double proportionOfPopulationInfected = 0.0;

  private SingleCaseIsolationPolicy isolationPolicy;

  @Before
  public void setUp() {
    IsolationProperties isolationProperties =
        ImmutableIsolationProperties.builder()
            .defaultPolicy(defaultZeroIsolationProperty)
            .addVirusStatusPolicies(infectedSymptomaticHundredPercentIsolationProperty)
            .addVirusStatusPolicies(recoveredZeroPercentIsolationProperty)
            .addVirusStatusPolicies(deadZeroPercentIsolationProperty)
            .addAlertStatusPolicies(testedPositiveHundredPercentIsolationProperty)
            .addAlertStatusPolicies(alertedHundredPercentIsolationProperty)
            .addGlobalIsolationPolicies(twentyPercentInfectedHundredPercentIsolationProperty)
            .isolationProbabilityDistributionThreshold(thresholdLinearDistribution)
            .build();

    this.distributionSampler = mock(DistributionSampler.class);
    when(this.distributionSampler.getDistributionValue(any(Distribution.class)))
        .thenAnswer(invocation -> (int) invocation.getArgument(0, Distribution.class).mean());
    when(this.distributionSampler.uniformBetweenZeroAndOne()).thenReturn(0.5);
    when(this.distributionSampler.uniformInteger(anyInt()))
        .thenAnswer(invocation -> invocation.getArgument(0, int.class) / 2);

    this.isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);
  }

  @Test
  public void testShouldIsolate_UsesIndividualPolicy_InfectedAndTestedPositive() {
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                TESTED_POSITIVE,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isTrue();
  }

  @Test
  public void testShouldIsolate_UsesIndividualPolicy_InfectedAndNoneTestedPositive() {
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                PRESYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isFalse();
  }

  @Test
  public void testShouldIsolate_UsesIndividualPolicy_ExposedInfectiousAndNoneAlerted() {
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                PRESYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isFalse();
  }

  @Test
  public void testShouldIsolate_UsesIndividualPolicy_ExposedInfectiousAndAlerted() {
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                PRESYMPTOMATIC,
                ALERTED,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isTrue();
  }

  @Test
  public void testShouldIsolate_UsesIndividualPolicy_ExposedUninfectiousAndNoneAlerted() {
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                EXPOSED,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isFalse();
  }

  @Test
  public void testShouldIsolate_UsesIndividualPolicy_ExposedUninfectiousAndBothAlerted() {
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                EXPOSED,
                ALERTED,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isTrue();
  }

  @Test
  public void testShouldIsolate_UsesIndividualPolicy_ExposedSuceptibleAndBothAlerted() {
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                ALERTED,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isTrue();
  }

  @Test
  public void testShouldIsolate_UsesIndividualPolicy_ExposedSuceptibleAndNoneAlerted() {
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isFalse();
  }

  @Test
  public void testShouldIsolate_UsesGlobalPolicy_ProportionInfectedAboveThreshold() {
    var proportionOfPopulationInfected = 0.25;
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isTrue();
  }

  @Test
  public void testShouldIsolate_UsesGlobalPolicy_ProportionInfectedBelowThreshold() {
    var proportionOfPopulationInfected = 0.15;
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isFalse();
  }

  @Test
  public void testShouldIsolate_RecoveredAndAlerted() {
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                RECOVERED,
                ALERTED,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isFalse();
  }

  @Test
  public void testShouldIsolate_RecoveredAndAlerted_LowCompliance() {
    var compliance = 0.1;
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                RECOVERED,
                ALERTED,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isFalse();
  }

  @Test
  public void testShouldIsolate_DeadAndAlerted() {
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                DEAD,
                ALERTED,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isTrue();
  }

  @Test
  public void testShouldIsolate_DeadAndAlerted_LowCompliance() {
    var compliance = 0.1;
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                DEAD,
                ALERTED,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isTrue();
  }

  @Test
  public void testShouldIsolate_ConflictingPolicies_UsesIndividualPolicyWithHigherPriority() {
    IsolationProperties isolationProperties =
        ImmutableIsolationProperties.builder()
            .addVirusStatusPolicies(
                ImmutableVirusStatusIsolationProperty.builder()
                    .virusStatus(SYMPTOMATIC)
                    .isolationProperty(
                        ImmutableIsolationProperty.builder()
                            .id("virusPolicyName3")
                            .priority(1)
                            .isolationProbabilityDistribution(flatHundredPercent)
                            .isolationTimeDistribution(flatOneDay)
                            .build())
                    .build())
            .addGlobalIsolationPolicies(twentyPercentInfectedZeroPercentIsolationProperty)
            .defaultPolicy(defaultZeroIsolationProperty)
            .isolationProbabilityDistributionThreshold(thresholdLinearDistribution)
            .build();

    var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

    var proportionOfPopulationInfected = 0.25;
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isTrue();
  }

  @Test
  public void testShouldIsolate_ConflictingPolicies_UsesGlobalPolicyWithHigherPriority() {
    IsolationProperties isolationProperties =
        ImmutableIsolationProperties.builder()
            .addVirusStatusPolicies(infectedSymptomaticHundredPercentIsolationProperty)
            .addGlobalIsolationPolicies(
                ImmutableProportionInfectedIsolationProperty.builder()
                    .proportionInfected(MinMax.of(20, 100))
                    .isolationProperty(
                        ImmutableIsolationProperty.builder()
                            .id("globalPolicyName3")
                            .priority(5)
                            .isolationProbabilityDistribution(flatZeroPercent)
                            .build())
                    .build())
            .defaultPolicy(defaultZeroIsolationProperty)
            .isolationProbabilityDistributionThreshold(thresholdLinearDistribution)
            .build();

    var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

    var proportionOfPopulationInfected = 0.25;
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isFalse();
  }

  @Test
  public void testShouldIsolate_Symptomatic_NotTested_StaysIsolated() {
    VirusStatusIsolationProperty virusStatusIsolationProperty =
        ImmutableVirusStatusIsolationProperty.builder()
            .virusStatus(SYMPTOMATIC)
            .isolationProperty(
                ImmutableIsolationProperty.builder()
                    .id("virusPolicyName4")
                    .priority(0)
                    .isolationProbabilityDistribution(flatHundredPercent)
                    .isolationTimeDistribution(flatThreeDays)
                    .build())
            .build();

    IsolationProperties isolationProperties =
        ImmutableIsolationProperties.builder()
            .addVirusStatusPolicies(virusStatusIsolationProperty)
            .addGlobalIsolationPolicies(twentyPercentInfectedZeroPercentIsolationProperty)
            .defaultPolicy(defaultZeroIsolationProperty)
            .isolationProbabilityDistributionThreshold(thresholdLinearDistribution)
            .build();

    var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 1,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 2,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 3,
                exposedTime))
        .isFalse();
  }

  @Test
  public void testShouldIsolate_Symptomatic_TestedPositive_StaysIsolated() {
    VirusStatusIsolationProperty virusStatusIsolationProperty =
        ImmutableVirusStatusIsolationProperty.builder()
            .virusStatus(SYMPTOMATIC)
            .isolationProperty(
                ImmutableIsolationProperty.builder()
                    .id("virusPolicyName4")
                    .priority(0)
                    .isolationProbabilityDistribution(flatHundredPercent)
                    .isolationTimeDistribution(flatThreeDays)
                    .build())
            .build();

    IsolationProperties isolationProperties =
        ImmutableIsolationProperties.builder()
            .addVirusStatusPolicies(virusStatusIsolationProperty)
            .addGlobalIsolationPolicies(twentyPercentInfectedZeroPercentIsolationProperty)
            .defaultPolicy(defaultZeroIsolationProperty)
            .isolationProbabilityDistributionThreshold(thresholdLinearDistribution)
            .build();

    var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                AWAITING_RESULT,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                AWAITING_RESULT,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 1,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                TESTED_POSITIVE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 2,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                TESTED_POSITIVE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 3,
                exposedTime))
        .isFalse();
  }

  @Test
  public void testShouldIsolate_Symptomatic_TestedNegative_StopsIsolating() {
    VirusStatusIsolationProperty virusStatusIsolationProperty =
        ImmutableVirusStatusIsolationProperty.builder()
            .virusStatus(SYMPTOMATIC)
            .isolationProperty(
                ImmutableIsolationProperty.builder()
                    .id("virusPolicyName4")
                    .priority(0)
                    .isolationProbabilityDistribution(flatHundredPercent)
                    .isolationTimeDistribution(flatThreeDays)
                    .build())
            .build();

    IsolationProperties isolationProperties =
        ImmutableIsolationProperties.builder()
            .addVirusStatusPolicies(virusStatusIsolationProperty)
            .addGlobalIsolationPolicies(twentyPercentInfectedZeroPercentIsolationProperty)
            .defaultPolicy(defaultZeroIsolationProperty)
            .isolationProbabilityDistributionThreshold(thresholdLinearDistribution)
            .build();

    var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                AWAITING_RESULT,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                AWAITING_RESULT,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 1,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                RECOVERED,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 2,
                exposedTime))
        .isFalse();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                RECOVERED,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 3,
                exposedTime))
        .isFalse();
  }

  @Test
  public void testShouldIsolate_InfectedAgain_SecondIsolation() {
    VirusStatusIsolationProperty virusStatusIsolationProperty =
        ImmutableVirusStatusIsolationProperty.builder()
            .virusStatus(SYMPTOMATIC)
            .isolationProperty(
                ImmutableIsolationProperty.builder()
                    .id("virusPolicyName4")
                    .priority(0)
                    .isolationProbabilityDistribution(flatHundredPercent)
                    .isolationTimeDistribution(flatThreeDays)
                    .build())
            .build();

    IsolationProperties isolationProperties =
        ImmutableIsolationProperties.builder()
            .addVirusStatusPolicies(virusStatusIsolationProperty)
            .addGlobalIsolationPolicies(twentyPercentInfectedZeroPercentIsolationProperty)
            .defaultPolicy(defaultZeroIsolationProperty)
            .isolationProbabilityDistributionThreshold(thresholdLinearDistribution)
            .build();

    var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                AWAITING_RESULT,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                AWAITING_RESULT,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 1,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                AWAITING_RESULT,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 2,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                AWAITING_RESULT,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 3,
                exposedTime))
        .isFalse();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                RECOVERED,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 4,
                exposedTime))
        .isFalse();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 5,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 6,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 7,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 8,
                exposedTime))
        .isFalse();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 9,
                exposedTime))
        .isFalse();
  }

  @Test
  public void testShouldIsolate_InfectedAgain_WithinExpiry_SecondIsolation() {
    VirusStatusIsolationProperty virusStatusIsolationProperty =
        ImmutableVirusStatusIsolationProperty.builder()
            .virusStatus(SYMPTOMATIC)
            .isolationProperty(
                ImmutableIsolationProperty.builder()
                    .id("virusPolicyName4")
                    .priority(0)
                    .isolationProbabilityDistribution(flatHundredPercent)
                    .isolationTimeDistribution(flatThreeDays)
                    .build())
            .build();

    IsolationProperties isolationProperties =
        ImmutableIsolationProperties.builder()
            .addVirusStatusPolicies(virusStatusIsolationProperty)
            .addGlobalIsolationPolicies(twentyPercentInfectedZeroPercentIsolationProperty)
            .defaultPolicy(defaultZeroIsolationProperty)
            .isolationProbabilityDistributionThreshold(thresholdLinearDistribution)
            .build();

    var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                AWAITING_RESULT,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                RECOVERED,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 1,
                exposedTime))
        .isFalse();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 2,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 3,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 4,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 5,
                exposedTime))
        .isFalse();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 6,
                exposedTime))
        .isFalse();
  }

  @Test
  public void testShouldIsolate_AlertTestNegativeThenInfected_SecondIsolation() {
    VirusStatusIsolationProperty virusStatusIsolationProperty =
        ImmutableVirusStatusIsolationProperty.builder()
            .virusStatus(SYMPTOMATIC)
            .isolationProperty(
                ImmutableIsolationProperty.builder()
                    .id("virusPolicyName4")
                    .priority(0)
                    .isolationProbabilityDistribution(flatHundredPercent)
                    .isolationTimeDistribution(flatThreeDays)
                    .build())
            .build();

    AlertStatusIsolationProperty alertStatusIsolationProperty =
        ImmutableAlertStatusIsolationProperty.builder()
            .alertStatus(AlertStatus.ALERTED)
            .isolationProperty(
                ImmutableIsolationProperty.builder()
                    .id("alertPolicyName4")
                    .priority(0)
                    .isolationProbabilityDistribution(flatHundredPercent)
                    .isolationTimeDistribution(flatThreeDays)
                    .build())
            .build();

    IsolationProperties isolationProperties =
        ImmutableIsolationProperties.builder()
            .addVirusStatusPolicies(virusStatusIsolationProperty)
            .addAlertStatusPolicies(alertStatusIsolationProperty)
            .addGlobalIsolationPolicies(twentyPercentInfectedZeroPercentIsolationProperty)
            .defaultPolicy(defaultZeroIsolationProperty)
            .isolationProbabilityDistributionThreshold(thresholdLinearDistribution)
            .build();

    var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                ALERTED,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                ALERTED,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 1,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 2,
                exposedTime))
        .isFalse();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 3,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 4,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 5,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 6,
                exposedTime))
        .isFalse();
  }

  @Test
  public void testShouldIsolate_AlertTestNegativeThenInfected_SecondIsolation_Short_Cycle() {
    VirusStatusIsolationProperty virusStatusIsolationProperty =
        ImmutableVirusStatusIsolationProperty.builder()
            .virusStatus(SYMPTOMATIC)
            .isolationProperty(
                ImmutableIsolationProperty.builder()
                    .id("virusPolicyName4")
                    .priority(0)
                    .isolationProbabilityDistribution(flatHundredPercent)
                    .isolationTimeDistribution(flatThreeDays)
                    .build())
            .build();

    AlertStatusIsolationProperty alertStatusIsolationProperty =
        ImmutableAlertStatusIsolationProperty.builder()
            .alertStatus(AlertStatus.ALERTED)
            .isolationProperty(
                ImmutableIsolationProperty.builder()
                    .id("alertPolicyName4")
                    .priority(0)
                    .isolationProbabilityDistribution(flatHundredPercent)
                    .isolationTimeDistribution(flatThreeDays)
                    .build())
            .build();

    IsolationProperties isolationProperties =
        ImmutableIsolationProperties.builder()
            .addVirusStatusPolicies(virusStatusIsolationProperty)
            .addAlertStatusPolicies(alertStatusIsolationProperty)
            .addGlobalIsolationPolicies(twentyPercentInfectedZeroPercentIsolationProperty)
            .defaultPolicy(defaultZeroIsolationProperty)
            .isolationProbabilityDistributionThreshold(thresholdLinearDistribution)
            .build();

    var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                ALERTED,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 1,
                exposedTime))
        .isFalse();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 2,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 3,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 4,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime + 5,
                exposedTime))
        .isFalse();
  }

  @Test
  public void testShouldIsolate_IsNotCompliant() {
    var compliance = 0.25;
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                ALERTED,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isFalse();
  }

  @Test
  public void testShouldIsolate_UseGlobalPolicy_IndividualPolicyExpired() {
    IsolationProperties isolationProperties =
        ImmutableIsolationProperties.builder()
            .addVirusStatusPolicies(infectedSymptomaticHundredPercentIsolationProperty)
            .addGlobalIsolationPolicies(
                ImmutableProportionInfectedIsolationProperty.builder()
                    .proportionInfected(MinMax.of(20, 100))
                    .isolationProperty(
                        ImmutableIsolationProperty.builder()
                            .id("globalPolicyName1")
                            .isolationProbabilityDistribution(flatHundredPercent)
                            .priority(2)
                            .build())
                    .build())
            .defaultPolicy(defaultZeroIsolationProperty)
            .isolationProbabilityDistributionThreshold(thresholdLinearDistribution)
            .build();

    var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);
    var proportionOfPopulationInfectious = 0.0;
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime,
                exposedTime))
        .isTrue();
    proportionOfPopulationInfectious = 0.3;
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime + 1,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime + 2,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime + 3,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime + 4,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime + 5,
                exposedTime))
        .isTrue();
    proportionOfPopulationInfectious = 0.0;
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime + 6,
                exposedTime))
        .isFalse();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime + 7,
                exposedTime))
        .isFalse();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime + 8,
                exposedTime))
        .isFalse();
  }

  @Test
  public void testShouldIsolate_UseGlobalPolicyWithNoTimeConstraintAfterFewTimeSteps() {
    var proportionOfPopulationInfectious = 0.0;
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime,
                exposedTime))
        .isFalse();
    proportionOfPopulationInfectious = 0.3;
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime + 1,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime + 2,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime + 3,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime + 4,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime + 5,
                exposedTime))
        .isTrue();
  }

  @Test
  public void testShouldIsolate_Alerted_Cycled() {
    var proportionOfPopulationInfectious = 0.0;
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                ALERTED,
                compliance,
                proportionOfPopulationInfectious,
                currentTime,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime + 1,
                exposedTime))
        .isFalse();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                ALERTED,
                compliance,
                proportionOfPopulationInfectious,
                currentTime + 2,
                exposedTime))
        .isTrue();
  }

  @Test
  public void testShouldIsolate_Alerted_Long_Cycled() {
    var proportionOfPopulationInfectious = 0.0;
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                ALERTED,
                compliance,
                proportionOfPopulationInfectious,
                currentTime,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime + 5,
                exposedTime))
        .isFalse();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                ALERTED,
                compliance,
                proportionOfPopulationInfectious,
                currentTime + 9,
                exposedTime))
        .isTrue();
  }

  @Test
  public void testShouldIsolate_ThrowsException_NonDeterministicPolicyOutcome() {
    AlertStatusIsolationProperty alertStatusProperty =
        ImmutableAlertStatusIsolationProperty.builder()
            .alertStatus(AlertStatus.ALERTED)
            .isolationProperty(
                ImmutableIsolationProperty.builder()
                    .id("alertedPolicyName1")
                    .priority(2)
                    .isolationProbabilityDistribution(flatHundredPercent)
                    .isolationTimeDistribution(flatThreeDays)
                    .build())
            .build();

    VirusStatusIsolationProperty virusStatusProperty =
        ImmutableVirusStatusIsolationProperty.builder()
            .virusStatus(SYMPTOMATIC)
            .isolationProperty(
                ImmutableIsolationProperty.builder()
                    .id("virusPolicyName1")
                    .priority(2)
                    .isolationProbabilityDistribution(flatHundredPercent)
                    .isolationTimeDistribution(flatOneDay)
                    .build())
            .build();

    IsolationProperties isolationProperties =
        ImmutableIsolationProperties.builder()
            .addVirusStatusPolicies(virusStatusProperty)
            .addAlertStatusPolicies(alertStatusProperty)
            .addGlobalIsolationPolicies(twentyPercentInfectedHundredPercentIsolationProperty)
            .defaultPolicy(defaultZeroIsolationProperty)
            .isolationProbabilityDistributionThreshold(thresholdLinearDistribution)
            .build();

    var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

    assertThatIllegalStateException()
        .isThrownBy(
            () ->
                isolationPolicy.isIndividualInIsolation(
                    id,
                    SYMPTOMATIC,
                    ALERTED,
                    compliance,
                    proportionOfPopulationInfected,
                    currentTime,
                    exposedTime))
        .withMessageContaining("Policy outcome description is not deterministic");
  }

  @Test
  public void testShouldIsolate_70PercentChance() {
    var seventyFlatDistribution =
        ImmutableDistribution.builder().mean(70).max(70).type(FLAT).build();

    ProportionInfectedIsolationProperty twentyPercentInfectedSeventyPercentIsolationProperty =
        ImmutableProportionInfectedIsolationProperty.builder()
            .proportionInfected(MinMax.of(20, 100))
            .isolationProperty(
                ImmutableIsolationProperty.builder()
                    .id("globalPolicyName1")
                    .priority(1)
                    .isolationProbabilityDistribution(seventyFlatDistribution)
                    .build())
            .build();

    IsolationProperties isolationProperties =
        ImmutableIsolationProperties.builder()
            .defaultPolicy(defaultZeroIsolationProperty)
            .addGlobalIsolationPolicies(twentyPercentInfectedSeventyPercentIsolationProperty)
            .isolationProbabilityDistributionThreshold(thresholdLinearDistribution)
            .build();

    var proportionOfPopulationInfectious = 0.2;

    when(this.distributionSampler.getDistributionValue(eq(thresholdLinearDistribution)))
        .thenAnswer(returnsElementsOf(List.of(5, 15, 25, 35, 45, 55, 65, 75, 85, 95)));
    when(this.distributionSampler.getDistributionValue(eq(seventyFlatDistribution))).thenReturn(75);
    when(this.distributionSampler.uniformBetweenZeroAndOne())
        .thenAnswer(
            returnsElementsOf(List.of(0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95)));

    var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime,
                exposedTime))
        .isTrue();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime,
                exposedTime))
        .isFalse();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime,
                exposedTime))
        .isFalse();
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SUSCEPTIBLE,
                NONE,
                compliance,
                proportionOfPopulationInfectious,
                currentTime,
                exposedTime))
        .isFalse();
  }

  @Test
  public void testStartIsolationFromExposedTime() {
    IsolationProperties isolationProperties =
        ImmutableIsolationProperties.builder()
            .defaultPolicy(defaultZeroIsolationProperty)
            .addGlobalIsolationPolicies(twentyPercentInfectedHundredPercentIsolationProperty)
            .addVirusStatusPolicies(
                ImmutableVirusStatusIsolationProperty.builder()
                    .virusStatus(SYMPTOMATIC)
                    .isolationProperty(
                        ImmutableIsolationProperty.builder()
                            .id("policy name")
                            .priority(2)
                            .isolationProbabilityDistribution(flatHundredPercent)
                            .isolationTimeDistribution(flatOneDay)
                            .startOfIsolationTime(IsolationStartTimeType.CONTACT_TIME)
                            .build())
                    .build())
            .isolationProbabilityDistributionThreshold(thresholdLinearDistribution)
            .build();

    var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);
    var exposedTime = 0;
    var currentTime = 0;
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isTrue();

    isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);
    exposedTime = 0;
    currentTime = 1;
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isTrue();

    isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);
    exposedTime = 0;
    currentTime = 2;
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isFalse();
  }

  @Test
  public void testStartIsolationFromAbsoluteCurrentTime() {
    IsolationProperties isolationProperties =
        ImmutableIsolationProperties.builder()
            .defaultPolicy(defaultZeroIsolationProperty)
            .addGlobalIsolationPolicies(twentyPercentInfectedHundredPercentIsolationProperty)
            .addVirusStatusPolicies(
                ImmutableVirusStatusIsolationProperty.builder()
                    .virusStatus(SYMPTOMATIC)
                    .isolationProperty(
                        ImmutableIsolationProperty.builder()
                            .id("policy name")
                            .priority(2)
                            .isolationProbabilityDistribution(flatHundredPercent)
                            .isolationTimeDistribution(flatOneDay)
                            .startOfIsolationTime(IsolationStartTimeType.ABSOLUTE)
                            .build())
                    .build())
            .isolationProbabilityDistributionThreshold(thresholdLinearDistribution)
            .build();

    var isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);

    var exposedTime = 0;
    var currentTime = 0;
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isTrue();

    isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);
    exposedTime = 0;
    currentTime = 1;
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isTrue();

    isolationPolicy = new SingleCaseIsolationPolicy(isolationProperties, distributionSampler);
    exposedTime = 0;
    currentTime = 2;
    assertThat(
            isolationPolicy.isIndividualInIsolation(
                id,
                SYMPTOMATIC,
                NONE,
                compliance,
                proportionOfPopulationInfected,
                currentTime,
                exposedTime))
        .isTrue();
  }
}
