package uk.co.ramp.policy;

import static uk.co.ramp.distribution.ProgressionDistribution.FLAT;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.immutables.value.Value;
import uk.co.ramp.distribution.Distribution;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.distribution.ImmutableDistribution;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.VirusStatus;

class SingleCaseIsolationPolicy {
  private final IsolationProperties isolationProperties;
  private final DistributionSampler distributionSampler;
  private final Distribution infinityDistribution =
      ImmutableDistribution.builder()
          .type(FLAT)
          .mean(Double.MAX_VALUE)
          .max(Double.MAX_VALUE)
          .build();

  @Value.Immutable
  interface IsolationMapValue {
    int startTime();

    int maxIsolationTime();

    IsolationProperty isolationProperty();
  }

  private final Map<Integer, IsolationMapValue> currentlyInIsolationMap = new HashMap<>();

  SingleCaseIsolationPolicy(
      IsolationProperties isolationProperties, DistributionSampler distributionSampler) {
    this.isolationProperties = isolationProperties;
    this.distributionSampler = distributionSampler;
  }

  private IsolationProperty findRelevantIsolationProperty(
      double actualInfectedProportion, VirusStatus virusStatus, AlertStatus alertStatus) {
    Stream<IsolationProperty> globalIsolationPolicy =
        isolationProperties.globalIsolationPolicies().stream()
            .filter(policy -> policy.proportionInfected().min() <= actualInfectedProportion * 100)
            .filter(policy -> policy.proportionInfected().max() > actualInfectedProportion * 100)
            .map(ProportionInfectedIsolationProperty::isolationProperty);

    Stream<IsolationProperty> virusIsolationPolicy =
        isolationProperties.virusStatusPolicies().stream()
            .filter(policy -> policy.virusStatus() == virusStatus)
            .map(VirusStatusIsolationProperty::isolationProperty);

    Stream<IsolationProperty> alertIsolationPolicy =
        isolationProperties.alertStatusPolicies().stream()
            .filter(policy -> policy.alertStatus() == alertStatus)
            .map(AlertStatusIsolationProperty::isolationProperty);

    Stream<IsolationProperty> defaultIsolationPolicy =
        Stream.of(isolationProperties.defaultPolicy());

    List<IsolationProperty> matchingHighestPriorityPolicies =
        Stream.of(
                globalIsolationPolicy,
                virusIsolationPolicy,
                alertIsolationPolicy,
                defaultIsolationPolicy)
            .flatMap(s -> s).collect(Collectors.groupingBy(IsolationProperty::priority)).entrySet()
            .stream()
            .max(Comparator.comparingLong(Map.Entry::getKey))
            .orElseThrow(() -> new IllegalStateException("No matching policies found."))
            .getValue();

    boolean allPolicyOutcomesAreEqual =
        matchingHighestPriorityPolicies.stream()
            .allMatch(
                p ->
                    p.isolationTimeDistribution()
                            .equals(
                                matchingHighestPriorityPolicies.get(0).isolationTimeDistribution())
                        && p.isolationProbabilityDistribution()
                            .equals(
                                matchingHighestPriorityPolicies
                                    .get(0)
                                    .isolationProbabilityDistribution()));

    if (allPolicyOutcomesAreEqual) {
      return matchingHighestPriorityPolicies.get(0);
    } else {
      throw new IllegalStateException(
          ""
              + "Policy outcome description is not deterministic. "
              + "Please also ensure that there is only one policy outcome from the matching policies with a max priority value.");
    }
  }

  boolean isIndividualInIsolation(Case aCase, double actualInfectedProportion, int currentTime) {
    return isIndividualInIsolation(
        aCase.id(),
        aCase.virusStatus(),
        aCase.alertStatus(),
        aCase.compliance(),
        actualInfectedProportion,
        currentTime);
  }

  boolean isIndividualInIsolation(
      int id,
      VirusStatus virusStatus,
      AlertStatus alertStatus,
      double compliance,
      double actualInfectedProportion,
      int currentTime) {
    IsolationMapValue isolationInfo = currentlyInIsolationMap.get(id);
    IsolationProperty matchingIsolationProperty =
        findRelevantIsolationProperty(actualInfectedProportion, virusStatus, alertStatus);
    boolean alreadyInIsolationMap =
        isolationInfo != null
            && matchingIsolationProperty.equals(isolationInfo.isolationProperty());

    if (alreadyInIsolationMap) {
      return currentTime - isolationInfo.startTime() < isolationInfo.maxIsolationTime();
    }

    return populateAndGet(id, compliance, matchingIsolationProperty, currentTime);
  }

  private IsolationMapValue updatedMapValue(
      IsolationMapValue currentMapValue,
      IsolationProperty isolationProperty,
      int currentTime,
      int requiredIsolationTime) {
    return ImmutableIsolationMapValue.builder()
        .isolationProperty(isolationProperty)
        .startTime(
            Optional.ofNullable(currentMapValue)
                .map(IsolationMapValue::startTime)
                .orElse(currentTime))
        .maxIsolationTime(requiredIsolationTime)
        .build();
  }

  private boolean populateAndGet(
      int id, double compliance, IsolationProperty matchingIsolationProperty, int currentTime) {
    int requiredIsolationTime =
        distributionSampler.getDistributionValue(
            matchingIsolationProperty.isolationTimeDistribution().orElse(infinityDistribution));
    double threshold =
        distributionSampler.getDistributionValue(
            isolationProperties.isolationProbabilityDistributionThreshold());
    double requiredIsolationFactor =
        distributionSampler.getDistributionValue(
            matchingIsolationProperty.isolationProbabilityDistribution());
    boolean timedPolicy = matchingIsolationProperty.isolationTimeDistribution().isPresent();
    boolean isDefaultPolicy = isolationProperties.defaultPolicy().equals(matchingIsolationProperty);

    boolean willIsolate =
        (threshold < requiredIsolationFactor)
            && (distributionSampler.uniformBetweenZeroAndOne() < compliance);
    if (timedPolicy || isDefaultPolicy) {
      currentlyInIsolationMap.compute(
          id,
          (i, val) ->
              willIsolate
                  ? updatedMapValue(
                      val, matchingIsolationProperty, currentTime, requiredIsolationTime)
                  : null);
    }
    return willIsolate;
  }
}
