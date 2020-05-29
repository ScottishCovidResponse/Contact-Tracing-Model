package uk.co.ramp.record;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.io.types.CmptRecord;
import uk.co.ramp.people.VirusStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static uk.co.ramp.people.VirusStatus.*;

public class CmptRecordTest {


    private CmptRecord testRecord;
    private int time;
    private Map<VirusStatus, Integer> counts;
    private int s, e1, e2, i1, i2, r, d;

    @Before
    public void setup() {

        Random random = new Random();
        time = random.nextInt(1000);

        s = random.nextInt(1000);
        e1 = random.nextInt(1000);
        e2 = random.nextInt(1000);
        i1 = random.nextInt(1000);
        i2 = random.nextInt(1000);
        r = random.nextInt(1000);
        d = random.nextInt(1000);


        counts = new HashMap<>();
        counts.put(SUSCEPTIBLE, s);
        counts.put(EXPOSED, e1);
        counts.put(EXPOSED_2, e2);
        counts.put(INFECTED, i1);
        counts.put(INFECTED_SYMP, i2);
        counts.put(RECOVERED, r);
        counts.put(DEAD, d);
        testRecord = CmptRecord.of(time, counts);

    }

    @Test
    public void simple() {

        CmptRecord cmptRecord = useSecondContructor();

        Assert.assertEquals(time, cmptRecord.time());
        Assert.assertEquals((int) counts.get(SUSCEPTIBLE), cmptRecord.s());
        Assert.assertEquals((int) counts.get(EXPOSED), cmptRecord.e1());
        Assert.assertEquals((int) counts.get(EXPOSED_2), cmptRecord.e2());
        Assert.assertEquals((int) counts.get(INFECTED), cmptRecord.ia());
        Assert.assertEquals((int) counts.get(INFECTED_SYMP), cmptRecord.is());
        Assert.assertEquals((int) counts.get(RECOVERED), cmptRecord.r());
        Assert.assertEquals((int) counts.get(DEAD), cmptRecord.d());

    }

    @Test
    public void testToString() {
        String expected = String.format("CmptRecord{time=%d, s=%d, e1=%d, e2=%d, ia=%d, is=%d, r=%d, d=%d}", time, s, e1, e2, i1, i2, r, d);
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

        counts.put(EXPOSED, e1);
        counts.put(INFECTED, -1);
        compareRecord = CmptRecord.of(time, counts);
        Assert.assertNotEquals(testRecord, compareRecord);

        counts.put(INFECTED, i1);
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
        compartmentCounts.put(EXPOSED, e1);
        compartmentCounts.put(EXPOSED_2, e2);
        compartmentCounts.put(INFECTED, i1);
        compartmentCounts.put(INFECTED_SYMP, i2);
        compartmentCounts.put(RECOVERED, r);
        compartmentCounts.put(DEAD, d);

        return CmptRecord.of(time, compartmentCounts);
    }
}