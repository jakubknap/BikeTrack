package pl.biketrack.bike;

import lombok.Getter;
import lombok.Setter;
import pl.biketrack.repair.RepairDetailsResponse;

import java.util.List;

@Getter
@Setter
public class BikeDetailsExtendedResponse extends BikeDetailsResponse {

    private List<RepairDetailsResponse> repairs;

    public BikeDetailsExtendedResponse(BikeDetailsResponse bikeDetailsResponse, List<RepairDetailsResponse> repairs) {
        super(bikeDetailsResponse.getName(),
              bikeDetailsResponse.getBrand(),
              bikeDetailsResponse.getModel(),
              bikeDetailsResponse.getType(),
              bikeDetailsResponse.getCreatedDate(),
              bikeDetailsResponse.getUserNick(),
              bikeDetailsResponse.getBikeUuid(),
              bikeDetailsResponse.getUserUuid());
        this.repairs = repairs;
    }
}