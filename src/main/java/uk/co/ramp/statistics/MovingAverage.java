package uk.co.ramp.statistics;

import com.google.common.annotations.VisibleForTesting;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MovingAverage {

  private final Queue<Double> previousDays = new LinkedList<>();
  private final int period;
  private double sum = 0;

  public MovingAverage(int period) {
    this.period = period;
  }

  public void add(double num) {
    sum += num;
    previousDays.add(num);
    if (previousDays.size() > period) {
      sum -= previousDays.remove();
    }
  }

  public double getAverage() {
    if (previousDays.isEmpty()) return 0d;
    if (sum < 1e-6) return 0d;
    double divisor = previousDays.size();
    return sum / divisor;
  }

  @VisibleForTesting
  List<Double> readData() {
    return List.copyOf(previousDays);
  }
}
