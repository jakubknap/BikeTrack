package pl.biketrack.repair;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.biketrack.bike.Bike;
import pl.biketrack.bike.BikeRepository;
import pl.biketrack.user.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepairServiceImplTest {

    @Mock
    private RepairRepository repairRepository;

    @Mock
    private BikeRepository bikeRepository;

    @InjectMocks
    private RepairServiceImpl repairService;

    private User testUser;
    private User otherUser;
    private Bike testBike;
    private Repair testRepair;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUuid(UUID.randomUUID());
        testUser.setId(1L);

        otherUser = new User();
        otherUser.setUuid(UUID.randomUUID());

        testBike = new Bike();
        testBike.setUuid(UUID.randomUUID());
        testBike.setUser(testUser);

        testRepair = new Repair();
        testRepair.setUuid(UUID.randomUUID());
        testRepair.setBike(testBike);
        testRepair.setCreatedBy(testUser.getId());

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(testUser, null, List.of());
        SecurityContextHolder.getContext()
                             .setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAddRepairByOwner() {
        AddRepairRequest request = new AddRepairRequest(testBike.getUuid(), "New Repair", "Description", BigDecimal.valueOf(100));

        when(bikeRepository.findByUuid(testBike.getUuid())).thenReturn(Optional.of(testBike));

        repairService.addRepair(request);

        verify(repairRepository).save(any(Repair.class));
        verify(repairRepository, times(1)).save(any());
    }

    @Test
    void shouldNotAllowAddRepairByNonOwner() {
        setLoggedUser(otherUser);

        AddRepairRequest request = new AddRepairRequest(testBike.getUuid(), "New Repair", "Description", BigDecimal.valueOf(100));

        when(bikeRepository.findByUuid(testBike.getUuid())).thenReturn(Optional.of(testBike));

        assertThrows(RuntimeException.class, () -> repairService.addRepair(request), "Nie jesteś upoważniony do dodania naprawy do tego roweru");
    }

    @Test
    void shouldEditRepairByOwner() {
        EditRepairRequest request = new EditRepairRequest(testRepair.getUuid(), "Updated Repair", "Updated Description", BigDecimal.valueOf(150));

        when(repairRepository.findByUuid(testRepair.getUuid())).thenReturn(Optional.of(testRepair));

        repairService.editRepair(request);

        verify(repairRepository).save(testRepair);
        assertEquals("Updated Repair", testRepair.getTitle());
        assertEquals("Updated Description", testRepair.getDescription());
        assertEquals(BigDecimal.valueOf(150), testRepair.getPrice());
    }

    @Test
    void shouldNotAllowEditRepairByNonOwner() {
        setLoggedUser(otherUser);

        EditRepairRequest request = new EditRepairRequest(testRepair.getUuid(), "Updated Repair", "Updated Description", BigDecimal.valueOf(150));

        when(repairRepository.findByUuid(testRepair.getUuid())).thenReturn(Optional.of(testRepair));

        assertThrows(RuntimeException.class, () -> repairService.editRepair(request), "Nie jesteś upoważniony do edycji tej naprawy");
    }

    @Test
    void shouldDeleteRepairByOwner() {
        when(repairRepository.findByUuid(testRepair.getUuid())).thenReturn(Optional.of(testRepair));

        repairService.deleteRepair(testRepair.getUuid());

        verify(repairRepository).delete(testRepair);
    }

    @Test
    void shouldNotAllowDeleteRepairByNonOwner() {
        setLoggedUser(otherUser);

        when(repairRepository.findByUuid(testRepair.getUuid())).thenReturn(Optional.of(testRepair));

        assertThrows(RuntimeException.class, () -> repairService.deleteRepair(testRepair.getUuid()), "Nie jesteś upoważniony do usunięcia tej naprawy");
    }

    private void setLoggedUser(User user) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());
        SecurityContextHolder.getContext()
                             .setAuthentication(authentication);
    }
}
