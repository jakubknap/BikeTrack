package pl.biketrack.bike;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface BikeRepository extends JpaRepository<Bike, Long> {

    Optional<Bike> findByUuid(UUID uuid);

    @Query("""
            SELECT new pl.biketrack.bike.BikeListResponse(b.name, b.createdDate, b.uuid)
            FROM Bike b
            JOIN FETCH User u ON u.uuid = b.user.uuid
            WHERE b.user.uuid = :uuid
            """)
    Page<BikeListResponse> findAllByUserUuid(UUID uuid, Pageable pageable);
}