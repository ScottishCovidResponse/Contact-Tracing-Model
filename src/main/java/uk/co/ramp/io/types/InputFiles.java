package uk.co.ramp.io.types;

import com.google.common.base.Preconditions;
import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Check;
import org.immutables.value.Value.Immutable;

@TypeAdapters
@Immutable
public interface InputFiles {

  String runSettings();

  String populationSettings();

  String diseaseSettings();

  String contactData();

  String initialExposures();

  String alertPolicies();

  String isolationPolicies();

  @Check
  default void check() {
    Preconditions.checkState(!runSettings().isBlank(), "Run settings location should not be blank");
    Preconditions.checkState(
        !populationSettings().isBlank(), "Population settings location should not be blank");
    Preconditions.checkState(
        !diseaseSettings().isBlank(), "Disease settings location should not be blank");
    Preconditions.checkState(!contactData().isBlank(), "Contact data location should not be blank");
    Preconditions.checkState(
        !initialExposures().isBlank(), "Initial exposure data location should not be blank");
    Preconditions.checkState(
        !alertPolicies().isBlank(), "Alert policies location should not be blank");
    Preconditions.checkState(
        !isolationPolicies().isBlank(), "Isolation policies location should not be blank");
  }
}
