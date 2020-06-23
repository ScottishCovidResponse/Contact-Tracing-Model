package uk.co.ramp.event;

import java.io.IOException;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.ramp.TestUtils;

public class EventListTest {

  @Autowired EventList eventList;
  Random random = TestUtils.getRandom();

  @Before
  public void setUp() throws Exception {}

  @Test
  public void addEvent() throws IOException {}

  @Test
  public void addEvents() {}

  @Test
  public void getForTime() {}

  @Test
  public void replace() {}

  @Test
  public void deleteTime() {}
}
