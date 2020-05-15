package uk.co.ramp.record;

import uk.co.ramp.people.VirusStatus;

import java.util.Map;
import java.util.Objects;

import static uk.co.ramp.people.VirusStatus.*;

public class CmptRecord {

    private final int time;
    private final int s;
    private final int e1;
    private final int e2;
    private final int iAsymp;
    private final int iSymp;
    private final int r;
    private final int d;

    public CmptRecord(int time, Map<VirusStatus, Integer> seirCounts) {

        this.time = time;
        this.s = seirCounts.get(SUSCEPTIBLE);
        this.e1 = seirCounts.get(EXPOSED);
        this.e2 = seirCounts.get(EXPOSED_2);
        this.iAsymp = seirCounts.get(INFECTED);
        this.iSymp = seirCounts.get(INFECTED_SYMP);
        this.r = seirCounts.get(RECOVERED);
        this.d = seirCounts.get(DEAD);

    }

    public int getTime() {
        return time;
    }

    public int getS() {
        return s;
    }

    public int getE1() {
        return e1;
    }

    public int getE2() {
        return e2;
    }

    public int getiAsymp() {
        return iAsymp;
    }

    public int getiSymp() {
        return iSymp;
    }

    public int getR() {
        return r;
    }


    public int getD() {
        return d;
    }

    @Override
    public String toString() {
        return "SeirRecord{" +
                "time=" + time +
                ", s=" + s +
                ", e1=" + e1 +
                ", e2=" + e2 +
                ", iAsymp=" + iAsymp +
                ", iSymp=" + iSymp +
                ", r=" + r +
                ", d=" + d +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CmptRecord that = (CmptRecord) o;
        return time == that.time &&
                s == that.s &&
                e1 == that.e1 &&
                e2 == that.e2 &&
                iAsymp == that.iAsymp &&
                iSymp == that.iSymp &&
                r == that.r &&
                d == that.d;
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, s, e1, e2, iAsymp, iSymp, r, d);
    }
}
