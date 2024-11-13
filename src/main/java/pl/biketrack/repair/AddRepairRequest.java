package pl.biketrack.repair;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record AddRepairRequest(

        @NotNull(message = "Uuid roweru jest wymagany")
        UUID bikeUuid,

        @NotNull(message = "Tytuł naprawy jest wymagany")
        @NotEmpty(message = "Tytuł naprawy nie może być pusty")
        String title,

        @NotNull(message = "Opis naprawy jest wymagany")
        @NotEmpty(message = "Opis naprawy nie może być pusty")
        String description,

        BigDecimal price
) {}