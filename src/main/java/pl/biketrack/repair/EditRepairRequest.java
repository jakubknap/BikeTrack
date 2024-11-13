package pl.biketrack.repair;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record EditRepairRequest(

        @NotNull(message = "Uuid naprawy jest wymagany")
        UUID repairUuid,

        @NotNull(message = "Tytuł naprawy jest wymagany")
        @NotEmpty(message = "Tytuł naprawy nie może być pusty")
        String title,

        @NotNull(message = "Opis naprawy jest wymagany")
        @NotEmpty(message = "Opis naprawy nie może być pusty")
        String description,

        BigDecimal price
) {}