package uk.co.ramp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.PopulationGenerator;
import uk.co.ramp.people.VirusStatus;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class Population {
    private final Map<Integer, Case> population;

    @Autowired
    public Population(PopulationGenerator populationGenerator) {
        this.population = populationGenerator.generate();
    }

    public Population(Map<Integer, Case> population) {
        this.population = population;
    }

    public Case get(int id) {
        return population.get(id);
    }

    public double proportionInfectious() {
        return population.values().parallelStream().filter(Case::isInfectious).count() / (double) population.size();
    }

    public Map<Integer, Case> view() {
        return Collections.unmodifiableMap(population);
    }

    public Map<VirusStatus, Integer> getCmptCounts() {

        Map<VirusStatus, Integer> stats = population.values().stream()
                .map(Case::virusStatus)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(e -> 1)));

        Stream.of(VirusStatus.values()).forEach(vs -> stats.putIfAbsent(vs, 0));

        return stats;
    }
}
