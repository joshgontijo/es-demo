package es.demo.esdemo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonTest {

    record Person(String name, int age) {}

    @Test
    void testJson() {
        var person = new Person("John", 30);
        var json = Json.toJson(person);
        Person from = Json.fromJson(json, Person.class);
        assertEquals(person, from);

    }

    @Test
    void toJson() {
    }
}