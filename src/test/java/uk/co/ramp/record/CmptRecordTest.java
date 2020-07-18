package uk.co.ramp.record;

import static uk.co.ramp.people.VirusStatus.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.TestUtils;
import uk.co.ramp.io.types.CmptRecord;
import uk.co.ramp.people.VirusStatus;

public class CmptRecordTest {

  private static final double DELTA = 1e-6;
  private CmptRecord testRecord;
  private double time;
  private Map<VirusStatus, Integer> counts;
  private int s, e, p, a, sym, sev, r, d;

  @Before
  public void setup() {

    Random random = TestUtils.getRandom();
    time = ((int) (random.nextDouble() * 100000)) / 100d;

    s = random.nextInt(1000);
    e = random.nextInt(1000);
    p = random.nextInt(1000);
    a = random.nextInt(1000);
    sym = random.nextInt(1000);
    sev = random.nextInt(1000);
    r = random.nextInt(1000);
    d = random.nextInt(1000);

    counts = new HashMap<>();
    counts.put(SUSCEPTIBLE, s);
    counts.put(EXPOSED, e);
    counts.put(ASYMPTOMATIC, a);
    counts.put(PRESYMPTOMATIC, p);
    counts.put(SYMPTOMATIC, sym);
    counts.put(SEVERELY_SYMPTOMATIC, sev);
    counts.put(RECOVERED, r);
    counts.put(DEAD, d);
    testRecord = CmptRecord.of(time, counts);
  }

  @Test
  public void simple() {

    CmptRecord cmptRecord = useSecondContructor();

    Assert.assertEquals(time, cmptRecord.time(), DELTA);
    Assert.assertEquals((int) counts.get(SUSCEPTIBLE), cmptRecord.s());
    Assert.assertEquals((int) counts.get(EXPOSED), cmptRecord.e());
    Assert.assertEquals((int) counts.get(ASYMPTOMATIC), cmptRecord.a());
    Assert.assertEquals((int) counts.get(PRESYMPTOMATIC), cmptRecord.p());
    Assert.assertEquals((int) counts.get(SYMPTOMATIC), cmptRecord.sym());
    Assert.assertEquals((int) counts.get(SEVERELY_SYMPTOMATIC), cmptRecord.sev());
    Assert.assertEquals((int) counts.get(RECOVERED), cmptRecord.r());
    Assert.assertEquals((int) counts.get(DEAD), cmptRecord.d());
  }

  @Test
  public void testToString() {
    String expected =
        String.format(
            "CmptRecord{time=%.2f, s=%d, e=%d, a=%d, p=%d, sym=%d, sev=%d, r=%d, d=%d}",
            time, s, e, a, p, sym, sev, r, d);
    Assert.assertEquals(expected, testRecord.toString());
  }

  @Test
  public void testEquals() {

    // self equal
    Assert.assertEquals(testRecord, testRecord);

    // identical
    CmptRecord compareRecord = CmptRecord.of(time, counts);
    Assert.assertEquals(testRecord, compareRecord);

    Assert.assertNotEquals(testRecord, testRecord.toString());
    Assert.assertNotEquals(testRecord, null);

    compareRecord = CmptRecord.of(-1, counts);
    Assert.assertNotEquals(testRecord, compareRecord);

    counts.put(SUSCEPTIBLE, -1);
    compareRecord = CmptRecord.of(time, counts);
    Assert.assertNotEquals(testRecord, compareRecord);

    counts.put(SUSCEPTIBLE, s);
    counts.put(EXPOSED, -1);
    compareRecord = CmptRecord.of(time, counts);
    Assert.assertNotEquals(testRecord, compareRecord);

    counts.put(EXPOSED, e);
    counts.put(ASYMPTOMATIC, -1);
    compareRecord = CmptRecord.of(time, counts);
    Assert.assertNotEquals(testRecord, compareRecord);

    counts.put(ASYMPTOMATIC, a);
    counts.put(RECOVERED, -1);
    compareRecord = CmptRecord.of(time, counts);
    Assert.assertNotEquals(testRecord, compareRecord);
  }

  @Test
  public void testHashCode() {
    CmptRecord cmptRecord = useSecondContructor();
    int hash1 = testRecord.hashCode();
    int hash2 = cmptRecord.hashCode();

    // identical data = identical hash
    Assert.assertEquals(hash1, hash2);

    counts.put(RECOVERED, 0);
    cmptRecord = CmptRecord.of(time, counts);
    hash2 = cmptRecord.hashCode();

    Assert.assertNotEquals(hash1, hash2);
  }

  private CmptRecord useSecondContructor() {
    Map<VirusStatus, Integer> compartmentCounts = new HashMap<>();
    compartmentCounts.put(SUSCEPTIBLE, s);
    compartmentCounts.put(EXPOSED, e);
    compartmentCounts.put(ASYMPTOMATIC, a);
    compartmentCounts.put(PRESYMPTOMATIC, p);
    compartmentCounts.put(SYMPTOMATIC, sym);
    compartmentCounts.put(SEVERELY_SYMPTOMATIC, sev);
    compartmentCounts.put(RECOVERED, r);
    compartmentCounts.put(DEAD, d);

    return CmptRecord.of(time, compartmentCounts);
  }
}
