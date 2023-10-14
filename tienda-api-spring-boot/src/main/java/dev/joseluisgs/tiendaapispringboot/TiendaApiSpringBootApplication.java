package dev.joseluisgs.tiendaapispringboot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TiendaApiSpringBootApplication implements CommandLineRunner {

    public static void main(String[] args) {
        // Iniciamos la aplicación de Spring Boot
        SpringApplication.run(TiendaApiSpringBootApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Aquí podemos ejecutar código al arrancar la aplicación
        System.out.println("🟢 Servidor arrancado 🚀");
    }
}
