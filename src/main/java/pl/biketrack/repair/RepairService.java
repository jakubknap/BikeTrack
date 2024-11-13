package pl.biketrack.repair;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface RepairService {

    void addRepair(AddRepairRequest request);

    void editRepair(EditRepairRequest request);

    void deleteRepair(UUID repairUuid);

    RepairDetailsResponse getRepairDetails(UUID repairUuid);

    Page<RepairListResponse> getAllRepairs(Pageable pageable);
}
