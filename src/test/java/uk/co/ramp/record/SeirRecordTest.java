package uk.co.ramp.record;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.Random;

// TODO: may be worth removing this test at some point as 'Immutables' gives us a guarantee of the things we are testing for below out of the box
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
        testRecord = ImmutableSeirRecord.builder()
                .time(time)
                .s(s)
                .e(e)
                .i(i)
                .r(r)
                .build();

    }

    @Test
    public void simple() {

        SeirRecord seirRecord = generateSeirRecord();

        Assert.assertEquals(time, seirRecord.time());
        Assert.assertEquals(s, seirRecord.s());
        Assert.assertEquals(e, seirRecord.e());
        Assert.assertEquals(i, seirRecord.i());
        Assert.assertEquals(r, seirRecord.r());

    }

    @Test
    public void testToString() {
        String expected = String.format("SeirRecord{time=%d, s=%d, e=%d, i=%d, r=%d}", time, s, e, i, r);
        Assert.assertEquals(expected, testRecord.toString());
    }

    @Test
    public void testEquals() {

        // self equal
        Assert.assertEquals(testRecord, testRecord);

        // identical
        SeirRecord compareRecord = ImmutableSeirRecord.builder()
                .time(time)
                .s(s)
                .e(e)
                .i(i)
                .r(r)
                .build();
        Assert.assertEquals(testRecord, compareRecord);

        Assert.assertNotEquals(testRecord, testRecord.toString());
        Assert.assertNotEquals(testRecord, null);

        compareRecord = ImmutableSeirRecord.builder()
                .time(-1)
                .s(s)
                .e(e)
                .i(i)
                .r(r)
                .build();
        Assert.assertNotEquals(testRecord, compareRecord);

        compareRecord = ImmutableSeirRecord.builder()
                .time(time)
                .s(-1)
                .e(e)
                .i(i)
                .r(r)
                .build();
        Assert.assertNotEquals(testRecord, compareRecord);

        compareRecord = ImmutableSeirRecord.builder()
                .time(time)
                .s(s)
                .e(-1)
                .i(i)
                .r(r)
                .build();
        Assert.assertNotEquals(testRecord, compareRecord);

        compareRecord = ImmutableSeirRecord.builder()
                .time(time)
                .s(s)
                .e(e)
                .i(-1)
                .r(r)
                .build();
        Assert.assertNotEquals(testRecord, compareRecord);

        compareRecord = ImmutableSeirRecord.builder()
                .time(time)
                .s(s)
                .e(e)
                .i(i)
                .r(-1)
                .build();
        Assert.assertNotEquals(testRecord, compareRecord);

    }

    @Test
    public void testHashCode() {
        SeirRecord seirRecord = generateSeirRecord();
        int hash1 = testRecord.hashCode();
        int hash2 = seirRecord.hashCode();

        // identical data = identical hash
        Assert.assertEquals(hash1, hash2);

        seirRecord = ImmutableSeirRecord.builder()
                .time(time)
                .s(s)
                .e(e)
                .i(i)
                .r(0)
                .build();
        hash2 = seirRecord.hashCode();

        Assert.assertNotEquals(hash1, hash2);

    }

    private SeirRecord generateSeirRecord() {
        return ImmutableSeirRecord.builder()
                .time(time)
                .s(s)
                .e(e)
                .i(i)
                .r(r)
                .build();
    }
}