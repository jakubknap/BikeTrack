package pl.biketrack.bike;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.biketrack.repair.Repair;
import pl.biketrack.repair.RepairDetailsResponse;
import pl.biketrack.repair.RepairRepository;
import pl.biketrack.user.User;

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
public class BikeServiceImplTest {

    @Mock
    private BikeRepository bikeRepository;

    @Mock
    private RepairRepository repairRepository;

    @InjectMocks
    private BikeServiceImpl bikeService;

    private User testUser;
    private User otherUser;
    private Bike testBike;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUuid(UUID.randomUUID());
        testUser.setNick("TestUser");

        otherUser = new User();
        otherUser.setUuid(UUID.randomUUID());
        otherUser.setNick("OtherUser");

        testBike = new Bike();
        testBike.setUuid(UUID.randomUUID());
        testBike.setUser(testUser);
        testBike.setName("TestBike");
        testBike.setBrand("TestBrand");
        testBike.setModel("TestModel");
        testBike.setType("Mountain");

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(testUser, null, List.of());
        SecurityContextHolder.getContext()
                             .setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAddBikeSuccessfully() {
        AddBikeRequest request = new AddBikeRequest("TestBike", "TestBrand", "TestModel", "Mountain");

        when(bikeRepository.save(any(Bike.class))).thenReturn(testBike);

        bikeService.addBike(request);

        verify(bikeRepository, times(1)).save(any(Bike.class));
    }

    @Test
    void shouldEditBikeSuccessfully() {
        EditBikeRequest request = new EditBikeRequest(testBike.getUuid(), "NewName", "NewBrand", "NewModel", "NewType");

        when(bikeRepository.findByUuid(testBike.getUuid())).thenReturn(Optional.of(testBike));

        bikeService.editBike(request);

        assertEquals("NewName", testBike.getName());
        assertEquals("NewBrand", testBike.getBrand());
        assertEquals("NewModel", testBike.getModel());
        assertEquals("NewType", testBike.getType());

        verify(bikeRepository, times(1)).save(testBike);
    }

    @Test
    void shouldThrowExceptionWhenUserUnauthorizedToEditBike() {
        EditBikeRequest request = new EditBikeRequest(UUID.randomUUID(), "NewName", "NewBrand", "NewModel", "NewType");
        User unauthorizedUser = new User();
        unauthorizedUser.setUuid(UUID.randomUUID());
        testBike.setUser(unauthorizedUser);

        when(bikeRepository.findByUuid(request.bikeUuid())).thenReturn(Optional.of(testBike));

        assertThrows(RuntimeException.class, () -> bikeService.editBike(request));
    }

    @Test
    void shouldDeleteBikeSuccessfully() {
        when(bikeRepository.findByUuid(testBike.getUuid())).thenReturn(Optional.of(testBike));

        bikeService.deleteBike(testBike.getUuid());

        verify(bikeRepository, times(1)).delete(testBike);
    }

    @Test
    void shouldThrowExceptionWhenUserUnauthorizedToDeleteBike() {
        User unauthorizedUser = new User();
        unauthorizedUser.setUuid(UUID.randomUUID());
        testBike.setUser(unauthorizedUser);

        when(bikeRepository.findByUuid(testBike.getUuid())).thenReturn(Optional.of(testBike));

        assertThrows(RuntimeException.class, () -> bikeService.deleteBike(testBike.getUuid()));
    }

    @Test
    void shouldReturnBikeDetailsSuccessfully() {
        when(bikeRepository.findByUuid(testBike.getUuid())).thenReturn(Optional.of(testBike));

        BikeDetailsResponse response = bikeService.getBikeDetails(testBike.getUuid());

        assertEquals(testBike.getName(), response.getName());
        assertEquals(testBike.getBrand(), response.getBrand());
    }

    @Test
    void shouldReturnBikeDetailsExtendedSuccessfully() {
        Repair repair = new Repair();
        repair.setUuid(UUID.randomUUID());
        repair.setTitle("TestRepair");
        repair.setDescription("Fixing brake");

        when(bikeRepository.findByUuid(testBike.getUuid())).thenReturn(Optional.of(testBike));
        when(repairRepository.findAllRepairsByBikeUuid(testBike.getUuid())).thenReturn(List.of(new RepairDetailsResponse(repair.getTitle(),
                                                                                                                         repair.getDescription(),
                                                                                                                         repair.getPrice(),
                                                                                                                         repair.getCreatedDate(),
                                                                                                                         testBike.getUuid(),
                                                                                                                         repair.getUuid())));

        BikeDetailsExtendedResponse response = bikeService.getBikeDetailsExtended(testBike.getUuid());

        assertEquals(testBike.getName(), response.getName());
        assertEquals(1,
                     response.getRepairs()
                             .size());
    }

    @Test
    void shouldReturnAllBikesForUser() {
        Pageable pageable = PageRequest.of(0, 5);
        when(bikeRepository.findAllByUserUuid(testUser.getUuid(), pageable)).thenReturn(new PageImpl<>(List.of(new BikeListResponse(testBike.getName(),
                                                                                                                                    testBike.getCreatedDate(),
                                                                                                                                    testBike.getUuid()))));

        Page<BikeListResponse> bikesPage = bikeService.getAllBikes(pageable);

        assertEquals(1, bikesPage.getTotalElements());
    }

    @Test
    void shouldReturnBikeRepairsSuccessfully() {
        Repair repair = new Repair();
        repair.setUuid(UUID.randomUUID());
        repair.setTitle("TestRepair");

        when(bikeRepository.findByUuid(testBike.getUuid())).thenReturn(Optional.of(testBike));
        when(repairRepository.findAllRepairsByBikeUuid(testBike.getUuid())).thenReturn(List.of(new RepairDetailsResponse(repair.getTitle(),
                                                                                                                         repair.getDescription(),
                                                                                                                         repair.getPrice(),
                                                                                                                         repair.getCreatedDate(),
                                                                                                                         testBike.getUuid(),
                                                                                                                         repair.getUuid())));

        List<RepairDetailsResponse> repairs = bikeService.getBikeRepairs(testBike.getUuid());

        assertEquals(1, repairs.size());
        assertEquals("TestRepair",
                     repairs.getFirst()
                            .title());
    }

    @Test
    void shouldNotAllowEditBikeByNonOwner() {
        // Symulowanie, że zalogowany jest otherUser
        setLoggedUser(otherUser);

        EditBikeRequest request = new EditBikeRequest(testBike.getUuid(), "NewName", "NewBrand", "NewModel", "NewType");
        when(bikeRepository.findByUuid(testBike.getUuid())).thenReturn(Optional.of(testBike));

        // Oczekujemy wyjątku RuntimeException
        assertThrows(RuntimeException.class, () -> bikeService.editBike(request), "Nie jesteś upoważniony do edycji tego roweru");
    }

    @Test
    void shouldNotAllowDeleteBikeByNonOwner() {
        // Symulowanie, że zalogowany jest otherUser
        setLoggedUser(otherUser);

        when(bikeRepository.findByUuid(testBike.getUuid())).thenReturn(Optional.of(testBike));

        // Oczekujemy wyjątku RuntimeException
        assertThrows(RuntimeException.class, () -> bikeService.deleteBike(testBike.getUuid()), "Nie jesteś upoważniony do usunięcia tego roweru");
    }

    private void setLoggedUser(User user) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());
        SecurityContextHolder.getContext()
                             .setAuthentication(authentication);
    }
}