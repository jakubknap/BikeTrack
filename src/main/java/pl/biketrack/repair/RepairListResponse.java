package pl.biketrack.repair;

import java.math.BigDecimal;
import java.util.UUID;

public record RepairListResponse(String title, BigDecimal price, UUID uuid, UUID bikeUuid) {}