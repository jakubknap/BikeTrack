package pl.biketrack.security;

import org.springframework.security.core.context.SecurityContextHolder;
import pl.biketrack.user.User;

import java.util.UUID;

import static java.util.Objects.nonNull;

public class SecurityUtils {

    public static User getLoggedUser() {
        Object principal = null;

        if (nonNull(SecurityContextHolder.getContext()) && nonNull(SecurityContextHolder.getContext()
                                                                                        .getAuthentication())) {
            principal = SecurityContextHolder.getContext()
                                             .getAuthentication()
                                             .getPrincipal();
        }

        SecurityContextHolder.getContext()
                             .getAuthentication()
                             .getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        }
        throw new SecurityException("User is not logged in");
    }

    public static UUID getLoggedUserUUID() {
        return getLoggedUser().getUuid();
    }
}