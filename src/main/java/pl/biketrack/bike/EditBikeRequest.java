package pl.biketrack.bike;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EditBikeRequest(

        @NotNull(message = "Uuid roweru jest wymagany")
        UUID bikeUuid,

        @NotNull(message = "Nazwa jest wymagana")
        @NotEmpty(message = "Nazwa jest wymagana")
        String name,

        @NotNull(message = "Marka jest wymagana")
        @NotEmpty(message = "Marka nie może być pusta")
        String brand,

        String model,

        @NotNull(message = "Typ roweru jest wymagany")
        @NotEmpty(message = "Typ roweru nie może być pusty")
        String type
) {}