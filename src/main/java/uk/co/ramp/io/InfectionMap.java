package uk.co.ramp.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.people.Case;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;

public class InfectionMap {

    private static final Logger LOGGER = LogManager.getLogger(InfectionMap.class);
    private final Map<Integer, Case> population;

    public InfectionMap(Map<Integer, Case> population) {
        this.population = population;
    }

    public void outputMap(Writer writer) {

        Map<Integer, Set<Case>> infectors = collectInfectors();

        // initial infections sorted by id
        List<Case> rootInfections = infectors.values().stream()
                .flatMap(Collection::stream)
                .filter(e -> e.exposedBy() == Case.getInitial())
                .sorted(Comparator.comparing(Case::id))
                .collect(Collectors.toList());

        //random infections sorted by exposed time
        rootInfections.addAll(
                infectors.entrySet().stream()
                        .flatMap(e -> e.getValue().stream())
                        .filter(e -> e.exposedBy() == Case.getRandomInfection())
                        .sorted(Comparator.comparing(Case::exposedTime))
                        .collect(Collectors.toList()));


        try {
            recurseSet(rootInfections, infectors, writer, 1);
        } catch (IOException e) {
            String message = "An error occurred while writing the map file: " + e.getMessage();
            LOGGER.error(message);
            throw new InfectionMapException(message);

        }

    }

    void recurseSet(List<Case> target, Map<Integer, Set<Case>> infectors, Writer writer, int tab) throws IOException {

        String spacer = "           ".repeat(tab - 1);

        for (Case seed : target) {
            if (infectors.containsKey(seed.id())) {
                List<Case> newSeeds = new ArrayList<>(infectors.get(seed.id()));
                if (tab == 1) {
                    writer.write(seed.getSource() + "  ->  " + newSeeds.stream().map(Case::getSource).map(String::trim).collect(Collectors.toList()) + "\n");
                } else {
                    writer.write(spacer + "   ->  " + seed.getSource() + "   ->  " + newSeeds.stream().map(Case::getSource).map(String::trim).collect(Collectors.toList()) + "\n");
                }

                recurseSet(newSeeds, infectors, writer, tab + 1);
                if (tab == 1) writer.write("\n");
            }
        }

    }

    Map<Integer, Set<Case>> collectInfectors() {

        List<Case> infections = population.values().stream()
                .filter(c -> c.virusStatus() != SUSCEPTIBLE)
                .sorted(Comparator.comparingInt(Case::exposedTime))
                .collect(Collectors.toList());

        Map<Integer, Set<Case>> infectors = new HashMap<>();

        infections.forEach(i -> {
            int key = i.exposedBy();
            infectors.putIfAbsent(key, new HashSet<>());
            infectors.computeIfPresent(key, (id, set) -> {
                set.add(i);
                return set;
            });
        });
        return infectors;
    }

}
