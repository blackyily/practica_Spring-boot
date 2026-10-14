package com.ejemplo.agenda.config;

import com.ejemplo.agenda.model.Contact;
import com.ejemplo.agenda.model.Role;
import com.ejemplo.agenda.model.User;
import com.ejemplo.agenda.repository.ContactRepository;
import com.ejemplo.agenda.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ContactRepository contactRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Inicializando datos de prueba...");

            User demoUser = User.builder()
                    .username("demo")
                    .email("demo@ejemplo.com")
                    .password(passwordEncoder.encode("demo123"))
                    .role(Role.ROLE_USER)
                    .build();

            User savedUser = userRepository.save(demoUser);

            Contact c1 = Contact.builder()
                    .name("Dra. Beatriz Solís")
                    .phone("+52 55 1234 5678")
                    .email("beatriz.solis@hospital.org")
                    .address("Av. Universidad 1200, CDMX")
                    .user(savedUser)
                    .build();

            Contact c2 = Contact.builder()
                    .name("Ing. Mateo Valdés")
                    .phone("+52 81 8765 4321")
                    .email("m.valdes@techcorp.io")
                    .address("Calzada del Valle 450, Monterrey")
                    .user(savedUser)
                    .build();

            contactRepository.save(c1);
            contactRepository.save(c2);

            log.info("Usuario precargado: 'demo' (contraseña: 'demo123') con 2 contactos asignados.");
        }
    }
}
