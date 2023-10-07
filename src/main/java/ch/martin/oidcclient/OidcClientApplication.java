package ch.martin.oidcclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
public class OidcClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(OidcClientApplication.class, args);
    }

}
