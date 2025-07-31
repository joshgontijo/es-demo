package es.demo.esdemo.form;

import es.demo.esdemo.repo2.Event;
import es.demo.esdemo.repo2.EventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
class FormController {

    private static final Logger log = LoggerFactory.getLogger(FormController.class);
    private final EventStore store;

    FormController(EventStore store) {
        this.store = store;
    }

    @PostMapping("/forms")
    public ResponseEntity<CreateFormResponse> createForm(@RequestBody AddPersonRequest request) {
        var formId = UUID.randomUUID().toString().substring(0, 8);
//        var event = new FormEvent.FormCreated(formId, request.name, request.email);

//        var result = store.append(formId, new Event("FORM_CREATED", ), 0);
//        log.info("Form created: {}", result);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new CreateFormResponse(formId));
    }


    @PostMapping("/forms/{formId}/people")
    public ResponseEntity<AddPersonResponse> post(@PathVariable String formId,
                                                  @RequestBody AddPersonRequest request) {
//        var form = store.load(formId, Form.class);
//        form.people.put(request.email, new Form.Person(request.name, request.email));

        return ResponseEntity.ok(
                new AddPersonResponse(UUID.randomUUID().toString().substring(0, 8))
        );
    }

    @DeleteMapping("/forms/{formId}/people/{email}")
    public ResponseEntity<AddPersonResponse> deletePerson(@PathVariable String formId, @PathVariable String email) {

//        var form = db.get(formId);
//        if (!form.people.containsKey(email)) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
//
//        db.append(formId, new FormEvent.PersonRemoved(email), form.version());

        return ResponseEntity.ok(
                new AddPersonResponse(UUID.randomUUID().toString().substring(0, 8))
        );
    }


    public record AddPersonRequest(String name, String email) {
    }

    public record AddPersonResponse(String id) {
    }

    public record CreateForm(String message) {
    }

    public record CreateFormResponse(String id) {
    }


}