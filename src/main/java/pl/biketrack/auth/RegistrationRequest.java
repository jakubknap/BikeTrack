package pl.biketrack.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegistrationRequest {

    @NotEmpty(message = "Nick jest wymagany")
    @NotBlank(message = "Nick jest wymagany")
    private String nick;

    @Email(message = "Format e-maila jest niepoprawny")
    @NotEmpty(message = "Email jest wymagany")
    @NotBlank(message = "Email jest wymagany")
    private String email;

    @Size(min = 8, message = "Hasło powinno mieć minimum 8 znaków")
    @NotEmpty(message = "Hasło jest wymagane")
    @NotBlank(message = "Hasło jest wymagane")
    private String password;
}
