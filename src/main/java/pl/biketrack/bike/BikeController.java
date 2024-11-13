package pl.biketrack.bike;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.biketrack.repair.RepairDetailsResponse;

import java.util.List;
import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@RestController
@RequestMapping("/bike")
@RequiredArgsConstructor
@Tag(name = "Bike")
public class BikeController {

    private final BikeService bikeService;

    @PostMapping
    @ResponseStatus(CREATED)
    public void addBike(@RequestBody @Valid AddBikeRequest request) {
        log.info("Add bike request: {}", request);
        bikeService.addBike(request);
    }

    @PutMapping
    public void editBike(@RequestBody @Valid EditBikeRequest request) {
        log.info("Edit bike request: {}", request);
        bikeService.editBike(request);
    }

    @DeleteMapping("/{uuid}")
    public void deleteBike(@PathVariable UUID uuid) {
        log.info("Delete bike with uuid: {}", uuid);
        bikeService.deleteBike(uuid);
    }

    @GetMapping("/{uuid}")
    public BikeDetailsResponse getBikeDetails(@PathVariable UUID uuid) {
        log.info("Get bike details with uuid: {}", uuid);
        return bikeService.getBikeDetails(uuid);
    }

    @GetMapping("/{uuid}/extended")
    public BikeDetailsExtendedResponse getBikeDetailsExtended(@PathVariable UUID uuid) {
        log.info("Get bike details with repairs for bike with uuid: {}", uuid);
        return bikeService.getBikeDetailsExtended(uuid);
    }

    @GetMapping("/search")
    public Page<BikeListResponse> getAllBikes(@PageableDefault(size = 2, sort = "createdDate", direction = DESC) Pageable pageable) {
        log.info("Get all user bikes");
        return bikeService.getAllBikes(pageable);
    }

    @GetMapping("/{uuid}/repairs")
    public List<RepairDetailsResponse> getBikeRepairs(@PathVariable UUID uuid) {
        log.info("Get bike repairs for bike with uuid: {}", uuid);
        return bikeService.getBikeRepairs(uuid);
    }
}