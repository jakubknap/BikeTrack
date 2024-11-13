package pl.biketrack.bike;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.biketrack.repair.RepairDetailsResponse;

import java.util.List;
import java.util.UUID;

public interface BikeService {

    void addBike(AddBikeRequest request);

    void editBike(EditBikeRequest request);

    void deleteBike(UUID bikeUuid);

    BikeDetailsResponse getBikeDetails(UUID bikeUuid);

    BikeDetailsExtendedResponse getBikeDetailsExtended(UUID bikeUuid);

    Page<BikeListResponse> getAllBikes(Pageable pageable);

    List<RepairDetailsResponse> getBikeRepairs(UUID bikeUuid);
}
