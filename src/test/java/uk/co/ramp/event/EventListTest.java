package uk.co.ramp.event;

import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.TestUtils;
import uk.co.ramp.io.ContactReader;
import uk.co.ramp.io.types.StandardProperties;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EventListTest {

    EventList eventList = new EventList();
    Random random = TestUtils.getRandom();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void addEvent() throws IOException {

        ContactReader contactReader = new ContactReader();

        StandardProperties standardProperties = TestUtils.standardProperties();
        Map<Integer, List<ContactEvent>> var = contactReader.readEvents(new FileReader("input/homogeneous_contacts.csv"), standardProperties);

        for (int day : var.keySet()) {
            eventList.addEvents(var.get(day));
        }

        for (int t = 0; t < 10; t++) {
            System.out.println("time = " + t + " there are :" + eventList.getForTime(t).size() + " events");
        }
    }

    @Test
    public void addEvents() {
    }

    @Test
    public void getForTime() {
    }

    @Test
    public void replace() {
    }

    @Test
    public void deleteTime() {
    }
}