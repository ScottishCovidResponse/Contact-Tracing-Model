package uk.co.ramp.event;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class EventList {

    Map<Integer, List<Event>> map = new HashMap<>();

    //create
    public void addEvent(Event e) {
        map.computeIfAbsent(e.time(), k -> new ArrayList<>()).add(e);
    }

    public void addEvents(List<ContactEvent> events) {
        events.forEach(this::addEvent);
    }

    public void addEvents(Map<Integer, List<ContactEvent>> readEvents) {
        for (int key : readEvents.keySet()) {
            bulkAdd(key, readEvents.get(key));
        }
    }

    private void bulkAdd(int time, List<ContactEvent> events) {
        map.computeIfAbsent(time, k -> new ArrayList<>()).addAll(events);
    }

    //read
    public List<Event> getForTime(int time) {
        return map.getOrDefault(time, new ArrayList<>());
    }

    public Map<Integer, List<Event>> getMap() {
        return map;
    }

    //update
    public void replace(int time, List<Event> events) {
        map.replace(time, events);
    }


    //delete
    public void deleteTime(int time) {
        map.remove(time);
    }


}
