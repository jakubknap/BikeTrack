package pl.biketrack.bike;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public record BikeListResponse(String name,
                               @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
                               LocalDateTime createdDate,
                               UUID bikeUuid) {}