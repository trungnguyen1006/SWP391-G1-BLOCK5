package com.hotelmanage.repository.amenity;

import com.hotelmanage.entity.amenity.Amenity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Integer> {
    Page<Amenity> findByDeletedAtIsNull(Pageable pageable);
}
