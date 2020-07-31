package uk.co.ramp.policy.isolation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.co.ramp.policy.isolation.IsolationStartTimeType.CONTACT_TIME;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;
import uk.co.ramp.distribution.ImmutableBoundedDistribution;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.utilities.MinMax;
import uk.ramp.distribution.Distribution.DistributionType;
import uk.ramp.distribution.ImmutableDistribution;
import uk.ramp.distribution.ImmutableMinMax;

public class IsolationPropertiesReaderTest {
  private static final String mockIsolationProperties =
      "{\n"
          + "  \"globalIsolationPolicies\": [\n"
          + "    {\n"
          + "      \"proportionInfected\": {\n"
          + "        \"min\": 10,\n"
          + "        \"max\": 100\n"
          + "      },\n"
          + "      \"isolationProperty\": {\n"
          + "        \"id\": \"Stay at Home Policy\",\n"
          + "        \"isolationProbabilityDistribution\": {\n"
          + "          \"distribution\": {\n"
          + "            \"distribution\": \"empirical\",\n"
          + "            \"empiricalSamples\": [90]\n"
          + "          },\n"
          + "          \"max\": 90\n"
          + "        },\n"
          + "        \"priority\": 1\n"
          + "      }\n"
          + "    },\n"
          + "    {\n"
          + "      \"proportionInfected\": {\n"
          + "        \"min\": 5,\n"
          + "        \"max\": 10\n"
          + "      },\n"
          + "      \"isolationProperty\": {\n"
          + "        \"id\": \"Stay Alert Policy\",\n"
          + "        \"isolationProbabilityDistribution\": {\n"
          + "          \"distribution\": {\n"
          + "            \"distribution\": \"empirical\",\n"
          + "            \"empiricalSamples\": [70]\n"
          + "          },\n"
          + "          \"max\": 70\n"
          + "        },\n"
          + "        \"priority\": 1\n"
          + "      }\n"
          + "    }\n"
          + "  ],\n"
          + "  \"virusStatusPolicies\": [\n"
          + "    {\n"
          + "      \"virusStatus\": \"SYMPTOMATIC\",\n"
          + "      \"isolationProperty\": {\n"
          + "        \"id\": \"Infected Symptomatic Policy\",\n"
          + "        \"isolationProbabilityDistribution\": {\n"
          + "          \"distribution\": {\n"
          + "            \"distribution\": \"empirical\",\n"
          + "            \"empiricalSamples\": [100]\n"
          + "          },\n"
          + "          \"max\": 100\n"
          + "        },\n"
          + "        \"isolationTimeDistribution\": {\n"
          + "          \"distribution\": {\n"
          + "            \"distribution\": \"categorical\",\n"
          + "            \"bins\": [\"[12,14]\"],\n"
          + "            \"weights\": [1.0]\n"
          + "          },\n"
          + "          \"max\": 14\n"
          + "        },\n"
          + "        \"startOfIsolationTime\": \"CONTACT_TIME\",\n"
          + "        \"priority\": 2\n"
          + "      }\n"
          + "    }\n"
          + "  ],\n"
          + "  \"alertStatusPolicies\": [\n"
          + "    {\n"
          + "      \"alertStatus\": \"ALERTED\",\n"
          + "      \"isolationProperty\": {\n"
          + "        \"id\": \"Alerted Policy\",\n"
          + "        \"isolationProbabilityDistribution\": {\n"
          + "          \"distribution\": {\n"
          + "            \"distribution\": \"empirical\",\n"
          + "            \"empiricalSamples\": [100]\n"
          + "          },\n"
          + "          \"max\": 100\n"
          + "        },\n"
          + "        \"isolationTimeDistribution\": {\n"
          + "          \"distribution\": {\n"
          + "            \"distribution\": \"categorical\",\n"
          + "            \"bins\": [\"[12,14]\"],\n"
          + "            \"weights\": [1.0]\n"
          + "          },\n"
          + "          \"max\": 14\n"
          + "        },\n"
          + "        \"startOfIsolationTime\": \"CONTACT_TIME\",\n"
          + "        \"priority\": 2\n"
          + "      }\n"
          + "    },\n"
          + "    {\n"
          + "      \"alertStatus\": \"REQUESTED_TEST\",\n"
          + "      \"isolationProperty\": {\n"
          + "        \"id\": \"Requested Test Policy\",\n"
          + "        \"isolationProbabilityDistribution\": {\n"
          + "          \"distribution\": {\n"
          + "            \"distribution\": \"empirical\",\n"
          + "            \"empiricalSamples\": [100]\n"
          + "          },\n"
          + "          \"max\": 100\n"
          + "        },\n"
          + "        \"isolationTimeDistribution\": {\n"
          + "          \"distribution\": {\n"
          + "            \"distribution\": \"categorical\",\n"
          + "            \"bins\": [\"[12,14]\"],\n"
          + "            \"weights\": [1.0]\n"
          + "          },\n"
          + "          \"max\": 14\n"
          + "        },\n"
          + "        \"startOfIsolationTime\": \"CONTACT_TIME\",\n"
          + "        \"priority\": 2\n"
          + "      }\n"
          + "    },\n"
          + "    {\n"
          + "      \"alertStatus\": \"AWAITING_RESULT\",\n"
          + "      \"isolationProperty\": {\n"
          + "        \"id\": \"Awaiting Result Policy\",\n"
          + "        \"isolationProbabilityDistribution\": {\n"
          + "          \"distribution\": {\n"
          + "            \"distribution\": \"empirical\",\n"
          + "            \"empiricalSamples\": [100]\n"
          + "          },\n"
          + "          \"max\": 100\n"
          + "        },\n"
          + "        \"isolationTimeDistribution\": {\n"
          + "          \"distribution\": {\n"
          + "            \"distribution\": \"categorical\",\n"
          + "            \"bins\": [\"[12,14]\"],\n"
          + "            \"weights\": [1.0]\n"
          + "          },\n"
          + "          \"max\": 14\n"
          + "        },\n"
          + "        \"startOfIsolationTime\": \"CONTACT_TIME\",\n"
          + "        \"priority\": 2\n"
          + "      }\n"
          + "    },\n"
          + "    {\n"
          + "      \"alertStatus\": \"TESTED_POSITIVE\",\n"
          + "      \"isolationProperty\": {\n"
          + "        \"id\": \"Tested Positive Policy\",\n"
          + "        \"isolationProbabilityDistribution\": {\n"
          + "          \"distribution\": {\n"
          + "            \"distribution\": \"empirical\",\n"
          + "            \"empiricalSamples\": [100]\n"
          + "          },\n"
          + "          \"max\": 100\n"
          + "        },\n"
          + "        \"isolationTimeDistribution\": {\n"
          + "          \"distribution\": {\n"
          + "            \"distribution\": \"categorical\",\n"
          + "            \"bins\": [\"[12,14]\"],\n"
          + "            \"weights\": [1.0]\n"
          + "          },\n"
          + "          \"max\": 14\n"
          + "        },\n"
          + "        \"startOfIsolationTime\": \"CONTACT_TIME\",\n"
          + "        \"priority\": 2\n"
          + "      }\n"
          + "    }\n"
          + "  ],\n"
          + "  \"defaultPolicy\": {\n"
          + "    \"id\": \"Default Policy\",\n"
          + "    \"isolationProbabilityDistribution\": {\n"
          + "      \"distribution\": {\n"
          + "        \"distribution\": \"empirical\",\n"
          + "        \"empiricalSamples\": [0]\n"
          + "      },\n"
          + "      \"max\": 0\n"
          + "    },\n"
          + "    \"priority\": 0\n"
          + "  },\n"
          + "  \"isolationProbabilityDistributionThreshold\": {\n"
          + "    \"distribution\": {\n"
          + "      \"distribution\": \"categorical\",\n"
          + "      \"bins\": [\"[0,100]\"],\n"
          + "      \"weights\": [1.0]\n"
          + "    },\n"
          + "    \"max\": 100\n"
          + "  }\n"
          + "}";

  @Test
  public void testRead() {
    var rng = mock(RandomGenerator.class);
    var underlyingReader = new BufferedReader(new StringReader(mockIsolationProperties));

    var reader = new IsolationPropertiesReader(rng);
    IsolationProperties actualIsolationProperties = reader.read(underlyingReader);

    var expectedIsolationProperties =
        ImmutableIsolationProperties.builder()
            .addGlobalIsolationPolicies(
                ImmutableProportionInfectedIsolationProperty.builder()
                    .proportionInfected(MinMax.of(10, 100))
                    .isolationProperty(
                        ImmutableIsolationProperty.builder()
                            .id("Stay at Home Policy")
                            .isolationProbabilityDistribution(
                                ImmutableBoundedDistribution.builder()
                                    .distribution(
                                        ImmutableDistribution.builder()
                                            .internalType(DistributionType.empirical)
                                            .empiricalSamples(List.of(90))
                                            .rng(rng)
                                            .build())
                                    .max(90)
                                    .build())
                            .priority(1)
                            .build())
                    .build())
            .addGlobalIsolationPolicies(
                ImmutableProportionInfectedIsolationProperty.builder()
                    .proportionInfected(MinMax.of(5, 10))
                    .isolationProperty(
                        ImmutableIsolationProperty.builder()
                            .id("Stay Alert Policy")
                            .isolationProbabilityDistribution(
                                ImmutableBoundedDistribution.builder()
                                    .distribution(
                                        ImmutableDistribution.builder()
                                            .internalType(DistributionType.empirical)
                                            .empiricalSamples(List.of(70))
                                            .rng(rng)
                                            .build())
                                    .max(70)
                                    .build())
                            .priority(1)
                            .build())
                    .build())
            .addVirusStatusPolicies(
                ImmutableVirusStatusIsolationProperty.builder()
                    .virusStatus(VirusStatus.SYMPTOMATIC)
                    .isolationProperty(
                        ImmutableIsolationProperty.builder()
                            .id("Infected Symptomatic Policy")
                            .isolationProbabilityDistribution(
                                ImmutableBoundedDistribution.builder()
                                    .distribution(
                                        ImmutableDistribution.builder()
                                            .internalType(DistributionType.empirical)
                                            .empiricalSamples(List.of(100))
                                            .rng(rng)
                                            .build())
                                    .max(100)
                                    .build())
                            .isolationTimeDistribution(
                                ImmutableBoundedDistribution.builder()
                                    .distribution(
                                        ImmutableDistribution.builder()
                                            .internalType(DistributionType.categorical)
                                            .addBins(
                                                ImmutableMinMax.builder()
                                                    .lowerBoundary(12)
                                                    .upperBoundary(14)
                                                    .isLowerInclusive(true)
                                                    .isUpperInclusive(true)
                                                    .build())
                                            .addWeights(1.0)
                                            .rng(rng)
                                            .build())
                                    .max(14)
                                    .build())
                            .startOfIsolationTime(CONTACT_TIME)
                            .priority(2)
                            .build())
                    .build())
            .addAlertStatusPolicies(
                ImmutableAlertStatusIsolationProperty.builder()
                    .alertStatus(AlertStatus.ALERTED)
                    .isolationProperty(
                        ImmutableIsolationProperty.builder()
                            .id("Alerted Policy")
                            .isolationProbabilityDistribution(
                                ImmutableBoundedDistribution.builder()
                                    .distribution(
                                        ImmutableDistribution.builder()
                                            .internalType(DistributionType.empirical)
                                            .empiricalSamples(List.of(100))
                                            .rng(rng)
                                            .build())
                                    .max(100)
                                    .build())
                            .isolationTimeDistribution(
                                ImmutableBoundedDistribution.builder()
                                    .distribution(
                                        ImmutableDistribution.builder()
                                            .internalType(DistributionType.categorical)
                                            .addBins(
                                                ImmutableMinMax.builder()
                                                    .lowerBoundary(12)
                                                    .upperBoundary(14)
                                                    .isLowerInclusive(true)
                                                    .isUpperInclusive(true)
                                                    .build())
                                            .addWeights(1.0)
                                            .rng(rng)
                                            .build())
                                    .max(14)
                                    .build())
                            .startOfIsolationTime(CONTACT_TIME)
                            .priority(2)
                            .build())
                    .build())
            .addAlertStatusPolicies(
                ImmutableAlertStatusIsolationProperty.builder()
                    .alertStatus(AlertStatus.REQUESTED_TEST)
                    .isolationProperty(
                        ImmutableIsolationProperty.builder()
                            .id("Requested Test Policy")
                            .isolationProbabilityDistribution(
                                ImmutableBoundedDistribution.builder()
                                    .distribution(
                                        ImmutableDistribution.builder()
                                            .internalType(DistributionType.empirical)
                                            .empiricalSamples(List.of(100))
                                            .rng(rng)
                                            .build())
                                    .max(100)
                                    .build())
                            .isolationTimeDistribution(
                                ImmutableBoundedDistribution.builder()
                                    .distribution(
                                        ImmutableDistribution.builder()
                                            .internalType(DistributionType.categorical)
                                            .addBins(
                                                ImmutableMinMax.builder()
                                                    .lowerBoundary(12)
                                                    .upperBoundary(14)
                                                    .isLowerInclusive(true)
                                                    .isUpperInclusive(true)
                                                    .build())
                                            .addWeights(1.0)
                                            .rng(rng)
                                            .build())
                                    .max(14)
                                    .build())
                            .startOfIsolationTime(CONTACT_TIME)
                            .priority(2)
                            .build())
                    .build())
            .addAlertStatusPolicies(
                ImmutableAlertStatusIsolationProperty.builder()
                    .alertStatus(AlertStatus.AWAITING_RESULT)
                    .isolationProperty(
                        ImmutableIsolationProperty.builder()
                            .id("Awaiting Result Policy")
                            .isolationProbabilityDistribution(
                                ImmutableBoundedDistribution.builder()
                                    .distribution(
                                        ImmutableDistribution.builder()
                                            .internalType(DistributionType.empirical)
                                            .empiricalSamples(List.of(100))
                                            .rng(rng)
                                            .build())
                                    .max(100)
                                    .build())
                            .isolationTimeDistribution(
                                ImmutableBoundedDistribution.builder()
                                    .distribution(
                                        ImmutableDistribution.builder()
                                            .internalType(DistributionType.categorical)
                                            .addBins(
                                                ImmutableMinMax.builder()
                                                    .lowerBoundary(12)
                                                    .upperBoundary(14)
                                                    .isLowerInclusive(true)
                                                    .isUpperInclusive(true)
                                                    .build())
                                            .addWeights(1.0)
                                            .rng(rng)
                                            .build())
                                    .max(14)
                                    .build())
                            .startOfIsolationTime(CONTACT_TIME)
                            .priority(2)
                            .build())
                    .build())
            .addAlertStatusPolicies(
                ImmutableAlertStatusIsolationProperty.builder()
                    .alertStatus(AlertStatus.TESTED_POSITIVE)
                    .isolationProperty(
                        ImmutableIsolationProperty.builder()
                            .id("Tested Positive Policy")
                            .isolationProbabilityDistribution(
                                ImmutableBoundedDistribution.builder()
                                    .distribution(
                                        ImmutableDistribution.builder()
                                            .internalType(DistributionType.empirical)
                                            .empiricalSamples(List.of(100))
                                            .rng(rng)
                                            .build())
                                    .max(100)
                                    .build())
                            .isolationTimeDistribution(
                                ImmutableBoundedDistribution.builder()
                                    .distribution(
                                        ImmutableDistribution.builder()
                                            .internalType(DistributionType.categorical)
                                            .addBins(
                                                ImmutableMinMax.builder()
                                                    .lowerBoundary(12)
                                                    .upperBoundary(14)
                                                    .isLowerInclusive(true)
                                                    .isUpperInclusive(true)
                                                    .build())
                                            .addWeights(1.0)
                                            .rng(rng)
                                            .build())
                                    .max(14)
                                    .build())
                            .startOfIsolationTime(CONTACT_TIME)
                            .priority(2)
                            .build())
                    .build())
            .defaultPolicy(
                ImmutableIsolationProperty.builder()
                    .id("Default Policy")
                    .isolationProbabilityDistribution(
                        ImmutableBoundedDistribution.builder()
                            .distribution(
                                ImmutableDistribution.builder()
                                    .internalType(DistributionType.empirical)
                                    .empiricalSamples(List.of(0))
                                    .rng(rng)
                                    .build())
                            .max(0)
                            .build())
                    .priority(0)
                    .build())
            .isolationProbabilityDistributionThreshold(
                ImmutableBoundedDistribution.builder()
                    .distribution(
                        ImmutableDistribution.builder()
                            .internalType(DistributionType.categorical)
                            .addBins(
                                ImmutableMinMax.builder()
                                    .lowerBoundary(0)
                                    .upperBoundary(100)
                                    .isLowerInclusive(true)
                                    .isUpperInclusive(true)
                                    .build())
                            .addWeights(1.0)
                            .rng(rng)
                            .build())
                    .max(100)
                    .build())
            .build();

    assertThat(actualIsolationProperties).isEqualTo(expectedIsolationProperties);
  }
}
