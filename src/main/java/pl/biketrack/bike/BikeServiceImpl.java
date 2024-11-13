package pl.biketrack.bike;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.biketrack.repair.RepairDetailsResponse;
import pl.biketrack.repair.RepairRepository;
import pl.biketrack.user.User;

import java.util.List;
import java.util.UUID;

import static pl.biketrack.security.SecurityUtils.getLoggedUser;
import static pl.biketrack.security.SecurityUtils.getLoggedUserUUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BikeServiceImpl implements BikeService {

    private static final int MAX_PAGE_SIZE = 5;

    private final BikeRepository bikeRepository;
    private final RepairRepository repairRepository;

    @Override
    public void addBike(AddBikeRequest request) {
        User user = getLoggedUser();
        bikeRepository.save(buildBike(request, user));
        log.info("Bike added");
    }

    @Override
    public void editBike(EditBikeRequest request) {
        Bike bike = findBike(request.bikeUuid());
        User user = getLoggedUser();

        if (!bike.getUser()
                 .getUuid()
                 .equals(user.getUuid())) {
            log.error("User is not authorized to modify this bike. User: {}, Bike Owner: {}",
                      user.getUuid(),
                      bike.getUser()
                          .getUuid());
            throw new RuntimeException("Nie jesteś upoważniony do edycji tego roweru");
        }

        bike.setName(request.name());
        bike.setBrand(request.brand());
        bike.setModel(request.model());
        bike.setType(request.type());

        bikeRepository.save(bike);
        log.info("Bike edited");
    }

    @Override
    public void deleteBike(UUID bikeUuid) {
        Bike bike = findBike(bikeUuid);
        UUID loggedUserUUID = getLoggedUserUUID();

        if (!bike.getUser()
                 .getUuid()
                 .equals(loggedUserUUID)) {
            log.error("User is not authorized to delete this bike. User: {}, Bike Owner: {}",
                      loggedUserUUID,
                      bike.getUser()
                          .getUuid());
            throw new RuntimeException("Nie jesteś upoważniony do usunięcia tego roweru");
        }

        bikeRepository.delete(bike);
        log.info("Bike deleted");
    }

    @Override
    public BikeDetailsResponse getBikeDetails(UUID bikeUuid) {
        Bike bike = findBike(bikeUuid);
        UUID loggedUserUUID = getLoggedUserUUID();

        if (!bike.getUser()
                 .getUuid()
                 .equals(loggedUserUUID)) {
            log.error("User is not authorized to view details of this bike. User: {}, Bike Owner: {}",
                      loggedUserUUID,
                      bike.getUser()
                          .getUuid());
            throw new RuntimeException("Nie jesteś upoważniony do podglądu tego roweru");
        }

        return new BikeDetailsResponse(bike.getName(),
                                       bike.getBrand(),
                                       bike.getModel(),
                                       bike.getType(),
                                       bike.getCreatedDate(),
                                       bike.getUser()
                                           .getNick(),
                                       bike.getUuid(),
                                       bike.getUser()
                                           .getUuid());
    }

    @Override
    public BikeDetailsExtendedResponse getBikeDetailsExtended(UUID bikeUuid) {
        BikeDetailsResponse bikeDetails = getBikeDetails(bikeUuid);
        List<RepairDetailsResponse> allRepairsByBikeUuid = repairRepository.findAllRepairsByBikeUuid(bikeUuid);
        return new BikeDetailsExtendedResponse(bikeDetails, allRepairsByBikeUuid);
    }

    @Override
    public Page<BikeListResponse> getAllBikes(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            pageable = PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort());
        }

        return bikeRepository.findAllByUserUuid(getLoggedUserUUID(), pageable);
    }

    private Bike buildBike(AddBikeRequest request, User user) {
        return Bike.builder()
                   .name(request.name())
                   .brand(request.brand())
                   .model(request.model())
                   .type(request.type())
                   .user(user)
                   .build();
    }

    private Bike findBike(UUID bikeUuid) {
        return bikeRepository.findByUuid(bikeUuid)
                             .orElseThrow(() -> {
                                 log.error("Bike with uuid: {} not found", bikeUuid);
                                 return new EntityNotFoundException("Nie znaleziono roweru");
                             });
    }
}