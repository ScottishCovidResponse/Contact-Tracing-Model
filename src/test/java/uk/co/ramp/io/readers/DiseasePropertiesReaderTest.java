// package uk.co.ramp.io.readers;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.when;
// import static uk.co.ramp.distribution.ProgressionDistribution.FLAT;
//
// import com.google.gson.JsonParser;
// import java.io.*;
// import org.junit.Before;
// import org.junit.Test;
// import uk.co.ramp.io.types.DiseaseProperties;
// import uk.co.ramp.io.types.ImmutableDiseaseProperties;
// import uk.co.ramp.utilities.ImmutableMeanMax;
// import uk.co.ramp.utilities.MeanMax;
// import uk.ramp.api.StandardApi;
//
// public class DiseasePropertiesReaderTest {
//  private static final String mockDiseaseSettings =
//      "{\n"
//          + "  \"timeLatent\": {\n"
//          + "    \"mean\": 5,\n"
//          + "    \"max\": 8\n"
//          + "  },\n"
//          + "  \"timeRecoveryAsymp\": {\n"
//          + "    \"mean\": 5,\n"
//          + "    \"max\": 8\n"
//          + "  },\n"
//          + "  \"timeRecoverySymp\": {\n"
//          + "    \"mean\": 5,\n"
//          + "    \"max\": 8\n"
//          + "  },\n"
//          + "  \"timeRecoverySev\": {\n"
//          + "    \"mean\": 5,\n"
//          + "    \"max\": 8\n"
//          + "  },\n"
//          + "  \"timeSymptomsOnset\": {\n"
//          + "    \"mean\": 5,\n"
//          + "    \"max\": 8\n"
//          + "  },\n"
//          + "  \"timeDecline\": {\n"
//          + "    \"mean\": 5,\n"
//          + "    \"max\": 8\n"
//          + "  },\n"
//          + "  \"timeDeath\": {\n"
//          + "    \"mean\": 5,\n"
//          + "    \"max\": 8\n"
//          + "  },\n"
//          + "  \"timeTestAdministered\": {\n"
//          + "    \"mean\": 5,\n"
//          + "    \"max\": 8\n"
//          + "  },\n"
//          + "  \"timeTestResult\": {\n"
//          + "    \"mean\": 5,\n"
//          + "    \"max\": 8\n"
//          + "  },\n"
//          + "  \"testPositiveAccuracy\": 0.95,\n"
//          + "  \"testNegativeAccuracy\": 0.95,\n"
//          + "  \"exposureThreshold\": 500,\n"
//          + "  \"exposureProbability4UnitContact\": 0.01,\n"
//          + "  \"exposureExponent\": 1.0,\n"
//          + "  \"randomInfectionRate\": 0.05,\n"
//          + "  \"progressionDistribution\": \"FLAT\"\n"
//          + "}";
//
//  private StandardApi dataPipelineApi;
//
//  @Before
//  public void setUp() {
//    dataPipelineApi = mock(StandardApi.class);
//    when(dataPipelineApi.readDistribution(eq("parameter"),
// eq("time-test-administered"))).thenReturn(null);
//  }
//
//  @Test
//  public void testRead() {
//    var underlyingReader = new BufferedReader(new StringReader(mockDiseaseSettings));
//
//    var reader = new DiseasePropertiesReader(dataPipelineApi);
//    DiseaseProperties actualDiseaseProperties = reader.read();
//
//    MeanMax meanMax = ImmutableMeanMax.builder().mean(5).max(8).build();
//
//    var expectedDiseaseProperties =
//        ImmutableDiseaseProperties.builder()
//            .timeLatent(meanMax)
//            .timeRecoveryAsymp(meanMax)
//            .timeRecoverySymp(meanMax)
//            .timeRecoverySev(meanMax)
//            .timeSymptomsOnset(meanMax)
//            .timeDecline(meanMax)
//            .timeDeath(meanMax)
//            .timeTestAdministered(meanMax)
//            .timeTestResult(meanMax)
//            .testPositiveAccuracy(0.95)
//            .testNegativeAccuracy(0.95)
//            .exposureThreshold(500)
//            .exposureProbability4UnitContact(0.01)
//            .exposureExponent(1.0)
//            .progressionDistribution(FLAT)
//            .randomInfectionRate(0.05)
//            .build();
//    assertThat(actualDiseaseProperties).isEqualTo(expectedDiseaseProperties);
//  }
// }
