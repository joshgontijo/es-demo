package es.demo.esdemo;

import es.demo.esdemo.repo2.EventStore;
import es.demo.esdemo.repository.EventRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EventController {

    private final EventStore store;

    public EventController(EventStore store) {
        this.store = store;
    }


    @GetMapping("/events")
    public String events(@RequestParam(value = "sequence", defaultValue = "0") long startSequence,
                         @RequestParam(value = "limit", defaultValue = "500") int limit) {

        return "TODO: Implement event listing logic";



    }

    @GetMapping("/streams/{streamId}")
    public String stream(@PathVariable String streamId,
                         @RequestParam(value = "offset", defaultValue = "0") int offset,
                         @RequestParam(value = "limit", defaultValue = "500") int limit) {
        return "stream"; // Return the view name for the stream
    }

}