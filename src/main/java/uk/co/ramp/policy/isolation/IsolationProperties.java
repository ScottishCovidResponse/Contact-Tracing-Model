package uk.co.ramp.policy.isolation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import uk.co.ramp.distribution.BoundedDistribution;

@JsonSerialize
@JsonDeserialize
@Immutable
interface IsolationProperties {
  List<ImmutableProportionInfectedIsolationProperty> globalIsolationPolicies();

  List<ImmutableVirusStatusIsolationProperty> virusStatusPolicies();

  List<ImmutableAlertStatusIsolationProperty> alertStatusPolicies();

  ImmutableIsolationProperty defaultPolicy();

  BoundedDistribution isolationProbabilityDistributionThreshold();

  @Value.Check
  default void check() {
    var ids1 = globalIsolationPolicies().stream().map(policy -> policy.isolationProperty().id());
    var ids2 = virusStatusPolicies().stream().map(policy -> policy.isolationProperty().id());
    var ids3 = alertStatusPolicies().stream().map(policy -> policy.isolationProperty().id());

    boolean idsAreUnique =
        Stream.of(ids1, ids2, ids3).flatMap(id -> id)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).values()
            .stream()
            .noneMatch(e -> e != 1);

    Preconditions.checkState(idsAreUnique, "policy ids should be unique");
  }
}
