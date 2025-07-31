package es.demo.esdemo.repository;

public sealed interface Version
        permits
        Version.Any, Version.Expect {

    static Version any() {
        return new Version.Any();
    }

    static Version expect(long version) {
        return new Version.Expect(version);
    }

    record Any() implements Version {
    }

    record Expect(long version) implements Version {    }

}