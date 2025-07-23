package es.demo.esdemo.form;

import es.demo.esdemo.repo2.AggregateRoot;
import es.demo.esdemo.repo2.Event;
import es.demo.esdemo.repository.Aggregate;

import java.util.HashMap;
import java.util.Map;

public class Form extends AggregateRoot {

    private String id;
    private String brand;
    private String email;

    public final Map<String, Person> people = new HashMap<>();
    private Status status = Status.CREATED;


    @Override
    public void when(Event event) {

    }

    public Form(String id, String brand, String email) {
        this.id = id;
        this.brand = brand;
        this.email = email;
    }


    public void onFormCreated(FormEvent.FormCreated event) {
        this.id = event.uuid();
        this.brand = event.brand();
        this.email = event.email();
    }

    public void onPersonAdded(FormEvent.PersonAdded event) {
        people.put(event.email(), new Person(event.name(), event.email()));
    }

    public void onPersonRemoved(FormEvent.PersonRemoved event) {
        people.remove(event.email());
    }

    public void onFormSubmitted(FormEvent.FormSubmitted event) {
        this.status = Status.SUBMITTED;
    }

    record Person(String name, String email) {
    }

    private enum Status {
        CREATED, SUBMITTED
    }
}