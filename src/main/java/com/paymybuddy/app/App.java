package com.paymybuddy.app;

import com.paymybuddy.app.user.User;
import com.paymybuddy.app.user.UserService;
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
    public CommandLineRunner demo(UserService service) {
        return (args) -> {
            service.deleteAllUsers();

            service.createUser(new User("Jack", "Jack@gmail.com", "password"));
            service.createUser(new User("Chloe", "Chloe@gmail.com", "password"));
            service.createUser(new User("Kim", "Kim@gmail.com", "password"));
            service.createUser(new User("David", "David@gmail.com", "password"));
            service.createUser(new User("Michelle", "Michelle@gmail.com", "password"));

            // fetch all users
            log.info("Customers found with findAll():");
            log.info("-------------------------------");
            service.getAllUsers().forEach(user -> {
                log.info(user.toString());
            });
            log.info("");

            // fetch an individual user by username
            User user = service.getUserByUsername("David");
            log.info("User found with findByUsername(\"David\"):");
            log.info("--------------------------------");
            log.info(user.toString());
            log.info("");

            // fetch users by last name
            log.info("User found with findByEmail('Chloe@gmail.com'):");
            log.info("--------------------------------------------");
            user = service.getUserByEmail("Chloe@gmail.com");
            log.info(user.toString());
            log.info("");

            // create relation then delete one of the users
            log.info("Create reciprocating relations and delete one of the users");
            log.info("--------------------------------------------");
            User user2 = service.getUserByUsername("David");

            user.createRelation(user2);
            service.updateUser(user);

            user2.createRelation(user);
            service.updateUser(user2);

            log.info(user.toString());
            log.info(user2.toString());

            service.deleteUser(user2.getId());
            log.info("");

            user = service.getUserByUsername("Chloe");

            user.createBankAccount("iban1", 500.);
            service.updateUser(user);
        };
    }
}
