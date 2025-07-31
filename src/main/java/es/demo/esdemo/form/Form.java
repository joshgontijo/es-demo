package es.demo.esdemo.form;

import es.demo.esdemo.repository.Aggregate;

public class Form extends Aggregate {

    private String id;
    private String brand;
    private String email;

    public Form(String id, String brand, String email) {
        this.id = id;
        this.brand = brand;
        this.email = email;
    }

//    @Override
//    public void when(EventRecord event) {
//
//    }
//
//    public void onFormCreated(FormProto.FormCreated event) {
//        this.id = event.getUuid();
//        this.brand = event.getBrandName();
//        this.email = event.getEmail();
//    }
//
//    public void onPersonAdded(FormEvent.PersonAdded event) {
//        people.put(event.email(), new Person(event.name(), event.email()));
//    }
//
//    public void onPersonRemoved(FormEvent.PersonRemoved event) {
//        people.remove(event.email());
//    }
//
//    public void onFormSubmitted(FormEvent.FormSubmitted event) {
//        this.status = Status.SUBMITTED;
//    }
//
//
//
//    record Person(String name, String email) {
//    }
//
//    private enum Status {
//        CREATED, SUBMITTED
//    }
}