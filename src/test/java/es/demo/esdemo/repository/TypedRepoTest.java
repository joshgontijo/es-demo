package es.demo.esdemo.repository;

import es.demo.esdemo.form.Form;
import es.demo.esdemo.form.FormEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TypedRepoTest {

    public static final String TEST_STREAM = UUID.randomUUID().toString().substring(0, 5);
    public static final String TEST_TYPE = "test-type";

    @Autowired
    private TypedRepo<Form> db;

    @Test
    void test() {
        var form = fromEvents(
                new FormEvent.FormCreated("123", "brand", "email2@test.com"),
                new FormEvent.PersonAdded("Josh", "josh@email.com"),
                new FormEvent.PersonAdded("Josh", "josh2@email.com"),
                new FormEvent.PersonRemoved("josh3@email.com")
        );

        assertEquals(2, form.people.size());
    }

    @Test
    void test3() {
        var form = fromEvents(
                new FormEvent.FormCreated("123", "brand", "email2@test.com"),
                new FormEvent.PersonAdded("Josh", "josh@email.com"),
                new FormEvent.PersonRemoved("josh2@email.com")
        );

        assertEquals(1, form.people.size());
    }


    private Form fromEvents(FormEvent... event) {
        for (FormEvent e : event) {
            db.append(TEST_STREAM, e, -1);
        }
        return db.get(TEST_STREAM);
    }

}