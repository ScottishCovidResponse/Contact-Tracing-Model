package uk.co.ramp.utilities;


import uk.co.ramp.people.Case;
import uk.co.ramp.people.VirusStatus;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UtilitiesBean {


    public Map<VirusStatus, Integer> getCmptCounts(Map<Integer, Case> population) {

        Map<VirusStatus, Integer> stats = population.values().stream()
                .map(Case::virusStatus)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(e -> 1)));

        Stream.of(VirusStatus.values()).forEach(vs -> stats.putIfAbsent(vs, 0));

        return stats;
    }

    public Case getMostSevere(Case personA, Case personB) {
        int a = personA.virusStatus().getVal();
        int b = personB.virusStatus().getVal();

        return a > b ? personA : personB;
    }

}
