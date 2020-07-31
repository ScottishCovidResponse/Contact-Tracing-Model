package uk.co.ramp.people;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import uk.co.ramp.TestUtils;

public class AgeRetrieverTest {
  @Test
  public void findAge() throws FileNotFoundException {
    var ageRetriever = new AgeRetriever(TestUtils.populationProperties(), Map.of());
    int n = 20000;
    List<Integer> ages = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      ages.add(ageRetriever.findAge(i));
    }

    double sum = ages.stream().mapToInt(Integer::intValue).average().orElseThrow();
    int max = ages.stream().mapToInt(Integer::intValue).max().orElseThrow();
    int min = ages.stream().mapToInt(Integer::intValue).min().orElseThrow();

    Assert.assertEquals(50d, sum, 0.5);
    Assert.assertTrue(max <= 100);
    Assert.assertTrue(max > 80);
    Assert.assertTrue(min < 20);
    Assert.assertTrue(min >= 0);

    long group0 = ages.stream().filter(a -> a < 20).count();
    long group1 = ages.stream().filter(a -> a >= 20 && a < 40).count();
    long group2 = ages.stream().filter(a -> a >= 40 && a < 60).count();
    long group3 = ages.stream().filter(a -> a >= 60 && a < 80).count();
    long group4 = ages.stream().filter(a -> a >= 80 && a < 100).count();

    Assert.assertEquals(0.2, group0 / (double) n, 0.01);
    Assert.assertEquals(0.2, group1 / (double) n, 0.01);
    Assert.assertEquals(0.2, group2 / (double) n, 0.01);
    Assert.assertEquals(0.2, group3 / (double) n, 0.01);
    Assert.assertEquals(0.2, group4 / (double) n, 0.01);
  }
}
