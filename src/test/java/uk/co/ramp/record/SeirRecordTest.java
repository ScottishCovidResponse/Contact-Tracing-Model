package uk.co.ramp.record;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.people.VirusStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SeirRecordTest {


    private SeirRecord testRecord;
    private int time;
    private int s;
    private int e;
    private int i;
    private int r;

    @Before
    public void setup() {

        Random random = new Random();
        time = random.nextInt(1000);
        s = random.nextInt(1000);
        e = random.nextInt(1000);
        i = random.nextInt(1000);
        r = random.nextInt(1000);
        testRecord = new SeirRecord(time, s, e, i, r);

    }

    @Test
    public void simple() {

        SeirRecord seirRecord = useSecondContructor();

        Assert.assertEquals(time, seirRecord.getTime());
        Assert.assertEquals(s, seirRecord.getS());
        Assert.assertEquals(e, seirRecord.getE());
        Assert.assertEquals(i, seirRecord.getI());
        Assert.assertEquals(r, seirRecord.getR());

    }

    @Test
    public void testTestToString() {
        String expected = String.format("SeirRecord{time=%d, s=%d, e=%d, i=%d, r=%d}", time, s, e, i, r);
        Assert.assertEquals(expected, testRecord.toString());
    }

    @Test
    public void testTestEquals() {

        // identical
        SeirRecord compareRecord = new SeirRecord(time, s, e, i, r);
        Assert.assertEquals(testRecord, compareRecord);

        Assert.assertNotEquals(testRecord, testRecord.toString());
        Assert.assertNotEquals(testRecord, null);

    }

    @Test
    public void testTestHashCode() {
        SeirRecord seirRecord = useSecondContructor();
        int hash1 = testRecord.hashCode();
        int hash2 = seirRecord.hashCode();

        // identical data = identical hash
        Assert.assertEquals(hash1, hash2);

        seirRecord = new SeirRecord(time, s, e, i, 0);

        hash1 = testRecord.hashCode();
        hash2 = seirRecord.hashCode();

        Assert.assertNotEquals(hash1, hash2);

    }

    private SeirRecord useSecondContructor() {
        Map<VirusStatus, Integer> seirCounts = new HashMap<>();
        seirCounts.put(VirusStatus.SUSCEPTIBLE, s);
        seirCounts.put(VirusStatus.EXPOSED, e);
        seirCounts.put(VirusStatus.INFECTED, i);
        seirCounts.put(VirusStatus.RECOVERED, r);

        return new SeirRecord(time, seirCounts);
    }
}