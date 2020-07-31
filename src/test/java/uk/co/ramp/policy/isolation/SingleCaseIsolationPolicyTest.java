package uk.co.ramp.policy.isolation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.AdditionalAnswers.returnsElementsOf;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.ramp.people.AlertStatus.*;
import static uk.co.ramp.people.VirusStatus.*;

import java.util.List;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.TestUtils;
import uk.co.ramp.distribution.BoundedDistribution;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.statistics.StatisticsRecorder;
import uk.co.ramp.utilities.MinMax;

public class SingleCaseIsolationPolicyTest {
  private DistributionSampler distributionSampler;
  private StandardProperties properties = TestUtils.standardProperties();
  private StatisticsRecorder statisticsRecorder = mock(StatisticsRecorder.class);

  private final BoundedDistribution flatZeroPercent = mock(BoundedDistribution.class);

  private final BoundedDistribution flatHundredPercent = mock(BoundedDistribution.class);

  private final BoundedDistribution flatOneDay = mock(BoundedDistribution.class);

  private final ImmutableIsolationProperty defaultZeroIsolationProperty =
      ImmutableIsolationProperty.builder()
          .id("zeroIsolationPolicy")
          .isolationProbabilityDistribution(flatZeroPercent)
          .priority(-1)
          .build();

  private final ImmutableVirusStatusIsolationProperty
      infectedSymptomaticHundredPercentIsolationProperty =
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

  private final ImmutableVirusStatusIsolationProperty recoveredZeroPercentIsolationProperty =
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

  private final ImmutableVirusStatusIsolationProperty deadZeroPercentIsolationProperty =
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

  private final ImmutableAlertStatusIsolationProperty
      testedPositiveHundredPercentIsolationProperty =
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

  private final ImmutableAlertStatusIsolationProperty alertedHundredPercentIsolationProperty =
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

  private final ImmutableProportionInfectedIsolationProperty
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

  private final ImmutableProportionInfectedIsolationProperty
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

  private final BoundedDistribution flatThreeDays = mock(BoundedDistribution.class);

  private final BoundedDistribution thresholdLinearBoundedDistribution =
      mock(BoundedDistribution.class);

  private final int currentTime = 0;
  private final int exposedTime = 0;
  private final int id = 0;
  private final double compliance = 1.0;
  private final double proportionOfPopulationInfected = 0.0;

  private SingleCaseIsolationPolicy isolationPolicy;
  private RandomGenerator rng;

  @Before
  public void setUp() {
    when(flatZeroPercent.getDistributionValue()).thenReturn(0);
    when(flatZeroPercent.max()).thenReturn(0D);
    when(flatHundredPercent.getDistributionValue()).thenReturn(100);
    when(flatHundredPercent.max()).thenReturn(100D);
    when(flatOneDay.getDistributionValue()).thenReturn(1);
    when(flatOneDay.max()).thenReturn(1D);
    when(flatThreeDays.getDistributionValue()).thenReturn(3);
    when(flatThreeDays.max()).thenReturn(3D);
    when(thresholdLinearBoundedDistribution.getDistributionValue())
        .thenAnswer(returnsElementsOf(List.of(5, 15, 25, 35, 45, 55, 65, 75, 85, 95)));
    when(thresholdLinearBoundedDistribution.max()).thenReturn(100D);

    IsolationProperties isolationProperties =
        ImmutableIsolationProperties.builder()
            .defaultPolicy(defaultZeroIsolationProperty)
            .addVirusStatusPolicies(infectedSymptomaticHundredPercentIsolationProperty)
            .addVirusStatusPolicies(recoveredZeroPercentIsolationProperty)
            .addVirusStatusPolicies(deadZeroPercentIsolationProperty)
            .addAlertStatusPolicies(testedPositiveHundredPercentIsolationProperty)
            .addAlertStatusPolicies(alertedHundredPercentIsolationProperty)
            .addGlobalIsolationPolicies(twentyPercentInfectedHundredPercentIsolationProperty)
            .isolationProbabilityDistributionThreshold(thresholdLinearBoundedDistribution)
            .build();

    this.distributionSampler = mock(DistributionSampler.class);
    //    when(this.distributionSampler.getDistributionValue(any(BoundedDistribution.class)))
    //        .thenAnswer(invocation -> (int) invocation.getArgument(0,
    // BoundedDistribution.class).mean());
    when(distributionSampler.uniformBetweenZeroAndOne())
        .thenAnswer(
            returnsElementsOf(List.of(0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95)));

    when(this.distributionSampler.uniformBetweenZeroAndOne()).thenReturn(0.5);
    when(this.distributionSampler.uniformInteger(anyInt()))
        .thenAnswer(invocation -> invocation.getArgument(0, int.class) / 2);
    rng = mock(RandomGenerator.class);
    when(rng.nextDouble()).thenReturn(0.5D);
    this.isolationPolicy =
        new SingleCaseIsolationPolicy(
            isolationProperties, distributionSampler, properties, statisticsRecorder, rng);
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
            .isolationProbabilityDistributionThreshold(thresholdLinearBoundedDistribution)
            .build();

    var isolationPolicy =
        new SingleCaseIsolationPolicy(
            isolationProperties, distributionSampler, properties, statisticsRecorder, rng);

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
            .isolationProbabilityDistributionThreshold(thresholdLinearBoundedDistribution)
            .build();

    var isolationPolicy =
        new SingleCaseIsolationPolicy(
            isolationProperties, distributionSampler, properties, statisticsRecorder, rng);

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
    ImmutableVirusStatusIsolationProperty virusStatusIsolationProperty =
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
            .isolationProbabilityDistributionThreshold(thresholdLinearBoundedDistribution)
            .build();

    var isolationPolicy =
        new SingleCaseIsolationPolicy(
            isolationProperties, distributionSampler, properties, statisticsRecorder, rng);

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
    ImmutableVirusStatusIsolationProperty virusStatusIsolationProperty =
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
            .isolationProbabilityDistributionThreshold(thresholdLinearBoundedDistribution)
            .build();

    var isolationPolicy =
        new SingleCaseIsolationPolicy(
            isolationProperties, distributionSampler, properties, statisticsRecorder, rng);

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
    ImmutableVirusStatusIsolationProperty virusStatusIsolationProperty =
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
            .isolationProbabilityDistributionThreshold(thresholdLinearBoundedDistribution)
            .build();

    var isolationPolicy =
        new SingleCaseIsolationPolicy(
            isolationProperties, distributionSampler, properties, statisticsRecorder, rng);

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
    ImmutableVirusStatusIsolationProperty virusStatusIsolationProperty =
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
            .isolationProbabilityDistributionThreshold(thresholdLinearBoundedDistribution)
            .build();

    var isolationPolicy =
        new SingleCaseIsolationPolicy(
            isolationProperties, distributionSampler, properties, statisticsRecorder, rng);

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
    ImmutableVirusStatusIsolationProperty virusStatusIsolationProperty =
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
            .isolationProbabilityDistributionThreshold(thresholdLinearBoundedDistribution)
            .build();

    var isolationPolicy =
        new SingleCaseIsolationPolicy(
            isolationProperties, distributionSampler, properties, statisticsRecorder, rng);

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
    ImmutableVirusStatusIsolationProperty virusStatusIsolationProperty =
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

    ImmutableAlertStatusIsolationProperty alertStatusIsolationProperty =
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
            .isolationProbabilityDistributionThreshold(thresholdLinearBoundedDistribution)
            .build();

    var isolationPolicy =
        new SingleCaseIsolationPolicy(
            isolationProperties, distributionSampler, properties, statisticsRecorder, rng);

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
    ImmutableVirusStatusIsolationProperty virusStatusIsolationProperty =
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

    ImmutableAlertStatusIsolationProperty alertStatusIsolationProperty =
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
            .isolationProbabilityDistributionThreshold(thresholdLinearBoundedDistribution)
            .build();

    var isolationPolicy =
        new SingleCaseIsolationPolicy(
            isolationProperties, distributionSampler, properties, statisticsRecorder, rng);

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
            .isolationProbabilityDistributionThreshold(thresholdLinearBoundedDistribution)
            .build();

    var isolationPolicy =
        new SingleCaseIsolationPolicy(
            isolationProperties, distributionSampler, properties, statisticsRecorder, rng);
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
    ImmutableAlertStatusIsolationProperty alertStatusProperty =
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

    ImmutableVirusStatusIsolationProperty virusStatusProperty =
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
            .isolationProbabilityDistributionThreshold(thresholdLinearBoundedDistribution)
            .build();

    var isolationPolicy =
        new SingleCaseIsolationPolicy(
            isolationProperties, distributionSampler, properties, statisticsRecorder, rng);

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
    var seventyFlatDistribution = mock(BoundedDistribution.class);
    when(seventyFlatDistribution.getDistributionValue()).thenReturn(75);
    when(seventyFlatDistribution.max()).thenReturn(75D);

    ImmutableProportionInfectedIsolationProperty
        twentyPercentInfectedSeventyPercentIsolationProperty =
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
            .isolationProbabilityDistributionThreshold(thresholdLinearBoundedDistribution)
            .build();

    var proportionOfPopulationInfectious = 0.2;

    var isolationPolicy =
        new SingleCaseIsolationPolicy(
            isolationProperties, distributionSampler, properties, statisticsRecorder, rng);

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
            .isolationProbabilityDistributionThreshold(thresholdLinearBoundedDistribution)
            .build();

    var isolationPolicy =
        new SingleCaseIsolationPolicy(
            isolationProperties, distributionSampler, properties, statisticsRecorder, rng);
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

    isolationPolicy =
        new SingleCaseIsolationPolicy(
            isolationProperties, distributionSampler, properties, statisticsRecorder, rng);
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

    isolationPolicy =
        new SingleCaseIsolationPolicy(
            isolationProperties, distributionSampler, properties, statisticsRecorder, rng);
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
            .isolationProbabilityDistributionThreshold(thresholdLinearBoundedDistribution)
            .build();

    var isolationPolicy =
        new SingleCaseIsolationPolicy(
            isolationProperties, distributionSampler, properties, statisticsRecorder, rng);

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

    isolationPolicy =
        new SingleCaseIsolationPolicy(
            isolationProperties, distributionSampler, properties, statisticsRecorder, rng);
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

    isolationPolicy =
        new SingleCaseIsolationPolicy(
            isolationProperties, distributionSampler, properties, statisticsRecorder, rng);
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
