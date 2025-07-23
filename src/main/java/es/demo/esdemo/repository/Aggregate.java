package es.demo.esdemo.repository;

public abstract class Aggregate {

    protected int version;

    public int version() {
        return version;
    }
}