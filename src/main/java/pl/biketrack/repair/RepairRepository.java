package pl.biketrack.repair;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepairRepository extends JpaRepository<Repair, Long> {

    Optional<Repair> findByUuid(UUID uuid);

    @Query("""
            SELECT new pl.biketrack.repair.RepairListResponse(r.title, r.price, r.uuid, b.uuid)
            FROM Repair r
            JOIN FETCH Bike b ON r.bike.uuid = b.uuid
            WHERE r.userUuid = :userUuid
            """)
    Page<RepairListResponse> findAllByUserUuid(UUID userUuid, Pageable pageable);

    @Query("""
            SELECT new pl.biketrack.repair.RepairDetailsResponse(r.title, r.description, r.price, r.createdDate, r.bike.uuid, r.uuid)
            FROM Repair r
            JOIN FETCH Bike b ON r.bike.uuid = b.uuid
            WHERE b.uuid = :bikeUuid
            """)
    List<RepairDetailsResponse> findAllRepairsByBikeUuid(UUID bikeUuid);
}