package pl.biketrack;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import pl.biketrack.role.Role;
import pl.biketrack.role.RoleRepository;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@EnableAsync
public class BikeTrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(BikeTrackApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner(RoleRepository roleRepository) {
        return args -> {
            String USER = "USER";
            if (roleRepository.findByName(USER)
                              .isEmpty()) {
                roleRepository.save(Role.builder()
                                        .name(USER)
                                        .build());
            }
        };
    }
}