package uk.co.ramp.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.ramp.distribution.ProgressionDistribution.FLAT;
import static uk.co.ramp.distribution.ProgressionDistribution.LINEAR;

import java.io.BufferedReader;
import java.io.StringReader;
import org.junit.Test;
import uk.co.ramp.distribution.ImmutableDistribution;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.utilities.MinMax;

public class IsolationPropertiesReaderTest {
  private static final String mockIsolationProperties =
      "{"
          + "  'globalIsolationPolicies': ["
          + "    {"
          + "      'proportionInfected': {"
          + "        'min': 20,"
          + "        'max': 100"
          + "      },"
          + "      'isolationProperty': {"
          + "        'id': 'Stay at Home Policy', "
          + "        'isolationProbabilityDistribution': {"
          + "          'type': 'FLAT',"
          + "          'mean': 90,"
          + "          'max': 90"
          + "        },"
          + "        'isolationTimeDistribution': {"
          + "          'type': 'FLAT',"
          + "          'mean': 1,"
          + "          'max': 1"
          + "        },"
          + "        'priority': 1"
          + "      }"
          + "    },"
          + "    {"
          + "      'proportionInfected': {"
          + "        'min': 10,"
          + "        'max': 20"
          + "      },"
          + "      'isolationProperty': {"
          + "      'id': 'Stay Alert Policy', "
          + "        'isolationProbabilityDistribution': {"
          + "          'type': 'FLAT',"
          + "          'mean': 50,"
          + "          'max': 50"
          + "        },"
          + "        'isolationTimeDistribution': {"
          + "          'type': 'FLAT',"
          + "          'mean': 1,"
          + "          'max': 1"
          + "        },"
          + "        'priority': 1"
          + "      }"
          + "    }"
          + "  ],"
          + "  'virusStatusPolicies': ["
          + "    {"
          + "      'virusStatus': 'SYMPTOMATIC',"
          + "      'isolationProperty': {"
          + "        'id': 'Infected Symptomatic Policy', "
          + "        'isolationProbabilityDistribution': {"
          + "          'type': 'FLAT',"
          + "          'mean': 100,"
          + "          'max': 100"
          + "        },"
          + "        'isolationTimeDistribution': {"
          + "          'type': 'LINEAR',"
          + "          'mean': 10,"
          + "          'max': 14"
          + "        },"
          + "        'priority': 1"
          + "      }"
          + "    }"
          + "  ],"
          + "  'alertStatusPolicies': ["
          + "    {"
          + "      'alertStatus': 'TESTED_POSITIVE',"
          + "      'isolationProperty': {"
          + "        'id': 'Tested Positive Policy', "
          + "        'isolationProbabilityDistribution': {"
          + "          'type': 'FLAT',"
          + "          'mean': 100,"
          + "          'max': 100"
          + "        },"
          + "        'isolationTimeDistribution': {"
          + "          'type': 'LINEAR',"
          + "          'mean': 10,"
          + "          'max': 14"
          + "        },"
          + "        'priority': 1"
          + "      }"
          + "    },"
          + "    {"
          + "      'alertStatus': 'ALERTED',"
          + "      'isolationProperty': {"
          + "      'id': 'Alerted Policy', "
          + "        'isolationProbabilityDistribution': {"
          + "          'type': 'FLAT',"
          + "          'mean': 100,"
          + "          'max': 100"
          + "        },"
          + "        'isolationTimeDistribution': {"
          + "          'type': 'LINEAR',"
          + "          'mean': 10,"
          + "          'max': 14"
          + "        },"
          + "        'priority': 1"
          + "      }"
          + "    }"
          + "  ],"
          + "  'defaultPolicy': {"
          + "    'id': 'Default Policy', "
          + "    'isolationProbabilityDistribution': {"
          + "      'type': 'FLAT',"
          + "      'mean': 0,"
          + "      'max': 0"
          + "    },"
          + "    'isolationTimeDistribution': {"
          + "      'type': 'FLAT',"
          + "      'mean': 1,"
          + "      'max': 1"
          + "    },"
          + "    'priority': 0"
          + "  },"
          + "  'isolationProbabilityDistributionThreshold': {"
          + "    'type': 'LINEAR',"
          + "    'mean': 50,"
          + "    'max': 100"
          + "  }"
          + "}";

  @Test
  public void testRead() {
    var underlyingReader = new BufferedReader(new StringReader(mockIsolationProperties));

    var reader = new IsolationPropertiesReader();
    IsolationProperties actualIsolationProperties = reader.read(underlyingReader);

    var expectedIsolationProperties =
        ImmutableIsolationProperties.builder()
            .addGlobalIsolationPolicies(
                ImmutableProportionInfectedIsolationProperty.builder()
                    .proportionInfected(MinMax.of(20, 100))
                    .isolationProperty(
                        ImmutableIsolationProperty.builder()
                            .id("Stay at Home Policy")
                            .isolationProbabilityDistribution(
                                ImmutableDistribution.builder().type(FLAT).mean(90).max(90).build())
                            .isolationTimeDistribution(
                                ImmutableDistribution.builder().type(FLAT).mean(1).max(1).build())
                            .priority(1)
                            .build())
                    .build())
            .addGlobalIsolationPolicies(
                ImmutableProportionInfectedIsolationProperty.builder()
                    .proportionInfected(MinMax.of(10, 20))
                    .isolationProperty(
                        ImmutableIsolationProperty.builder()
                            .id("Stay Alert Policy")
                            .isolationProbabilityDistribution(
                                ImmutableDistribution.builder().type(FLAT).mean(50).max(50).build())
                            .isolationTimeDistribution(
                                ImmutableDistribution.builder().type(FLAT).mean(1).max(1).build())
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
                                ImmutableDistribution.builder()
                                    .type(FLAT)
                                    .mean(100)
                                    .max(100)
                                    .build())
                            .isolationTimeDistribution(
                                ImmutableDistribution.builder()
                                    .type(LINEAR)
                                    .mean(10)
                                    .max(14)
                                    .build())
                            .priority(1)
                            .build())
                    .build())
            .addAlertStatusPolicies(
                ImmutableAlertStatusIsolationProperty.builder()
                    .alertStatus(AlertStatus.TESTED_POSITIVE)
                    .isolationProperty(
                        ImmutableIsolationProperty.builder()
                            .id("Tested Positive Policy")
                            .isolationProbabilityDistribution(
                                ImmutableDistribution.builder()
                                    .type(FLAT)
                                    .mean(100)
                                    .max(100)
                                    .build())
                            .isolationTimeDistribution(
                                ImmutableDistribution.builder()
                                    .type(LINEAR)
                                    .mean(10)
                                    .max(14)
                                    .build())
                            .priority(1)
                            .build())
                    .build())
            .addAlertStatusPolicies(
                ImmutableAlertStatusIsolationProperty.builder()
                    .alertStatus(AlertStatus.ALERTED)
                    .isolationProperty(
                        ImmutableIsolationProperty.builder()
                            .id("Alerted Policy")
                            .isolationProbabilityDistribution(
                                ImmutableDistribution.builder()
                                    .type(FLAT)
                                    .mean(100)
                                    .max(100)
                                    .build())
                            .isolationTimeDistribution(
                                ImmutableDistribution.builder()
                                    .type(LINEAR)
                                    .mean(10)
                                    .max(14)
                                    .build())
                            .priority(1)
                            .build())
                    .build())
            .defaultPolicy(
                ImmutableIsolationProperty.builder()
                    .id("Default Policy")
                    .isolationProbabilityDistribution(
                        ImmutableDistribution.builder().type(FLAT).mean(0).max(0).build())
                    .isolationTimeDistribution(
                        ImmutableDistribution.builder().type(FLAT).mean(1).max(1).build())
                    .priority(0)
                    .build())
            .isolationProbabilityDistributionThreshold(
                ImmutableDistribution.builder().type(LINEAR).mean(50).max(100).build())
            .build();

    assertThat(actualIsolationProperties).isEqualTo(expectedIsolationProperties);
  }
}
