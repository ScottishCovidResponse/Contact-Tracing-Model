package uk.co.ramp.record;

import uk.co.ramp.people.VirusStatus;

import java.util.Map;

import static uk.co.ramp.people.VirusStatus.*;

public class SeirRecord {

    private final int time;
    private final int s;
    private final int e;
    private final int i;
    private final int r;

    public SeirRecord(int time, Map<VirusStatus, Integer> seirCounts) {

        this.time = time;
        this.s = seirCounts.get(SUSCEPTIBLE);
        this.e = seirCounts.get(EXPOSED);
        this.i = seirCounts.get(INFECTED);
        this.r = seirCounts.get(RECOVERED);

    }


    public SeirRecord(final int time, final int s, final int e, final int i, final int r) {

        this.time = time;
        this.s = s;
        this.e = e;
        this.i = i;
        this.r = r;

    }

    public int getTime() {
        return time;
    }

    public int getS() {
        return s;
    }

    public int getE() {
        return e;
    }

    public int getI() {
        return i;
    }

    public int getR() {
        return r;
    }
}