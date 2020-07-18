package uk.co.ramp.io;

import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;

import com.google.common.base.Strings;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.people.Case;
import uk.co.ramp.statistics.StatisticsRecorder;

public class InfectionMap {

  private static final Logger LOGGER = LogManager.getLogger(InfectionMap.class);
  private final Map<Integer, Case> population;
  private final StatisticsRecorder statisticsRecorder;

  public InfectionMap(Map<Integer, Case> population, StatisticsRecorder statisticsRecorder) {
    this.population = population;
    this.statisticsRecorder = statisticsRecorder;
  }

  public void outputMap(Writer writer) {

    Map<Integer, List<Case>> infectors = collectInfectors();

    // initial infections sorted by id
    List<Case> rootInfections =
        infectors.values().stream()
            .flatMap(Collection::stream)
            .filter(e -> e.exposedBy() == Case.getInitial())
            .sorted(Comparator.comparing(Case::id))
            .collect(Collectors.toList());

    // random infections sorted by exposed time
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

  void recurseSet(List<Case> target, Map<Integer, List<Case>> infectors, Writer writer, int tab)
      throws IOException {

    String spacer = "           ".repeat(tab - 1);

    for (Case seed : target) {
      if (infectors.containsKey(seed.id())) {
        List<Case> newSeeds = new ArrayList<>(infectors.get(seed.id()));

        statisticsRecorder.recordInfectionSpread(seed, newSeeds.size());
        List<String> infections = getInfections(newSeeds);

        if (tab == 1) {
          writer.write(getSource(seed) + "  ->  " + infections + "\n");
        } else {
          writer.write(spacer + "   ->  " + getSource(seed) + "   ->  " + infections + "\n");
        }

        recurseSet(newSeeds, infectors, writer, tab + 1);
        if (tab == 1) writer.write("\n");

      } else {
        statisticsRecorder.recordInfectionSpread(seed, 0);
      }
    }
  }

  private List<String> getInfections(List<Case> newSeeds) {
    return newSeeds.stream().map(this::getSource).map(String::trim).collect(Collectors.toList());
  }

  Map<Integer, List<Case>> collectInfectors() {

    List<Case> infections =
        population.values().stream()
            .filter(c -> c.virusStatus() != SUSCEPTIBLE)
            .sorted(Comparator.comparingInt(Case::exposedTime))
            .collect(Collectors.toList());

    Map<Integer, List<Case>> infectors = new HashMap<>();

    infections.forEach(
        i -> {
          int key = i.exposedBy();
          infectors.putIfAbsent(key, new ArrayList<>());
          infectors.computeIfPresent(
              key,
              (id, set) -> {
                set.add(i);
                return set;
              });
        });
    return infectors;
  }

  public String getSource(Case c) {
    return Strings.padEnd(c.id() + "(" + c.exposedTime() + ")", 12, ' ');
  }
}
