package es.demo.esdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication
public class EsDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsDemoApplication.class, args);
    }


//    @Bean
//    public TypedRepo<Form> formRepo(EventRepository repo) {
//        return new TypedRepo<>(repo, Form.class);
//    }

}