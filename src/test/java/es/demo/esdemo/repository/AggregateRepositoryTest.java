package es.demo.esdemo.repository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.util.UUID;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AggregateRepositoryTest {

    public static final String TEST_STREAM = UUID.randomUUID().toString().substring(0, 5);
    public static final String TEST_TYPE = "test-type";

    @Test
    void test() throws IOException {
        var resources = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader()).getResources("es.demo.esdemo.proto");
        for (var resource : resources) {
            System.out.println("Found resource: " + resource.getFilename());
        }
    }

    //    @Autowired
//    private AggregateRepository<Form> db;

//    @Test
//    void test() {
//        var form = fromEvents(
//                new FormEvent.FormCreated("123", "brand", "email2@test.com"),
//                new FormEvent.PersonAdded("Josh", "josh@email.com"),
//                new FormEvent.PersonAdded("Josh", "josh2@email.com"),
//                new FormEvent.PersonRemoved("josh3@email.com")
//        );
//
//        assertEquals(2, form.people.size());
//    }
//
//    @Test
//    void test3() {
//        var form = fromEvents(
//                new FormEvent.FormCreated("123", "brand", "email2@test.com"),
//                new FormEvent.PersonAdded("Josh", "josh@email.com"),
//                new FormEvent.PersonRemoved("josh2@email.com")
//        );
//
//        assertEquals(1, form.people.size());
//    }
//
//
//    private Form fromEvents(FormEvent... event) {
//        for (FormEvent e : event) {
//            db.append(TEST_STREAM, e, new Version.Expect(0));
//        }
////        return db.get(TEST_STREAM);
//        throw new RuntimeException("Not implemented yet");
//    }

}