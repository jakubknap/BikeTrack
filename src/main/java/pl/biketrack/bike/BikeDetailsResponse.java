package pl.biketrack.bike;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public record BikeDetailsResponse(String name,
                                  String brand,
                                  String model,
                                  String type,
                                  @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
                                  LocalDateTime createdDate,
                                  String userNick,
                                  UUID bikeUuid,
                                  UUID userUuid) {}