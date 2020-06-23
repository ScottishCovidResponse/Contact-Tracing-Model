package uk.co.ramp.event;

import static uk.co.ramp.people.VirusStatus.EXPOSED;
import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.Population;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.ImmutableInfectionEvent;
import uk.co.ramp.event.types.InfectionEvent;
import uk.co.ramp.io.InitialCaseReader;
import uk.co.ramp.people.Case;

public class InfectionCreator {
  private static final Logger LOGGER = LogManager.getLogger(InfectionCreator.class);

  private final Population population;
  private final DistributionSampler distributionSampler;
  private final InitialCaseReader initialCaseReader;

  public InfectionCreator(
      Population population,
      DistributionSampler distributionSampler,
      InitialCaseReader initialCaseReader) {
    this.population = population;
    this.distributionSampler = distributionSampler;
    this.initialCaseReader = initialCaseReader;
  }

  List<InfectionEvent> createRandomInfections(
      int time, double randomInfectionRate, double randomCutoff) {
    if (randomInfectionRate > 0d && time < randomCutoff) {
      List<Case> sus =
          population.view().values().stream()
              .filter(aCase -> aCase.virusStatus() == SUSCEPTIBLE)
              .collect(Collectors.toList());

      List<InfectionEvent> randomInfections = new ArrayList<>();
      for (Case aCase : sus) {
        if (distributionSampler.uniformBetweenZeroAndOne() < randomInfectionRate) {
          randomInfections.add(
              ImmutableInfectionEvent.builder()
                  .time(time + 1)
                  .id(aCase.id())
                  .nextStatus(EXPOSED)
                  .oldStatus(SUSCEPTIBLE)
                  .exposedTime(time)
                  .exposedBy(Case.getRandomInfection())
                  .build());
        }
      }

      return randomInfections;
    }
    return List.of();
  }

  List<InfectionEvent> generateInitialInfections(int time) {
    if (time == 0) {
      Set<Integer> infectedIds = initialCaseReader.getCases();
      List<InfectionEvent> virusEvents = new ArrayList<>();

      InfectionEvent genericEvent =
          ImmutableInfectionEvent.builder()
              .exposedBy(Case.getInitial())
              .oldStatus(SUSCEPTIBLE)
              .nextStatus(EXPOSED)
              .exposedTime(0)
              .id(-1)
              .time(0)
              .build();

      infectedIds.forEach(
          id -> virusEvents.add(ImmutableInfectionEvent.copyOf(genericEvent).withId(id)));

      LOGGER.info("Generated initial outbreak of {} cases", virusEvents.size());

      return virusEvents;
    }
    return List.of();
  }
}
