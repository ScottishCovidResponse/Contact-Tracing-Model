package uk.co.ramp.record;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import uk.co.ramp.people.VirusStatus;

import java.util.Map;

import static uk.co.ramp.people.VirusStatus.*;

@Value.Immutable
@JsonSerialize
@JsonDeserialize
@JsonPropertyOrder({"time", "s", "e1", "e2", "ia", "is", "r", "d"})
public interface CmptRecord {

    static CmptRecord of(int time, Map<VirusStatus, Integer> counts) {
        return ImmutableCmptRecord.builder().
                time(time).
                s(counts.get(SUSCEPTIBLE)).
                e1(counts.get(EXPOSED)).
                e2(counts.get(EXPOSED_2)).
                ia(counts.get(INFECTED)).
                is(counts.get(INFECTED_SYMP)).
                r(counts.get(RECOVERED)).
                d(counts.get(DEAD)).build();
    }

    int time();

    int s();

    int e1();

    int e2();

    int ia();

    int is();

    int r();

    int d();
}