package es.demo.esdemo.repo2;

public sealed interface ExpectedVersion
    permits
        ExpectedVersion.Any, ExpectedVersion.Of
{

    record Any() implements ExpectedVersion {}
    record Of(long version) implements ExpectedVersion {
        public Of {
            if (version < 0) {
                throw new IllegalArgumentException("Version must be non-negative");
            }
        }
    }


}