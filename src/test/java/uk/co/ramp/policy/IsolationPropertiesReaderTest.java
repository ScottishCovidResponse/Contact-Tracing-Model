package uk.co.ramp.policy;

import org.junit.Test;
import uk.co.ramp.distribution.ImmutableDistribution;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.utilities.MinMax;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.ramp.distribution.ProgressionDistribution.FLAT;
import static uk.co.ramp.distribution.ProgressionDistribution.LINEAR;

public class IsolationPropertiesReaderTest {
    private static final String mockIsolationProperties = "{" +
            "  'globalIsolationPolicies': {" +
            "    'Stay at Home Policy': {" +
            "      'proportionInfected': {" +
            "        'min': 20," +
            "        'max': 100" +
            "      }," +
            "      'isolationProperty': {" +
            "        'isolationProbabilityDistribution': {" +
            "          'type': 'FLAT'," +
            "          'mean': 90," +
            "          'max': 90" +
            "        }," +
            "        'isolationTimeDistribution': {" +
            "          'type': 'FLAT'," +
            "          'mean': 1," +
            "          'max': 1" +
            "        }," +
            "        'priority': 1" +
            "      }" +
            "    }," +
            "    'Stay Alert Policy': {" +
            "      'proportionInfected': {" +
            "        'min': 10," +
            "        'max': 20" +
            "      }," +
            "      'isolationProperty': {" +
            "        'isolationProbabilityDistribution': {" +
            "          'type': 'FLAT'," +
            "          'mean': 50," +
            "          'max': 50" +
            "        }," +
            "        'isolationTimeDistribution': {" +
            "          'type': 'FLAT'," +
            "          'mean': 1," +
            "          'max': 1" +
            "        }," +
            "        'priority': 1" +
            "      }" +
            "    }" +
            "  }," +
            "  'individualIsolationPolicies': {" +
            "    'virusStatusPolicies': {" +
            "      'Infected Symptomatic Policy': {" +
            "        'virusStatus': 'INFECTED_SYMP'," +
            "        'isolationProperty': {" +
            "          'isolationProbabilityDistribution': {" +
            "            'type': 'FLAT'," +
            "            'mean': 100," +
            "            'max': 100" +
            "          }," +
            "          'isolationTimeDistribution': {" +
            "            'type': 'LINEAR'," +
            "            'mean': 10," +
            "            'max': 14" +
            "          }," +
            "          'priority': 1" +
            "        }" +
            "      }" +
            "    }," +
            "    'alertStatusPolicies': {" +
            "      'Tested Positive Policy': {" +
            "        'alertStatus': 'TESTED_POSITIVE'," +
            "        'isolationProperty': {" +
            "          'isolationProbabilityDistribution': {" +
            "            'type': 'FLAT'," +
            "            'mean': 100," +
            "            'max': 100" +
            "          }," +
            "          'isolationTimeDistribution': {" +
            "            'type': 'LINEAR'," +
            "            'mean': 10," +
            "            'max': 14" +
            "          }," +
            "          'priority': 1" +
            "        }" +
            "      }," +
            "      'Alerted Policy': {" +
            "        'alertStatus': 'ALERTED'," +
            "        'isolationProperty': {" +
            "          'isolationProbabilityDistribution': {" +
            "            'type': 'FLAT'," +
            "            'mean': 100," +
            "            'max': 100" +
            "          }," +
            "          'isolationTimeDistribution': {" +
            "            'type': 'LINEAR'," +
            "            'mean': 10," +
            "            'max': 14" +
            "          }," +
            "          'priority': 1" +
            "        }" +
            "      }" +
            "    }" +
            "  }," +
            "  'defaultPolicy': {" +
            "    'isolationProbabilityDistribution': {" +
            "      'type': 'FLAT'," +
            "      'mean': 0," +
            "      'max': 0" +
            "    }," +
            "    'isolationTimeDistribution': {" +
            "      'type': 'FLAT'," +
            "      'mean': 1," +
            "      'max': 1" +
            "    }," +
            "    'priority': 0" +
            "  }," +
            "  'isolationProbabilityDistributionThreshold': 50" +
            "}";

    @Test
    public void testRead() {
        var underlyingReader = new BufferedReader(new StringReader(mockIsolationProperties));

        var reader = new IsolationPropertiesReader();
        IsolationProperties actualIsolationProperties = reader.read(underlyingReader);

        var expectedIsolationProperties = ImmutableIsolationProperties.builder()
                .putGlobalIsolationPolicies("Stay at Home Policy", ImmutableProportionInfectedIsolationProperty.builder()
                        .proportionInfected(MinMax.of(20, 100))
                        .isolationProperty(ImmutableIsolationProperty.builder()
                                .isolationProbabilityDistribution(ImmutableDistribution.builder()
                                        .type(FLAT)
                                        .mean(90)
                                        .max(90)
                                        .build())
                                .isolationTimeDistribution(ImmutableDistribution.builder()
                                        .type(FLAT)
                                        .mean(1)
                                        .max(1)
                                        .build())
                                .priority(1)
                                .build())
                        .build())
                .putGlobalIsolationPolicies("Stay Alert Policy", ImmutableProportionInfectedIsolationProperty.builder()
                        .proportionInfected(MinMax.of(10, 20))
                        .isolationProperty(ImmutableIsolationProperty.builder()
                                .isolationProbabilityDistribution(ImmutableDistribution.builder()
                                        .type(FLAT)
                                        .mean(50)
                                        .max(50)
                                        .build())
                                .isolationTimeDistribution(ImmutableDistribution.builder()
                                        .type(FLAT)
                                        .mean(1)
                                        .max(1)
                                        .build())
                                .priority(1)
                                .build())
                        .build())
                .individualIsolationPolicies(ImmutableIndividualIsolationPolicies.builder()
                        .putVirusStatusPolicies("Infected Symptomatic Policy", ImmutableVirusStatusIsolationProperty.builder()
                                .virusStatus(VirusStatus.INFECTED_SYMP)
                                .isolationProperty(ImmutableIsolationProperty.builder()
                                        .isolationProbabilityDistribution(ImmutableDistribution.builder()
                                                .type(FLAT)
                                                .mean(100)
                                                .max(100)
                                                .build())
                                        .isolationTimeDistribution(ImmutableDistribution.builder()
                                                .type(LINEAR)
                                                .mean(10)
                                                .max(14)
                                                .build())
                                        .priority(1)
                                        .build())
                                .build())
                        .putAlertStatusPolicies("Tested Positive Policy", ImmutableAlertStatusIsolationProperty.builder()
                                .alertStatus(AlertStatus.TESTED_POSITIVE)
                                .isolationProperty(ImmutableIsolationProperty.builder()
                                        .isolationProbabilityDistribution(ImmutableDistribution.builder()
                                                .type(FLAT)
                                                .mean(100)
                                                .max(100)
                                                .build())
                                        .isolationTimeDistribution(ImmutableDistribution.builder()
                                                .type(LINEAR)
                                                .mean(10)
                                                .max(14)
                                                .build())
                                        .priority(1)
                                        .build())
                                .build())
                        .putAlertStatusPolicies("Alerted Policy", ImmutableAlertStatusIsolationProperty.builder()
                                .alertStatus(AlertStatus.ALERTED)
                                .isolationProperty(ImmutableIsolationProperty.builder()
                                        .isolationProbabilityDistribution(ImmutableDistribution.builder()
                                                .type(FLAT)
                                                .mean(100)
                                                .max(100)
                                                .build())
                                        .isolationTimeDistribution(ImmutableDistribution.builder()
                                                .type(LINEAR)
                                                .mean(10)
                                                .max(14)
                                                .build())
                                        .priority(1)
                                        .build())
                                .build())
                        .build())
                .defaultPolicy(ImmutableIsolationProperty.builder()
                        .isolationProbabilityDistribution(ImmutableDistribution.builder()
                                .type(FLAT)
                                .mean(0)
                                .max(0)
                                .build())
                        .isolationTimeDistribution(ImmutableDistribution.builder()
                                .type(FLAT)
                                .mean(1)
                                .max(1)
                                .build())
                        .priority(0)
                        .build())
                .isolationProbabilityDistributionThreshold(50)
                .build();

        assertThat(actualIsolationProperties).isEqualTo(expectedIsolationProperties);
    }
}