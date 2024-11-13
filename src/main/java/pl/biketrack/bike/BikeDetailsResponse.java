package pl.biketrack.bike;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class BikeDetailsResponse {

    private String name;
    private String brand;
    private String model;
    private String type;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdDate;
    private String userNick;
    private UUID bikeUuid;
    private UUID userUuid;
}