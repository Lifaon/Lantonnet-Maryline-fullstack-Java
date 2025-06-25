package com.paymybuddy.app;

import com.paymybuddy.app.models.User;
import com.paymybuddy.app.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        SpringApplication.run(App.class);
    }

    @Bean
    public CommandLineRunner demo(UserRepository repository) {
        return (args) -> {
            // save a few users
            repository.deleteAll();

            repository.save(new User("Jack", "Jack@gmail.com", "password"));
            repository.save(new User("Chloe", "Chloe@gmail.com", "password"));
            repository.save(new User("Kim", "Kim@gmail.com", "password"));
            repository.save(new User("David", "David@gmail.com", "password"));
            repository.save(new User("Michelle", "Michelle@gmail.com", "password"));

            // fetch all users
            log.info("Customers found with findAll():");
            log.info("-------------------------------");
            repository.findAll().forEach(user -> {
                log.info(user.toString());
            });
            log.info("");

            // fetch an individual user by username
            User user = repository.findByUsername("David");
            log.info("User found with findByUsername(\"David\"):");
            log.info("--------------------------------");
            log.info(user.toString());
            log.info("");

            // fetch users by last name
            log.info("User found with findByEmail('Chloe@gmail.com'):");
            log.info("--------------------------------------------");
            user = repository.findByEmail("Chloe@gmail.com");
            log.info(user.toString());
            log.info("");

            // create connection then delete it
            User user2 = repository.findByUsername("David");

            user.addConnection(user2);
            repository.save(user);

            repository.delete(user2);
        };
    }
}
