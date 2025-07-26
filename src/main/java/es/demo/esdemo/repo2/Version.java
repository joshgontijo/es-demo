package es.demo.esdemo.repo2;

public sealed interface Version
    permits
        Version.Any, Version.Expect
{

    record Any() implements Version {}
    record Expect(long version) implements Version {
        public Expect {
            if (version < 0) {
                throw new IllegalArgumentException("Version must be non-negative");
            }
        }
    }


}