//package es.demo.esdemo.form;
//
//import es.demo.esdemo.repository.DomainEvent;
//
//public sealed interface FormEvent
//        permits
//        FormEvent.FormCreated,
//        FormEvent.PersonAdded,
//        FormEvent.PersonRemoved,
//        FormEvent.FormSubmitted {
//
//
//    /// FormCreated event is triggered when a new form is created.
//    /// A single stream can only have one of this event.
//    @DomainEvent
//    record FormCreated(String uuid, String brand, String email) implements FormEvent {}
//
//    @DomainEvent
//    record PersonAdded(String name, String email) implements FormEvent {}
//
//    @DomainEvent
//    record PersonRemoved(String email) implements FormEvent {}
//
//    /// FormCreated event is triggered when a new form is created.
//    /// A single stream can only have one of this event.
//    @DomainEvent
//    record FormSubmitted() implements FormEvent {}
//
//
//
//
//}