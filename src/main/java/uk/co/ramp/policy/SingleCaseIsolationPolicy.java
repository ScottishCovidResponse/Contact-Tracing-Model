package uk.co.ramp.policy;

import org.immutables.value.Value;
import uk.co.ramp.distribution.Distribution;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.distribution.ImmutableDistribution;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.VirusStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static uk.co.ramp.distribution.ProgressionDistribution.FLAT;

class SingleCaseIsolationPolicy {
    private final IsolationProperties isolationProperties;
    private final DistributionSampler distributionSampler;
    private final Distribution infinityDistribution = ImmutableDistribution.builder()
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

    SingleCaseIsolationPolicy(IsolationProperties isolationProperties, DistributionSampler distributionSampler) {
        this.isolationProperties = isolationProperties;
        this.distributionSampler = distributionSampler;
    }

    private IsolationProperty findRelevantIsolationProperty(double actualInfectedProportion, VirusStatus virusStatus, AlertStatus alertStatus) {
        Stream<IsolationProperty> globalIsolationPolicy = isolationProperties.globalIsolationPolicies().values().stream()
                .filter(policy -> policy.proportionInfected().min() < actualInfectedProportion * 100)
                .filter(policy -> policy.proportionInfected().max() > actualInfectedProportion * 100)
                .map(ProportionInfectedIsolationProperty::isolationProperty);

        Stream<IsolationProperty> virusIsolationPolicy = isolationProperties.individualIsolationPolicies().virusStatusPolicies().values().stream()
                .filter(policy -> policy.virusStatus() == virusStatus)
                .map(VirusStatusIsolationProperty::isolationProperty);

        Stream<IsolationProperty> alertIsolationPolicy = isolationProperties.individualIsolationPolicies().alertStatusPolicies().values().stream()
                .filter(policy -> policy.alertStatus() == alertStatus)
                .map(AlertStatusIsolationProperty::isolationProperty);

        Stream<IsolationProperty> defaultIsolationPolicy = Stream.of(isolationProperties.defaultPolicy());

        return Stream.of(globalIsolationPolicy, virusIsolationPolicy, alertIsolationPolicy, defaultIsolationPolicy)
                .flatMap(s -> s)
                .max(comparingInt(IsolationProperty::priority))
                .orElseThrow();
    }

    boolean isIndividualInIsolation(int id, VirusStatus virusStatus, AlertStatus alertStatus, double compliance, double actualInfectedProportion, int currentTime) {
        IsolationMapValue isolationInfo = currentlyInIsolationMap.get(id);
        IsolationProperty matchingIsolationProperty = findRelevantIsolationProperty(actualInfectedProportion, virusStatus, alertStatus);
        boolean alreadyInIsolationMap = isolationInfo != null && matchingIsolationProperty.equals(isolationInfo.isolationProperty());

        if (alreadyInIsolationMap) {
            return currentTime - isolationInfo.startTime() < isolationInfo.maxIsolationTime();
        }

        return populateAndGet(id, compliance, matchingIsolationProperty, currentTime);
    }

    private IsolationMapValue updatedMapValue(IsolationMapValue currentMapValue, IsolationProperty isolationProperty, int currentTime, int requiredIsolationTime) {
        return ImmutableIsolationMapValue.builder()
                .isolationProperty(isolationProperty)
                .startTime(Optional.ofNullable(currentMapValue)
                        .map(IsolationMapValue::startTime)
                        .orElse(currentTime))
                .maxIsolationTime(requiredIsolationTime)
                .build();
    }

    private boolean populateAndGet(int id, double compliance, IsolationProperty matchingIsolationProperty, int currentTime) {
        int requiredIsolationTime = distributionSampler.getDistributionValue(matchingIsolationProperty.isolationTimeDistribution().orElse(infinityDistribution));
        double threshold = isolationProperties.isolationProbabilityDistributionThreshold();
        double requiredIsolationFactor = distributionSampler.getDistributionValue(matchingIsolationProperty.isolationProbabilityDistribution());
        boolean willIsolate = (threshold < requiredIsolationFactor) && (distributionSampler.uniformBetweenZeroAndOne() < compliance);
        currentlyInIsolationMap.compute(id, (i, val) -> willIsolate ? updatedMapValue(val, matchingIsolationProperty, currentTime, requiredIsolationTime) : null);
        return willIsolate;
    }
}
