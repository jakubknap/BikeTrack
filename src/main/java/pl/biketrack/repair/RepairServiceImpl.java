package pl.biketrack.repair;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.biketrack.bike.Bike;
import pl.biketrack.bike.BikeRepository;

import java.util.UUID;

import static pl.biketrack.security.SecurityUtils.getLoggedUser;
import static pl.biketrack.security.SecurityUtils.getLoggedUserUUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepairServiceImpl implements RepairService {

    private static final int MAX_PAGE_SIZE = 5;

    private final RepairRepository repairRepository;
    private final BikeRepository bikeRepository;

    @Override
    public void addRepair(AddRepairRequest request) {
        Bike bike = findBike(request.bikeUuid());
        UUID loggedUserUUID = getLoggedUserUUID();

        if (!bike.getUser()
                 .getUuid()
                 .equals(loggedUserUUID)) {
            log.error("User is not authorized to add repair to this bike. User: {}, Bike Owner: {}",
                      loggedUserUUID,
                      bike.getUser()
                          .getUuid());
            throw new RuntimeException("Nie jesteś upoważniony do dodania naprawy do tego roweru");
        }

        Repair repair = buildRepair(request, bike, loggedUserUUID);

        repairRepository.save(repair);
        log.info("Repair successfully added to the bike");
    }

    @Override
    public void editRepair(EditRepairRequest request) {
        Repair repair = findRepair(request.repairUuid());

        if (!repair.getCreatedBy()
                   .equals(getLoggedUser().getId())) {
            log.error("User is not authorized to edit this repair. User: {}, Bike Owner: {}", getLoggedUser().getId(), repair.getCreatedBy());
            throw new RuntimeException("Nie jesteś upoważniony do edycji tej naprawy");
        }

        repair.setTitle(request.title());
        repair.setDescription(request.description());
        repair.setPrice(request.price());

        repairRepository.save(repair);
        log.info("Repair successfully edited");
    }

    @Override
    public void deleteRepair(UUID repairUuid) {
        Repair repair = findRepair(repairUuid);

        if (!repair.getCreatedBy()
                   .equals(getLoggedUser().getId())) {
            log.error("User is not authorized to delete this repair. User: {}, Bike Owner: {}", getLoggedUser().getId(), repair.getCreatedBy());
            throw new RuntimeException("Nie jesteś upoważniony do usunięcia tej naprawy");
        }

        repairRepository.delete(repair);
        log.info("Repair successfully deleted");
    }

    @Override
    public RepairDetailsResponse getRepairDetails(UUID repairUuid) {
        Repair repair = findRepair(repairUuid);

        if (!repair.getCreatedBy()
                   .equals(getLoggedUser().getId())) {
            log.error("User is not authorized to view details of this repair. User: {}, Bike Owner: {}", getLoggedUser().getId(), repair.getCreatedBy());
            throw new RuntimeException("Nie jesteś upoważniony do podglądu tej naprawy");
        }
        return new RepairDetailsResponse(repair.getTitle(),
                                         repair.getDescription(),
                                         repair.getPrice(),
                                         repair.getCreatedDate(),
                                         repair.getBike()
                                               .getUuid());
    }

    @Override
    public Page<RepairListResponse> getAllRepairs(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            pageable = PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort());
        }

        return repairRepository.findAllByUserUuid(getLoggedUserUUID(), pageable);
    }

    private Bike findBike(UUID bikeUuid) {
        return bikeRepository.findByUuid(bikeUuid)
                             .orElseThrow(() -> {
                                 log.error("Bike with uuid: {} not found", bikeUuid);
                                 return new EntityNotFoundException("Nie znaleziono roweru");
                             });
    }

    private Repair buildRepair(AddRepairRequest request, Bike bike, UUID loggedUserUUID) {
        return Repair.builder()
                     .title(request.title())
                     .description(request.description())
                     .price(request.price())
                     .bike(bike)
                     .userUuid(loggedUserUUID)
                     .build();
    }

    private Repair findRepair(UUID repairUuid) {
        return repairRepository.findByUuid(repairUuid)
                               .orElseThrow(() -> {
                                   log.error("Repair with uuid: {} not found", repairUuid);
                                   return new EntityNotFoundException("Nie znaleziono naprawy");
                               });
    }
}
