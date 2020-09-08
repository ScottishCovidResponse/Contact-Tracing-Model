package uk.co.ramp.statistics;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.TestUtils;

public class MovingAverageTest {

  private final Random random = TestUtils.getRandom();
  private List<Double> data;
  private MovingAverage movingAverage;
  private int duration;

  @Before
  public void setUp() {
    movingAverage = new MovingAverage(duration);
    duration = random.nextInt(200);
    data = generateData(duration * 2);
  }

  private List<Double> generateData(int points) {

    data = new ArrayList<>();
    for (int i = 0; i < points; i++) {
      data.add(random.nextDouble());
    }
    return data;
  }

  @Test
  public void add() {

    assertThat(movingAverage.getAverage()).isEqualTo(0d);
    for (int i = 0; i < data.size(); i++) {

      movingAverage.add(data.get(i));
      var store = movingAverage.readData();
      assertThat(store.size()).isLessThanOrEqualTo(duration);
      int bottom = Math.max(i - duration, 0);
      for (int j = i; j > bottom; j--) {
        assertThat(store.contains(data.get(j)));
      }
    }
  }

  @Test
  public void getAverage() {

    double sum = 0d;
    for (int i = 0; i < data.size(); i++) {
      double d = data.get(i);
      sum += d;
      movingAverage.add(d);
      if (i >= duration) sum -= data.get(i - duration);
      assertThat(movingAverage.readData().size()).isLessThanOrEqualTo(duration);
      if (movingAverage.readData().size() == 0) {
        assertThat(movingAverage.getAverage()).isEqualTo(0d, Offset.offset(1e-6));
      } else {
        assertThat(movingAverage.getAverage())
            .isEqualTo(sum / movingAverage.readData().size(), Offset.offset(1e-6));
      }
    }
  }
}
