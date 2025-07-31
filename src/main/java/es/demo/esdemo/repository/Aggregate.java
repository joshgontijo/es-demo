package es.demo.esdemo.repository;

public abstract class Aggregate {

    long version;

    public long version() {
        return version;
    }

    public Aggregate() {
    }
}